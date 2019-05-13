package com.mxt.anitrend.view.fragment.detail;

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
import com.mxt.anitrend.databinding.FragmentUserAboutBinding;
import com.mxt.anitrend.model.entity.anilist.User;
import com.mxt.anitrend.model.entity.base.StatsRing;
import com.mxt.anitrend.model.entity.container.request.QueryContainerBuilder;
import com.mxt.anitrend.presenter.base.BasePresenter;
import com.mxt.anitrend.util.CompatUtil;
import com.mxt.anitrend.util.GraphUtil;
import com.mxt.anitrend.util.KeyUtil;
import com.mxt.anitrend.util.NotifyUtil;

import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by max on 2017/11/27.
 * about user fragment for the profile
 */

public class UserOverviewFragment extends FragmentBase<User, BasePresenter, User> {

    private FragmentUserAboutBinding binding;
    private User model;

    private long userId;
    private String userName;

    public static UserOverviewFragment newInstance(Bundle args) {
        UserOverviewFragment fragment = new UserOverviewFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            if (getArguments().containsKey(KeyUtil.arg_id))
                userId = getArguments().getLong(KeyUtil.arg_id);
            else
                userName = getArguments().getString(KeyUtil.arg_userName);
        }
        isMenuDisabled = true;
        setPresenter(new BasePresenter(getContext()));
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

    @Override
    public void onStart() {
        super.onStart();
        makeRequest();
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
        QueryContainerBuilder queryContainer = GraphUtil.INSTANCE.getDefaultQuery(false)
                .putVariable(KeyUtil.arg_userName, userName);
        if(userId > 0)
            queryContainer.putVariable(KeyUtil.arg_id, userId);
        getViewModel().getParams().putParcelable(KeyUtil.arg_graph_params, queryContainer);
        getViewModel().requestData(KeyUtil.USER_OVERVIEW_REQ, getContext());
    }

    /**
     * Called when the model state is changed.
     *
     * @param model The new data
     */
    @Override
    public void onChanged(@Nullable User model) {
        if(model != null) {
            this.model = model;
            updateUI();
        } else
            binding.stateLayout.showError(CompatUtil.INSTANCE.getDrawable(getContext(), R.drawable.ic_emoji_sweat),
                    getString(R.string.layout_empty_response), getString(R.string.try_again), view -> { binding.stateLayout.showLoading(); makeRequest(); });
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
        List<StatsRing> userGenreStats = new ArrayList<>();
        if(model.getStats() != null && !CompatUtil.INSTANCE.isEmpty(model.getStats().getFavouredGenres())) {
            int highestValue = Stream.of(model.getStats().getFavouredGenres())
                    .max((o1, o2) -> o1.getAmount() > o2.getAmount() ? 1 : -1)
                    .get().getAmount();

            userGenreStats = Stream.of(model.getStats().getFavouredGenres())
                    .sortBy(s -> - s.getAmount()).map(genreStats -> {
                        float percentage = (((float)genreStats.getAmount()) / ((float)highestValue)) * 100f;
                        return new StatsRing((int)percentage, genreStats.getGenre(), String.valueOf(genreStats.getAmount()));
                    }).limit(5).toList();
        }

        return userGenreStats;
    }

    private void showRingStats() {
        List<StatsRing> ringList = generateStatsData();
        if(ringList.size() > 1) {
            binding.userStats.setDrawBg(CompatUtil.INSTANCE.isLightTheme(getContext()), CompatUtil.INSTANCE.getColorFromAttr(getContext(), R.attr.subtitleColor));
            binding.userStats.setData(ringList, 500);
        }
    }

    /**
     * Called when a view has been clicked.
     *
     * @param view The view that was clicked.
     */
    @Override @OnClick({R.id.user_avatar, R.id.user_stats_container})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.user_avatar:
                CompatUtil.INSTANCE.imagePreview(getActivity(), view, model.getAvatar().getLarge(), R.string.image_preview_error_user_avatar);
                break;
            case R.id.user_stats_container:
                List<StatsRing> ringList = generateStatsData();
                if(ringList.size() > 1) {
                    binding.userStats.setDrawBg(CompatUtil.INSTANCE.isLightTheme(getContext()), CompatUtil.INSTANCE.getColorFromAttr(getContext(), R.attr.subtitleColor));
                    binding.userStats.setData(ringList, 500);
                }
                else
                    NotifyUtil.makeText(getActivity(), R.string.text_error_request, Toast.LENGTH_SHORT).show();
                break;
        }
    }
}
