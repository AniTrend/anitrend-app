package com.mxt.anitrend.presenter.base

import android.content.Context
import com.mxt.anitrend.base.interfaces.dao.BoxQuery
import com.mxt.anitrend.util.Settings
import org.junit.Assert.assertEquals
import org.junit.Test
import org.mockito.Mockito.mock

class BasePresenterTests {
    @Test
    fun getTopFavouriteYears_returnsCachedYears() {
        val presenter = BasePresenter(
            context = mock(Context::class.java),
            boxQuery = mock(BoxQuery::class.java),
            settings = mock(Settings::class.java),
        )
        val years = listOf("2024", "2023")
        val tags = listOf("Action", "Drama")

        presenter.setPrivateField("favouriteYears", years)
        presenter.setPrivateField("favouriteTags", tags)

        assertEquals(years, presenter.getTopFavouriteYears(limit = 2))
    }

    private fun BasePresenter.setPrivateField(
        name: String,
        value: Any?,
    ) {
        val field = BasePresenter::class.java.getDeclaredField(name)
        field.isAccessible = true
        field.set(this, value)
    }
}
