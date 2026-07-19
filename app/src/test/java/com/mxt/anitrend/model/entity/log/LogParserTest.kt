package com.mxt.anitrend.model.entity.log

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.ByteArrayInputStream
import java.nio.charset.StandardCharsets

class LogParserTest {

    // ── parseLine with real persisted format ──

    @Test
    fun `parseLine real ERROR line`() {
        val line = "07-17 22:42:01:600 E/ExceptionCrashHandler(2) : main"
        val entry = LogParser.parseLine(line)
        assertNotNull(entry)
        entry!!
        assertEquals("07-17", entry.date)
        assertEquals("22:42:01:600", entry.time)
        assertEquals(LogEntry.Level.ERROR, entry.level)
        assertEquals("main", entry.message)
    }

    @Test
    fun `parseLine real INFO line`() {
        val line = "07-17 21:58:44:896 I/[Koin](2) : Started 29 definitions in 7.362 ms"
        val entry = LogParser.parseLine(line)
        assertNotNull(entry)
        entry!!
        assertEquals("07-17", entry.date)
        assertEquals("21:58:44:896", entry.time)
        assertEquals(LogEntry.Level.INFO, entry.level)
        assertEquals("Started 29 definitions in 7.362 ms", entry.message)
    }

    @Test
    fun `parseLine real WARNING line`() {
        val line = "07-19 08:53:37:227 W/chromium(4596) : [ERROR:variations_seed_loader.cc(39)] Seed missing"
        val entry = LogParser.parseLine(line)
        assertNotNull(entry)
        entry!!
        assertEquals(LogEntry.Level.WARNING, entry.level)
        assertEquals("[ERROR:variations_seed_loader.cc(39)] Seed missing", entry.message)
    }

    @Test
    fun `parseLine real DEBUG line`() {
        val line = "07-19 08:53:36:817 D/WM-Schedulers(4573) : Created SystemJobScheduler"
        val entry = LogParser.parseLine(line)
        assertNotNull(entry)
        entry!!
        assertEquals(LogEntry.Level.DEBUG, entry.level)
    }

    @Test
    fun `parseLine real VERBOSE line`() {
        val line = "07-19 08:53:36:720 V/[Koin](4573) : |- 'android.content.Context'..."
        val entry = LogParser.parseLine(line)
        assertNotNull(entry)
        entry!!
        assertEquals(LogEntry.Level.VERBOSE, entry.level)
    }

    @Test
    fun `parseLine message contains colon is preserved`() {
        // URL with colon in message must not be split by the parser
        val line = "07-19 10:15:30:123 I/chucker(6176) : http://example.com:8080/path"
        val entry = LogParser.parseLine(line)
        assertNotNull(entry)
        entry!!
        assertEquals("http://example.com:8080/path", entry.message)
    }

    @Test
    fun `parseLine message contains internal colon`() {
        val line = "07-19 10:15:30:456 E/Timber(1000) : FATAL EXCEPTION: main"
        val entry = LogParser.parseLine(line)
        assertNotNull(entry)
        entry!!
        assertEquals("FATAL EXCEPTION: main", entry.message)
    }

    @Test
    fun `parseLine message with spaces and punctuation`() {
        val line = "07-19 10:15:30:789 I/Tag(42) : Started 29 definitions in 7.362 ms"
        val entry = LogParser.parseLine(line)
        assertNotNull(entry)
        entry!!
        assertEquals("Started 29 definitions in 7.362 ms", entry.message)
    }

    // ── spacing resilience ──

    @Test
    fun `parseLine double-space between time and level-tag part`() {
        // Extra space between time token and level/tag token
        val line = "07-19 08:53:36:720  V/[Koin](4573) : |- 'android.content.Context'..."
        val entry = LogParser.parseLine(line)
        assertNotNull("Should tolerate double space between time and level/tag", entry)
        entry!!
        assertEquals(LogEntry.Level.VERBOSE, entry.level)
    }

    @Test
    fun `parseLine tab between header tokens`() {
        val line = "07-19\t08:53:36:720\tV/[Koin](4573) : Tab separated"
        val entry = LogParser.parseLine(line)
        assertNotNull("Should parse tab-separated header tokens", entry)
        entry!!
        assertEquals(LogEntry.Level.VERBOSE, entry.level)
    }

    // ── null returns ──

    @Test
    fun `parseLine null for invalid date`() {
        val line = "not-a-date 08:53:36:720 V/[Koin](4573) : msg"
        assertNull(LogParser.parseLine(line))
    }

    @Test
    fun `parseLine null for invalid time`() {
        val line = "07-19 not-a-time V/[Koin](4573) : msg"
        assertNull(LogParser.parseLine(line))
    }

    @Test
    fun `parseLine null for unknown level`() {
        val line = "07-19 08:53:36:720 X/Tag(4573) : Unknown level"
        assertNull(LogParser.parseLine(line))
    }

