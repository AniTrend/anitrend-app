package com.mxt.anitrend.view.fragment.detail;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.mxt.anitrend.R;
import com.mxt.anitrend.base.custom.fragment.FragmentBase;
import com.mxt.anitrend.base.interfaces.event.PublisherListener;
import com.mxt.anitrend.databinding.FragmentStaffOverviewBinding;
import com.mxt.anitrend.presenter.base.BasePresenter;
import com.mxt.anitrend.util.CompatUtil;
import com.mxt.anitrend.util.KeyUtils;
import com.mxt.anitrend.view.activity.base.ImagePreviewActivity;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by max on 2018/01/30.
 */

public class StaffOverviewFragment extends FragmentBase<Staff, BasePresenter, Staff> implements PublisherListener<Staff> {

    private Staff model;

    private FragmentStaffOverviewBinding binding;

    public static StaffOverviewFragment newInstance(Bundle args) {
        StaffOverviewFragment fragment = new StaffOverviewFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
        binding.setModel(model);
        binding.stateLayout.showContent();
    }

    @Override
    public void makeRequest() {

    }

    @Override
    public void onChanged(@Nullable Staff model) {

    }

    @Override
    @Subscribe(threadMode = ThreadMode.MAIN_ORDERED)
    public void onEventPublished(Staff param) {
        this.model = param;
        updateUI();
    }

    /**
     * Called when a view has been clicked.
     *
     * @param v The view that was clicked.
     */
    @Override @OnClick(R.id.staff_img)
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.staff_img:
                Intent intent = new Intent(getActivity(), ImagePreviewActivity.class);
                intent.putExtra(KeyUtils.arg_model, model.getImage_url_lge());
                CompatUtil.startSharedImageTransition(getActivity(), v, intent, R.string.transition_image_preview);
                break;
            default:
                super.onClick(v);
                break;
        }
    }
}
