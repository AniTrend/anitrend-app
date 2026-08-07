package com.mxt.anitrend.adapter.recycler.index

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import com.bumptech.glide.Glide
import com.mxt.anitrend.R
import com.mxt.anitrend.base.custom.recycler.RecyclerViewAdapter
import com.mxt.anitrend.base.custom.recycler.RecyclerViewHolder
import com.mxt.anitrend.base.custom.view.widget.FollowStateWidget
import com.mxt.anitrend.binding.setImage
import com.mxt.anitrend.databinding.AdapterUserBinding
import com.mxt.anitrend.extension.getLayoutInflater
import com.mxt.anitrend.model.entity.base.UserBase
import java.util.Locale

/**
 * Created by max on 2017/11/10.
 */
class UserAdapter(
    context: Context,
    private val currentUser: UserBase?,
    private val onToggleFollowAction: (Long, (Result<UserBase>) -> Unit) -> Unit,
) : RecyclerViewAdapter<UserBase>(context) {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): RecyclerViewHolder<UserBase> = UserViewHolder(
        AdapterUserBinding.inflate(parent.context.getLayoutInflater(), parent, false),
    )

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val results = FilterResults()
                if (clone.isNullOrEmpty()) {
                    clone = data
                }
                val filter = constraint?.toString()?.lowercase(Locale.getDefault()).orEmpty()
                results.values =
                    if (filter.isBlank()) {
                        val snapshot = ArrayList(clone ?: emptyList())
                        clone = null
                        snapshot
                    } else {
                        (clone ?: emptyList()).filter { model ->
                            model.name?.lowercase(Locale.getDefault())?.contains(filter) == true
                        }
                    }
                return results
            }

            @Suppress("UNCHECKED_CAST")
            override fun publishResults(
                constraint: CharSequence?,
                results: FilterResults,
            ) {
                val filtered = results.values as? List<UserBase>
                if (filtered != null) {
                    onItemsInserted(filtered)
                }
            }
        }
    }

    /**
     * Bridges the render-only widget's fire-and-forget userId delivery to the owner's
     * callback. The widget no longer accepts a result; the owner decides how the
     * mutation result is applied. Legacy owners still receive their original result
     * callback contract.
     */
    private val followListener = FollowStateWidget.Listener { userId ->
        onToggleFollowAction(userId) { _ -> }
    }

    inner class UserViewHolder(
        private val binding: AdapterUserBinding,
    ) : RecyclerViewHolder<UserBase>(binding.root) {
        init {
            bindClickListeners(R.id.container)
        }

        override fun onBindViewHolder(model: UserBase) {
            binding.userAvatar.setImage(model.avatar)
            binding.userName.text = model.name
            // Current-user context first: the widget evaluates control visibility on
            // model bind, so a fresh row must never evaluate against a null context.
            binding.userFollowStateWidget.setCurrentUser(currentUser)
            binding.userFollowStateWidget.setUserModel(model)
            binding.userFollowStateWidget.setListener(followListener)
        }

        override fun onViewRecycled() {
            Glide.with(getContext()).clear(binding.userAvatar)
            binding.userFollowStateWidget.setListener(null)
            binding.userFollowStateWidget.onViewRecycled()
        }

        override fun onClick(v: View) {
            performClick(clickListener, data, v)
        }

        override fun onLongClick(v: View): Boolean = performLongClick(clickListener, data, v)
    }
}
