package com.mxt.anitrend.view.detail.fragment;

import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.GridLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.mxt.anitrend.BuildConfig;
import com.mxt.anitrend.R;
import com.mxt.anitrend.adapter.recycler.details.AnimeEpisodeAdapter;
import com.mxt.anitrend.api.call.EpisodeModel;
import com.mxt.anitrend.api.service.ServiceGenerator;
import com.mxt.anitrend.api.structure.Channel;
import com.mxt.anitrend.api.structure.Episode;
import com.mxt.anitrend.api.structure.ExternalLink;
import com.mxt.anitrend.api.structure.Rss;
import com.mxt.anitrend.custom.recycler.RecyclerViewAdapter;
import com.mxt.anitrend.custom.view.StatefulRecyclerView;
import com.mxt.anitrend.custom.event.InteractionListener;
import com.mxt.anitrend.presenter.index.FragmentPresenter;
import com.mxt.anitrend.util.DialogManager;
import com.mxt.anitrend.util.EpisodesHelper;
import com.mxt.anitrend.util.MarkDown;
import com.nguyenhoanglam.progresslayout.ProgressLayout;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AnimeWatchFragment extends Fragment implements InteractionListener, Callback<Rss>{

    private static final String ARG_PARAM = "external_links";
    private final String KEY_EXTERNAL_TARGET = "target_link_key";
    private final String KEY_EXTERNAL_LINKS = "external_links_key";
    private final String KEY_RSS = "rss_list_key";

    @BindView(R.id.watch_container) ProgressLayout progressLayout;
    @BindView(R.id.detail_watch_recycler) StatefulRecyclerView recyclerView;

    private Rss rssFeed;
    private String targetLink;
    private List<ExternalLink> externalLinks;

    private GridLayoutManager mLayoutManager;
    private RecyclerViewAdapter mAdapter;

    private Unbinder unbinder;
    private FragmentPresenter fragmentPresenter;

    public AnimeWatchFragment() {
        // Required empty public constructor
    }

    public static AnimeWatchFragment newInstance(List<ExternalLink> param) {
        AnimeWatchFragment fragment = new AnimeWatchFragment();
        Bundle args = new Bundle();
        args.putParcelableArrayList(ARG_PARAM, (ArrayList<? extends Parcelable>) param);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            externalLinks = getArguments().getParcelableArrayList(ARG_PARAM);
        }
        fragmentPresenter = new FragmentPresenter(getContext());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View root = inflater.inflate(R.layout.fragment_anime_watch, container, false);
        unbinder = ButterKnife.bind(this, root);
        if(savedInstanceState != null) {
            externalLinks = savedInstanceState.getParcelableArrayList(KEY_EXTERNAL_LINKS);
            targetLink = savedInstanceState.getString(KEY_EXTERNAL_TARGET);
            rssFeed = savedInstanceState.getParcelable(KEY_RSS);
        }
        return root;
    }

    @Override
    public void onStart() {
        super.onStart();
        progressLayout.showLoading();
        mLayoutManager = new GridLayoutManager(getContext(), getResources().getInteger(R.integer.card_col_size_home));
        recyclerView.setLayoutManager(mLayoutManager);
        if(rssFeed == null)
            new PreviewCheck().execute();
        else
            updateUI();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putParcelableArrayList(KEY_EXTERNAL_LINKS, (ArrayList<? extends Parcelable>) externalLinks);
        outState.putString(KEY_EXTERNAL_TARGET, targetLink);
        outState.putParcelable(KEY_RSS, rssFeed);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onPause() {
        super.onPause();
        if(recyclerView != null)
            fragmentPresenter.setParcelable(recyclerView.onSaveInstanceState());
    }

    @Override
    public void onResume() {
        super.onResume();
        if(recyclerView != null)
            recyclerView.onRestoreInstanceState(fragmentPresenter.getSavedParse());
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    private void preUpdateUi() {
        if (targetLink == null) {
            progressLayout.showEmpty(ContextCompat.getDrawable(getContext(), R.drawable.request_empty), getString(R.string.waring_missing_episode_links));
        } else {
            boolean feed = (targetLink.startsWith(BuildConfig.FEEDS_LINK));
            EpisodeModel model = ServiceGenerator.createCrunchyService(feed);
            Call<Rss> call = feed? model.getLatestFeed() : model.getRSS(targetLink);
            call.enqueue(this);
        }
    }

    private void updateUI() {
        if(recyclerView != null) {
            mAdapter = new AnimeEpisodeAdapter(this, getContext(), rssFeed.getChannel());
            recyclerView.setAdapter(mAdapter);
            progressLayout.showContent();
        }
    }

    @Override
    public void onResponse(Call<Rss> call, Response<Rss> response) {
        if (isVisible() && (!isDetached() || !isRemoving())) {
            try {
                if(response.isSuccessful() && response.body() != null){
                    rssFeed = response.body();
                    updateUI();
                } else {
                    progressLayout.showError(ContextCompat.getDrawable(getContext(), R.drawable.request_error), getString(R.string.error_episode_fetch, response.code()), getString(R.string.Retry), new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            new PreviewCheck().execute();
                        }
                    });
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onFailure(Call<Rss> call, Throwable t) {
        if(isVisible() && (!isDetached() || !isRemoving())) {
            try {
                progressLayout.showError(ContextCompat.getDrawable(getContext(), R.drawable.request_error), t.getLocalizedMessage(), getString(R.string.Retry), new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        new PreviewCheck().execute();
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onItemClick(int index) {
        Channel channel = rssFeed.getChannel();
        final Episode ep = channel.getEpisode().get(index);
        if(ep.getContent() != null) {
            new DialogManager(getContext()).createDialogMessage(String.format(Locale.getDefault(), "%s", ep.getTitle()), MarkDown.convert(ep.getDescription()+"<br/><br/>"+channel.getCopyright()),
                    getString(R.string.Watch), getString(R.string.Dismiss),
                    new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                            switch (which) {
                                case POSITIVE:
                                    if(ep.getLink() != null) {
                                        Toast.makeText(getContext(), R.string.text_experimental_feature, Toast.LENGTH_LONG).show();
                                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(ep.getLink()));
                                        startActivity(intent);
                                        dialog.dismiss();
                                    } else {
                                        Toast.makeText(getContext(), R.string.text_premium_show, Toast.LENGTH_SHORT).show();
                                    }
                                    break;
                                case NEUTRAL:
                                    break;
                                case NEGATIVE:
                                    dialog.dismiss();
                                    break;
                            }
                        }
                    });

        } else {
            Toast.makeText(getContext(), R.string.text_link_unavailable, Toast.LENGTH_SHORT).show();
        }
    }

    private class PreviewCheck extends AsyncTask<Void, Void, String> {

        @Override
        protected void onPreExecute() {
            if(!progressLayout.isLoading())
                progressLayout.showLoading();
        }

        @Override
        protected String doInBackground(Void... voids) {
            if(externalLinks != null) {
                return EpisodesHelper.episodeSupport(externalLinks);
            }
            return null;
        }

        @Override
        protected void onPostExecute(String link) {
            if(!isDetached() || !isCancelled() || !isRemoving()) {
                try {
                    targetLink = link;
                    preUpdateUi();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
}