package com.mxt.anitrend.base.custom.recycler

import android.content.Context
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.annimon.stream.IntPair
import com.mxt.anitrend.base.interfaces.event.ItemClickListener
import com.mxt.anitrend.util.ActionModeUtil

/**
 * Created by max on 2017/06/09.
 * Recycler view holder implementation
 */
abstract class RecyclerViewHolder<T>(view: View) :
    RecyclerView.ViewHolder(view),
    View.OnClickListener,
    View.OnLongClickListener {

    private var callback: ActionModeUtil<T>? = null

    /**
     * Load image, text, buttons, etc. in this method from the given parameter
     *
     * @param model Is the model at the current adapter position
     */
    abstract fun onBindViewHolder(model: T)

    /**
     * If any image views are used within the view holder, clear any pending async img requests
     * by using Glide.clear(ImageView) or Glide.with(context).clear(view) if using Glide v4.0
     *
     * @see com.bumptech.glide.Glide
     */
    abstract fun onViewRecycled()

    /**
     * Handle any onclick events from our views
     *
     * @param v the view that has been clicked
     * @see View.OnClickListener
     */
    abstract override fun onClick(v: View)

    fun getContext(): Context =
        itemView.context.applicationContext

    /**
     * Applying selection styling on the desired item
     * @param model the current model item
     */
    fun onBindSelectionState(model: T) {
        callback?.setBackgroundColor(this, callback?.isSelected(model) == true)
    }

    /**
     * Constructs an int pair container with a boolean representing a valid adapter position
     * @return IntPair
     */
    protected fun isValidIndexPair(): IntPair<Boolean> {
        val index = bindingAdapterPosition
        return IntPair(index, index != RecyclerView.NO_POSITION)
    }

    /**
     * Handle any onclick events from our views
     *
     * @param v the view that has been clicked
     * @see View.OnClickListener
     */
    protected fun performClick(clickListener: ItemClickListener<T>?, data: List<T>, v: View) {
        if (clickListener == null)
            return
        val pair = isValidIndexPair()
        if (pair.second) {
            val model = data[pair.first]
            if (isClickable(model))
                clickListener.onItemClick(v, IntPair(pair.first, model))
        }
    }

    /**
     * Called when a view has been clicked and held.
     *
     * @param v The view that was clicked and held.
     * @return true if the callback consumed the long click, false otherwise.
     */
    protected fun performLongClick(clickListener: ItemClickListener<T>?, data: List<T>, v: View): Boolean {
        if (clickListener == null)
            return false
        val pair = isValidIndexPair()
        if (pair.second) {
            val model = data[pair.first]
            if (isLongClickable(model)) {
                clickListener.onItemLongClick(v, IntPair(pair.first, model))
                return true
            }
        }
        return false
    }

    protected fun isClickable(clicked: T): Boolean =
        callback?.onItemClick(this, clicked) != true

    protected fun isLongClickable(clicked: T): Boolean =
        callback?.onItemLongClick(this, clicked) != true

    protected fun bindClickListeners(vararg viewIds: Int) {
        for (viewId in viewIds) {
            val view = itemView.findViewById<View?>(viewId)
            view?.setOnClickListener(this)
        }
    }

    protected fun bindLongClickListeners(vararg viewIds: Int) {
        for (viewId in viewIds) {
            val view = itemView.findViewById<View?>(viewId)
            view?.setOnLongClickListener(this)
        }
    }

    fun setActionMode(actionModeUtil: ActionModeUtil<T>?) {
        callback = actionModeUtil
    }
}
