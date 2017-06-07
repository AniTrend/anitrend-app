package com.mxt.anitrend.view.detail.fragment;

import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.GridLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.mxt.anitrend.R;
import com.mxt.anitrend.adapter.recycler.details.CharacterAdapter;
import com.mxt.anitrend.adapter.recycler.details.StaffAdapter;
import com.mxt.anitrend.adapter.recycler.index.SeriesAnimeAdapter;
import com.mxt.anitrend.adapter.recycler.index.SeriesMangaAdapter;
import com.mxt.anitrend.adapter.recycler.index.StudioAdapter;
import com.mxt.anitrend.api.model.CharacterSmall;
import com.mxt.anitrend.api.model.Series;
import com.mxt.anitrend.api.model.StaffSmall;
import com.mxt.anitrend.api.model.StudioSmall;
import com.mxt.anitrend.custom.RecyclerViewAdapter;
import com.mxt.anitrend.custom.StatefulRecyclerView;
import com.mxt.anitrend.presenter.CommonPresenter;
import com.mxt.anitrend.presenter.detail.GenericPresenter;
import com.mxt.anitrend.util.ErrorHandler;
import com.nguyenhoanglam.progresslayout.ProgressLayout;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import retrofit2.Response;

/**
 * Created by max on 2017/05/16.
 */
@SuppressWarnings("unchecked")
public class FavouritesFragment <T> extends Fragment {

    private static final String param_key = "model_key";
    private static final String param_type = "type_key";

    public @BindView(R.id.generic_recycler) StatefulRecyclerView recyclerView;
    public @BindView(R.id.generic_progress_state) ProgressLayout progressLayout;

    protected RecyclerViewAdapter mAdapter;
    private GridLayoutManager mLayoutManager;

    private int type;
    private List<T> model;
    private CommonPresenter<T> mPresenter;
    private Unbinder unbinder;

    public static <T> FavouritesFragment newInstance(List<T> model, int description) {
        Bundle args = new Bundle();
        args.putParcelableArrayList(param_key, (ArrayList<? extends Parcelable>)model);
        args.putInt(param_type, description);
        FavouritesFragment fragment = new FavouritesFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(!getArguments().isEmpty()) {
            model = (List<T>) getArguments().getParcelableArrayList(param_key);
            type = getArguments().getInt(param_type);
        }
        mPresenter = new GenericPresenter<>(getContext());
    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_bottomsheet_base, container, false);
        unbinder = ButterKnife.bind(this, root);
        recyclerView.setHasFixedSize(true);
        recyclerView.setNestedScrollingEnabled(true);
        return root;
    }

    /**
     * Called when the Fragment is visible to the user.  This is generally
     * tied to {@link Activity#onStart() Activity.onStart} of the containing
     * Activity's lifecycle.
     */
    @Override
    public void onStart() {
        super.onStart();
        updateUI();
    }

    public void updateUI() {
        if(recyclerView != null) {
            if(mAdapter == null) {
                switch (type) {
                    case 0:
                        // Anime
                        mAdapter = new SeriesAnimeAdapter((List<Series>) model, getActivity(), mPresenter.getAppPrefs());
                        mLayoutManager = new GridLayoutManager(getContext(), getResources().getInteger(R.integer.card_col_size_home));
                        recyclerView.setLayoutManager(mLayoutManager);
                        recyclerView.setAdapter(mAdapter);
                        break;
                    case 1:
                        // Character
                        mAdapter = new CharacterAdapter((List<CharacterSmall>) model, getActivity());
                        mLayoutManager = new GridLayoutManager(getContext(), getResources().getInteger(R.integer.list_col_size_rank));
                        recyclerView.setLayoutManager(mLayoutManager);
                        recyclerView.setAdapter(mAdapter);
                        break;
                    case 2:
                        // Manga
                        mAdapter = new SeriesMangaAdapter((List<Series>) model, getActivity(), mPresenter.getAppPrefs());
                        mLayoutManager = new GridLayoutManager(getContext(), getResources().getInteger(R.integer.card_col_size_home));
                        recyclerView.setLayoutManager(mLayoutManager);
                        recyclerView.setAdapter(mAdapter);
                        break;
                    case 3:
                        // Staff
                        mAdapter = new StaffAdapter((List<StaffSmall>) model, getActivity());
                        mLayoutManager = new GridLayoutManager(getContext(), getResources().getInteger(R.integer.list_col_size_rank));
                        recyclerView.setLayoutManager(mLayoutManager);
                        recyclerView.setAdapter(mAdapter);
                        break;
                    case 4:
                        // Studio
                        mAdapter = new StudioAdapter((List<StudioSmall>) model, getActivity());
                        mLayoutManager = new GridLayoutManager(getContext(), getResources().getInteger(R.integer.card_col_size_home));
                        recyclerView.setLayoutManager(mLayoutManager);
                        recyclerView.setAdapter(mAdapter);
                        break;
                }
            } else {
                mAdapter.onDataSetModified(model);
            }

            if (mAdapter != null && mAdapter.getItemCount() > 0) {
                progressLayout.showContent();
            } else
                showEmpty(getString(R.string.layout_empty_response));
        }
    }

    public void showError(Response body) {
        progressLayout.showError(ContextCompat.getDrawable(getContext(), R.drawable.request_error),
                ErrorHandler.getError(body).toString(), getString(R.string.button_try_again), null);
    }

    public void showEmpty(String message) {
        progressLayout.showEmpty(ContextCompat.getDrawable(getContext(), R.drawable.request_empty), message);
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
        if(unbinder != null)
            unbinder.unbind();
        super.onDestroyView();
    }
}
