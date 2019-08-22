package com.mxt.anitrend.view.fragment.detail;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.mxt.anitrend.R;
import com.mxt.anitrend.base.custom.fragment.FragmentBase;
import com.mxt.anitrend.databinding.FragmentStaffOverviewBinding;
import com.mxt.anitrend.model.entity.base.StaffBase;
import com.mxt.anitrend.presenter.base.BasePresenter;
import com.mxt.anitrend.util.CompatUtil;
import com.mxt.anitrend.util.KeyUtil;
import com.mxt.anitrend.util.graphql.GraphUtil;

import butterknife.ButterKnife;
import butterknife.OnClick;
import io.github.wax911.library.model.request.QueryContainerBuilder;

/**
 * Created by max on 2018/01/30.
 * StaffOverviewFragment
 */

public class StaffOverviewFragment extends FragmentBase<StaffBase, BasePresenter, StaffBase> {

    private StaffBase model;

    private FragmentStaffOverviewBinding binding;

    private long id;

    public static StaffOverviewFragment newInstance(Bundle args) {
        StaffOverviewFragment fragment = new StaffOverviewFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(getArguments() != null)
            id = getArguments().getLong(KeyUtil.arg_id);
        setViewModel(true);
    }

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentStaffOverviewBinding.inflate(inflater, container, false);
        unbinder = ButterKnife.bind(this, binding.getRoot());
        binding.stateLayout.showLoading();
        return binding.getRoot();
    }

    @Override
    protected void updateUI() {
        if(model != null) {
            binding.setModel(model);
            binding.stateLayout.showContent();
        } else
            binding.stateLayout.showError(CompatUtil.INSTANCE.getDrawable(getContext(), R.drawable.ic_emoji_sweat),
                    getString(R.string.layout_empty_response), getString(R.string.try_again), (view) -> makeRequest());
    }

    @Override
    public void onStart() {
        super.onStart();
        if(model != null)
            updateUI();
        else
            makeRequest();
    }

    @Override
    public void makeRequest() {
        QueryContainerBuilder queryContainer = GraphUtil.INSTANCE.getDefaultQuery(false)
                .putVariable(KeyUtil.arg_id, id);
        getViewModel().getParams().putParcelable(KeyUtil.arg_graph_params, queryContainer);
        getViewModel().requestData(KeyUtil.STAFF_OVERVIEW_REQ, getContext());

    }

    /**
     * Called when a view has been clicked.
     *
     * @param view The view that was clicked.
     */
    @Override @OnClick(R.id.staff_img)
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.staff_img:
                CompatUtil.INSTANCE.imagePreview(getActivity(), view, model.getImage().getLarge(), R.string.image_preview_error_staff_image);
                break;
            default:
                super.onClick(view);
                break;
        }
    }

    @Override
    public void onChanged(@Nullable StaffBase model) {
        if(model != null)
            this.model = model;
        updateUI();
    }
}
