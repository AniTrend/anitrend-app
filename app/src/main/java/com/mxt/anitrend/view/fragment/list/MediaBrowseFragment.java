package com.mxt.anitrend.view.fragment.list;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.afollestad.materialdialogs.DialogAction;
import com.annimon.stream.IntPair;
import com.annimon.stream.Stream;
import com.mxt.anitrend.R;
import com.mxt.anitrend.adapter.recycler.index.MediaAdapter;
import com.mxt.anitrend.base.custom.fragment.FragmentBaseList;
import com.mxt.anitrend.model.entity.anilist.Genre;
import com.mxt.anitrend.model.entity.anilist.MediaTag;
import com.mxt.anitrend.model.entity.base.MediaBase;
import com.mxt.anitrend.model.entity.container.body.PageContainer;
import com.mxt.anitrend.presenter.fragment.MediaPresenter;
import com.mxt.anitrend.util.CompatUtil;
import com.mxt.anitrend.util.DialogUtil;
import com.mxt.anitrend.util.KeyUtil;
import com.mxt.anitrend.util.NotifyUtil;
import com.mxt.anitrend.util.Settings;
import com.mxt.anitrend.util.collection.GenreTagUtil;
import com.mxt.anitrend.util.date.DateUtil;
import com.mxt.anitrend.util.media.MediaActionUtil;
import com.mxt.anitrend.util.media.MediaBrowseUtil;
import com.mxt.anitrend.view.activity.detail.MediaActivity;

import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.WeakHashMap;

import io.github.wax911.library.model.request.QueryContainerBuilder;

/**
 * Created by max on 2018/02/03.
 * Multi purpose media browse fragment
 */

public class MediaBrowseFragment extends FragmentBaseList<MediaBase, PageContainer<MediaBase>, MediaPresenter> {

    protected QueryContainerBuilder queryContainer;
    private MediaBrowseUtil mediaBrowseUtil;

    public static MediaBrowseFragment newInstance(Bundle params, QueryContainerBuilder queryContainer) {
        Bundle args = new Bundle(params);
        args.putParcelable(KeyUtil.arg_graph_params, queryContainer);
        MediaBrowseFragment fragment = new MediaBrowseFragment();
        fragment.setArguments(args);
        return fragment;
    }

    public static MediaBrowseFragment newInstance(Bundle params) {
        Bundle args = new Bundle(params);
        MediaBrowseFragment fragment = new MediaBrowseFragment();
        fragment.setArguments(args);
        return fragment;
    }

    /**
     * Override and set presenter, mColumnSize, and fetch argument/s
     *
     * @param savedInstanceState
     */
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(getArguments() != null) {
            queryContainer = getArguments().getParcelable(KeyUtil.arg_graph_params);
            mediaBrowseUtil = getArguments().getParcelable(KeyUtil.arg_media_util);
        }
        if(mediaBrowseUtil == null)
            mediaBrowseUtil = new MediaBrowseUtil(true);

