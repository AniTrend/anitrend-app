package com.mxt.anitrend.base.custom.view.search

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.mxt.anitrend.R

/** Host activity for instrumentation tests of [MaterialSearchView]. */
class MaterialSearchViewTestActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val searchView = MaterialSearchView(this).apply {
            id = R.id.searchView
        }
        setContentView(searchView)
    }
}
