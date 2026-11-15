package com.mxt.anitrend.widget

import android.os.Bundle
import android.widget.Button
import android.widget.FrameLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.mxt.anitrend.R

class ProgressLayoutTestActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val progressLayout =
            ProgressLayout(this).apply {
                id = R.id.progressLayout
                layoutParams =
                    FrameLayout.LayoutParams(
                        FrameLayout.LayoutParams.MATCH_PARENT,
                        FrameLayout.LayoutParams.MATCH_PARENT,
                    )
            }

        val contentChild =
            TextView(this).apply {
                id = R.id.contentChild
                text = "content"
            }
        val secondContentChild =
            Button(this).apply {
                id = R.id.secondContentChild
                text = "second"
            }
        val initiallyGoneContentChild =
            TextView(this).apply {
                id = R.id.initiallyGoneContentChild
                text = "gone"
                visibility = android.view.View.GONE
            }
        val initiallyInvisibleContentChild =
            TextView(this).apply {
                id = R.id.initiallyInvisibleContentChild
                text = "invisible"
                visibility = android.view.View.INVISIBLE
            }

        progressLayout.addView(contentChild)
        progressLayout.addView(secondContentChild)
        progressLayout.addView(initiallyGoneContentChild)
        progressLayout.addView(initiallyInvisibleContentChild)

        setContentView(progressLayout)
    }
}
