package com.mxt.anitrend.base.custom.view.search

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.mxt.anitrend.test.R

/**
 * Host activity for [MaterialSearchViewInstrumentationTest]. Builds the search
 * view programmatically with the stable harness id the tests resolve,
 * following the same pattern as
 * [com.mxt.anitrend.widget.ProgressLayoutTestActivity].
 */
class MaterialSearchViewTestActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val searchView =
            MaterialSearchView(this).apply {
                id = R.id.searchView
            }
        setContentView(searchView)
    }
}