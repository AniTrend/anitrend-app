package com.mxt.anitrend.util.media

import android.os.Parcelable
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize

/**
 * Helper configuration class for global configurable browsing activity
 * @see com.mxt.anitrend.view.activity.detail.MediaBrowseActivity
 */
@Parcelize
class MediaBrowseUtil @JvmOverloads constructor(
    private var filterEnabled: Boolean = false
) : Parcelable {

    @IgnoredOnParcel
    private var compactType: Boolean = false
    @IgnoredOnParcel
    private var basicFilter: Boolean = false

    val isCompactType: Boolean
        get() = compactType

    val isFilterEnabled: Boolean
        get() = filterEnabled

    val isBasicFilter: Boolean
        get() = basicFilter

    fun setCompactType(compactType: Boolean) = apply {
        this.compactType = compactType
    }

    fun setFilterEnabled(filterEnabled: Boolean) = apply {
        this.filterEnabled = filterEnabled
    }

    fun setBasicFilter(basicFiltering: Boolean) = apply {
        this.basicFilter = basicFiltering
    }

}
