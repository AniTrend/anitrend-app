package com.mxt.anitrend.util;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.mxt.anitrend.base.custom.recycler.SpeedRecyclerView;

/**
 * Created by jameson on 8/30/16.
 */
public class LinearScaleHelper extends RecyclerView.OnScrollListener {

    private SpeedRecyclerView speedRecyclerView;
    private Context mContext;

    private float mScale = 0.9f; // 两边视图scale
    private int mPagePadding = 15; // 卡片的padding, 卡片间的距离等于2倍的mPagePadding
    private int mShowLeftCardWidth = 15;   // 左边卡片显示大小

    private int mCardWidth; // 卡片宽度
    private int mOnePageWidth; // 滑动一页的距离
    private int mCardGalleryWidth;

    private int mCurrentItemPos;
    private int mCurrentItemOffset;

    private PageChangeListener pageChangeListener;

    public void attachToRecyclerView(SpeedRecyclerView mRecyclerView) {
        mRecyclerView.addOnScrollListener(this);
        speedRecyclerView = mRecyclerView;
        mContext = mRecyclerView.getContext();
        initWidth();
    }

    public void setPageChangeListener(PageChangeListener pageChangeListener) {
        this.pageChangeListener = pageChangeListener;
    }

    @Override
    public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
        super.onScrollStateChanged(recyclerView, newState);
        LinearSnapHelper snapHelper = ((SpeedRecyclerView)recyclerView).getSnapHelper();
        if (newState == RecyclerView.SCROLL_STATE_IDLE)
            snapHelper.mNoNeedToScroll = mCurrentItemOffset == 0 || mCurrentItemOffset == getDestItemOffset(recyclerView.getAdapter().getItemCount() - 1);
        else
            snapHelper.mNoNeedToScroll = false;
    }

    @Override
    public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
        super.onScrolled(recyclerView, dx, dy);
        // dx>0则表示右滑, dx<0表示左滑, dy<0表示上滑, dy>0表示下滑
        mCurrentItemOffset += dx;
        computeCurrentItemPos();
        onScrolledChangedCallback();
    }

    /**
     * 初始化卡片宽度
     */
    private void initWidth() {
        speedRecyclerView.post(() -> {
            mCardGalleryWidth = speedRecyclerView.getWidth();
            mCardWidth = mCardGalleryWidth - CompatUtil.dipToPx(2 * (mPagePadding + mShowLeftCardWidth));
            mOnePageWidth = mCardWidth;
            speedRecyclerView.smoothScrollToPosition(mCurrentItemPos);
            onScrolledChangedCallback();
        });
    }

    public void setCurrentItemPos(int currentItemPos) {
        this.mCurrentItemPos = currentItemPos;
    }

    public int getCurrentItemPos() {
        return mCurrentItemPos;
    }

    private int getDestItemOffset(int destPos) {
        return mOnePageWidth * destPos;
    }

    /**
     * 计算mCurrentItemOffset
     */
    private void computeCurrentItemPos() {
        if (mOnePageWidth <= 0) return;
        boolean pageChanged = false;
        // 滑动超过一页说明已翻页
        if (Math.abs(mCurrentItemOffset - mCurrentItemPos * mOnePageWidth) >= mOnePageWidth) {
            pageChanged = true;
        }
        if (pageChanged) {
            int tempPos = mCurrentItemPos;
            mCurrentItemPos = mCurrentItemOffset / mOnePageWidth;
            if(pageChangeListener != null)
                pageChangeListener.onPageChanged(mCurrentItemPos + 1);
        }
    }

    /**
     * RecyclerView位移事件监听, view大小随位移事件变化
     */
    private void onScrolledChangedCallback() {
        int offset = mCurrentItemOffset - mCurrentItemPos * mOnePageWidth;
        float percent = (float) Math.max(Math.abs(offset) * 1.0 / mOnePageWidth, 0.0001);

        View leftView = null;
        View currentView;
        View rightView = null;
        if (mCurrentItemPos > 0) {
            leftView = speedRecyclerView.getLayoutManager().findViewByPosition(mCurrentItemPos - 1);
        }
        currentView = speedRecyclerView.getLayoutManager().findViewByPosition(mCurrentItemPos);
        if (mCurrentItemPos < speedRecyclerView.getAdapter().getItemCount() - 1) {
            rightView = speedRecyclerView.getLayoutManager().findViewByPosition(mCurrentItemPos + 1);
        }

        if (leftView != null) {
            // y = (1 - mScale)x + mScale
            leftView.setScaleY((1 - mScale) * percent + mScale);
        }
        if (currentView != null) {
            // y = (mScale - 1)x + 1
            currentView.setScaleY((mScale - 1) * percent + 1);
        }
        if (rightView != null) {
            // y = (1 - mScale)x + mScale
            rightView.setScaleY((1 - mScale) * percent + mScale);
        }
    }

    public void setScale(float scale) {
        mScale = scale;
    }

    public void setPagePadding(int pagePadding) {
        mPagePadding = pagePadding;
    }

    public void setShowLeftCardWidth(int showLeftCardWidth) {
        mShowLeftCardWidth = showLeftCardWidth;
    }

    public void onViewRecycled() {
        if(pageChangeListener != null)
            pageChangeListener = null;
    }

    public interface PageChangeListener {
        void onPageChanged(int page);
    }
}
