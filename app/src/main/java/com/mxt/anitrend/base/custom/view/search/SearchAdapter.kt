/*******************************************************************************
 * Copyright (c) 2025 Miguel Catalan Banuls
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/

package com.mxt.anitrend.base.custom.view.search

import android.content.Context
import android.graphics.drawable.Drawable
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.Filter
import android.widget.Filterable
import com.mxt.anitrend.databinding.SuggestItemBinding

/**
 * Suggestions Adapter.
 *
 * @author Miguel Catalan Banuls
 */
class SearchAdapter(
    context: Context,
    private val suggestions: Array<String>,
    private val suggestionIcon: Drawable? = null,
    private val ellipsize: Boolean = false,
    private val filterSuggestionsWhenSearchEmpty: Boolean = true,
) : BaseAdapter(), Filterable {

    private var data: ArrayList<String> = ArrayList()
    private val inflater: LayoutInflater = LayoutInflater.from(context)

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val filterResults = FilterResults()
                val searchData = ArrayList<String>()

                for (string in suggestions) {
                    if ((TextUtils.isEmpty(constraint) && !filterSuggestionsWhenSearchEmpty) ||
                        string.lowercase().startsWith(constraint.toString().lowercase())
                    ) {
                        searchData.add(string)
                    }
                }

                filterResults.values = searchData
                filterResults.count = searchData.size
                return filterResults
            }

            @Suppress("UNCHECKED_CAST")
            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                if (results?.values != null) {
                    data = results.values as ArrayList<String>
                    notifyDataSetChanged()
                }
            }
        }
    }

    override fun getCount(): Int = data.size

    override fun getItem(position: Int): Any = data[position]

    override fun getItemId(position: Int): Long = position.toLong()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val binding: SuggestItemBinding
        val holder: SuggestionsViewHolder

        if (convertView == null) {
            binding = SuggestItemBinding.inflate(inflater, parent, false)
            holder = SuggestionsViewHolder(binding)
            binding.root.tag = holder
        } else {
            holder = convertView.tag as SuggestionsViewHolder
            binding = holder.binding
        }

        val currentListData = getItem(position) as String
        holder.bind(currentListData)

        return binding.root
    }

    private inner class SuggestionsViewHolder(val binding: SuggestItemBinding) {

        init {
            if (suggestionIcon != null) {
                binding.suggestionIcon.setImageDrawable(suggestionIcon)
            } else {
                binding.suggestionIcon.visibility = View.INVISIBLE
            }
        }

        fun bind(text: String) {
            binding.suggestionText.text = text
            if (ellipsize) {
                binding.suggestionText.isSingleLine = true
                binding.suggestionText.ellipsize = TextUtils.TruncateAt.END
            }
        }
    }
}
