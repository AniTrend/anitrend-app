package com.mxt.anitrend.util.graphql

import com.mxt.anitrend.util.CompatUtil
import com.mxt.anitrend.util.Settings

/**
 * Created by max on 2018/03/22.
 * Graph request helper class
 */
object GraphUtil {
    /**
     * Used to check if the newly applied preference key is a should trigger an application refresh
     */
    fun isKeyFilter(preferenceKey: String): Boolean = !CompatUtil.equals(preferenceKey, Settings._appTheme) &&
        !CompatUtil.equals(preferenceKey, Settings._updateChannel)
}
