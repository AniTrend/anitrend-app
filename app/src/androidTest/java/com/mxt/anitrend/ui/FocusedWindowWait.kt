@file:Suppress("UndocumentedPublicClass", "UndocumentedPublicFunction")

package com.mxt.anitrend.ui

import android.accessibilityservice.AccessibilityServiceInfo
import android.app.Instrumentation
import android.os.SystemClock
import android.view.accessibility.AccessibilityNodeInfo
import java.util.concurrent.TimeoutException

/** Waits until the target app's focused popup or dialog exposes the expected text. */
@Suppress("DEPRECATION")
internal fun Instrumentation.waitForFocusedWindowContainingText(text: String, windowType: Int) {
    val automation = uiAutomation
    val serviceInfo = checkNotNull(automation.serviceInfo)
    serviceInfo.flags =
        serviceInfo.flags or AccessibilityServiceInfo.FLAG_RETRIEVE_INTERACTIVE_WINDOWS
    automation.setServiceInfo(serviceInfo)

    val targetPackage = targetContext.packageName
    val deadline = SystemClock.uptimeMillis() + 15_000L
    val observedWindows = linkedSetOf<String>()
    var lastWindowCount = 0

    while (SystemClock.uptimeMillis() < deadline) {
        var matchingWindowFound = false
        val windows = automation.windows
        lastWindowCount = windows.size
        for (window in windows) {
            try {
                val observedType = window.type
                val isFocused = window.isFocused
                val root = window.root
                if (root == null) {
                    observedWindows += "type=$observedType, focused=$isFocused, root=null"
                    continue
                }
                try {
                    val packageName = root.packageName?.toString()
                    val matchingNodes = root.findAccessibilityNodeInfosByText(text)
                    try {
                        val matchesText = matchingNodes.isNotEmpty()
                        observedWindows +=
                            "type=$observedType, focused=$isFocused, package=$packageName, matchesText=$matchesText"
                        if (
                            isFocused &&
                            observedType == windowType &&
                            packageName == targetPackage &&
                            matchesText
                        ) {
                            matchingWindowFound = true
                        }
                    } finally {
                        matchingNodes.forEach(AccessibilityNodeInfo::recycle)
                    }
                } finally {
                    root.recycle()
                }
            } finally {
                window.recycle()
            }
        }
        if (matchingWindowFound) return

        try {
            automation.waitForIdle(100L, 250L)
        } catch (_: TimeoutException) {
            // Continue checking until the focus deadline.
        }
    }

    throw AssertionError(
        "No focused window of type $windowType in $targetPackage showed '$text'; " +
            "flags=${serviceInfo.flags}, lastWindowCount=$lastWindowCount, " +
            "observed windows: ${observedWindows.joinToString()}",
    )
}
