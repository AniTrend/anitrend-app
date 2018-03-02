package com.mxt.anitrend.model.entity.base;

import android.graphics.RectF;

/**
 * Created by max on 2017/12/01.
 * StatsRing data holder
 */

public class StatsRing {

    private int progress;
    private String name;
    private String value;
    private RectF rectFRing;

    public StatsRing() {

    }

    public StatsRing(int progress, String name, String value) {
        this.progress = progress;
        this.name = name;
        this.value = value;
    }

    public int getProgress() {
        return progress;
    }

    public void setProgress(int progress) {
        this.progress = progress;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public RectF getRectFRing() {
        return rectFRing;
    }

    public void setRectFRing(RectF rectFRing) {
        this.rectFRing = rectFRing;
    }
}
