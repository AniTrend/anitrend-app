package com.mxt.anitrend.view.detail.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.mxt.anitrend.R;
import com.mxt.anitrend.adapter.recycler.details.AnimeDetailAdapter;
import com.mxt.anitrend.adapter.recycler.details.MangaDetailAdapter;
import com.mxt.anitrend.adapter.recycler.details.SeriesCharacterAdapter;
import com.mxt.anitrend.adapter.recycler.details.SeriesStaffAdapter;
import com.mxt.anitrend.api.model.Series;
import com.mxt.anitrend.base.custom.recycler.StatefulRecyclerView;
import com.mxt.anitrend.base.interfaces.event.InteractionListener;
import com.mxt.anitrend.view.detail.activity.StaffActivity;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

/**
 * Created by Maxwell on 12/4/2016.
 */
public class MangaExtrasFragment extends Fragment implements InteractionListener {

    @BindView(R.id.characters_recycler) StatefulRecyclerView mRecyclerCharacters;
    @BindView(R.id.staff_recycler) StatefulRecyclerView mRecyclerStaff;
    @BindView(R.id.relation_anime_recycler) StatefulRecyclerView mRecyclerAnime;
    @BindView(R.id.relation_manga_recycler) StatefulRecyclerView mRecyclerManga;

    @BindView(R.id.character_holder) View mCharacterCard;
    @BindView(R.id.staff_holder) View mStaffCard;
    @BindView(R.id.anime_holder) View mAnimeCard;
    @BindView(R.id.manga_holder) View mMangaCard;

    private Series model;
    private Unbinder unbinder;
    private RecyclerView.Adapter mCharacterAdapter;
    private RecyclerView.Adapter mStaffAdapter;
    private RecyclerView.Adapter mAnimeAdapter;
    private RecyclerView.Adapter mMangaAdapter;

    private final static String ARG_KEY = "arg_data";


    public MangaExtrasFragment() {
        // Required empty public constructor
    }

    public static MangaExtrasFragment newInstance() {
        return new MangaExtrasFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(getArguments() != null) {
            model = getArguments().getParcelable(ARG_KEY);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View root = inflater.inflate(R.layout.fragment_series_extras, container, false);
        unbinder = ButterKnife.bind(this, root);
        return root;
    }

    private void setupRecyclers() {
        mRecyclerCharacters.setLayoutManager(new LinearLayoutManager(getContext(),LinearLayoutManager.HORIZONTAL, false));
        mRecyclerCharacters.setNestedScrollingEnabled(false);
        mRecyclerCharacters.setHasFixedSize(true);
        mRecyclerStaff.setLayoutManager(new LinearLayoutManager(getContext(),LinearLayoutManager.HORIZONTAL, false));
        mRecyclerStaff.setNestedScrollingEnabled(false);
        mRecyclerStaff.setHasFixedSize(true);
        mRecyclerAnime.setLayoutManager(new LinearLayoutManager(getContext(),LinearLayoutManager.HORIZONTAL, false));
        mRecyclerAnime.setNestedScrollingEnabled(false);
        mRecyclerAnime.setHasFixedSize(true);
        mRecyclerManga.setLayoutManager(new LinearLayoutManager(getContext(),LinearLayoutManager.HORIZONTAL, false));
        mRecyclerManga.setNestedScrollingEnabled(false);
        mRecyclerManga.setHasFixedSize(true);
    }

    @Override
    public void onStart() {
        super.onStart();
        setupRecyclers();
    }

    private void updateUI() {
        mCharacterAdapter = new SeriesCharacterAdapter(model.getCharacters(), getActivity());
        mStaffAdapter = new SeriesStaffAdapter(model.getStaff(), getActivity(), this);
        mAnimeAdapter = new AnimeDetailAdapter(model.getRelations_anime(), getActivity());
        mMangaAdapter = new MangaDetailAdapter(model.getRelations(), getActivity());

        if(mCharacterAdapter.getItemCount() < 1)
            mCharacterCard.setVisibility(View.GONE);
        else
            mRecyclerCharacters.setAdapter(mCharacterAdapter);

        if(mStaffAdapter.getItemCount() < 1)
            mStaffCard.setVisibility(View.GONE);
        else
            mRecyclerStaff.setAdapter(mStaffAdapter);

        if(mAnimeAdapter.getItemCount() < 1)
            mAnimeCard.setVisibility(View.GONE);
        else
            mRecyclerAnime.setAdapter(mAnimeAdapter);

        if(mMangaAdapter.getItemCount() < 1)
            mMangaCard.setVisibility(View.GONE);
        else
            mRecyclerManga.setAdapter(mMangaAdapter);
    }

    @Override
    public void onPause() {
        EventBus.getDefault().unregister(this);
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        EventBus.getDefault().register(this);
    }

    /**
     * Responds to published events
     *
     * @param param
     */
    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onEventPublished(Series param) {
        if(!isRemoving() && model == null) {
            model = param;
            updateUI();
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    @Override
    public void onItemClick(int index) {
        Intent intent = new Intent(getContext(), StaffActivity.class);
        intent.putExtra(StaffActivity.STAFF_INTENT_KEY, model.getStaff().get(index));
        startActivity(intent);
    }
}
