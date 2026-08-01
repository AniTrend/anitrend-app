package com.mxt.anitrend.view.activity.detail

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import com.mxt.anitrend.R
import com.mxt.anitrend.databinding.ActivityFrameGenericBinding
import com.mxt.anitrend.view.activity.CommonActivity
import com.mxt.anitrend.view.activity.index.MainActivity
import com.mxt.anitrend.view.fragment.detail.NotificationFragment

class NotificationActivity : CommonActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val binding = ActivityFrameGenericBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.customToolbar.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val fragment = NotificationFragment.newInstance()
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.content_frame, fragment, fragment.TAG)
            .commit()
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        super.onBackPressed()
        navigateBack()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            navigateBack()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    @Suppress("DEPRECATION")
    private fun navigateBack() {
        if (isTaskRoot) {
            startActivity(Intent(this, MainActivity::class.java))
        }
        super.onBackPressed()
    }
}
