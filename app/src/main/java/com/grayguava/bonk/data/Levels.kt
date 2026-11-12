package com.grayguava.bonk.data

/** The five tiers, each with its colour. */
enum class Tier(val title: String, val color: Long) {
    I("The Struggle", 0xFF8A8F98),
    II("Getting Serious", 0xFF7FA0C4),
    III("Built Different", 0xFF8FB9A0),
    IV("Questionably Powerful", 0xFFC9AE7E),
    V("Final Form", 0xFFE8D08A),
}

data class Level(
    val number: Int,
    val name: String,
    val days: Int,
    val tier: Tier,
)

/** Ten levels, in order. The days are how long a streak must last to reach them. */
val LEVELS = listOf(
    Level(1, "Fresh Spawn", 0, Tier.I),
    Level(2, "Still Standing", 1, Tier.I),
    Level(3, "The Determined", 3, Tier.I),
    Level(4, "Week Warrior", 7, Tier.II),
    Level(5, "Monk in Training", 14, Tier.II),
    Level(6, "Certified Monk", 30, Tier.III),
    Level(7, "Bonk Resistant", 60, Tier.III),
    Level(8, "Grass Toucher", 90, Tier.IV),
    Level(9, "Beyond Human", 180, Tier.IV),
    Level(10, "The Bonkening", 365, Tier.V),
)

/** The tiers in order, each with the levels inside it. */
val TIERS: List<Tier> = LEVELS.map { it.tier }.distinct()

/** The levels belonging to a tier. */
fun levelsIn(tier: Tier): List<Level> = LEVELS.filter { it.tier == tier }

/** The level a streak of [days] has reached. */
fun levelFor(days: Int): Level = LEVELS.lastOrNull { days >= it.days } ?: LEVELS.first()

/** The level after the one reached, or null at the top. */
fun nextLevel(after: Level): Level? = LEVELS.firstOrNull { it.days > after.days }
