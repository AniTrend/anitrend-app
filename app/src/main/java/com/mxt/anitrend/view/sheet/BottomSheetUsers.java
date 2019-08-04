package com.mxt.anitrend.view.sheet;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;
import android.text.TextUtils;
import android.view.View;

import com.annimon.stream.IntPair;
import com.miguelcatalan.materialsearchview.MaterialSearchView;
import com.mxt.anitrend.R;
import com.mxt.anitrend.adapter.recycler.index.UserAdapter;
import com.mxt.anitrend.base.custom.sheet.BottomSheetBase;
import com.mxt.anitrend.base.custom.sheet.BottomSheetList;
import com.mxt.anitrend.databinding.BottomSheetListBinding;
import com.mxt.anitrend.model.entity.base.UserBase;
import com.mxt.anitrend.presenter.base.BasePresenter;
import com.mxt.anitrend.util.CompatUtil;
import com.mxt.anitrend.util.KeyUtil;
import com.mxt.anitrend.view.activity.detail.ProfileActivity;

import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;

public class BottomSheetUsers extends BottomSheetList<UserBase> implements MaterialSearchView.OnQueryTextListener, MaterialSearchView.SearchViewListener {

    private BottomSheetListBinding binding;

    public static BottomSheetUsers newInstance(Bundle bundle) {
        BottomSheetUsers fragment = new BottomSheetUsers();
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        presenter = new BasePresenter(getContext());
        mColumnSize = getResources().getInteger(R.integer.single_list_x1);
        mAdapter = new UserAdapter(getActivity());
        if(getArguments() != null) {
            List<UserBase> baseList = getArguments().getParcelableArrayList(KeyUtil.arg_list_model);
            if(!CompatUtil.INSTANCE.isEmpty(baseList))
                mAdapter.onItemsInserted(baseList);
        }
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
        binding = BottomSheetListBinding.inflate(CompatUtil.INSTANCE.getLayoutInflater(getActivity()));
        dialog.setContentView(binding.getRoot());
        unbinder = ButterKnife.bind(this, dialog);
        createBottomSheetBehavior(binding.getRoot());
        mLayoutManager = new StaggeredGridLayoutManager(mColumnSize, StaggeredGridLayoutManager.VERTICAL);
        return dialog;
    }

    /**
     * Is automatically called in the @onStart Method if overridden in list implementation
     */
    @Override
    protected void updateUI() {
        toolbarTitle.setText(getString(mTitle, mAdapter.getItemCount()));
        toolbarSearch.setVisibility(View.VISIBLE);
        searchView.setOnQueryTextListener(this);
        searchView.setOnSearchViewListener(this);
        injectAdapter();
    }

    /**
     * All new or updated network requests should be handled in this method
     */
    @Override
    public void makeRequest() {

    }

    /**
     * Called when the user submits the query. This could be due to a key press on the
     * keyboard or due to pressing a submit button.
     * The listener can override the standard behavior by returning true
     * to indicate that it has handled the submit request. Otherwise return false to
     * let the SearchView handle the submission by launching any associated intent.
     *
     * @param query the query text that is to be submitted
     * @return true if the query has been handled by the listener, false to let the
     * SearchView perform the default action.
     */
    @Override
    public boolean onQueryTextSubmit(String query) {
        return false;
    }

    /**
     * Called when the query text is changed by the user.
     *
     * @param newText the new content of the query text field.
     * @return false if the SearchView should perform the default action of showing any
     * suggestions if available, true if the action was handled by the listener.
     */
    @Override
    public boolean onQueryTextChange(String newText) {
        if(!TextUtils.isEmpty(newText) && mAdapter != null && mAdapter.getFilter() != null) {
            mAdapter.getFilter().filter(newText);
            return true;
        }
        return false;
    }

    @Override
    public void onSearchViewShown() {
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
    }

    @Override
    public void onSearchViewClosed() {
        if(mAdapter != null && mAdapter.getFilter() != null)
            mAdapter.getFilter().filter("");
    }

    /**
     * When the target view from {@link View.OnClickListener}
     * is clicked from a view holder this method will be called
     *
     * @param target view that has been clicked
     * @param data   the model that at the clicked index
     */
    @Override
    public void onItemClick(View target, IntPair<UserBase> data) {
        switch (target.getId()) {
            case R.id.container:
                Intent intent = new Intent(getActivity(), ProfileActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.putExtra(KeyUtil.arg_id, data.getSecond().getId());
                if(getActivity() != null)
                    getActivity().startActivity(intent);
                break;
        }
    }

    /**
     * When the target view from {@link View.OnLongClickListener}
     * is clicked from a view holder this method will be called
     *
     * @param target view that has been long clicked
     * @param data   the model that at the long clicked index
     */
    @Override
    public void onItemLongClick(View target, IntPair<UserBase> data) {

    }

    /**
     * Builder class for bottom sheet
     */
    public static class Builder extends BottomSheetBuilder {

        @Override
        public BottomSheetBase build() {
            return newInstance(bundle);
        }

        public BottomSheetBuilder setModel(List<UserBase> model) {
            bundle.putParcelableArrayList(KeyUtil.arg_list_model, (ArrayList<? extends Parcelable>) model);
            return this;
        }
    }
}
