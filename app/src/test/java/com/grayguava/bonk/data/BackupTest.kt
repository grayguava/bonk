package com.grayguava.bonk.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class BackupTest {
    private val history = listOf(
        StreakRecord(1_000L, 2_000L, 1, "That was quick. Back to square one."),
        StreakRecord(3_000L, 9_000L, 7, "A whole week of effort, gone."),
    )

    @Test
    fun `a backup keeps the name, the running streak and the history`() {
        val backup = importBackup(backupJson("Daniel", 4_000L, history))

        assertEquals("Daniel", backup.name)
        assertEquals(4_000L, backup.currentStart)
        assertEquals(history, backup.history)
    }

    @Test
    fun `a backup with no name and no running streak reads back as empty`() {
        val backup = importBackup(backupJson(null, null, emptyList()))

        assertNull(backup.name)
        assertNull(backup.currentStart)
        assertTrue(backup.history.isEmpty())
    }

    @Test
    fun `a file that is not a backup is rejected`() {
        val failure = runCatching { importBackup("""{"hello":"world"}""") }.exceptionOrNull()

        assertTrue(failure is IllegalArgumentException)
    }

    @Test
    fun `a backup from a newer version is rejected`() {
        val failure = runCatching {
            importBackup("""{"format":"$BACKUP_FORMAT","version":99,"history":[]}""")
        }.exceptionOrNull()

        assertTrue(failure is IllegalArgumentException)
    }

    @Test
    fun `the history survives being written and read back`() {
        assertEquals(history, parseHistory(historyJson(history)))
    }

    @Test
    fun `history that is not readable reads as no streaks`() {
        assertTrue(parseHistory(null).isEmpty())
        assertTrue(parseHistory("not json").isEmpty())
    }
}