        isPager = true; isFilterable = mediaBrowseUtil.isFilterEnabled();
        mColumnSize = mediaBrowseUtil.isCompactType() ? R.integer.grid_giphy_x3 : R.integer.grid_list_x2;
        mAdapter = new MediaAdapter(getContext(), mediaBrowseUtil.isCompactType());
        setPresenter(new MediaPresenter(getContext()));
        setViewModel(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        if(mediaBrowseUtil.isBasicFilter()) {
            menu.findItem(R.id.action_type).setVisible(false);
            menu.findItem(R.id.action_year).setVisible(false);
            menu.findItem(R.id.action_status).setVisible(false);
            menu.findItem(R.id.action_genre).setVisible(false);
            menu.findItem(R.id.action_tag).setVisible(false);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (getContext() != null)
            switch (item.getItemId()) {
                case R.id.action_sort:
                    DialogUtil.createSelection(getContext(), R.string.app_filter_sort, CompatUtil.INSTANCE.getIndexOf(KeyUtil.MediaSortType,
                            getPresenter().getSettings().getMediaSort()), CompatUtil.INSTANCE.capitalizeWords(KeyUtil.MediaSortType),
                            (dialog, which) -> {
                                if(which == DialogAction.POSITIVE)
                                    getPresenter().getSettings().setMediaSort(KeyUtil.MediaSortType[dialog.getSelectedIndex()]);
                            });
                    return true;
                case R.id.action_order:
                    DialogUtil.createSelection(getContext(), R.string.app_filter_order, CompatUtil.INSTANCE.getIndexOf(KeyUtil.SortOrderType,
                            getPresenter().getSettings().getSortOrder()), CompatUtil.INSTANCE.getStringList(getContext(), R.array.order_by_types),
                            (dialog, which) -> {
                                if(which == DialogAction.POSITIVE)
                                    getPresenter().getSettings().saveSortOrder(KeyUtil.SortOrderType[dialog.getSelectedIndex()]);
                            });
                    return true;
                case R.id.action_genre:
                    List<Genre> genres = getPresenter().getDatabase().getGenreCollection();
                    if(CompatUtil.INSTANCE.isEmpty(genres)) {
                        NotifyUtil.INSTANCE.makeText(getContext(), R.string.app_splash_loading, R.drawable.ic_warning_white_18dp, Toast.LENGTH_SHORT).show();
                        getPresenter().checkGenresAndTags(getActivity());
                    } else {
                        Map<Integer, String> genresIndexMap = getPresenter()
                                .getSettings().getSelectedGenres();

                        Integer[] selectedGenres = Stream.of(genresIndexMap)
                                .map(Map.Entry::getKey)
                                .toArray(Integer[]::new);

                        DialogUtil.createCheckList(getContext(), R.string.app_filter_genres, genres, selectedGenres,
                                (dialog, which, text) -> false, (dialog, which) -> {
                                    switch (which) {
                                        case POSITIVE:
                                            Map<Integer, String> selectedIndices = GenreTagUtil
                                                    .createGenreSelectionMap(genres, dialog.getSelectedIndices());

                                            getPresenter().getSettings()
                                                    .setSelectedGenres(selectedIndices);
                                            break;
                                        case NEGATIVE:
                                            getPresenter().getSettings()
                                                    .setSelectedGenres(new WeakHashMap<>());
                                            break;
                                    }
                                });
                    }
                    return true;
                case R.id.action_tag:
                    List<MediaTag> tagList = getPresenter().getDatabase().getMediaTags();
                    if(CompatUtil.INSTANCE.isEmpty(tagList)) {
                        NotifyUtil.INSTANCE.makeText(getContext(), R.string.app_splash_loading, R.drawable.ic_warning_white_18dp, Toast.LENGTH_SHORT).show();
                        getPresenter().checkGenresAndTags(getActivity());
                    } else {
                        Map<Integer, String> tagsIndexMap = getPresenter()
                                .getSettings().getSelectedTags();

                        Integer[] selectedTags = Stream.of(tagsIndexMap)
                                .map(Map.Entry::getKey)
                                .toArray(Integer[]::new);

                        DialogUtil.createCheckList(getContext(), R.string.app_filter_tags, tagList, selectedTags,
                                (dialog, which, text) -> false, (dialog, which) -> {
                                    switch (which) {
                                        case POSITIVE:
                                            Map<Integer, String> selectedIndices = GenreTagUtil
                                                    .createTagSelectionMap(tagList, dialog.getSelectedIndices());

                                            getPresenter().getSettings()
                                                    .setSelectedTags(selectedIndices);
                                            break;
                                        case NEGATIVE:
                                            getPresenter().getSettings()
                                                    .setSelectedTags(new WeakHashMap<>());
                                            break;
                                    }
                                });
                    }
                    return true;
                case R.id.action_type:
                    if (CompatUtil.INSTANCE.equals(queryContainer.getVariable(KeyUtil.arg_mediaType), KeyUtil.ANIME)) {
                        DialogUtil.createSelection(getContext(), R.string.app_filter_show_type, CompatUtil.INSTANCE.getIndexOf(KeyUtil.AnimeFormat,
                                getPresenter().getSettings().getAnimeFormat()), CompatUtil.INSTANCE.getStringList(getContext(), R.array.anime_formats),
                                (dialog, which) -> {
                                    if(which == DialogAction.POSITIVE)
                                        getPresenter().getSettings().setAnimeFormat(KeyUtil.AnimeFormat[dialog.getSelectedIndex()]);
                                });
                    } else {
                        DialogUtil.createSelection(getContext(), R.string.app_filter_show_type, CompatUtil.INSTANCE.getIndexOf(KeyUtil.MangaFormat,
                                getPresenter().getSettings().getMangaFormat()), CompatUtil.INSTANCE.getStringList(getContext(), R.array.manga_formats),
                                (dialog, which) -> {
                                    if(which == DialogAction.POSITIVE)
                                        getPresenter().getSettings().setMangaFormat(KeyUtil.MangaFormat[dialog.getSelectedIndex()]);
                                });
                    }
                    return true;
                case R.id.action_year:
                    final List<Integer> yearRanges = DateUtil.INSTANCE.getYearRanges(1950, 1);
                    DialogUtil.createSelection(getContext(), R.string.app_filter_year, CompatUtil.INSTANCE.getIndexOf(yearRanges, getPresenter().getSettings().getSeasonYear()),
                            yearRanges, (dialog, which) -> {
                                if(which == DialogAction.POSITIVE)
                                    getPresenter().getSettings().saveSeasonYear(yearRanges.get(dialog.getSelectedIndex()));
                            });
                    return true;
                case R.id.action_status:
                    DialogUtil.createSelection(getContext(), R.string.anime, CompatUtil.INSTANCE.getIndexOf(KeyUtil.MediaStatus,
                            getPresenter().getSettings().getMediaStatus()), CompatUtil.INSTANCE.getStringList(getContext(), R.array.media_status),
                            (dialog, which) -> {
                                if(which == DialogAction.POSITIVE)
                                    getPresenter().getSettings().setMediaStatus(KeyUtil.MediaStatus[dialog.getSelectedIndex()]);
                            });
                    return true;
            }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void updateUI() {
        injectAdapter();
    }

    @Override
    public void makeRequest() {
        Bundle bundle = getViewModel().getParams();
        Settings pref = getPresenter().getSettings();
        queryContainer.putVariable(KeyUtil.arg_page, getPresenter().getCurrentPage());

        if(isFilterable) {
            if(!mediaBrowseUtil.isBasicFilter()) {
                if (CompatUtil.INSTANCE.equals(queryContainer.getVariable(KeyUtil.arg_mediaType), KeyUtil.MANGA)) {
                    queryContainer.putVariable(KeyUtil.arg_startDateLike, String.format(Locale.getDefault(),
                            "%d%%", getPresenter().getSettings().getSeasonYear()))
                            .putVariable(KeyUtil.arg_format, pref.getMangaFormat());
                } else {
                    queryContainer.putVariable(KeyUtil.arg_seasonYear, getPresenter().getSettings().getSeasonYear())
                            .putVariable(KeyUtil.arg_format, pref.getAnimeFormat());
                }

                queryContainer.putVariable(KeyUtil.arg_status, pref.getMediaStatus())
                        .putVariable(KeyUtil.arg_genres, GenreTagUtil.getMappedValues(pref.getSelectedGenres()))
                        .putVariable(KeyUtil.arg_tags, GenreTagUtil.getMappedValues(pref.getSelectedTags()));
            }
            queryContainer.putVariable(KeyUtil.arg_sort, pref.getMediaSort() + pref.getSortOrder());
        }
        bundle.putParcelable(KeyUtil.arg_graph_params, queryContainer);
        getViewModel().requestData(KeyUtil.MEDIA_BROWSE_REQ, getContext());
    }

    @Override
    public void onChanged(@Nullable PageContainer<MediaBase> content) {
        if(content != null) {
            if(content.hasPageInfo())
                getPresenter().setPageInfo(content.getPageInfo());
            if(!content.isEmpty())
                onPostProcessed(content.getPageData());
            else
                onPostProcessed(Collections.emptyList());
        } else
            onPostProcessed(Collections.emptyList());
        if(mAdapter.getItemCount() < 1)
            onPostProcessed(null);
    }

    /**
     * When the target view from {@link View.OnClickListener}
     * is clicked from a view holder this method will be called
     *
     * @param target view that has been clicked
     * @param data   the model that at the click index
     */
    @Override
    public void onItemClick(View target, IntPair<MediaBase> data) {
        switch (target.getId()) {
            case R.id.container:
                Intent intent = new Intent(getActivity(), MediaActivity.class);
                intent.putExtra(KeyUtil.arg_id, data.getSecond().getId());
                intent.putExtra(KeyUtil.arg_mediaType, data.getSecond().getType());
                CompatUtil.INSTANCE.startRevealAnim(getActivity(), target, intent);
                break;
        }
    }

    /**
     * When the target view from {@link View.OnLongClickListener}
     * is clicked from a view holder this method will be called
     *
     * @param target view that has been long clicked
     * @param data   the model that at the long click index
     */
    @Override
    public void onItemLongClick(View target, IntPair<MediaBase> data) {
        switch (target.getId()) {
            case R.id.container:
                if(getPresenter().getSettings().isAuthenticated()) {
                    mediaActionUtil = new MediaActionUtil.Builder()
                            .setId(data.getSecond().getId()).build(getActivity());
                    mediaActionUtil.startSeriesAction();
                } else
                    NotifyUtil.INSTANCE.makeText(getContext(), R.string.info_login_req, R.drawable.ic_group_add_grey_600_18dp, Toast.LENGTH_SHORT).show();
                break;
        }
    }
}
