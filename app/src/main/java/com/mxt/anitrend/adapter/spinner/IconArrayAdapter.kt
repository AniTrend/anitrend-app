package com.mxt.anitrend.adapter.spinner

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import com.mxt.anitrend.R
import com.mxt.anitrend.base.custom.view.image.AppCompatTintImageView
import com.mxt.anitrend.base.custom.view.text.SingleLineTextView

class IconArrayAdapter(
    context: Context,
    resource: Int,
    textViewResourceId: Int,
    objects: List<String>
) : ArrayAdapter<String>(context, resource, textViewResourceId, objects) {

    private lateinit var indexIconMap: Map<Int, Int>

    /**
     * Set a map containing the index relative to the title containing a drawable int res
     * @param indexIconMap map of signature (position, R.drawable)
     */
    fun setIndexIconMap(indexIconMap: Map<Int, Int>) {
        this.indexIconMap = indexIconMap
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = super.getView(position, convertView, parent)

        val title = view.findViewById<SingleLineTextView>(R.id.spinner_text)
        val icon = view.findViewById<AppCompatTintImageView>(R.id.spinner_icon)
        val drawable = indexIconMap[position]
            ?: throw IllegalStateException("Missing icon for position $position")

        title.text = getItem(position)
        icon.setTintDrawableAttr(drawable, R.attr.titleColor)

        return view
    }

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = super.getDropDownView(position, convertView, parent)

        val title = view.findViewById<SingleLineTextView>(R.id.spinner_text)
        val icon = view.findViewById<AppCompatTintImageView>(R.id.spinner_icon)
        val drawable = indexIconMap[position]
            ?: throw IllegalStateException("Missing icon for position $position")

        title.text = getItem(position)
        icon.setTintDrawableAttr(drawable, R.attr.titleColor)

        return view
    }
}
