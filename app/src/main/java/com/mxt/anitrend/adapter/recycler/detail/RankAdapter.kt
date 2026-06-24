package com.mxt.anitrend.adapter.recycler.detail

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import com.mxt.anitrend.R
import com.mxt.anitrend.base.custom.recycler.RecyclerViewAdapter
import com.mxt.anitrend.base.custom.recycler.RecyclerViewHolder
import com.mxt.anitrend.binding.htmlText
import com.mxt.anitrend.databinding.AdapterRankingBinding
import com.mxt.anitrend.extension.getCompatDrawable
import com.mxt.anitrend.extension.getLayoutInflater
import com.mxt.anitrend.model.entity.anilist.MediaRank
import com.mxt.anitrend.util.KeyUtil

/**
 * Created by max on 2018/01/01.
 */
class RankAdapter(
    context: Context,
) : RecyclerViewAdapter<MediaRank>(context) {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): RecyclerViewHolder<MediaRank> = RankViewHolder(
        AdapterRankingBinding.inflate(parent.context.getLayoutInflater(), parent, false),
    )

    override fun getFilter(): Filter? = null

    inner class RankViewHolder(
        private val binding: AdapterRankingBinding,
    ) : RecyclerViewHolder<MediaRank>(binding.root) {
        init {
            bindClickListeners(R.id.container, R.id.sub_container)
            bindLongClickListeners(R.id.container)
        }

        override fun onBindViewHolder(model: MediaRank) {
            binding.subContainer.htmlText(model.typeHtml)
            val icon =
                if (model.type == KeyUtil.RATED) {
                    R.drawable.ic_star_yellow_700_24dp
                } else {
                    R.drawable.ic_favorite_red_700_24dp
                }
            binding.rankingType.setImageDrawable(getContext().getCompatDrawable(icon))
        }

        override fun onViewRecycled() {
        }

        override fun onClick(v: View) {
            performClick(clickListener, data, v)
        }

        override fun onLongClick(v: View): Boolean = performLongClick(clickListener, data, v)
    }
}
