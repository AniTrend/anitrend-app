package com.mxt.anitrend.event;

/**
 * Created by max on 2017/03/09.
 */

public interface ReviewClickListener {
    void onReadMore(int index);
    void onCardClicked(int index);
    void onUpvote(int index);
    void onDownvote(int index);
}
