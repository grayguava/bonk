package com.grayguava.bonk.data

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

data class StreakRecord(
    val startTime: Long,
    val resetTime: Long,
    val days: Int,
    val reason: String,
)

/** Whole days elapsed between two timestamps. */
fun streakDays(start: Long, end: Long): Int =
    ((end - start) / 86_400_000L).toInt().coerceAtLeast(0)

/** A count with the right form of its noun: "1 day", "0 days", "12 mins". */
fun plural(count: Int, singular: String, plural: String = singular + "s"): String =
    "$count ${if (count == 1) singular else plural}"

/** A duration in one unit: "20 days", "3 hours", "12 mins", "40 sec". */
fun formatDuration(millis: Long): String {
    val total = (millis / 1000).coerceAtLeast(0)
    val days = total / 86_400
    val hours = (total % 86_400) / 3600
    val minutes = (total % 3600) / 60
    val seconds = total % 60
    return when {
        days > 0 -> plural(days.toInt(), "day")
        hours > 0 -> plural(hours.toInt(), "hour")
        minutes > 0 -> plural(minutes.toInt(), "min", "mins")
        else -> plural(seconds.toInt(), "sec", "secs")
    }
}

/** A duration broken down: "20 days 3 hours 12 mins". */
fun formatDurationFull(millis: Long): String {
    val total = (millis / 1000).coerceAtLeast(0)
    val days = total / 86_400
    val hours = (total % 86_400) / 3600
    val minutes = (total % 3600) / 60
    val seconds = total % 60
    val parts = buildList {
        if (days > 0) add(plural(days.toInt(), "day"))
        if (hours > 0) add(plural(hours.toInt(), "hour"))
        if (minutes > 0) add(plural(minutes.toInt(), "min", "mins"))
        if (seconds > 0 || isEmpty()) add(plural(seconds.toInt(), "sec", "secs"))
    }
    return parts.joinToString(" ")
}

/**
 * Streak state on disk. SharedPreferences plus JSON keeps the app dependency-free;
 * swap in DataStore when the schema settles.
 */
class StreakStore(context: Context) {
    private val prefs = context.applicationContext.getSharedPreferences("bonk", Context.MODE_PRIVATE)

    /** Start of the running streak, or null when no streak is running. */
    var currentStart: Long?
        get() = prefs.getLong(KEY_CURRENT, -1L).takeIf { it > 0L }
        set(value) {
            prefs.edit()
                .apply { if (value == null) remove(KEY_CURRENT) else putLong(KEY_CURRENT, value) }
                .apply()
        }

    /** Finished streaks, newest first. */
    fun history(): List<StreakRecord> {
        val raw = prefs.getString(KEY_HISTORY, null) ?: return emptyList()
        return runCatching {
            val array = JSONArray(raw)
            (0 until array.length()).map { index ->
                val item = array.getJSONObject(index)
                StreakRecord(
                    startTime = item.getLong("start"),
                    resetTime = item.getLong("reset"),
                    days = item.getInt("days"),
                    reason = item.optString("reason"),
                )
            }
        }.getOrDefault(emptyList())
    }

    fun add(record: StreakRecord) {
        val array = JSONArray()
        array.put(record.toJson())
        history().forEach { array.put(it.toJson()) }
        prefs.edit().putString(KEY_HISTORY, array.toString()).apply()
    }

    private fun StreakRecord.toJson(): JSONObject = JSONObject().apply {
        put("start", startTime)
        put("reset", resetTime)
        put("days", days)
        put("reason", reason)
    }

    private companion object {
        const val KEY_CURRENT = "current_start"
        const val KEY_HISTORY = "history"
    }
}
