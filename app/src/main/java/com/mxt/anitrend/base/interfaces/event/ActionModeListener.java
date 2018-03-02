package com.mxt.anitrend.base.interfaces.event;

import android.view.ActionMode;
import android.view.ActionMode.Callback;

/**
 * Created by max on 2017/07/17.
 * Action mode trigger callback
 */

public interface ActionModeListener extends Callback {
    void onSelectionChanged(ActionMode actionMode, int count);
}
