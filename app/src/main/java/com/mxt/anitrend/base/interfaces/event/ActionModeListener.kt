package com.mxt.anitrend.base.interfaces.event

import android.view.ActionMode

/**
 * Created by max on 2017/07/17.
 * Action mode trigger callback
 */
interface ActionModeListener : ActionMode.Callback {
    fun onSelectionChanged(actionMode: ActionMode, count: Int)
}
