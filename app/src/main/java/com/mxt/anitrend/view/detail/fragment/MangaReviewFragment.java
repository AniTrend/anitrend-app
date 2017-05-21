package com.mxt.anitrend.view.detail.fragment;

import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.mxt.anitrend.R;
import com.mxt.anitrend.adapter.recycler.details.SeriesReviewAdapter;
import com.mxt.anitrend.api.model.Review;
import com.mxt.anitrend.api.structure.FilterTypes;
import com.mxt.anitrend.async.AsyncTaskFetch;
import com.mxt.anitrend.async.RequestApiAction;
import com.mxt.anitrend.custom.Payload;
import com.mxt.anitrend.custom.RecyclerViewAdapter;
import com.mxt.anitrend.event.ReviewClickListener;
import com.mxt.anitrend.utils.ApplicationPrefs;
import com.mxt.anitrend.utils.DialogManager;
import com.mxt.anitrend.utils.ErrorHandler;
import com.nguyenhoanglam.progresslayout.ProgressLayout;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by Maxwell on 12/4/2016.
 */

public class MangaReviewFragment extends Fragment implements Callback<List<Review>>, ReviewClickListener {

    @BindView(R.id.detail_review_recycler)
    RecyclerView mRecyclerReview;
    @BindView(R.id.review_container)
    ProgressLayout progressLayout;

    private int item_id;
    private List<Review> reviews;
    private Unbinder unbinder;
    private RecyclerViewAdapter<Review> mReviewAdapter;
    private ApplicationPrefs prefs;

    private final static String ARG_KEY = "arg_data";
    private final String KEY_ID = "model_id";
    private final String KEY_REVIEWS = "review_list_key";

    public MangaReviewFragment() {
        // Required empty public constructor
    }

    public static MangaReviewFragment newInstance(int id) {
        Bundle args = new Bundle();
        args.putInt(ARG_KEY, id);
        MangaReviewFragment fragment = new MangaReviewFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(getArguments() != null) {
            item_id = getArguments().getInt(ARG_KEY);
        }
        prefs = new ApplicationPrefs(getContext());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View root = inflater.inflate(R.layout.fragment_manga_reviews, container, false);
        unbinder = ButterKnife.bind(this, root);
        progressLayout.showLoading();
        if(savedInstanceState != null) {
            item_id = savedInstanceState.getInt(KEY_ID);
            reviews = savedInstanceState.getParcelableArrayList(KEY_REVIEWS);
        }
        setupRecyclers();
        return root;
    }

