package com.grayguava.bonk.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.File
import java.net.HttpURLConnection
import java.net.URL

const val RELEASES_API = "https://api.github.com/repos/grayguava/bonk/releases/latest"

/** A published release, and the apk it carries. */
data class Release(
    val tag: String,
    val version: String,
    val apkUrl: String?,
)

/** The latest release from the github api, or null when the answer has no release in it. */
fun parseLatestRelease(json: String): Release? {
    val root = JSONObject(json)
    val tag = root.optString("tag_name").trim().removePrefix("v")
    if (tag.isEmpty()) return null

    val assets = root.optJSONArray("assets")
    var apkUrl: String? = null
    for (index in 0 until (assets?.length() ?: 0)) {
        val asset = assets!!.getJSONObject(index)
        if (asset.optString("name").endsWith(".apk")) {
            apkUrl = asset.optString("browser_download_url").takeIf { it.isNotBlank() }
            break
        }
    }
    return Release(tag = "v$tag", version = tag, apkUrl = apkUrl)
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
    } catch (e: Exception) {
        null
    } finally {
        connection.disconnect()
    }
}

/** Download an apk to [target]. False when the download did not finish. */
suspend fun downloadApk(url: String, target: File): Boolean = withContext(Dispatchers.IO) {
    val connection = (URL(url).openConnection() as HttpURLConnection).apply {
        connectTimeout = 15_000
        readTimeout = 30_000
        instanceFollowRedirects = true
    }
    try {
        if (connection.responseCode !in 200..299) return@withContext false
        target.parentFile?.mkdirs()
        connection.inputStream.use { input ->
            target.outputStream().use { output -> input.copyTo(output) }
        }
        target.length() > 0
    } catch (e: Exception) {
        false
    } finally {
        connection.disconnect()
    }
}
