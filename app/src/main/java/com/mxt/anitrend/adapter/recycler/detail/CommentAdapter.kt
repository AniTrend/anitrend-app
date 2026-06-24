package com.mxt.anitrend.adapter.recycler.detail

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import com.bumptech.glide.Glide
import com.mxt.anitrend.R
import com.mxt.anitrend.base.custom.recycler.RecyclerViewAdapter
import com.mxt.anitrend.base.custom.recycler.RecyclerViewHolder
import com.mxt.anitrend.binding.richMarkDown
import com.mxt.anitrend.binding.setImage
import com.mxt.anitrend.databinding.AdapterCommentBinding
import com.mxt.anitrend.extension.getLayoutInflater
import com.mxt.anitrend.model.entity.anilist.FeedReply
import com.mxt.anitrend.util.KeyUtil
import com.mxt.anitrend.util.date.DateUtil

/**
 * Created by max on 2017/12/03.
 * comment activity adapter
 */
class CommentAdapter(
    context: Context,
) : RecyclerViewAdapter<FeedReply>(context) {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): RecyclerViewHolder<FeedReply> = CommentViewHolder(
        AdapterCommentBinding.inflate(parent.context.getLayoutInflater(), parent, false),
    )

    override fun getFilter(): Filter? = null

    inner class CommentViewHolder(
        private val binding: AdapterCommentBinding,
    ) : RecyclerViewHolder<FeedReply>(binding.root) {
        init {
            bindClickListeners(
                R.id.widget_edit,
                R.id.widget_users,
                R.id.user_avatar,
                R.id.widget_mention,
            )
        }

        override fun onBindViewHolder(model: FeedReply) {
            binding.userAvatar.setImage(model.user?.avatar)
            binding.userName.text = model.user?.name
            binding.feedTime.text = DateUtil.getPrettyDateUnix(model.createdAt)
            if (!presenter.settings.experimentalMarkdown) {
                binding.widgetStatus.visibility = View.VISIBLE
                binding.widgetStatus.setModel(model)
            } else {
                binding.widgetStatus.visibility = View.GONE
            }
            binding.widgetStatusText.richMarkDown(model.reply)
            binding.widgetMention.visibility = View.GONE

            binding.widgetFavourite.setRequestParams(KeyUtil.ACTIVITY_REPLY, model.id)
            binding.widgetFavourite.setModel(model.likes)

            val isCurrentUser = model.user?.id?.let { presenter.isCurrentUser(it) } == true
            if (isCurrentUser) {
                binding.widgetDelete.setModel(model, KeyUtil.MUT_DELETE_FEED_REPLY)

                binding.widgetMention.visibility = View.GONE
                binding.widgetEdit.visibility = View.VISIBLE
                binding.widgetDelete.visibility = View.VISIBLE
            } else {
                binding.widgetMention.visibility = View.VISIBLE
                binding.widgetEdit.visibility = View.GONE
                binding.widgetDelete.visibility = View.GONE
            }
        }

        override fun onViewRecycled() {
            Glide.with(getContext()).clear(binding.userAvatar)
            binding.widgetStatus.onViewRecycled()
            binding.widgetDelete.onViewRecycled()
        }

        override fun onClick(v: View) {
            performClick(clickListener, data, v)
        }

        override fun onLongClick(view: View): Boolean = performLongClick(clickListener, data, view)
    }
}
