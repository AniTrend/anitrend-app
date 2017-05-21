package com.mxt.anitrend.view.index.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.github.johnpersano.supertoasts.library.Style;
import com.mxt.anitrend.R;
import com.mxt.anitrend.adapter.recycler.index.StatusAdapter;
import com.mxt.anitrend.api.model.UserActivity;
import com.mxt.anitrend.api.model.UserSmall;
import com.mxt.anitrend.api.structure.FilterTypes;
import com.mxt.anitrend.async.RequestApiAction;
import com.mxt.anitrend.custom.Payload;
import com.mxt.anitrend.custom.RecyclerViewAdapter;
import com.mxt.anitrend.custom.StatefulRecyclerView;
import com.mxt.anitrend.custom.emoji4j.EmojiUtils;
import com.mxt.anitrend.event.MultiInteractionListener;
import com.mxt.anitrend.event.RecyclerLoadListener;
import com.mxt.anitrend.presenter.index.UserActivityPresenter;
import com.mxt.anitrend.utils.DialogManager;
import com.mxt.anitrend.utils.ErrorHandler;
import com.mxt.anitrend.view.detail.activity.UserReplyActivity;
import com.mxt.anitrend.view.index.activity.UserProfileActivity;
import com.nguyenhoanglam.progresslayout.ProgressLayout;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import fm.jiecao.jcvideoplayer_lib.JCVideoPlayer;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by max on 2017/03/13.
 */
public class PublicStatusFragment extends Fragment implements Callback<List<UserActivity>>, SwipeRefreshLayout.OnRefreshListener, MultiInteractionListener, RecyclerLoadListener {

    @BindView(R.id.generic_pull_refresh) SwipeRefreshLayout swipeRefreshLayout;
    @BindView(R.id.generic_recycler) StatefulRecyclerView recyclerView;
    @BindView(R.id.generic_progress_state) ProgressLayout progressLayout;

    private int mPage = 1;
    private boolean isLimit;
    private List<UserActivity> mData;

    private static final String CONTENT_DESCRIPTION = "model_type";

    private final String MODEL_LIMIT = "loading_limit_reached";
    private final String MODEL_PAGE = "current_page_number";
    private final String MODEL_CACHE = "model_list";
    private final String MODEL_TYPE = "model_type";

    private String model_type;

    private StaggeredGridLayoutManager mLayoutManager;

    private Unbinder unbinder;

    private List<Integer> skipIds = new ArrayList<>();


    private UserActivityPresenter mFragmentPresenter;

    private RecyclerViewAdapter<UserActivity> mAdapter;

    public PublicStatusFragment() {
        // Required empty public constructor
    }

