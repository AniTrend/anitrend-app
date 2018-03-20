package com.mxt.anitrend.view.fragment.detail;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.mxt.anitrend.BuildConfig;
import com.mxt.anitrend.R;
import com.mxt.anitrend.base.custom.fragment.FragmentBase;
import com.mxt.anitrend.presenter.base.BasePresenter;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import mehdi.sakout.aboutpage.AboutPage;
import mehdi.sakout.aboutpage.Element;

/**
 * Created by max on 2018/03/04.
 * Application about screen
 */

public class AboutFragment extends FragmentBase<Void, BasePresenter, Void> {

    public static AboutFragment newInstance() {
        return new AboutFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return new AboutPage(getActivity())
                .setImage(R.mipmap.ic_launcher)
                .addGroup("About AniTrend")
                .setDescription(getString(R.string.app_description))
                .addItem(new Element().setTitle(String.format("Version %s", BuildConfig.VERSION_NAME)))
                .addPlayStore("com.mxt.anitrend")
                .addTwitter("anitrend_app")
                .create();
    }

    @Override
    protected void updateUI() {

    }

    @Override
    public void makeRequest() {

    }

    @Override @Subscribe(threadMode = ThreadMode.MAIN_ORDERED)
    public void onChanged(@Nullable Void model) {

    }
}
