package com.mxt.anitrend.view.detail.fragment;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.AppCompatButton;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.github.johnpersano.supertoasts.library.Style;
import com.mxt.anitrend.R;
import com.mxt.anitrend.api.model.Favourite;
import com.mxt.anitrend.api.model.User;
import com.mxt.anitrend.api.model.UserSmall;
import com.mxt.anitrend.base.custom.view.widget.bottomsheet.BottomSheet;
import com.mxt.anitrend.base.custom.view.widget.bottomsheet.BottomSheetUsers;
import com.mxt.anitrend.util.KeyUtils;
import com.mxt.anitrend.api.structure.KeyValueTemp;
import com.mxt.anitrend.api.structure.UserStats;
import com.mxt.anitrend.base.custom.async.RequestApiAction;
import com.mxt.anitrend.base.custom.Payload;
import com.mxt.anitrend.base.custom.view.widget.PolygonView;
import com.mxt.anitrend.presenter.detail.UserProfilePresenter;
import com.mxt.anitrend.util.DialogManager;
import com.mxt.anitrend.util.PatternMatcher;
import com.mxt.anitrend.util.TransitionHelper;
import com.mxt.anitrend.view.base.activity.FavouriteActivity;
import com.mxt.anitrend.view.base.activity.GalleryPreviewActivity;
import com.mxt.anitrend.view.base.activity.ImagePreviewActivity;
import com.mxt.anitrend.view.base.activity.VideoPlayerActivity;
import com.nguyenhoanglam.progresslayout.ProgressLayout;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import de.hdodenhof.circleimageview.CircleImageView;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class UserAboutFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener, View.OnClickListener {

    private final static String BUNDLE_MODEL = "User_Value";

    private final static String MODEL_USER_KEY = "user_model_key";
    private final static String MODEL_FOLLOWING_KEY = "following_model_key";
    private final static String MODEL_FOLLOWERS_KEY = "followers_model_key";
    private final static String MODEL_FAVOURITES_KEY = "favourites_model_key";

    private final static String MODEL_LINKS_KEY = "images_about_user_key";
    private final static String MODEL_TYPES_KEY = "images_about_types_key";

    private User mModel;
    private List<String> mLinks, mTypes;

    private List<UserSmall> mFollowingList, mFollowersList;
    private Favourite mFavouritesList;

    private Integer mFollowing, mFollowers, mFavourites;

    private Unbinder unbinder;

    private BottomSheet mSheet;

    @BindView(R.id.extra_holder) View mContainer;
    @BindView(R.id.extra_img) ImageView mExtra;
    @BindView(R.id.extra_img_gallery) TextView mGallery;

    @BindView(R.id.user_followers_container) View mFollowersContainer;
    @BindView(R.id.user_following_container) View mFollowingContainer;
    @BindView(R.id.user_favourites_container) View mFavouritesContainer;

    @BindView(R.id.scrollProgressLayout) ProgressLayout mProgressLayout;
    @BindView(R.id.user_lists_pull_refresh) SwipeRefreshLayout swipeRefreshLayout;
    @BindView(R.id.user_profile_image) CircleImageView mProfile_Image;
    @BindView(R.id.user_name) TextView mUser_Name;
    @BindView(R.id.user_followers_count) TextView mUser_Followers;
    @BindView(R.id.user_following_count) TextView mUser_Following;
    @BindView(R.id.user_favourites_count) TextView mUser_Favourites;
    @BindView(R.id.user_description) TextView mUser_Description;
    @BindView(R.id.user_stats) PolygonView mUser_Stats;
    @BindView(R.id.user_follow_action) AppCompatButton mUser_Action;

    private UserProfilePresenter mPresenter;

    public UserAboutFragment() {
        // Required empty public constructor
    }

    public static UserAboutFragment newInstance(User model) {
        Bundle bundle = new Bundle();
        bundle.putParcelable(BUNDLE_MODEL, model);
        UserAboutFragment userAboutFragment = new UserAboutFragment();
        userAboutFragment.setArguments(bundle);
        return userAboutFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(getArguments() != null)
            mModel = getArguments().getParcelable(BUNDLE_MODEL);
        mPresenter = new UserProfilePresenter(getContext(), mModel.getId());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View root = inflater.inflate(R.layout.fragment_user_about, container, false);
        unbinder = ButterKnife.bind(this, root);
        swipeRefreshLayout.setOnRefreshListener(this);
        mProgressLayout.showLoading();

        mUser_Action.setOnClickListener(this);
        mProfile_Image.setOnClickListener(this);

        mContainer.setOnClickListener(this);
        mExtra.setOnClickListener(this);
        mGallery.setOnClickListener(this);

        mFollowersContainer.setOnClickListener(this);
        mFollowingContainer.setOnClickListener(this);
        mFavouritesContainer.setOnClickListener(this);

        if(savedInstanceState != null){
            mModel = savedInstanceState.getParcelable(MODEL_USER_KEY);
            mFavourites = savedInstanceState.getInt(MODEL_FAVOURITES_KEY);
            mFollowers = savedInstanceState.getInt(MODEL_FOLLOWERS_KEY);
            mFollowing = savedInstanceState.getInt(MODEL_FOLLOWING_KEY);
            mLinks = savedInstanceState.getStringArrayList(MODEL_LINKS_KEY);
            mTypes = savedInstanceState.getStringArrayList(MODEL_TYPES_KEY);

            mUser_Favourites.setText(String.valueOf(mFavourites));
            mUser_Followers.setText(String.valueOf(mFollowers));
            mUser_Following.setText(String.valueOf(mFollowing));
        } else {
            mUser_Favourites.setText("..");
            mUser_Followers.setText("..");
            mUser_Following.setText("..");
        }
        return root;
    }

    @Override
    public void onStart() {
        super.onStart();
        updateUI();
    }

    private void setActionFollowState() {
        if(mModel.getId() != mPresenter.getCurrentUser().getId()) {
            if(mModel.getFollowing()) {
                mUser_Action.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_close_white_24dp,0,0,0);
                mUser_Action.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.colorAccent));
                mUser_Action.setText(R.string.Unfollow);
            }
            else {
                mUser_Action.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_done_all_white_24dp,0,0,0);
                mUser_Action.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.colorStateBlue));
                mUser_Action.setText(R.string.Follow);
            }
            mUser_Action.setVisibility(View.VISIBLE);
        }
    }

    private void updateUI() {
        if(isVisible() && !isDetached() || !isRemoving()) {

            Glide.with(this).load(mModel.getImage_url_lge())
                 .centerCrop()
                 .diskCacheStrategy(DiskCacheStrategy.ALL)
                 .into(mProfile_Image);

            setActionFollowState();

            mUser_Description.setMovementMethod(LinkMovementMethod.getInstance());
            mUser_Description.setText(mModel.getAbout());
            mUser_Name.setText(mModel.getDisplay_name());

            if(mFavouritesList == null)
                mPresenter.requestFavourites(new Callback<Favourite>() {
                @Override
                public void onResponse(Call<Favourite> call, Response<Favourite> response) {
                    if(isVisible() && (!isRemoving() ||!isDetached())) {
                        if(response.isSuccessful() && response.body() != null) {
                            mFavouritesList = response.body();
                            mFavourites = mFavouritesList.getFavouritesCount();
                            try {
                                mUser_Favourites.setText(valueFormatter(mFavourites));
                            } catch (Exception ex){
                                ex.printStackTrace();
                            }
                        } else {
                            try {
                                mFavourites = 0;
                                mUser_Favourites.setText(valueFormatter(mFavourites));
                            } catch (Exception ex){
                                ex.printStackTrace();
                            }
                            if(response.errorBody() != null){
                                try {
                                    Toast.makeText(getContext(), response.errorBody().string(), Toast.LENGTH_LONG).show();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    }
                }

                @Override
                public void onFailure(Call<Favourite> call, Throwable t) {
                    t.printStackTrace();
                }
            });

            if(mFollowersList == null)
                mPresenter.requestFollowers(new Callback<List<UserSmall>>() {
                @Override
                public void onResponse(Call<List<UserSmall>> call, Response<List<UserSmall>> response) {
                    if (isVisible() && (!isRemoving() ||!isDetached())) {
                        if (response.isSuccessful() && response.body() != null) {
                            mFollowersList = response.body();
                            mFollowers = mFollowersList.size();
                            try {
                                mUser_Followers.setText(valueFormatter(mFollowers));
                            } catch (Exception ex) {
                                ex.printStackTrace();
                            }
                        } else {
                            try {
                                mFollowers = 0;
                                mUser_Followers.setText(valueFormatter(mFollowers));
                            } catch (Exception ex) {
                                ex.printStackTrace();
                            }
                            if (response.errorBody() != null) {
                                try {
                                    Toast.makeText(getContext(), response.errorBody().string(), Toast.LENGTH_LONG).show();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    }
                }

                @Override
                public void onFailure(Call<List<UserSmall>> call, Throwable t) {
                    if(isVisible() && (!isRemoving() ||!isDetached()))
                        Toast.makeText(getContext(), t.getLocalizedMessage(), Toast.LENGTH_LONG).show();
                }
            });

            if(mFollowingList == null)
                mPresenter.requestFollowing(new Callback<List<UserSmall>>() {
                public void onResponse(Call<List<UserSmall>> call, Response<List<UserSmall>> response) {
                    if(isVisible() && (!isRemoving() ||!isDetached())) {
                        if(response.isSuccessful() && response.body() != null) {
                            mFollowingList = response.body();
                            mFollowing = mFollowingList.size();
                            try {
                                mUser_Following.setText(valueFormatter(mFollowing));
                            } catch (Exception ex){
                                ex.printStackTrace();
                            }
                        } else {
                            try {
                                mFollowing = 0;
                                mUser_Following.setText(valueFormatter(mFollowing));
                            } catch (Exception ex){
                                ex.printStackTrace();
                            }
                            if(response.errorBody() != null){
                                try {
                                    Toast.makeText(getContext(), response.errorBody().string(), Toast.LENGTH_LONG).show();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    }
                }

                @Override
                public void onFailure(Call<List<UserSmall>> call, Throwable t) {
                    if(isVisible() && (!isRemoving() ||!isDetached()))
                        Toast.makeText(getContext(), t.getLocalizedMessage(), Toast.LENGTH_LONG).show();
                }
            });

            UserStats userStats = mModel.getStats();
            HashMap<String, Integer> genreList = null;
            if(userStats != null)
                genreList = userStats.getFavourite_genres_map();

            if(genreList != null && genreList.size() > 0) {
                final KeyValueTemp result = new KeyValueTemp(genreList.size()).setKeyVals(genreList);
                mUser_Stats.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        try {
                            mUser_Stats.initial(result.values.length, result.values, result.keys);
                            mUser_Stats.animateProgress();
                        } catch (Exception ex){
                            Toast.makeText(getActivity(), ex.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            } else
                mUser_Stats.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Toast.makeText(getActivity(), R.string.text_insufficient_genres, Toast.LENGTH_LONG).show();
                    }
                });

            if(mLinks == null) {
                Matcher matcher = PatternMatcher.findImages(mModel.getAboutRaw());
                mLinks = new ArrayList<>();
                mTypes = new ArrayList<>();

                while(matcher.find()) {
                    int gc = matcher.groupCount();
                    String tag = matcher.group(gc - 1);
                    String media = matcher.group(gc);
                    mTypes.add(tag);
                    if(tag.equals(PatternMatcher.KEY_YOU))
                        mLinks.add(PatternMatcher.buildYoutube(media.replace("(", "").replace(")", "")));
                    else
                        mLinks.add(media.replace("(", "").replace(")", ""));
                }
            }

            int mCount = mLinks.size();

            mContainer.setVisibility(mCount > 0 ? View.VISIBLE : View.GONE);
            if(mCount > 0) {
                mContainer.setVisibility(View.VISIBLE);
                boolean isVideo = !mTypes.get(0).equals(PatternMatcher.KEY_IMG);
                if(isVideo) {
                    mGallery.setText(R.string.text_play_video);
                    switch (mTypes.get(0)) {
                        case PatternMatcher.KEY_WEB:
                            Glide.with(getContext())
                                    .load(PatternMatcher.NO_THUMBNAIL)
                                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                                    .centerCrop()
                                    .into(mExtra);
                            break;
                        default:
                            Glide.with(getContext())
                                    .load(PatternMatcher.getYoutubeThumb(mLinks.get(0)))
                                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                                    .centerCrop()
                                    .into(mExtra);
                            break;
                    }
                }
                else {
                    Glide.with(getContext()).load(mLinks.get(0))
                            .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                            .centerCrop()
                            .into(mExtra);
                    mGallery.setText(R.string.text_multiple_images);
                }

                if (mCount > 1 || isVideo) {
                            mExtra.setVisibility(View.VISIBLE);
                            mGallery.setVisibility(View.VISIBLE);
                    }
                else {
                    mGallery.setVisibility(View.GONE);
                    mExtra.setVisibility(View.VISIBLE);
                }
            }

            mProgressLayout.showContent();
            swipeRefreshLayout.setRefreshing(false);
        }
    }

    private String valueFormatter(int size){
        if(size != 0){
            return size > 1000? String.format(Locale.getDefault(),"%.1f K", (float)size/1000): String.valueOf(size);
        }
        return "0";
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putParcelable(MODEL_USER_KEY, mModel);
        if(mFavourites != null)
            outState.putInt(MODEL_FAVOURITES_KEY, mFavourites);
        if(mFollowing != null)
            outState.putInt(MODEL_FOLLOWING_KEY, mFollowing);
        if(mFollowers != null)
            outState.putInt(MODEL_FOLLOWERS_KEY, mFollowers);
        if(mLinks != null)
            outState.putStringArrayList(MODEL_LINKS_KEY, (ArrayList<String>) mLinks);
        if(mTypes != null)
            outState.putStringArrayList(MODEL_TYPES_KEY, (ArrayList<String>) mTypes);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
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

    /**
     * Called when a swipe gesture triggers a refresh.
     */
    @Override
    public void onRefresh() {
        mFavourites = null;
        mFollowers = null;
        mFollowing = null;
        mUser_Favourites.setText("..");
        mUser_Followers.setText("..");
        mUser_Following.setText("..");
        updateUI();
    }

    void showSlide() {
        if(mLinks.size() > 1) {
            Intent preview = new Intent(getContext(), GalleryPreviewActivity.class);
            preview.putStringArrayListExtra(GalleryPreviewActivity.PARAM_TYPE_LIST, (ArrayList<String>) mTypes);
            preview.putStringArrayListExtra(GalleryPreviewActivity.PARAM_IMAGE_LIST, (ArrayList<String>) mLinks);
            startActivity(preview);
        } else {
            Intent intent = new Intent(getActivity(), ImagePreviewActivity.class);
            intent.putExtra(ImagePreviewActivity.IMAGE_SOURCE, mLinks.get(0));
            startActivity(intent);
        }
    }

    void handleAction() {
        Intent intent;
        if (this.mLinks.size() > 1) {
            showSlide();
            return;
        }

        if (mTypes.get(0).equals(PatternMatcher.KEY_IMG)) {
            intent = new Intent(getContext(), ImagePreviewActivity.class);
            intent.putExtra(ImagePreviewActivity.IMAGE_SOURCE, mLinks.get(0));
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            return;
        }
        switch (mTypes.get(0)) {
            case PatternMatcher.KEY_WEB:
                intent = new Intent(getContext(), VideoPlayerActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.putExtra(VideoPlayerActivity.URL_VIDEO_LINK, mLinks.get(0));
                break;
            default:
                intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse(mLinks.get(0)));
                break;
        }
        startActivity(intent);
    }

    private void toggleFollowState() {
        Payload.ActionIdBased payload = new Payload.ActionIdBased(mModel.getId());
        RequestApiAction.IdActions action = new RequestApiAction.IdActions(getContext(), new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if(response.isSuccessful()) {
                    if(isVisible() && !isRemoving() || !isDetached()) {
                        mModel.setFollowing();
                        setActionFollowState();
                    }
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                if(isVisible() && !isRemoving() || !isDetached()) {
                    t.printStackTrace();
                    Toast.makeText(getContext(), t.getLocalizedMessage(), Toast.LENGTH_LONG).show();
                }
            }
        }, KeyUtils.ActionType.ACTION_FOLLOW_TOGGLE, payload);
        action.execute();
        mPresenter.createSuperToast(getActivity(), getString(R.string.text_sending_request), R.drawable.ic_info_outline_white_18dp, Style.TYPE_PROGRESS_BAR);
    }

    @Override
    public void onClick(View v) {
        Intent intent;
        switch (v.getId()) {
            case R.id.extra_img_gallery:
                handleAction();
                return;
            case R.id.extra_img:
                handleAction();
                return;
            case R.id.user_profile_image:
                intent = new Intent(getActivity(), ImagePreviewActivity.class);
                intent.putExtra(ImagePreviewActivity.IMAGE_SOURCE, mModel.getImage_url_lge());
                TransitionHelper.startSharedImageTransition(getActivity(), v, getString(R.string.transition_image_preview), intent);
                break;
            case R.id.user_follow_action:
                if(mModel.getFollowing())
                    new DialogManager(getContext()).createDialogMessage(getString(R.string.dialog_confirm_delete),
                            getString(R.string.dialog_unfollow_message),
                            getString(R.string.Yes),
                            getString(R.string.No),
                            new MaterialDialog.SingleButtonCallback() {
                                @Override
                                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                    switch (which) {
                                        case POSITIVE:
                                            toggleFollowState();
                                            break;
                                        case NEUTRAL:
                                            break;
                                        case NEGATIVE:
                                            dialog.dismiss();
                                            break;
                                    }
                                }
                            });
                else
                    toggleFollowState();
                break;
            case R.id.user_followers_container:
                if(mFollowersList == null || mFollowers == null)
                    mPresenter.createSuperToast(getActivity(), getString(R.string.text_activity_loading), R.drawable.ic_info_outline_white_18dp, Style.TYPE_STANDARD);
                else
                    if(mFollowers > 0) {
                        mSheet = BottomSheetUsers.newInstance(getString(R.string.title_bottom_sheet_followers, mFollowers), mFollowersList);
                        mSheet.show(getFragmentManager(), mSheet.getTag());
                    }
                    else
                        mPresenter.createSuperToast(getActivity(), getString(R.string.layout_empty_response), R.drawable.ic_info_outline_white_18dp, Style.TYPE_STANDARD);
                break;
            case R.id.user_following_container:
                if(mFollowingList == null || mFollowing == null)
                    mPresenter.createSuperToast(getActivity(), getString(R.string.text_activity_loading), R.drawable.ic_info_outline_white_18dp, Style.TYPE_STANDARD);
                else
                    if(mFollowing > 0) {
                        mSheet = BottomSheetUsers.newInstance(getString(R.string.title_bottom_sheet_following, mFollowing), mFollowingList);
                        mSheet.show(getFragmentManager(), mSheet.getTag());
                    }
                    else
                        mPresenter.createSuperToast(getActivity(), getString(R.string.layout_empty_response), R.drawable.ic_info_outline_white_18dp, Style.TYPE_STANDARD);
                break;
            case R.id.user_favourites_container:
                if(mFavouritesList == null)
                    mPresenter.createSuperToast(getActivity(), getString(R.string.text_activity_loading), R.drawable.ic_info_outline_white_18dp, Style.TYPE_STANDARD);
                else {
                    intent = new Intent(getActivity(), FavouriteActivity.class);
                    intent.putExtra(FavouriteActivity.FAVOURITES_PARAM, mFavouritesList);
                    startActivity(intent);
                }
                break;
        }
    }
}
