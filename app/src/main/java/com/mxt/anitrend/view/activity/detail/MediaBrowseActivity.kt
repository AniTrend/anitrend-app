package com.mxt.anitrend.view.activity.detail

import android.os.Bundle
import android.view.MenuItem
import com.mxt.anitrend.R
import com.mxt.anitrend.databinding.ActivityFrameGenericBinding
import com.mxt.anitrend.util.KeyUtil
import com.mxt.anitrend.util.markdown.MarkDownUtil
import com.mxt.anitrend.view.activity.CommonActivity
import com.mxt.anitrend.view.fragment.list.MediaBrowseFragment

class MediaBrowseActivity : CommonActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val binding = ActivityFrameGenericBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.customToolbar.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        intent.getStringExtra(KeyUtil.arg_activity_tag)?.let { tag ->
            supportActionBar?.title = MarkDownUtil.convert(this, tag)
        }

        val fragment = MediaBrowseFragment.newInstance(intent.extras ?: Bundle.EMPTY)
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.content_frame, fragment, fragment.TAG)
            .commit()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            onBackPressedDispatcher.onBackPressed()
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}
