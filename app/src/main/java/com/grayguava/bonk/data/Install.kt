package com.grayguava.bonk.data

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageInstaller
import android.net.Uri
import android.provider.Settings
import android.util.Log
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.withContext
import java.security.DigestOutputStream
import java.security.MessageDigest

/** How an install ended. */
sealed interface InstallResult {
    /** The system installed it. */
    data class Installed(val version: String) : InstallResult

    /** The system did not install it, and said why. */
    data class Refused(val reason: String) : InstallResult
}

/** The result of an install, for whichever screen is listening. */
object InstallEvents {
    val results = MutableSharedFlow<InstallResult>(extraBufferCapacity = 1)
}

/** What came of trying to hand an apk over to the system. */
sealed interface InstallOutcome {
    /** The system has the apk and will ask the user to install it. */
    data object HandedOver : InstallOutcome

    /** The download or the file was not good enough to install. */
    data class Broken(val reason: String) : InstallOutcome
}

/** The system tells us how the install went. */
class InstallResultReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val status = intent.getIntExtra(PackageInstaller.EXTRA_STATUS, Int.MIN_VALUE)
        val message = intent.getStringExtra(PackageInstaller.EXTRA_STATUS_MESSAGE).orEmpty()
        val version = intent.getStringExtra(EXTRA_VERSION).orEmpty()
        Log.i("Bonk", "the install finished: status=$status message=$message")
        when (status) {
            PackageInstaller.STATUS_SUCCESS ->
                InstallEvents.results.tryEmit(InstallResult.Installed(version))

            PackageInstaller.STATUS_PENDING_USER_ACTION -> {
                // The system wants its own screen shown before it goes on.
                @Suppress("DEPRECATION")
                val next = intent.getParcelableExtra<Intent>(Intent.EXTRA_INTENT)
                if (next != null) {
                    next.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    runCatching { context.startActivity(next) }
                        .onFailure { Log.e("Bonk", "could not show the install screen", it) }
                }
            }

            else -> InstallEvents.results.tryEmit(
                InstallResult.Refused(message.ifBlank { "The install was refused." })
            )
        }
    }

    companion object {
        const val EXTRA_VERSION = "com.grayguava.bonk.EXTRA_VERSION"
    }
}

/** May this app ask the system to install a package? */
fun canRequestInstall(context: Context): Boolean =
    context.packageManager.canRequestPackageInstalls()

/** Send the user to the screen where they can allow installs from this app. */
fun openInstallPermissionSettings(context: Context) {
    val intent = Intent(
        Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES,
        Uri.parse("package:${context.packageName}"),
    )
    runCatching { context.startActivity(intent) }.onFailure {
        runCatching { context.startActivity(Intent(Settings.ACTION_SETTINGS)) }
            .onFailure { Log.e("Bonk", "could not open the settings", it) }
    }
}

/**
 * Download the apk of [release] and hand it to the system installer.
 *
 * The bytes go straight into an install session, so the copy the system installs is
 * one it holds, rather than one lying in the app's cache where it could be reclaimed
 * halfway through. The checksum the release published is checked before the session
 * is committed: an apk that does not match is never handed over.
 */
suspend fun installApk(
    context: Context,
    release: Release,
    onProgress: (downloaded: Long, total: Long) -> Unit = { _, _ -> },
): InstallOutcome {
    val url = release.apkUrl ?: return InstallOutcome.Broken("The release has no apk.")
    val checksum = release.checksumUrl?.let { fetchChecksum(it) }
        ?: return InstallOutcome.Broken("The release published no checksum.")

    val installer = context.packageManager.packageInstaller
    val params = PackageInstaller.SessionParams(
        PackageInstaller.SessionParams.MODE_FULL_INSTALL
    )
    val sessionId = installer.createSession(params)
    val session = installer.openSession(sessionId)
    try {
        return withContext(Dispatchers.IO) {
            val digest = MessageDigest.getInstance("SHA-256")
            val out = session.openWrite(SESSION_FILE, 0, -1L)
            try {
                val copied = DigestOutputStream(out, digest)
                val finished = downloadTo(url, copied, onProgress)
                copied.flush()
                if (!finished) {
                    runCatching { session.abandon() }
                    return@withContext InstallOutcome.Broken("The download did not finish.")
                }
                session.fsync(out)
            } finally {
                runCatching { out.close() }
            }

            val actual = digest.digest().joinToString("") { "%02x".format(it) }
            if (actual != checksum) {
                Log.e("Bonk", "the download did not match its checksum")
                runCatching { session.abandon() }
                return@withContext InstallOutcome.Broken("The download did not match its checksum.")
            }

            session.commit(commitSender(context, release.version))
            Log.i("Bonk", "the apk was handed over for ${release.version}")
            InstallOutcome.HandedOver
        }
    } catch (e: CancellationException) {
        runCatching { session.abandon() }
        throw e
    } catch (e: Exception) {
        Log.e("Bonk", "the install could not be prepared", e)
        runCatching { session.abandon() }
        return InstallOutcome.Broken("The install could not be prepared.")
    } finally {
        runCatching { session.close() }
    }
}

private const val SESSION_FILE = "bonk.apk"

private fun commitSender(context: Context, version: String): IntentSender {
    val intent = Intent(context, InstallResultReceiver::class.java)
        .putExtra(InstallResultReceiver.EXTRA_VERSION, version)
    // The system fills in the outcome, so the pending intent has to let it.
    return PendingIntent.getBroadcast(
        context,
        0,
        intent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE,
    ).intentSender
}
