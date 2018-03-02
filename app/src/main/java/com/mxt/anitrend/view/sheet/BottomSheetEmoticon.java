package com.mxt.anitrend.view.sheet;

import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.View;

import com.mxt.anitrend.R;
import com.mxt.anitrend.adapter.recycler.detail.EmoticonAdapter;
import com.mxt.anitrend.base.custom.sheet.BottomSheetBase;
import com.mxt.anitrend.base.custom.sheet.BottomSheetList;
import com.mxt.anitrend.databinding.BottomSheetListBinding;
import com.mxt.anitrend.presenter.base.BasePresenter;
import com.mxt.anitrend.util.CompatUtil;

import java.util.ArrayList;

import butterknife.ButterKnife;
import io.wax911.emojify.Emoji;
import io.wax911.emojify.EmojiUtils;

/**
 * Created by max on 2017/12/07.
 */

public class BottomSheetEmoticon extends BottomSheetList<Emoji> {

    private BottomSheetListBinding binding;

    public static BottomSheetEmoticon newInstance(Bundle bundle) {
        BottomSheetEmoticon fragment = new BottomSheetEmoticon();
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        presenter = new BasePresenter(getContext());
        mColumnSize = getResources().getInteger(R.integer.grid_emoji_x3);
    }

    /**
     * Setup your view un-binder here as well as inflating other views as needed
     * into your view stub
     *
     * @param savedInstanceState
     */
    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        binding = BottomSheetListBinding.inflate(CompatUtil.getLayoutInflater(getActivity()));
        dialog.setContentView(binding.getRoot());
        unbinder = ButterKnife.bind(this, dialog);
        createBottomSheetBehavior(binding.getRoot());
        mLayoutManager = new StaggeredGridLayoutManager(mColumnSize, StaggeredGridLayoutManager.HORIZONTAL);
        return dialog;
    }

    /**
     * Is automatically called in the @onStart Method if overridden in list implementation
     */
    @Override
    protected void updateUI() {
        if(mAdapter == null)
            mAdapter = new EmoticonAdapter(model, getActivity());
        injectAdapter();
    }

    /**
     * All new or updated network requests should be handled in this method
     */
    @Override
    public void makeRequest() {
        model = new ArrayList<>(EmojiUtils.getAllEmojis());
        updateUI();
    }

    /**
     * When the target view from {@link View.OnClickListener}
     * is clicked from a view holder this method will be called
     *
     * @param target view that has been clicked
     * @param data   the model that at the click index
     */
    @Override
    public void onItemClick(View target, Emoji data) {
        presenter.notifyAllListeners(data, false);
        closeDialog();
    }

    /**
     * When the target view from {@link View.OnLongClickListener}
     * is clicked from a view holder this method will be called
     *
     * @param target view that has been long clicked
     * @param data   the model that at the long click index
     */
    @Override
    public void onItemLongClick(View target, Emoji data) {

    }

    /**
     * Builder class for bottom sheet
     */
    public static class Builder extends BottomSheetBuilder {
        @Override
        public BottomSheetBase build() {
            return newInstance(bundle);
        }
    }
}