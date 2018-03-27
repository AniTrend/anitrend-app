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
import com.mxt.anitrend.databinding.FragmentCharacterOverviewBinding;
import com.mxt.anitrend.model.entity.anilist.Character;
import com.mxt.anitrend.model.entity.container.request.QueryContainerBuilder;
import com.mxt.anitrend.presenter.base.BasePresenter;
import com.mxt.anitrend.util.CompatUtil;
import com.mxt.anitrend.util.GraphUtil;
import com.mxt.anitrend.util.KeyUtil;
import com.mxt.anitrend.view.activity.base.ImagePreviewActivity;

import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by max on 2018/01/30.
 * CharacterOverviewFragment
 */

public class CharacterOverviewFragment extends FragmentBase<Character, BasePresenter, Character> {

    private Character model;

    private FragmentCharacterOverviewBinding binding;

    private long id;

    public static CharacterOverviewFragment newInstance(Bundle args) {
        CharacterOverviewFragment fragment = new CharacterOverviewFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(getArguments() != null)
            id = getArguments().getLong(KeyUtil.arg_id);
    }

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentCharacterOverviewBinding.inflate(inflater, container, false);
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
            binding.stateLayout.showError(CompatUtil.getDrawable(getContext(), R.drawable.ic_warning_white_18dp, R.color.colorStateBlue),
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
        QueryContainerBuilder queryContainer = GraphUtil.getDefaultQuery(false)
                .putVariable(KeyUtil.arg_id, id);
        getViewModel().getParams().putParcelable(KeyUtil.arg_graph_params, queryContainer);
        getViewModel().requestData(KeyUtil.CHARACTER_OVERVIEW_REQ, getContext());
    }

    /**
     * Called when a view has been clicked.
     *
     * @param v The view that was clicked.
     */
    @Override @OnClick(R.id.character_img)
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.character_img:
                Intent intent = new Intent(getActivity(), ImagePreviewActivity.class);
                intent.putExtra(KeyUtil.arg_model, model.getImage().getLarge());
                CompatUtil.startSharedImageTransition(getActivity(), v, intent, R.string.transition_image_preview);
                break;
            default:
                super.onClick(v);
                break;
        }
    }

    @Override
    public void onChanged(@Nullable Character model) {
        if(model != null)
            this.model = model;
        updateUI();
    }
}
