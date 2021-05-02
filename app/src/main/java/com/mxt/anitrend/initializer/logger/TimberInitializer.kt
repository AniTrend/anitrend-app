package com.mxt.anitrend.initializer.logger

import android.content.Context
import android.util.Log
import androidx.startup.Initializer
import com.mxt.anitrend.BuildConfig
import com.mxt.anitrend.extension.logDirectory
import fr.bipi.tressence.file.FileLoggerTree
import timber.log.Timber

class TimberInitializer : Initializer<Unit> {

    private fun createFileLoggingTree(context: Context): Timber.Tree {
        return FileLoggerTree.Builder()
            .withFileName("${context.packageName}.log")
            .withDirName(context.logDirectory().absolutePath)
            .withSizeLimit(FILE_SIZE_LIMIT)
            .withFileLimit(FILE_CREATION_LIMIT)
            .withMinPriority(MIN_LOG_LEVEL)
            .appendToFile(true)
            .build()
    }

    /**
     * Initializes and a component given the application [Context]
     *
     * @param context The application context.
     */
    override fun create(context: Context) {
        if (BuildConfig.DEBUG)
            Timber.plant(Timber.DebugTree())

        runCatching {
            val fileTree = createFileLoggingTree(context)
            Timber.plant(fileTree)
        }.exceptionOrNull()?.printStackTrace()
    }

    /**
     * @return A list of dependencies that this [Initializer] depends on. This is
     * used to determine initialization order of [Initializer]s.
     *
     * For e.g. if a [Initializer] `B` defines another
     * [Initializer] `A` as its dependency, then `A` gets initialized before `B`.
     */
    override fun dependencies(): List<Class<out Initializer<*>>> = emptyList()

    companion object {
        val MIN_LOG_LEVEL = if (BuildConfig.DEBUG) Log.DEBUG else Log.WARN
        const val FILE_SIZE_LIMIT = 800 * 1024
        const val FILE_CREATION_LIMIT = 1
    }
}