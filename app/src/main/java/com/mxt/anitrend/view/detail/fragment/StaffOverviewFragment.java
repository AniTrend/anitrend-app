package com.mxt.anitrend.view.detail.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.mxt.anitrend.R;
import com.mxt.anitrend.api.model.Staff;
import com.mxt.anitrend.utils.DateTimeConverter;
import com.mxt.anitrend.utils.TransitionHelper;
import com.mxt.anitrend.view.base.activity.ImagePreviewActivity;
import com.mxt.anitrend.viewmodel.fragment.DefaultFragment;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by max on 2017/04/09.
 */
public class StaffOverviewFragment extends DefaultFragment<Staff> implements View.OnClickListener {

    @BindView(R.id.staff_detail_image) ImageView mStaff_Image;
    @BindView(R.id.staff_website) TextView mStaff_Website;

    @BindView(R.id.staff_first_name) TextView mFirst_Name;
    @BindView(R.id.staff_last_name) TextView mLast_Name;

    @BindView(R.id.staff_dob) TextView mDateOfBirth;
    @BindView(R.id.staff_info) TextView mInfo;

    @BindView(R.id.staff_jap_name) TextView mJapanese;
    @BindView(R.id.staff_language) TextView mStaff_Language;

    public static StaffOverviewFragment newInstance(Staff result) {
        Bundle args = new Bundle();
        args.putParcelable(ARG_KEY, result);
        StaffOverviewFragment fragment = new StaffOverviewFragment();
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
        View root = inflater.inflate(R.layout.fragment_staff_overview, container, false);
        unbinder = ButterKnife.bind(this, root);
        super.onCreateView(inflater, container, savedInstanceState);
        return root;
    }

    @Override
    public void onStart() {
        super.onStart();
        mStaff_Image.setOnClickListener(this);
    }

    /**
     * Is automatically called in the @onStart Method
     */
    @Override
    protected void updateUI() {

        Glide.with(this).load(model.getImage_url_lge())
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .centerCrop().into(mStaff_Image);

        mStaff_Website.setMovementMethod(LinkMovementMethod.getInstance());
        mInfo.setMovementMethod(LinkMovementMethod.getInstance());

        mStaff_Website.setText(model.getWebsite());

        mInfo.setText(model.getInfo());

        mFirst_Name.setText(model.getName_first());
        mLast_Name.setText(model.getName_last());

        mJapanese.setText(model.getFullJapaneseName());

        mStaff_Language.setText(model.getLanguage());

        mDateOfBirth.setText(DateTimeConverter.convertLongDate(model.getDob()));

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.staff_detail_image:
                Intent intent = new Intent(getContext(), ImagePreviewActivity.class);
                intent.putExtra(ImagePreviewActivity.IMAGE_SOURCE, model.getImage_url_lge());
                TransitionHelper.startSharedImageTransition(getActivity(), v, getString(R.string.transition_image_preview), intent);
                break;
        }
    }
}
