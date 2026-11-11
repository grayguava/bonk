package com.grayguava.bonk.data

/**
 * The app's voice. One dry sentence at the right moment — never a wall of text, never
 * a lecture. Every line is picked from a fixed catalogue so the tone stays consistent.
 */

/** Something worth saying once the streak has run for a while. */
fun milestoneMessage(days: Int): String? = when (days) {
    1 -> "Day one. It begins."
    3 -> "Three days. Something's forming."
    7 -> "A whole week. Look at you go."
    14 -> "Two weeks. You're a problem now."
    30 -> "A month. Thirty chances to quit. You didn't."
    60 -> "Two months. This is getting personal."
    100 -> "Triple digits. Show-off."
    180 -> "Half a year. Statistically improbable, honestly."
    365 -> "A whole year. A whole year."
    else -> null
}

/** A new level, and what it says about you. */
fun levelUnlockMessage(level: Level): String = when (level.number) {
    1 -> "Fresh Start. Obviously."
    2 -> "Beginner. Yesterday you were that too."
    3 -> "Initiate. Initiated."
    4 -> "Week One. You have done this before."
    5 -> "Disciplined. The hard part starts now."
    6 -> "Consistent. Suspiciously consistent."
    7 -> "Resilient. Bounced back, apparently."
    8 -> "Dedicated. This is no longer a phase."
    9 -> "Unshaken. Nothing moved you."
    else -> "Year One. A full orbit."
}

/** After a bonk. Never a lecture, never a scolding. */
fun resetMessage(durationMillis: Long, hour: Int): String = when {
    hour == 0 -> "Midnight bonk. Theatrical."
    durationMillis < 86_400_000L -> "That was quick. Back to square one."
    durationMillis < 7 * 86_400_000L -> "Back to square one."
    else -> "That one hurt. Back to square one."
}

/** Nothing in the history yet. */
fun emptyHistoryMessage(): String = "Nothing to report. Yet."

/** Rare days that earn a little extra. */
fun easterEgg(days: Int): String? = when (days) {
    42 -> "The answer to life, the universe, and your streak."
    666 -> "Six hundred and sixty-six. Carry on."
    1000 -> "Thousand. Another one."
    else -> null
}
