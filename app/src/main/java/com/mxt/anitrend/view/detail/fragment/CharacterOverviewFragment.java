package com.mxt.anitrend.view.detail.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.mxt.anitrend.R;
import com.mxt.anitrend.api.model.Character;
import com.mxt.anitrend.databinding.FragmentCharacterOverviewBinding;
import com.mxt.anitrend.util.TransitionHelper;
import com.mxt.anitrend.view.base.activity.ImagePreviewActivity;
import com.mxt.anitrend.viewmodel.fragment.DefaultFragment;

import butterknife.ButterKnife;

/**
 * Created by max on 2017/08/11.
 */

public class CharacterOverviewFragment extends DefaultFragment<Character> implements View.OnClickListener {

    private FragmentCharacterOverviewBinding binding;

    public static CharacterOverviewFragment newInstance(Character result) {
        Bundle args = new Bundle();
        args.putParcelable(ARG_KEY, result);
        CharacterOverviewFragment fragment = new CharacterOverviewFragment();
        fragment.setArguments(args);
        return fragment;
    }

    /**
     * Override this as normal the save instance for your model will be managed for you,
     * so there is no need to to restore the state of your model from save state
     *
     * @param inflater
     * @param container
     * @param savedInstanceState
     */
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        binding = FragmentCharacterOverviewBinding.inflate(inflater, container, false);
        unbinder = ButterKnife.bind(this, binding.getRoot());
        return binding.getRoot();
    }

    @Override
    public void onStart() {
        super.onStart();
        binding.setView(this);
    }

    /**
     * Is automatically called in the @onStart Method
     */
    @Override
    protected void updateUI() {
        binding.setModel(model);
        Glide.with(this).load(model.getImage_url_lge())
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .centerCrop().into(binding.characterDetailsImage);
    }

    @Override
    public void onClick(View v) {
        Intent intent;
        switch (v.getId()) {
            case R.id.character_details_image:
                intent = new Intent(getContext(), ImagePreviewActivity.class);
                intent.putExtra(ImagePreviewActivity.IMAGE_SOURCE, model.getImage_url_lge());
                TransitionHelper.startSharedImageTransition(getActivity(), v, getString(R.string.transition_image_preview), intent);
                break;
        }
    }
}
