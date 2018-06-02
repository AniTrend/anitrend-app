package com.mxt.anitrend.view.fragment.detail;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.mxt.anitrend.BuildConfig;
import com.mxt.anitrend.R;
import com.mxt.anitrend.base.custom.fragment.FragmentBase;
import com.mxt.anitrend.presenter.base.BasePresenter;
import com.mxt.anitrend.util.NotifyUtil;

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
        AboutPage aboutPage = new AboutPage(getActivity())
                .setImage(R.mipmap.ic_launcher)
                .addGroup("General Information")
                .setDescription(getString(R.string.app_description))
                .addItem(new Element().setTitle(String.format("Version %s", BuildConfig.VERSION_NAME)))
                .addPlayStore("com.mxt.anitrend")
                .addTwitter("anitrend_app")
                .addGroup("Additional Information")
                .addGitHub("AniTrend")
                .addWebsite("https://anitrend.co")
                .addGroup("Legal Information")
                .addItem(new Element().setTitle("Terms & Conditions").setIconDrawable(R.drawable.ic_privacy_grey_600_24dp)
                        .setIntent(new Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/AniTrend/anitrend-app/blob/develop/TERMS_OF_SERVICE.md"))))
                .addItem(new Element().setTitle("Code Of Conduct").setIconDrawable(R.drawable.ic_privacy_grey_600_24dp)
                        .setIntent(new Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/AniTrend/anitrend-app/blob/develop/CODE_OF_CONDUCT.md"))));
        return aboutPage.create();
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