    @Test
    fun `parseLine null for missing separator`() {
        // No " : " separator
        val line = "07-19 08:53:36:720 I/Tag(4573) message without separator"
        assertNull(LogParser.parseLine(line))
    }

    @Test
    fun `parseLine null for empty line`() {
        assertNull(LogParser.parseLine(""))
    }

    @Test
    fun `parseLine null for line without header`() {
        // Stack trace continuation line
        val line = "\t\tat com.example.Test.testMethod(Test.kt:42)"
        assertNull(LogParser.parseLine(line))
    }

    // ── parse (stream) ──

    @Test
    fun `parse stream newest-first ordering`() {
        val input = """
            07-19 09:15:30:100 I/first(1000) : Line one
            07-19 09:15:30:200 E/second(1000) : Line two
            07-19 09:15:30:300 W/third(1000) : Line three
        """.trimIndent()

        val entries = LogParser.parse(inputStream(input))
        assertEquals(3, entries.size)
        assertEquals("Line three", entries[0].message)
        assertEquals("Line two", entries[1].message)
        assertEquals("Line one", entries[2].message)
    }

    @Test
    fun `parse stream multi-line stack trace accumulation`() {
        val input = """
            07-19 09:15:30:100 E/CrashHandler(1000) : NullPointerException!
            \t\tat com.example.MyClass.doWork(MyClass.kt:42)
            \t\tat com.example.MainActivity.onCreate(MainActivity.kt:15)
            07-19 09:15:30:200 I/Tag(1000) : Normal line after crash
        """.trimIndent()

        val entries = LogParser.parse(inputStream(input))
        assertEquals(2, entries.size)
        // Newest first
        assertEquals("Normal line after crash", entries[0].message)
        val crashEntry = entries[1]
        assertEquals(LogEntry.Level.ERROR, crashEntry.level)
        assertTrue(
            "Stack trace should be attached, got: ${crashEntry.message}",
            crashEntry.message.contains("at com.example.MyClass.doWork") &&
                crashEntry.message.contains("at com.example.MainActivity.onCreate"),
        )
        assertTrue(
            "Message should contain the first line",
            crashEntry.message.contains("NullPointerException!"),
        )
    }

    @Test
    fun `parse stream respects maxLines bound`() {
        val maxLines = 3
        val lines = (1..10).joinToString("\n") { i ->
            "07-19 09:15:30:${String.format("%03d", i)} I/t(1000) : Message $i"
        }
        val entries = LogParser.parse(inputStream(lines), maxLines = maxLines)
        assertEquals(maxLines, entries.size)
        assertEquals("Message 10", entries[0].message)
        assertEquals("Message 9", entries[1].message)
        assertEquals("Message 8", entries[2].message)
    }

    @Test
    fun `parse stream empty input`() {
        val entries = LogParser.parse(inputStream(""))
        assertTrue(entries.isEmpty())
    }

    @Test
    fun `parse stream blank lines are ignored`() {
        val input = """
            
            07-19 09:15:30:100 I/first(1000) : Line one
            
            07-19 09:15:30:200 E/second(1000) : Line two
            
        """.trimIndent()

        val entries = LogParser.parse(inputStream(input))
        assertEquals(2, entries.size)
    }

    @Test
    fun `parse stream leading garbled lines discarded`() {
        val input = """
            not a log line
            also not a log line
            07-19 09:15:30:100 I/t(1000) : Valid line
        """.trimIndent()

        val entries = LogParser.parse(inputStream(input))
        assertEquals(1, entries.size)
        assertEquals("Valid line", entries[0].message)
    }

    // ── parseLevel ──

    @Test
    fun `parseLevel maps from level-tag token`() {
        // Real tokens contain level char + slash + tag
        assertEquals(LogEntry.Level.ERROR, LogParser.parseLevel("E/ExceptionCrashHandler(2)"))
        assertEquals(LogEntry.Level.WARNING, LogParser.parseLevel("W/chromium(4596)"))
        assertEquals(LogEntry.Level.INFO, LogParser.parseLevel("I/[Koin](2)"))
        assertEquals(LogEntry.Level.DEBUG, LogParser.parseLevel("D/WM-Schedulers(4573)"))
        assertEquals(LogEntry.Level.VERBOSE, LogParser.parseLevel("V/[Koin](4573)"))
    }

    @Test
    fun `parseLevel null for invalid input`() {
        assertNull(LogParser.parseLevel(null))
        assertNull(LogParser.parseLevel(""))
        assertNull(LogParser.parseLevel("X"))
        assertNull(LogParser.parseLevel("error"))
    }

    // ── helpers ──

    private fun inputStream(s: String) =
        ByteArrayInputStream(s.toByteArray(StandardCharsets.UTF_8))
}
