package com.mxt.anitrend.ui

import android.content.Context
import com.mxt.anitrend.data.DatabaseHelper
import com.mxt.anitrend.model.entity.anilist.WebToken
import com.mxt.anitrend.model.entity.base.AuthBase
import com.mxt.anitrend.util.Settings

object TestSessionUtil {
    fun setAuthenticated(
        context: Context,
        authenticated: Boolean,
    ) {
        val settings = Settings(context)
        settings.isAuthenticated = authenticated
        val database = DatabaseHelper()
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
}