    public static PublicStatusFragment newInstance(String type) {
        PublicStatusFragment fragment = new PublicStatusFragment();
        Bundle bundle = new Bundle();
        bundle.putString(CONTENT_DESCRIPTION, type);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        model_type = getArguments().getString(CONTENT_DESCRIPTION);
        skipIds.add(R.id.generic_recycler);
        mFragmentPresenter = new UserActivityPresenter(getContext());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View views = inflater.inflate(R.layout.fragment_base_view, container, false);
        unbinder = ButterKnife.bind(this,views);
        progressLayout.showLoading();
        swipeRefreshLayout.setOnRefreshListener(this);
        recyclerView.setHasFixedSize(true); //originally set to fixed size true
        recyclerView.setNestedScrollingEnabled(false);//set to false if somethings fail to work properly
        mLayoutManager = new StaggeredGridLayoutManager(getResources().getInteger(R.integer.card_col_size_home), StaggeredGridLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(mLayoutManager);
        if(savedInstanceState != null)
            model_type = savedInstanceState.getString(MODEL_TYPE);
        return views;
    }

    @Override
    public void onStart() {
        super.onStart();
        if(mData == null)
            mFragmentPresenter.beginAsync(this, mPage, model_type);
        else
            showData();
    }

    private void addScrollLoadTrigger() {
        if(!recyclerView.hasOnScrollListener()) {
            mFragmentPresenter.initListener(mLayoutManager, mPage, this);
            recyclerView.addOnScrollListener(mFragmentPresenter);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }


    @Override
    public void onPause() {
        super.onPause();
        JCVideoPlayer.releaseAllVideos();
        recyclerView.clearOnScrollListeners();
        mFragmentPresenter.setParcelable(recyclerView.onSaveInstanceState());
    }

    @Override
    public void onResume() {
        super.onResume();
        if(mFragmentPresenter.getSavedParse() != null)
            recyclerView.onRestoreInstanceState(mFragmentPresenter.getSavedParse());
        addScrollLoadTrigger();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        //outState.putParcelable(VIEW_PARCABLE, recyclerView.onSaveInstanceState());
        outState.putInt(MODEL_PAGE, mPage);
        outState.putString(MODEL_TYPE, model_type);
        outState.putBoolean(MODEL_LIMIT, isLimit);
        outState.putParcelableArrayList(MODEL_CACHE, (ArrayList<? extends Parcelable>) mData);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        if(savedInstanceState != null) {
            //recyclerView.onRestoreInstanceState(savedInstanceState.getParcelable(VIEW_PARCABLE));
            mPage = savedInstanceState.getInt(MODEL_PAGE);
            isLimit = savedInstanceState.getBoolean(MODEL_LIMIT);
            mData = savedInstanceState.getParcelableArrayList(MODEL_CACHE);
        }
    }

    @Override
    public void onResponse(Call<List<UserActivity>> call, Response<List<UserActivity>> response) {
        if(response.body() != null && response.body().size() > 0) {
            if (mData == null)
                mData = response.body();
            else
                mData.addAll(response.body());
        } else if(response.isSuccessful() && response.body() != null && mPage != 1) {
            isLimit = true;
        }
        showData();
    }

    private void showData() {
        if(recyclerView != null) {
            if(swipeRefreshLayout.isRefreshing())
                swipeRefreshLayout.setRefreshing(false);
            if(mAdapter == null) {
                mAdapter = new StatusAdapter(mData, getContext(), mFragmentPresenter.getAppPrefs(), mFragmentPresenter.getApiPrefs(), this);
                recyclerView.setAdapter(mAdapter);
            } else {
                mAdapter.onDataSetModified(mData);
            }

            if (mAdapter.getItemCount() > 0) {
                progressLayout.showContent();
                addScrollLoadTrigger();
            } else
                progressLayout.showEmpty(ContextCompat.getDrawable(getContext(), R.drawable.request_empty), "No results found", skipIds);
        }
    }

    @Override
    public void onFailure(Call<List<UserActivity>> call, Throwable t) {
        progressLayout.showError(ContextCompat.getDrawable(getContext(), R.drawable.request_error), t.getLocalizedMessage(), "Try Again", new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                progressLayout.showLoading();
                mFragmentPresenter.beginAsync(PublicStatusFragment.this, mPage, model_type);
            }
        });
    }

    /**
     * Called when a swipe gesture triggers a refresh.
     */
    @Override
    public void onRefresh() {
        mPage = 1;
        mData = null;
        mFragmentPresenter.onRefreshPage();
        mFragmentPresenter.beginAsync(this, mPage, model_type);
    }

