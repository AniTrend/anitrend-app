package com.mxt.anitrend.model.entity.group

import com.mxt.anitrend.util.CompatUtil
import com.mxt.anitrend.util.KeyUtil

/**
 * Created by max on 2018/02/18.
 */
class RecyclerHeaderItem
@JvmOverloads
constructor(
    private var title: String,
    var size: Int = 0,
    private var capitalize: Boolean = true,
) : RecyclerItem() {
    init {
        contentType = KeyUtil.RECYCLER_TYPE_HEADER
    }

    fun getTitle(): String = if (capitalize) CompatUtil.capitalizeWords(title) else title

    fun setTitle(value: String) {
        title = value
    }

    override fun equals(other: Any?): Boolean = if (other is RecyclerHeaderItem) {
        CompatUtil.equals(other.title, title)
    } else {
        super.equals(other)
    }
}
