package com.mxt.anitrend.view.fragment.youtube;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.request.RequestOptions;
import com.mxt.anitrend.R;
import com.mxt.anitrend.base.custom.fragment.FragmentBase;
import com.mxt.anitrend.databinding.AdapterFeedSlideBinding;
import com.mxt.anitrend.model.entity.anilist.meta.MediaTrailer;
import com.mxt.anitrend.presenter.base.BasePresenter;
import com.mxt.anitrend.util.KeyUtil;
import com.mxt.anitrend.util.NotifyUtil;
import com.mxt.anitrend.util.RegexUtil;

import butterknife.ButterKnife;

public class YouTubeEmbedFragment extends FragmentBase<MediaTrailer, BasePresenter, MediaTrailer> {

    private MediaTrailer mediaTrailer;

    private AdapterFeedSlideBinding binding;

    public static YouTubeEmbedFragment newInstance(MediaTrailer model) {
        Bundle args = new Bundle();
        args.putParcelable(KeyUtil.arg_media_trailer, model);
        YouTubeEmbedFragment fragment = new YouTubeEmbedFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(getArguments() != null)
            mediaTrailer = getArguments().getParcelable(KeyUtil.arg_media_trailer);
        setPresenter(new BasePresenter(getContext()));
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
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = AdapterFeedSlideBinding.inflate(inflater, container, false);
        unbinder = ButterKnife.bind(this, binding.getRoot());
        return binding.getRoot();
    }

    @Override
    public void onResume() {
        super.onResume();
        makeRequest();
    }

    /**
     * Is automatically called in the @onStart Method if overridden in list implementation
     */
    @Override
    protected void updateUI() {
        String youtubeLink = RegexUtil.INSTANCE.buildYoutube(mediaTrailer.getId());
        String thumbnailUrl = RegexUtil.INSTANCE.getYoutubeThumb(youtubeLink);
        if (getActivity() != null)
            Glide.with(getActivity()).load(thumbnailUrl)
                    .transition(DrawableTransitionOptions.withCrossFade(250))
                    .apply(RequestOptions.centerCropTransform())
                    .into(binding.feedStatusImage);
    }

    /**
     * All new or updated network requests should be handled in this method
     */
    @Override
    public void makeRequest() {
        binding.setOnClickListener((v) -> {
            try {
                String youtubeLink = RegexUtil.INSTANCE.buildYoutube(mediaTrailer.getId());
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse(youtubeLink));
                startActivity(intent);
            } catch (ActivityNotFoundException e) {
                e.printStackTrace();
                NotifyUtil.makeText(getContext(), R.string.init_youtube_missing, Toast.LENGTH_SHORT).show();
            }
        });
        updateUI();
    }

    /**
     * Called when the model state is changed.
     *
     * @param model The new data
     */
    @Override
    public void onChanged(@Nullable MediaTrailer model) {

    }
}
