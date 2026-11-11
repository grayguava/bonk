package com.grayguava.bonk.ui

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Snackbar
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.grayguava.bonk.R
import com.grayguava.bonk.data.Backup
import com.grayguava.bonk.data.StreakStore
import com.grayguava.bonk.data.UserStore
import com.grayguava.bonk.data.exportBackup
import com.grayguava.bonk.data.importBackup
import com.grayguava.bonk.ui.theme.CardSurface
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

/** Where the app lives. Change these when the repo moves. */
private const val REPO_URL = "https://github.com/grayguava/bonk"
private const val ISSUES_URL = "$REPO_URL/issues"

/** The page behind the "i" button: what this app is, and where to find it. */
@Composable
fun AboutScreen(
    onBack: () -> Unit,
    streakStore: StreakStore,
    userStore: UserStore,
    onRestore: () -> Unit,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    var pendingRestore by remember { mutableStateOf<Backup?>(null) }
    val open = { url: String ->
        runCatching { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url))) }
    }

    val exportLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        if (uri == null) {
            scope.launch { snackbarHostState.showSnackbar("Backup cancelled.") }
            return@rememberLauncherForActivityResult
        }
        val result = runCatching {
            val bytes = exportBackup(userStore, streakStore).toByteArray()
            // "wt" truncates: without it the write can land after what is already there.
            context.contentResolver.openOutputStream(uri, "wt")?.use { stream ->
                stream.write(bytes)
                stream.flush()
            } ?: error("no output stream for $uri")
            bytes.size
        }
        result.onFailure { Log.e("Bonk", "backup failed", it) }
        result.onSuccess { Log.i("Bonk", "backup written, ${it} bytes") }
        scope.launch {
            snackbarHostState.showSnackbar(
                result.fold(
                    onSuccess = { "Backup saved." },
                    onFailure = { "Could not write the backup." },
                )
            )
        }
    }

    val importLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        val parsed = runCatching {
            context.contentResolver.openInputStream(uri)?.use { it.readBytes().toString(Charsets.UTF_8) }
        }.getOrNull()?.let { runCatching { importBackup(it) }.getOrNull() }
        if (parsed == null) {
            scope.launch { snackbarHostState.showSnackbar("That file is not a Bonk! backup.") }
        } else {
            pendingRestore = parsed
        }
    }

    val details = remember { appDetails(context) }
    val topDrop = screenTopGap()

    // The page scales with the width it has. dp is a physical unit, so density is
    // already handled; what differs is how much screen the app gets, and that is the
    // width. A narrow phone gets a smaller page so it reads the same as a wide one.
    val screenWidth = LocalConfiguration.current.screenWidthDp
    val scale = ((screenWidth / 480f).coerceIn(0.8f, 1f)) * 0.95f
    val titleSize = 57.sp * scale
    val cardTitleSize = 22.sp * scale
    val bodyStyle = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp * scale)
    val taglineSize = 16.sp * scale
    val cardPadding = 20.dp * scale
    val gap = 16.dp * scale
    val markSize = 56.dp * scale

    // Sit just above the navigation bar, which the system measures for us: a gesture
    // bar and a row of buttons are both covered by the same inset.
    val navigationBar = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
    val snackbarBottom = navigationBar + 12.dp

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        snackbarHost = {
            // The default snackbar is a light toast on a dark app. This one is part of
            // the app: a dark surface, a hairline edge, white text.
            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier.padding(bottom = snackbarBottom),
            ) { data ->
                // An outline, not a panel: no fill, a hairline edge, and the message
                // centred on the screen with its text running from the left.
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center,
                ) {
                    Snackbar(
                        snackbarData = data,
                        containerColor = Color.Transparent,
                        contentColor = Color.White,
                        actionColor = Color(0xFFB6C9FF),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth(0.8f)
                            .border(1.dp, Color.White.copy(alpha = 0.25f), RoundedCornerShape(16.dp)),
                    )
                }
            }
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .statusBarsPadding()
                .windowInsetsPadding(WindowInsets.navigationBars)
                .verticalScroll(rememberScrollState())
                .padding(start = 20.dp, end = 20.dp, top = topDrop),
        ) {
            // Pulled left by the icon button's own padding, so the arrow lines up with the
            // title's left edge instead of sitting 12 dp inside it.
            IconButton(
                onClick = onBack,
                modifier = Modifier.offset(x = (-12).dp),
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.White,
                )
            }
            Spacer(Modifier.height(20.dp * scale))

            Text(
                text = "Bonk!",
                style = MaterialTheme.typography.displayLarge.copy(fontSize = titleSize),
                fontWeight = FontWeight.Bold,
                color = Color.White,
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = "A streak counter that greets you by name.",
                style = MaterialTheme.typography.bodyLarge.copy(fontSize = taglineSize),
                color = Color.White.copy(alpha = 0.55f),
            )
            Spacer(Modifier.height(28.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = CardSurface),
            ) {
                Column(Modifier.padding(cardPadding)) {
                    DetailRow("Version", details.versionName, bodyStyle)
                    DetailRow("Package", details.packageName, bodyStyle)
                    DetailRow("Android", "API ${details.minSdk} - ${details.targetSdk}", bodyStyle)
                    DetailRow("Device", details.device, bodyStyle)
                    DetailRow("Installed on", details.installedOn, bodyStyle)
                }
            }
            Spacer(Modifier.height(gap))

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = CardSurface),
            ) {
                Column(Modifier.padding(cardPadding)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Image(
                            painter = painterResource(R.drawable.ic_github),
                            contentDescription = "GitHub",
                            modifier = Modifier.size(markSize),
                        )
                        Spacer(Modifier.width(16.dp * scale))
                        Column(Modifier.weight(1f)) {
                            Text(
                                text = "Star me on GitHub",
                                style = MaterialTheme.typography.titleLarge.copy(fontSize = cardTitleSize),
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = "A star helps it find more people than a download count ever will.",
                                style = bodyStyle,
                                color = Color.White.copy(alpha = 0.55f),
                            )
                        }
                    }
                    Spacer(Modifier.height(20.dp * scale))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        OutlinedButton(onClick = { open(ISSUES_URL) }) {
                            Text("Report an issue", style = MaterialTheme.typography.labelLarge.copy(fontSize = 14.sp * scale))
                        }
                        Spacer(Modifier.width(8.dp))
                        Button(onClick = { open(REPO_URL) }) {
                            Icon(
                                imageVector = Icons.Filled.Star,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp),
                            )
                            Spacer(Modifier.width(8.dp))
                            Text("Star the repo", style = MaterialTheme.typography.labelLarge.copy(fontSize = 14.sp * scale))
                        }
                    }
                }
            }
            Spacer(Modifier.height(gap))

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = CardSurface),
            ) {
                Column(Modifier.padding(cardPadding)) {
                    Text(
                        text = "Backup",
                        style = MaterialTheme.typography.titleLarge.copy(fontSize = cardTitleSize),
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "Save your streaks and your name to a file, and put them back on " +
                            "another device.",
                        style = bodyStyle,
                        color = Color.White.copy(alpha = 0.55f),
                    )
                    Spacer(Modifier.height(20.dp * scale))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        OutlinedButton(
                            onClick = { importLauncher.launch(arrayOf("application/json", "*/*")) }
                        ) {
                            Text("Restore", style = MaterialTheme.typography.labelLarge.copy(fontSize = 14.sp * scale))
                        }
                        Spacer(Modifier.width(8.dp))
                        Button(onClick = { exportLauncher.launch("bonk-backup.json") }) {
                            Text("Back up", style = MaterialTheme.typography.labelLarge.copy(fontSize = 14.sp * scale))
                        }
                    }
                }
            }
            Spacer(Modifier.height(32.dp))
        }
    }

    val backup = pendingRestore
    if (backup != null) {
        AlertDialog(
            onDismissRequest = { pendingRestore = null },
            containerColor = Color(0xFF0A0A0A),
            titleContentColor = Color.White,
            textContentColor = Color.White.copy(alpha = 0.7f),
            title = {
                Text(
                    text = "Replace everything on this device?",
                    style = MaterialTheme.typography.titleLarge.copy(fontSize = cardTitleSize),
                    fontWeight = FontWeight.Bold,
                )
            },
            text = {
                Text(
                    text = "Restoring puts back ${backup.history.size} " +
                        "${if (backup.history.size == 1) "streak" else "streaks"} and the name from the " +
                        "backup, and drops what is here now.",
                    style = bodyStyle,
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        streakStore.restore(backup.currentStart, backup.history)
                        userStore.name = backup.name
                        pendingRestore = null
                        onRestore()
                        scope.launch { snackbarHostState.showSnackbar("Restored.") }
                    }
                ) { Text("Restore", style = MaterialTheme.typography.labelLarge.copy(fontSize = 14.sp * scale)) }
            },
            dismissButton = {
                TextButton(onClick = { pendingRestore = null }) { Text("Cancel") }
            },
        )
    }
}

