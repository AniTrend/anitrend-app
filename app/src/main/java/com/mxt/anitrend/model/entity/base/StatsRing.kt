package com.mxt.anitrend.model.entity.base

import android.graphics.RectF

/**
 * Created by max on 2017/12/01.
 * StatsRing data holder
 */
class StatsRing() {
    var progress: Int = 0
    var name: String? = null
    var value: String? = null
    var rectFRing: RectF = RectF()

    constructor(progress: Int, name: String?, value: String?) : this() {
        this.progress = progress
        this.name = name
        this.value = value
    }
}
