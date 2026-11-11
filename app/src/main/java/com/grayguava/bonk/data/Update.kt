package com.grayguava.bonk.data

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.OutputStream
import java.net.HttpURLConnection
import java.net.URL

const val RELEASES_API = "https://api.github.com/repos/grayguava/bonk/releases/latest"

/** A published release, and the files it carries. */
data class Release(
    val tag: String,
    val version: String,
    val apkUrl: String?,
    val checksumUrl: String?,
)

/** The latest release from the github api, or null when the answer has no release in it. */
fun parseLatestRelease(json: String): Release? {
    val root = JSONObject(json)
    val tag = root.optString("tag_name").trim().removePrefix("v")
    if (tag.isEmpty()) return null

    val assets = root.optJSONArray("assets")
    var apkUrl: String? = null
    var checksumUrl: String? = null
    for (index in 0 until (assets?.length() ?: 0)) {
        val asset = assets!!.getJSONObject(index)
        val name = asset.optString("name")
        val url = asset.optString("browser_download_url").takeIf { it.isNotBlank() }
        when {
            name.endsWith(".apk") -> apkUrl = url
            name.endsWith(".apk.sha256") -> checksumUrl = url
        }
    }
    return Release(tag = "v$tag", version = tag, apkUrl = apkUrl, checksumUrl = checksumUrl)
}

/** Is [candidate] a later version than [current]? */
fun isNewerVersion(candidate: String, current: String): Boolean {
    val left = versionParts(candidate) ?: return false
    val right = versionParts(current) ?: return false
    for (index in left.indices) {
        if (left[index] != right[index]) return left[index] > right[index]
    }
    return false
}

private fun versionParts(version: String): List<Int>? =
    version.trim().removePrefix("v").split(".")
        .mapNotNull { it.toIntOrNull() }
        .takeIf { it.size == 3 }

/**
 * The checksum out of a `sha256sum` line: the hash, then the name of the file.
 *
 * Null when the line holds nothing that could be a hash.
 */
fun parseChecksum(text: String): String? =
    text.trim().split(Regex("\\s+")).firstOrNull()
        ?.lowercase()
        ?.takeIf { it.length == 64 && it.all { c -> c in "0123456789abcdef" } }

/** Ask github for the latest release. Null when there is none, or when the call fails. */
suspend fun fetchLatestRelease(url: String = RELEASES_API): Release? = withContext(Dispatchers.IO) {
    val connection = (URL(url).openConnection() as HttpURLConnection).apply {
        connectTimeout = 15_000
        readTimeout = 15_000
        setRequestProperty("Accept", "application/vnd.github+json")
    }
    try {
        if (connection.responseCode !in 200..299) return@withContext null
        parseLatestRelease(connection.inputStream.bufferedReader().use { it.readText() })
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        null
    } finally {
        connection.disconnect()
    }
}

/** The checksum a release published for its apk. Null when there is none to be had. */
suspend fun fetchChecksum(url: String): String? = withContext(Dispatchers.IO) {
    val connection = (URL(url).openConnection() as HttpURLConnection).apply {
        connectTimeout = 15_000
        readTimeout = 15_000
    }
    try {
        if (connection.responseCode !in 200..299) return@withContext null
        parseChecksum(connection.inputStream.bufferedReader().use { it.readText() })
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        null
    } finally {
        connection.disconnect()
    }
}

/**
 * Copy the bytes at [url] into [out], reporting how far it has got as it goes.
 *
 * False when the transfer did not finish.
 */
suspend fun downloadTo(
    url: String,
    out: OutputStream,
    onProgress: (downloaded: Long, total: Long) -> Unit = { _, _ -> },
): Boolean = withContext(Dispatchers.IO) {
    val connection = (URL(url).openConnection() as HttpURLConnection).apply {
        connectTimeout = 15_000
        readTimeout = 30_000
        instanceFollowRedirects = true
    }
    try {
        if (connection.responseCode !in 200..299) return@withContext false
        val total = connection.contentLengthLong
        var downloaded = 0L
        connection.inputStream.use { input ->
            val buffer = ByteArray(64 * 1024)
            while (true) {
                val read = input.read(buffer)
                if (read < 0) break
                out.write(buffer, 0, read)
                downloaded += read
                onProgress(downloaded, total)
            }
        }
        true
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        false
    } finally {
        connection.disconnect()
    }
}
