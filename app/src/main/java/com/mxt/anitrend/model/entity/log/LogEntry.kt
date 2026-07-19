package com.mxt.anitrend.model.entity.log

data class LogEntry(
    val date: String,
    val time: String,
    val level: Level,
    val message: String,
) {
    enum class Level(
        val identifier: Char,
    ) {
        ERROR('E'),
        WARNING('W'),
        INFO('I'),
        DEBUG('D'),
        VERBOSE('V'),
    }
}
