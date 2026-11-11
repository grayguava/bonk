package com.grayguava.bonk.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class UpdateTest {
    private val release = """
        {
          "tag_name": "v0.2.0",
          "assets": [
            { "name": "SHA256SUMS.txt", "browser_download_url": "https://example.com/sums" },
            { "name": "v0.2.0_universal.apk", "browser_download_url": "https://example.com/bonk.apk" },
            { "name": "v0.2.0_universal.apk.sha256", "browser_download_url": "https://example.com/bonk.apk.sha256" }
          ]
        }
    """.trimIndent()

    @Test
    fun `the tag and the apk are read from the answer`() {
        val parsed = parseLatestRelease(release)

        assertEquals("v0.2.0", parsed?.tag)
        assertEquals("0.2.0", parsed?.version)
        assertEquals("https://example.com/bonk.apk", parsed?.apkUrl)
    }

    @Test
    fun `the checksum beside the apk is read too`() {
        val parsed = parseLatestRelease(release)

        assertEquals("https://example.com/bonk.apk.sha256", parsed?.checksumUrl)
    }

    @Test
    fun `an answer without a release reads as none`() {
        assertNull(parseLatestRelease("""{"message":"Not Found"}"""))
    }

    @Test
    fun `a release without an apk still reads`() {
        val parsed = parseLatestRelease("""{"tag_name":"v1.0.0"}""")

        assertEquals("1.0.0", parsed?.version)
        assertNull(parsed?.apkUrl)
    }

    @Test
    fun `a later version is newer`() {
        assertTrue(isNewerVersion("0.2.0", "0.1.0"))
        assertTrue(isNewerVersion("v1.0.0", "0.9.9"))
        assertTrue(isNewerVersion("0.2.1", "0.2.0"))
    }

    @Test
    fun `the same or an older version is not newer`() {
        assertFalse(isNewerVersion("0.1.0", "0.1.0"))
        assertFalse(isNewerVersion("0.1.0", "0.2.0"))
        assertFalse(isNewerVersion("0.0.1", "0.1.0"))
    }

    @Test
    fun `a version that is not three numbers is ignored`() {
        assertFalse(isNewerVersion("0.2", "0.1.0"))
        assertFalse(isNewerVersion("nightly", "0.1.0"))
    }

    @Test
    fun `the hash is read out of a checksum line`() {
        val hash = "de20f50839748651338dcb9e05af35dd4c2e87fa62768c8a4a0628e7c12fdc12"

        assertEquals(hash, parseChecksum("$hash  v0.2.0_universal.apk"))
        assertEquals(hash, parseChecksum("$hash *v0.2.0_universal.apk"))
        assertEquals(hash, parseChecksum("  $hash  \n"))
    }

    @Test
    fun `a checksum line that holds no hash reads as none`() {
        assertNull(parseChecksum(""))
        assertNull(parseChecksum("not a hash"))
        assertNull(parseChecksum("de20f508"))
    }
}
