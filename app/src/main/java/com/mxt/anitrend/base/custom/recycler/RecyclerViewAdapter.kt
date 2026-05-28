package com.mxt.anitrend.base.custom.recycler

import android.animation.Animator
import android.content.Context
import android.view.ViewGroup
import android.widget.Filterable
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.mxt.anitrend.base.custom.animation.SlideInAnimation
import com.mxt.anitrend.base.interfaces.base.BaseAnimation
import com.mxt.anitrend.base.interfaces.event.ItemClickListener
import com.mxt.anitrend.base.interfaces.event.RecyclerChangeListener
import com.mxt.anitrend.presenter.base.BasePresenter
import com.mxt.anitrend.util.ActionModeUtil
import com.mxt.anitrend.util.CompatUtil
import com.mxt.anitrend.util.KeyUtil
import java.util.ArrayList

/**
 * Created by max on 2017/06/09.
 * Recycler view adapter implementation
 */
abstract class RecyclerViewAdapter<T>(context: Context) :
    RecyclerView.Adapter<RecyclerViewHolder<T>>(),
    Filterable,
    RecyclerChangeListener<T> {

    protected val context: Context = context.applicationContext
    var data: MutableList<T> = ArrayList()
        protected set
    protected var clone: MutableList<T>? = null
    protected val presenter: BasePresenter = BasePresenter(this.context)
    var clickListener: ItemClickListener<T>? = null
        @JvmName("setClickListenerInternal") set

    private var actionMode: ActionModeUtil<T>? = null
    private var customAnimation: BaseAnimation? = null

    private var lastPosition = 0
    private val isLowRamDevice = CompatUtil.isLowRamDevice(this.context)

    override fun getItemId(position: Int): Long {
        if (!hasStableIds())
            return super.getItemId(position)
        return data[position].hashCode().toLong()
    }

    fun setActionModeCallback(selectorCallback: ActionModeUtil<T>) {
        actionMode = selectorCallback
        selectorCallback.setRecyclerAdapter(this)
    }

    fun setClickListener(clickListener: ItemClickListener<T>) {
        this.clickListener = clickListener
    }

    override fun onItemsInserted(swap: List<T>) {
        data = ArrayList(swap)
        notifyDataSetChanged()
    }

    override fun onItemRangeInserted(swap: List<T>) {
        val startRange = itemCount
        data.addAll(swap)
        val difference = itemCount - startRange
        if (difference > 5)
            notifyItemRangeInserted(startRange, difference)
        else if (difference != 0)
            notifyDataSetChanged()
    }

    override fun onItemRangeChanged(swap: List<T>) {
        val startRange = itemCount
        val difference = swap.size - startRange
        data = ArrayList(swap)
        notifyItemRangeChanged(startRange, difference)
    }

    override fun onItemChanged(swap: T, position: Int) {
        data[position] = swap
        notifyItemChanged(position)
    }

    override fun onItemRemoved(position: Int) {
        data.removeAt(position)
        notifyItemRemoved(position)
    }

    abstract override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerViewHolder<T>

    override fun onViewAttachedToWindow(holder: RecyclerViewHolder<T>) {
        super.onViewAttachedToWindow(holder)
        val layoutParams = holder.itemView.layoutParams
        if (layoutParams is StaggeredGridLayoutManager.LayoutParams)
            setLayoutSpanSize(layoutParams, holder.bindingAdapterPosition)
    }

    override fun onViewDetachedFromWindow(holder: RecyclerViewHolder<T>) {
        super.onViewDetachedFromWindow(holder)
        holder.itemView.clearAnimation()
    }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        val layoutManager = recyclerView.layoutManager
        if (layoutManager is GridLayoutManager)
            setLayoutSpanSize(layoutManager)
    }

    /**
     * Calls the the recycler view holder to perform view binding
     * @see RecyclerViewHolder
     *
     * default implementation is already done for you
     */
    override fun onBindViewHolder(holder: RecyclerViewHolder<T>, position: Int) {
        if (itemCount > 0) {
            animateViewHolder(holder, position)
            val model = data[position]
            if (model != null) {
                holder.setActionMode(actionMode)
                holder.onBindViewHolder(model)
                holder.onBindSelectionState(model)
            }
        }
    }

    /**
     * Calls the the recycler view holder impl to perform view recycling
     * @see RecyclerViewHolder
     *
     * default implementation is already done for you
     */
    override fun onViewRecycled(holder: RecyclerViewHolder<T>) {
        holder.onViewRecycled()
    }

    /**
     * Returns the total number of items in the data set held by the adapter.
     *
     * @return The total number of items in this adapter.
     */
    override fun getItemCount(): Int = data.size

    /**
     * Clears data sets and notifies the recycler observer about the changed data set
     */
    fun clearDataSet() {
        data = ArrayList()
        if (clone != null)
            clone = ArrayList()
        notifyDataSetChanged()
    }

    /**
     * Initial implementation is only specific for group types of recyclers,
     * in order to customize this an override is required.
     *
     * @param layoutManager grid layout manage for your recycler
     */
    private fun setLayoutSpanSize(layoutManager: GridLayoutManager) {
        layoutManager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
            override fun getSpanSize(position: Int): Int {
                if (isFullSpanItem(position))
                    return 1
                return layoutManager.spanCount
            }
        }
    }

    /**
     * Initial implementation is only specific for group types of recyclers,
     * in order to customize this an override is required.
     *
     * @param layoutParams StaggeredGridLayoutManager.LayoutParams for your recycler
     */
    private fun setLayoutSpanSize(layoutParams: StaggeredGridLayoutManager.LayoutParams, position: Int) {
        if (isFullSpanItem(position))
            layoutParams.isFullSpan = true
    }

    /**
     * Get currently set animation type for recycler view holder items,
     * if no custom animation is set {@link SlideInAnimation}
     * will be assigned in {@link #onAttachedToRecyclerView(RecyclerView)}
     *
     * @see BaseAnimation
     */
    private fun getCustomAnimation(): BaseAnimation {
        return customAnimation ?: SlideInAnimation().also { customAnimation = it }
    }

    /**
     * Set your own custom animation that will be used in
     * {@link #onAttachedToRecyclerView(RecyclerView)}
     *
     * @see BaseAnimation
     */
    fun setCustomAnimation(customAnimation: BaseAnimation) {
        this.customAnimation = customAnimation
    }

    protected fun isRecyclerStateType(viewType: Int): Boolean =
        viewType == KeyUtil.RECYCLER_TYPE_EMPTY ||
            viewType == KeyUtil.RECYCLER_TYPE_LOADING ||
            viewType == KeyUtil.RECYCLER_TYPE_ERROR

    private fun isFullSpanItem(position: Int): Boolean {
        val viewType = if (position != RecyclerView.NO_POSITION) getItemViewType(position) else KeyUtil.RECYCLER_TYPE_ERROR
        return viewType == KeyUtil.RECYCLER_TYPE_HEADER || viewType == KeyUtil.RECYCLER_TYPE_EMPTY ||
            viewType == KeyUtil.RECYCLER_TYPE_LOADING || viewType == KeyUtil.RECYCLER_TYPE_ERROR
    }

    private fun animateViewHolder(holder: RecyclerViewHolder<T>, position: Int) {
        if (!isLowRamDevice && position > lastPosition) {
            val animation = getCustomAnimation()
            val animators = animation.getAnimators(holder.itemView)
            for (animator: Animator in animators) {
                animator.duration = animation.getAnimationDuration().toLong()
                animator.interpolator = animation.getInterpolator()
                animator.start()
            }
        }
        lastPosition = position
    }
}
