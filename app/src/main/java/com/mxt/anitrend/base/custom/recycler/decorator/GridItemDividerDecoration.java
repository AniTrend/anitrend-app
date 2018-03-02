package com.mxt.anitrend.base.custom.recycler.decorator;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.support.annotation.ColorInt;
import android.support.annotation.Dimension;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.mxt.anitrend.base.custom.recycler.RecyclerViewHolder;

/**
 * Created by max on 2017/12/10.
 *  A {@link RecyclerView.ItemDecoration} which draws dividers (along the right & bottom)
 * for {@link RecyclerView.ViewHolder}s which implement {@link RecyclerViewHolder}.
 * credits @plaid
 */

public class GridItemDividerDecoration extends RecyclerView.ItemDecoration {

    private final int dividerSize;
    private final Paint paint;

    public GridItemDividerDecoration(@Dimension int dividerSize, @ColorInt int dividerColor) {
        this.dividerSize = dividerSize;
        paint = new Paint();
        paint.setColor(dividerColor);
        paint.setStyle(Paint.Style.FILL);
    }

    @Override
    public void onDrawOver(Canvas canvas, RecyclerView parent, RecyclerView.State state) {
        if (parent.isAnimating()) return;

        final int childCount = parent.getChildCount();
        final RecyclerView.LayoutManager lm = parent.getLayoutManager();
        for (int i = 0; i < childCount; i++) {
            final View child = parent.getChildAt(i);
            RecyclerView.ViewHolder viewHolder = parent.getChildViewHolder(child);

            if (viewHolder instanceof RecyclerViewHolder) {
                final int right = lm.getDecoratedRight(child);
                final int bottom = lm.getDecoratedBottom(child);
                // draw the bottom divider
                canvas.drawRect(lm.getDecoratedLeft(child), bottom - dividerSize,
                        right, bottom, paint);
                // draw the right edge divider
                canvas.drawRect(right - dividerSize, lm.getDecoratedTop(child),
                        right, bottom - dividerSize, paint);
            }
        }
    }

}
