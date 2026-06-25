package com.mxt.anitrend.adapter.recycler.detail

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import com.mxt.anitrend.R
import com.mxt.anitrend.base.custom.recycler.RecyclerViewAdapter
import com.mxt.anitrend.base.custom.recycler.RecyclerViewHolder
import com.mxt.anitrend.databinding.AdapterGenreBinding
import com.mxt.anitrend.extension.getLayoutInflater
import com.mxt.anitrend.model.entity.anilist.Genre

/**
 * Created by max on 2018/01/01.
 */
class GenreAdapter(
    context: Context,
) : RecyclerViewAdapter<Genre>(context) {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): RecyclerViewHolder<Genre> = GenreViewHolder(
        AdapterGenreBinding.inflate(parent.context.getLayoutInflater(), parent, false),
    )

    override fun getFilter(): Filter? = null

    inner class GenreViewHolder(
        private val binding: AdapterGenreBinding,
    ) : RecyclerViewHolder<Genre>(binding.root) {
        init {
            bindClickListeners(R.id.container)
            bindLongClickListeners(R.id.container)
        }

        override fun onBindViewHolder(model: Genre) {
            binding.genreName.text = model.genre
        }

        override fun onViewRecycled() = Unit

        override fun onClick(v: View) {
            performClick(clickListener, data, v)
        }

        override fun onLongClick(view: View): Boolean = performLongClick(clickListener, data, view)
    }
}
