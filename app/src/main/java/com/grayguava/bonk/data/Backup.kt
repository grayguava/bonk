package com.grayguava.bonk.data

import org.json.JSONArray
import org.json.JSONObject

const val BACKUP_FORMAT = "bonk-backup"
const val BACKUP_VERSION = 1

/** Everything the app keeps, in one document. */
data class Backup(
    val name: String?,
    val currentStart: Long?,
    val history: List<StreakRecord>,
)

/** The whole app's state as a JSON document. */
fun exportBackup(userStore: UserStore, streakStore: StreakStore): String =
    backupJson(userStore.name, streakStore.currentStart, streakStore.history())

/** The backup document, without the stores. */
fun backupJson(name: String?, currentStart: Long?, history: List<StreakRecord>): String {
    val root = JSONObject()
    root.put("format", BACKUP_FORMAT)
    root.put("version", BACKUP_VERSION)
    root.put("exportedAt", System.currentTimeMillis())
    root.put("name", name ?: JSONObject.NULL)
    root.put("currentStart", currentStart ?: JSONObject.NULL)

    val historyArray = JSONArray()
    history.forEach { record ->
        historyArray.put(
            JSONObject().apply {
                put("start", record.startTime)
                put("reset", record.resetTime)
                put("days", record.days)
                put("reason", record.reason)
            }
        )
    }
    root.put("history", historyArray)
    return root.toString(2)
}

/**
 * Read a backup back. Throws with a message worth showing if the document is not one
 * of ours, or is a version this build does not know.
 */
fun importBackup(json: String): Backup {
    val root = JSONObject(json)
    require(root.optString("format") == BACKUP_FORMAT) { "not a Bonk! backup" }
    val version = root.optInt("version", 0)
    require(version in 1..BACKUP_VERSION) { "backup version $version is not supported" }

    val array = root.optJSONArray("history") ?: JSONArray()
    val history = (0 until array.length()).map { index ->
        val item = array.getJSONObject(index)
        StreakRecord(
            startTime = item.getLong("start"),
            resetTime = item.getLong("reset"),
            days = item.getInt("days"),
            reason = item.optString("reason"),
        )
    }

    return Backup(
        name = root.optString("name").takeIf { it.isNotBlank() && it != "null" },
        currentStart = root.optLong("currentStart", -1L).takeIf { it > 0L },
        history = history,
    )
}
