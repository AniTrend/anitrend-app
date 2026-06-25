package com.mxt.anitrend.base.custom.recycler

import android.content.Context
import android.util.AttributeSet
import androidx.recyclerview.widget.RecyclerView
import com.mxt.anitrend.base.interfaces.view.CustomView

/**
 * Class [StatefulRecyclerView] extends [RecyclerView] and adds position management on configuration changes.
 *
 * @author FrantisekGazo
 * @version 2016-03-15
 * Modified by max
 */
open class StatefulRecyclerView
@JvmOverloads
constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0,
) : RecyclerView(context, attrs, defStyle),
    CustomView {
    private var isListenerPresent = false

    init {
        onInit()
    }

    /**
     * Add a listener that will be notified of any changes in scroll state or position.
     *
     * Components that add a listener should take care to remove it when finished.
     * Other components that take ownership of a view may call [clearOnScrollListeners]
     * to remove all attached listeners.
     *
     * @param listener listener to set or null to clear
     */
    override fun addOnScrollListener(listener: OnScrollListener) {
        super.addOnScrollListener(listener)
        isListenerPresent = true
    }

    /**
     * Remove all secondary listener that were notified of any changes in scroll state or position.
     */
    override fun clearOnScrollListeners() {
        super.clearOnScrollListeners()
        isListenerPresent = false
    }

    // To avoid multiple instances of scroll listener from being added
    fun hasOnScrollListener(): Boolean = isListenerPresent

    /**
     * Optionally included when constructing custom views
     */
    override fun onInit() = Unit

    /**
     * Clean up any resources that won't be needed
     */
    override fun onViewRecycled() = Unit
}
