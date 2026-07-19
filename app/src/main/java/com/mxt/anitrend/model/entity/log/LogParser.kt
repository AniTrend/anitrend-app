package com.mxt.anitrend.model.entity.log

import java.io.InputStream
import java.io.InputStreamReader

/**
 * Pure parser for persisted log lines written by this app.
 *
 * Real format (from the device persisted log):
 * `MM-DD HH:MM:SS:mmm LEVEL/TAG(PID) : message`
 *
 * Examples:
 * - `07-17 21:58:44:896 I/[Koin](2) : Started 29 definitions in 7.362 ms`
 * - `07-17 22:42:01:600 E/ExceptionCrashHandler(2) : main`
 *
 * The " : " (space-colon-space) token reliably separates the header from the
 * free-form message, even when the message itself contains colons.
 *
 * Multi-line stack trace lines that do not match the log header are accumulated
 * onto the preceding entry. Returns newest-first, bounded to [maxLines].
 */
object LogParser {

    /** Matches one or more whitespace characters. */
    val DELIMITER: Regex = Regex("\\s+")

    /** Separator between the log header and the free-form message. */
    private const val HEADER_SEPARATOR = " : "

    /**
     * Parses the given [input] stream, returning newest-first entries up to [maxLines].
     */
    fun parse(
        input: InputStream,
        maxLines: Int = 5_000,
    ): List<LogEntry> {
        val buffer = ArrayDeque<LogEntry>(maxLines)

        InputStreamReader(input).forEachLine { line ->
            val trimmed = line.trimEnd()
            if (trimmed.isEmpty()) {
                return@forEachLine
            }

            val entry = parseLine(trimmed)
            if (entry != null) {
                if (buffer.size >= maxLines) {
                    buffer.removeFirst()
                }
                buffer.addLast(entry)
            } else if (buffer.isNotEmpty()) {
                val last = buffer.removeLast()
                buffer.addLast(last.copy(message = last.message + "\n" + trimmed))
            }
        }

        return buffer.reversed()
    }

    /**
     * Attempts to parse a single line into a [LogEntry].
     * Returns null if the line does not match the log header format.
     *
     * Format: `MM-DD HH:MM:SS:mmm LEVEL/TAG(PID) : message`
     */
    internal fun parseLine(line: String): LogEntry? {
        val separatorIndex = line.indexOf(HEADER_SEPARATOR)
        if (separatorIndex < 0) {
            return null
        }

        val prefix = line.substring(0, separatorIndex)
        val message = line.substring(separatorIndex + HEADER_SEPARATOR.length)

        // Prefix: "MM-DD HH:MM:SS:mmm LEVEL/TAG(PID)"
        val prefixTokens = prefix.split(DELIMITER, limit = 3)
        if (prefixTokens.size < 3) {
            return null
        }

        val dateToken = prefixTokens[0]
        val timeToken = prefixTokens[1]
        val levelTagPart = prefixTokens[2] // e.g. "E/ExceptionCrashHandler(2)"

        // Validate date: MM-DD (position 2 must be '-')
        if (dateToken.length < 5 || dateToken[2] != '-') {
            return null
        }

        // Validate time: HH:MM:SS:mmm (at least 3 colons, position 2 and 5 are ':')
        if (timeToken.length < 12 || timeToken[2] != ':' || timeToken[5] != ':') {
            return null
        }

        // Level is the first character of the level/tag token
        val level = parseLevel(levelTagPart)
            ?: return null

        return LogEntry(
            date = dateToken,
            time = timeToken,
            level = level,
            message = message,
        )
    }

    /**
     * Maps the first character of a log-header token to [LogEntry.Level].
     */
    internal fun parseLevel(token: String?): LogEntry.Level? {
        if (token.isNullOrEmpty()) {
            return null
        }
        return when (token[0]) {
            'E' -> LogEntry.Level.ERROR
            'W' -> LogEntry.Level.WARNING
            'I' -> LogEntry.Level.INFO
            'D' -> LogEntry.Level.DEBUG
            'V' -> LogEntry.Level.VERBOSE
            else -> null
        }
    }
}