    @Override
    public void onItemClick(final int index, int viewId) {
        Intent intent;
        final UserActivity mItem = mData.get(index);
        switch (viewId) {
            case R.id.status_avatar:
                intent = new Intent(getContext(), UserProfileActivity.class);
                intent.putExtra(UserProfileActivity.PROFILE_INTENT_KEY, mItem.getUsers().get(0));
                startActivity(intent);
                break;
            case R.id.status_comment:
                // Open comment activity
                intent = new Intent(getActivity(), UserReplyActivity.class);
                intent.putExtra(UserReplyActivity.USER_ACTIVITY_EXTRA, mItem);
                startActivity(intent);
                break;
            case R.id.mFlipper:
                // Like the feed
                Payload.ActionIdBased actionIdBased = new Payload.ActionIdBased(mItem.getId());
                RequestApiAction.IdActions userPostActions = new RequestApiAction.IdActions(getContext(), new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                        if (isVisible() && !isRemoving() || !isDetached()) {
                            if (response.isSuccessful() && response.body() != null) {
                                List<UserSmall> mItemLikes = mItem.getLikes();

                                if(mItemLikes.contains(mFragmentPresenter.getCurrentUser()))
                                    mItemLikes.remove(mFragmentPresenter.getCurrentUser());
                                else
                                    mItemLikes.add(mFragmentPresenter.getCurrentUser());
                                mData.set(index, mItem);
                                mAdapter.onItemChanged(mData, index);
                            } else {
                                Toast.makeText(getContext(), ErrorHandler.getError(response).toString(), Toast.LENGTH_LONG).show();
                                mAdapter.refreshItem(index);
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<ResponseBody> call, Throwable t) {
                        t.printStackTrace();
                        if(isVisible() && !isRemoving() || !isDetached())
                            try {
                                Toast.makeText(getContext(), t.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                                mAdapter.refreshItem(index);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                    }
                }, FilterTypes.ActionType.ACTIVITY_FAVOURITE, actionIdBased);
                userPostActions.execute();
                break;
            case R.id.status_edit:
                new DialogManager(getActivity()).createDialogActivityEdit(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull final MaterialDialog dialog, @NonNull DialogAction which) {
                        switch (which) {
                            case POSITIVE:
                                EditText editText = dialog.getInputEditText();
                                if(editText != null) {
                                    if(!TextUtils.isEmpty(editText.getText())) {
                                        Payload.ActivityStruct status = new Payload.ActivityStruct(mItem.getId(), EmojiUtils.hexHtmlify(editText.getText().toString()), mItem.getUser_id(), -1);
                                        RequestApiAction.ActivityActions<UserActivity> action = new RequestApiAction.ActivityActions<>(getContext(), new Callback<UserActivity>() {
                                            @Override
                                            public void onResponse(Call<UserActivity> call, Response<UserActivity> response) {
                                                if(isVisible() && !isRemoving() || !isDetached()) {
                                                    if (response.isSuccessful() && response.body() != null) {
                                                        dialog.dismiss();
                                                        mItem.setValue(response.body().getValue());
                                                        mData.set(index, mItem);
                                                        mAdapter.onItemChanged(mData, index);
                                                    }
                                                    else
                                                        Toast.makeText(getContext(), ErrorHandler.getError(response).toString(), Toast.LENGTH_LONG).show();
                                                }
                                            }

                                            @Override
                                            public void onFailure(Call<UserActivity> call, Throwable t) {
                                                if(isVisible() && !isRemoving() || !isDetached()) {
                                                    t.printStackTrace();
                                                    Toast.makeText(getActivity(), t.getLocalizedMessage(), Toast.LENGTH_LONG).show();
                                                }
                                            }
                                        }, FilterTypes.ActionType.ACTIVITY_EDIT, status);
                                        action.execute();
                                        mFragmentPresenter.createSuperToast(getActivity(), getString(R.string.text_processing_request), R.drawable.ic_info_outline_white_18dp, Style.TYPE_PROGRESS_BAR);
                                    } else
                                        Toast.makeText(getContext(), getString(R.string.warning_empty_input), Toast.LENGTH_SHORT).show();
                                }
                                break;
                            case NEUTRAL:
                                // TODO: 2017/05/16 Open bottom bar for inserting media
                                break;
                            case NEGATIVE:
                                dialog.dismiss();
                                break;
                        }
                    }
                }, mItem.getSpannedValue());
                break;
            case R.id.status_delete:
                new DialogManager(getActivity()).createDialogMessage(getString(R.string.dialog_confirm_delete),
                        getString(R.string.dialog_delete_message),
                        getString(R.string.Yes),
                        getString(R.string.No),
                        new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                switch (which) {
                                    case POSITIVE:
                                        Payload.ActionIdBased action = new Payload.ActionIdBased(mItem.getId());
                                        RequestApiAction.IdActions deleteAction = new RequestApiAction.IdActions(getContext(), new Callback<ResponseBody>() {
                                            @Override
                                            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                                                if (isVisible() && !isRemoving() || !isDetached()) {
                                                    if (response.isSuccessful() && response.body() != null) {
                                                        mData.remove(index);
                                                        mAdapter.onItemRemoved(mData, index);
                                                    }
                                                    else
                                                        Toast.makeText(getContext(), ErrorHandler.getError(response).toString(), Toast.LENGTH_LONG).show();
                                                }
                                            }

                                            @Override
                                            public void onFailure(Call<ResponseBody> call, Throwable t) {
                                                t.printStackTrace();
                                                if(isVisible() && !isRemoving() || !isDetached())
                                                    try {
                                                        Toast.makeText(getContext(), t.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                                                    } catch (Exception e) {
                                                        e.printStackTrace();
                                                    }
                                            }
                                        }, FilterTypes.ActionType.ACTIVITY_DELETE, action);
                                        deleteAction.execute();
                                        mFragmentPresenter.createSuperToast(getActivity(), getString(R.string.text_processing_request), R.drawable.ic_info_outline_white_18dp, Style.TYPE_PROGRESS_BAR);
                                        break;
                                    case NEUTRAL:
                                        break;
                                    case NEGATIVE:
                                        dialog.dismiss();
                                        break;
                                }
                            }
                        });
                break;
        }
    }

    @Override
    public void onLoadMore(int currentPage) {
        if(!isLimit) {
            mPage = currentPage;
            swipeRefreshLayout.setRefreshing(true);
            mFragmentPresenter.beginAsync(this, mPage, model_type);
        }
    }
}
