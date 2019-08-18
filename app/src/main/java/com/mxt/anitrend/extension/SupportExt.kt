package com.mxt.anitrend.extension



/**
 * No locks are used to synchronize an access to the [Lazy] instance value; if the instance is accessed from multiple threads,
 * its behavior is undefined.
 *
 * This mode should not be used unless the [Lazy] instance is guaranteed never to be initialized from more than one thread.
 */
val LAZY_MODE_UNSAFE = LazyThreadSafetyMode.NONE

/**
 * Initializer function can be called several times on concurrent access to uninitialized [Lazy] instance value,
 * but only the first returned value will be used as the value of [Lazy] instance.
 */
val LAZY_MODE_PUBLICATION = LazyThreadSafetyMode.PUBLICATION

/**
 * Locks are used to ensure that only a single thread can initialize the [Lazy] instance.
 */
val LAZY_MODE_SYNCHRONIZED = LazyThreadSafetyMode.SYNCHRONIZED

/**
 * Potentially useless but returns an empty string, the signature may change in future
 *
 * @see String.isNullOrBlank
 */
fun String.Companion.empty() = ""


/**
 * Returns a copy of this strings having its first letter uppercase, or the original string,
 * if it's empty or already starts with an upper case letter.
 *
 * @param exceptions words or characters to exclude during capitalization
 */
fun String?.capitalizeWords(exceptions: List<String>? = null): String = when {
    !this.isNullOrEmpty() -> {
        val result = StringBuilder(length)
        val words = split("_|\\s".toRegex()).dropLastWhile { it.isEmpty() }
        for ((index, word) in words.withIndex()) {
            when (word.isNotEmpty()) {
                true -> {
                    if (!exceptions.isNullOrEmpty() && exceptions.contains(word)) result.append(word)
                    else result.append(word.capitalize())
                }
            }
            if (index != words.size - 1)
                result.append(" ")
        }
        result.toString()
    }
    else -> String.empty()
}


