package com.mxt.anitrend.adapter.recycler.index

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.neverEqualPolicy
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.recyclerview.widget.RecyclerView
import com.mxt.anitrend.R
import com.mxt.anitrend.base.custom.recycler.RecyclerViewAdapter
import com.mxt.anitrend.base.custom.recycler.RecyclerViewHolder
import com.mxt.anitrend.base.custom.view.widget.FollowStateWidget
import com.mxt.anitrend.model.entity.base.UserBase
import com.mxt.anitrend.ui.designsystem.AniTrendTheme
import com.mxt.anitrend.ui.user.UserRow
import com.mxt.anitrend.ui.user.UserRowUiState
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
    ): RecyclerViewHolder<UserBase> = UserViewHolder(parent)

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
        parent: ViewGroup,
    ) : RecyclerViewHolder<UserBase>(
        ComposeView(parent.context).apply {
            id = R.id.container
            layoutParams =
                RecyclerView.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                ).apply {
                    bottomMargin = parent.resources.getDimensionPixelSize(R.dimen.md_margin)
                }
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnDetachedFromWindowOrReleasedFromPool)
        },
    ) {
        private val composeView = itemView as ComposeView
        private val user = mutableStateOf<UserRowUiState?>(null, neverEqualPolicy())

        init {
            composeView.setContent {
                AniTrendTheme {
                    user.value?.let { current ->
                        UserRow(
                            state = current,
                            currentUser = currentUser,
                            followListener = followListener,
                            onClick = { this@UserViewHolder.onClick(composeView) },
                        )
                    }
                }
            }
        }

        override fun onBindViewHolder(model: UserBase) {
            user.value =
                UserRowUiState(
                    model = model,
                    name = model.name,
                    avatar = model.avatar,
                    isFollowing = model.isFollowing,
                )
        }

        override fun onViewRecycled() {
            user.value = null
        }

        override fun onClick(v: View) {
            performClick(clickListener, data, v)
        }

        override fun onLongClick(v: View): Boolean = performLongClick(clickListener, data, v)
    }
}
