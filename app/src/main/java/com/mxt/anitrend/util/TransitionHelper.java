package com.mxt.anitrend.util;

import android.app.Activity;
import android.content.Intent;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.util.Pair;
import android.support.v4.view.ViewCompat;
import android.view.View;

/**
 * Created by Maxwell on 1/20/2017.
 */

public class TransitionHelper {

    /**
     * Starts a shared transition of activities connected by views
     * <br/>
     *
     * @param base The calling activity
     * @param target The view from the calling activity with transition name
     * @param data Intent with bundle and or activity to start
     */
    public static void startSharedTransitionActivity(Activity base, View target, Intent data) {
        Pair participants = new Pair<>(target, ViewCompat.getTransitionName(target));
        ActivityOptionsCompat transitionActivityOptions =
                ActivityOptionsCompat.makeSceneTransitionAnimation(
                        base, participants);
        ActivityCompat.startActivity(base, data, transitionActivityOptions.toBundle());
    }

    /**
     * Starts a shared transition of activities connected by views
     * by making use of the provided transition name
     * <br/>
     *
     * @param base The calling activity
     * @param target The view from the calling activity with transition name
     * @param transitionName The name of the target transition
     * @param data Intent with bundle and or activity to start
     */
    public static void startSharedImageTransition(Activity base, View target, String transitionName, Intent data) {
        ActivityOptionsCompat transition = ActivityOptionsCompat.makeSceneTransitionAnimation(
                base, target, transitionName);
        base.startActivity(data, transition.toBundle());
    }

}