    private void setupRecyclers() {
        mRecyclerReview.setLayoutManager(new GridLayoutManager(getContext(), getResources().getInteger(R.integer.card_col_size_home)));
        mRecyclerReview.setHasFixedSize(true); //originally set to fixed size true
        mRecyclerReview.setNestedScrollingEnabled(true);//set to false if somethings fail to work properly
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putInt(KEY_ID, item_id);
        outState.putParcelableArrayList(KEY_REVIEWS, (ArrayList<? extends Parcelable>) reviews);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onStart() {
        super.onStart();
        if(reviews != null){
            UpdateUI();
        } else {
            makeReviewsRequest();
        }
    }

    private void makeReviewsRequest() {
        new AsyncTaskFetch<>(this, getContext(), item_id).execute(AsyncTaskFetch.RequestType.MANGA_REVIEWS_REQ);
    }

    private void UpdateUI() {
        if(reviews.size() > 0) {
            if(mReviewAdapter == null) {
                mReviewAdapter = new SeriesReviewAdapter(reviews, getContext(), this);
                mRecyclerReview.setAdapter(mReviewAdapter);
            } else {
                mReviewAdapter.onDataSetModified(reviews);
            }
            if(!progressLayout.isContent())
                progressLayout.showContent();
        }
        else
            progressLayout.showEmpty(ContextCompat.getDrawable(getContext(), R.drawable.request_empty), getString(R.string.layout_empty_response));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }


    /**
     * Invoked for a received HTTP response.
     * <p>
     * Note: An HTTP response may still indicate an application-level failure such as a 404 or 500.
     * Call {@link Response#isSuccessful()} to determine if the response indicates success.
     *
     * @param call
     * @param response
     */
    @Override
    public void onResponse(Call<List<Review>> call, Response<List<Review>> response) {
        if(response.isSuccessful() && response.body() != null) {
            if(isVisible() && !isDetached() || !isRemoving())
                try {
                    reviews = response.body();
                    UpdateUI();
                } catch (Exception e) {
                    e.printStackTrace();
                }
        }
    }

    /**
     * Invoked when a network exception occurred talking to the server or when an unexpected
     * exception occurred creating the request or processing the response.
     *
     * @param call
     * @param t
     */
    @Override
    public void onFailure(Call<List<Review>> call, Throwable t) {
        if(isVisible() && !isDetached() || !isRemoving()) {
            try {
                progressLayout.showError(ContextCompat.getDrawable(getContext(), R.drawable.request_error), t.getLocalizedMessage(), "Retry", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        makeReviewsRequest();
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onReadMore(int index) {
        Review review = reviews.get(index);
        new DialogManager(getContext()).createDialogMessage(String.format(Locale.getDefault(), "Review by: %s", review.getUser().getDisplay_name()), review.getText());
    }

    @Override
    public void onCardClicked(int index) {
        switch (reviews.get(index).getUser_rating()){
            case 0:
                Toast.makeText(getContext(), "You haven't rated this review", Toast.LENGTH_SHORT).show();
                break;
            case 1:
                Toast.makeText(getContext(), "You liked this review", Toast.LENGTH_SHORT).show();
                break;
            case 2:
                Toast.makeText(getContext(), "You disliked this review", Toast.LENGTH_SHORT).show();
                break;
        }
    }

    @Override
    public void onUpvote(int index) {
        if(!prefs.isAuthenticated()) {
            Toast.makeText(getContext(), "Please sign into first!", Toast.LENGTH_SHORT).show();
            return;
        }
        Review review = reviews.get(index);
        Toast.makeText(getContext(), review.getUser_rating() == 0?"Up voting review":"Removing previous rating", Toast.LENGTH_SHORT).show();
        //Toast.makeText(mContext, review.getUser_rating() == 0?"Up voting review":"Removing previous rating", Toast.LENGTH_SHORT).show();
        Payload.ReviewActions payload = new Payload.ReviewActions(review.getId(), review.getUser_rating() == 0? 1:0);
        new RequestApiAction.ReviewBasedActions(getContext(), new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if(isVisible() && !isDetached() || !isRemoving())
                    if(response.isSuccessful() && response.body() != null)
                        makeReviewsRequest();
                    else
                        Toast.makeText(getContext(), ErrorHandler.getError(response).toString(), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                try {
                    Toast.makeText(getContext(), t.getCause().getMessage(), Toast.LENGTH_SHORT).show();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }, FilterTypes.ActionType.REVIEW_MANGA_RATE, payload).execute();
    }

    @Override
    public void onDownvote(int index) {
        if(!prefs.isAuthenticated()) {
            Toast.makeText(getContext(), "Please sign into first!", Toast.LENGTH_SHORT).show();
            return;
        }
        Review review = reviews.get(index);
        Toast.makeText(getContext(), review.getUser_rating() == 0?"Down voting review":"Removing previous rating", Toast.LENGTH_SHORT).show();
        Payload.ReviewActions payload = new Payload.ReviewActions(review.getId(), review.getUser_rating() == 0? 2:0);
        new RequestApiAction.ReviewBasedActions(getContext(), new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if(isVisible() && !isDetached() || !isRemoving())
                    if(response.isSuccessful() && response.body() != null)
                        makeReviewsRequest();
                    else
                        Toast.makeText(getContext(), ErrorHandler.getError(response).toString(), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                try {
                    Toast.makeText(getContext(), t.getCause().getMessage(), Toast.LENGTH_SHORT).show();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }, FilterTypes.ActionType.REVIEW_MANGA_RATE, payload).execute();
    }
}