@Composable
private fun DetailRow(label: String, value: String, style: TextStyle) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = label,
            style = style,
            color = Color.White.copy(alpha = 0.5f),
        )
        Text(
            text = value,
            style = style,
            color = Color.White.copy(alpha = 0.85f),
        )
    }
}

private class AppDetails(
    val versionName: String,
    val packageName: String,
    val minSdk: Int,
    val targetSdk: Int,
    val device: String,
    val installedOn: String,
)

private fun appDetails(context: android.content.Context): AppDetails {
    val info = runCatching {
        context.packageManager.getPackageInfo(context.packageName, 0)
    }.getOrNull()
    val appInfo = info?.applicationInfo
    return AppDetails(
        versionName = info?.versionName ?: "—",
        packageName = context.packageName,
        minSdk = appInfo?.minSdkVersion ?: 0,
        targetSdk = appInfo?.targetSdkVersion ?: 0,
        device = deviceName(context),
        installedOn = info?.firstInstallTime?.let { millis ->
            LocalDateTime.ofInstant(Instant.ofEpochMilli(millis), ZoneId.systemDefault())
                .format(DateTimeFormatter.ofPattern("d MMM yyyy"))
        } ?: "—",
    )
}

/**
 * The phone's name as its owner knows it: the device name from Settings (what Bluetooth
 * and Wi-Fi show, e.g. "Xiaomi 14 5G"). [Build.MODEL] is the fallback — on some makers
 * that is a raw hardware code rather than a name anyone would recognise.
 */
private fun deviceName(context: android.content.Context): String {
    val friendly = runCatching {
        Settings.Global.getString(context.contentResolver, Settings.Global.DEVICE_NAME)
    }.getOrNull()?.trim()?.takeIf { it.isNotEmpty() }
    if (friendly != null) return friendly

    val model = Build.MODEL.orEmpty()
    val maker = Build.MANUFACTURER.orEmpty()
    return when {
        model.isBlank() -> "—"
        maker.isBlank() || model.startsWith(maker, ignoreCase = true) -> model
        else -> "$maker $model"
    }
}
