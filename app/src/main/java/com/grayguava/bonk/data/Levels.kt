package com.grayguava.bonk.data

/** The five tiers, each with its badge colour. */
enum class Tier(val title: String, val color: Long) {
    I("Awakening", 0xFF8A8F98),
    II("Building", 0xFF7FA0C4),
    III("Strength", 0xFF8FB9A0),
    IV("Mastery", 0xFFC9AE7E),
    V("Transcendence", 0xFFE8D08A),
}

data class Level(
    val number: Int,
    val name: String,
    val days: Int,
    val tier: Tier,
)

/** Ten levels, in order. The days are how long a streak must last to reach them. */
val LEVELS = listOf(
    Level(1, "Fresh Start", 0, Tier.I),
    Level(2, "Beginner", 1, Tier.I),
    Level(3, "Initiate", 3, Tier.I),
    Level(4, "Week One", 7, Tier.II),
    Level(5, "Disciplined", 14, Tier.II),
    Level(6, "Consistent", 30, Tier.III),
    Level(7, "Resilient", 60, Tier.III),
    Level(8, "Dedicated", 90, Tier.IV),
    Level(9, "Unshaken", 180, Tier.IV),
    Level(10, "Year One", 365, Tier.V),
)

/** The tiers in order, each with the levels inside it. */
val TIERS: List<Tier> = LEVELS.map { it.tier }.distinct()

/** The levels belonging to a tier. */
fun levelsIn(tier: Tier): List<Level> = LEVELS.filter { it.tier == tier }

/** The level a streak of [days] has reached. */
fun levelFor(days: Int): Level = LEVELS.lastOrNull { days >= it.days } ?: LEVELS.first()

/** The level after the one reached, or null at the top. */
fun nextLevel(after: Level): Level? = LEVELS.firstOrNull { it.days > after.days }
