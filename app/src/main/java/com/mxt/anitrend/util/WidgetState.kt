package com.mxt.anitrend.util

import java.util.Locale

object WidgetState {
    const val CONTENT_STATE = 0
    const val LOADING_STATE = 1

    fun convertToText(count: Int): String = String.format(Locale.getDefault(), " %d ", count)

    fun valueFormatter(size: Int): String {
        if (size != 0) {
            return if (size > 1000) {
                String.format(Locale.getDefault(), "%.1f K", size / 1000f)
            } else {
                size.toString()
            }
        }
        return "0"
    }
}
