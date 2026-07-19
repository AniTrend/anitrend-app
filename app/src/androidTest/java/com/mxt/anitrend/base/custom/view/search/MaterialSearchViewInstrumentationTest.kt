package com.mxt.anitrend.base.custom.view.search

import androidx.core.view.isVisible
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.mxt.anitrend.R
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@LargeTest
@RunWith(AndroidJUnit4::class)
class MaterialSearchViewInstrumentationTest {

    @Test
    fun showSearch_setsStateOpenAndVisible() {
        ActivityScenario.launch(MaterialSearchViewTestActivity::class.java).use { scenario ->
            scenario.onActivity { activity ->
                val searchView = activity.findViewById<MaterialSearchView>(R.id.searchView)

                assertFalse("Initially search should be closed", searchView.isSearchOpen)

                searchView.showSearch(false)

                assertTrue("Search should be open", searchView.isSearchOpen)
                // We check the internal layout visibility
                // MaterialSearchView uses binding.root which is search_layout
                // Since we can't access binding directly, we find it by ID
                val searchLayout = searchView.findViewById<android.view.View>(R.id.search_layout)
                assertTrue("Search layout should be visible", searchLayout.isVisible)
            }
        }
    }

    @Test
    fun clickingUpButton_closesSearch() {
        ActivityScenario.launch(MaterialSearchViewTestActivity::class.java).use { scenario ->
            scenario.onActivity { activity ->
                val searchView = activity.findViewById<MaterialSearchView>(R.id.searchView)
                var backClicked = false
                searchView.setOnClickBackListener(object : MaterialSearchView.OnClickBackListener {
                    override fun onClickBack() {
                        backClicked = true
                    }
                })

                searchView.showSearch(false)
                assertTrue("Search should be open", searchView.isSearchOpen)

                val upButton = searchView.findViewById<android.view.View>(R.id.action_up_btn)
                upButton.performClick()

                assertTrue("OnClickBackListener should be notified", backClicked)
                assertFalse("Search should be closed after clicking up", searchView.isSearchOpen)

                val searchLayout = searchView.findViewById<android.view.View>(R.id.search_layout)
                assertFalse("Search layout should be hidden", searchLayout.isVisible)
            }
        }
    }
}
