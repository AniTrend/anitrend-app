package com.mxt.anitrend.ui

import android.Manifest
import android.content.Context
import android.os.Build
import androidx.test.platform.app.InstrumentationRegistry
import com.mxt.anitrend.data.DatabaseHelper
import com.mxt.anitrend.extension.KoinExt
import com.mxt.anitrend.model.entity.anilist.WebToken
import com.mxt.anitrend.model.entity.base.AuthBase
import com.mxt.anitrend.util.Settings

object TestSessionUtil {
    fun setAuthenticated(
        context: Context,
        authenticated: Boolean,
    ) {
        grantNotificationPermission(context)
        val settings = Settings(context)
        settings.isAuthenticated = authenticated
        settings.isFreshInstall = false
        // Pin the stored version code to the current build so the first cold
        // MainActivity launch of a run never gets an unexpected changelog push
        // on top of the destination under test (checkUpdatedVersion).
        settings.setUpdated()
        val database = KoinExt.get(DatabaseHelper::class.java)
        if (authenticated) {
            val token =
                WebToken(
                    access_token = "test-token",
                    token_type = "Bearer",
                    expires_in = 3600,
                    refresh_token = "refresh",
                )
            token.calculateExpires()
            database.webToken = token
            database.authCode = AuthBase("test-code", "refresh")
        } else {
            database.invalidateBoxStores()
        }
    }

    /**
     * Grants POST_NOTIFICATIONS when it exists (API 33+) so the system
     * permission dialog never pauses the activity under test mid-launch.
     * Every MainActivity cold start requests the permission in onPostCreate;
     * an undismissed dialog leaves the activity paused and can stall
     * ActivityScenario teardown on slow emulators.
     */
    private fun grantNotificationPermission(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            InstrumentationRegistry.getInstrumentation().uiAutomation.grantRuntimePermission(
                context.packageName,
                Manifest.permission.POST_NOTIFICATIONS,
            )
        }
    }
}
