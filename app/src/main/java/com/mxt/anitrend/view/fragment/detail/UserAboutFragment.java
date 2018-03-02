package com.mxt.anitrend.view.fragment.detail;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.annimon.stream.Stream;
import com.mxt.anitrend.R;
import com.mxt.anitrend.base.custom.fragment.FragmentBase;
import com.mxt.anitrend.base.interfaces.event.PublisherListener;
import com.mxt.anitrend.databinding.FragmentUserAboutBinding;
import com.mxt.anitrend.model.entity.anilist.User;
import com.mxt.anitrend.model.entity.base.StatsRing;
import com.mxt.anitrend.presenter.activity.ProfilePresenter;
import com.mxt.anitrend.util.ComparatorProvider;
import com.mxt.anitrend.util.CompatUtil;
import com.mxt.anitrend.util.KeyUtils;
import com.mxt.anitrend.util.NotifyUtil;
import com.mxt.anitrend.view.activity.base.ImagePreviewActivity;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by max on 2017/11/27.
 * about user fragment for the profile
 */

public class UserAboutFragment extends FragmentBase<User, ProfilePresenter, User> implements PublisherListener<User> {

    private FragmentUserAboutBinding binding;
    private User model;

    public static UserAboutFragment newInstance() {
        return new UserAboutFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        isMenuDisabled = true;
        setPresenter(new ProfilePresenter(getContext()));
        setViewModel(true);
    }

    /**
     * Called to have the fragment instantiate its user interface view.
     * This is optional, and non-graphical fragments can return null (which
     * is the default implementation).  This will be called between
     * {@link #onCreate(Bundle)} and {@link #onActivityCreated(Bundle)}.
     * <p>
     * <p>If you return a View from here, you will later be called in
     * {@link #onDestroyView} when the view is being released.
     *
     * @param inflater           The LayoutInflater object that can be used to inflate
     *                           any views in the fragment,
     * @param container          If non-null, this is the parent view that the fragment's
     *                           UI should be attached to.  The fragment should not add the view itself,
     *                           but this can be used to generate the LayoutParams of the view.
     * @param savedInstanceState If non-null, this fragment is being re-constructed
     *                           from a previous saved state as given here.
     * @return Return the View for the fragment's UI, or null.
     */
    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentUserAboutBinding.inflate(inflater, container, false);
        unbinder = ButterKnife.bind(this, binding.getRoot());
        binding.stateLayout.showLoading();
        return binding.getRoot();
    }

    /**
     * Is automatically called in the @onStart Method if overridden in list implementation
     */
    @Override
    protected void updateUI() {
        binding.setModel(model);
        binding.stateLayout.showContent();
        binding.widgetStatus.setTextData(model.getAbout());

        binding.userFollowStateWidget.setUserModel(model);
        binding.userAboutPanelWidget.setFragmentActivity(getActivity());
        binding.userAboutPanelWidget.setUserId(model.getId(), getLifecycle());
        showRingStats();
    }

    /**
     * All new or updated network requests should be handled in this method
     */
    @Override
    public void makeRequest() {

    }

    @Override @Subscribe(threadMode = ThreadMode.MAIN_ORDERED)
    public void onEventPublished(User param) {
        if(model == null)
            model = param;
        updateUI();
    }

    /**
     * Called when the model state is changed.
     *
     * @param model The new data
     */
    @Override
    public void onChanged(@Nullable User model) {

    }

    /**
     * Called when the view previously created by {@link #onCreateView} has
     * been detached from the fragment.  The next time the fragment needs
     * to be displayed, a new view will be created.  This is called
     * after {@link #onStop()} and before {@link #onDestroy()}.  It is called
     * <em>regardless</em> of whether {@link #onCreateView} returned a
     * non-null view.  Internally it is called after the view's state has
     * been saved but before it has been removed from its parent.
     */
    @Override
    public void onDestroyView() {
        if(binding != null)
            binding.userAboutPanelWidget.onViewRecycled();
        super.onDestroyView();
    }

    private List<StatsRing> generateStatsData() {
        if(model.getStats() != null) {
            HashMap<String, Integer> genreList = model.getStats().getFavourite_genres();
            if(genreList != null && genreList.size() > 0) {
                Integer maximum = Stream.of(genreList)
                        .max((o1, o2) -> o1.getValue() > o2.getValue() ? 1:-1)
                        .get().getValue();

                List<Map.Entry<String, Integer>> mapEntry = Stream.of(genreList)
                        .sorted(ComparatorProvider.getGenreValueComparator())
                        .limit(5).toList();

                List<StatsRing> ringList = new ArrayList<>(mapEntry.size());
                for (Map.Entry<String, Integer> entry: mapEntry) {
                    float percentage = (((float)entry.getValue()) / ((float)maximum)) * 100f;
                    ringList.add(new StatsRing((int)percentage, entry.getKey(), String.valueOf(entry.getValue())));
                }
                return ringList;
            }
        }
        return new ArrayList<>();
    }

    private void showRingStats() {
        List<StatsRing> ringList = generateStatsData();
        if(ringList.size() > 1) {
            binding.userStats.setDrawBg(CompatUtil.isLightTheme(getContext()), CompatUtil.getColorFromAttr(getContext(), R.attr.subtitleColor));
            binding.userStats.setData(ringList, 500);
        }
    }

    /**
     * Called when a view has been clicked.
     *
     * @param v The view that was clicked.
     */
    @Override @OnClick({R.id.user_avatar, R.id.user_stats_container})
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.user_avatar:
                Intent intent = new Intent(getActivity(), ImagePreviewActivity.class);
                intent.putExtra(KeyUtils.arg_model, model.getImage_url_lge());
                CompatUtil.startSharedImageTransition(getActivity(), v, intent, R.string.transition_image_preview);
                break;
            case R.id.user_stats_container:
                List<StatsRing> ringList = generateStatsData();
                if(ringList.size() > 1) {
                    binding.userStats.setDrawBg(CompatUtil.isLightTheme(getContext()), CompatUtil.getColorFromAttr(getContext(), R.attr.subtitleColor));
                    binding.userStats.setData(ringList, 500);
                }
                else
                    NotifyUtil.makeText(getActivity(), R.string.text_error_request, Toast.LENGTH_SHORT).show();
                break;
        }
    }
}
