package com.mxt.anitrend.util;

import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Typeface;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputEditText;
import android.text.Html;
import android.text.InputType;
import android.text.Spanned;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.Theme;
import com.mxt.anitrend.R;
import com.mxt.anitrend.api.model.Series;
import com.mxt.anitrend.api.structure.Anime;
import com.mxt.anitrend.api.structure.ListItem;
import com.mxt.anitrend.api.structure.Manga;
import com.mxt.anitrend.async.RequestApiAction;
import com.mxt.anitrend.custom.Payload;
import com.mxt.anitrend.custom.event.RemoteChangeListener;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;

import it.gmariotti.changelibs.library.view.ChangeLogRecyclerView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.mxt.anitrend.util.KeyUtils.ActionType.ANIME_LIST_ADD;
import static com.mxt.anitrend.util.KeyUtils.ActionType.ANIME_LIST_DELETE;
import static com.mxt.anitrend.util.KeyUtils.ActionType.ANIME_LIST_EDIT;
import static com.mxt.anitrend.util.KeyUtils.ActionType.MANGA_LIST_ADD;
import static com.mxt.anitrend.util.KeyUtils.ActionType.MANGA_LIST_DELETE;
import static com.mxt.anitrend.util.KeyUtils.ActionType.MANGA_LIST_EDIT;
import static com.mxt.anitrend.util.KeyUtils.AnimeStatusTypes;
import static com.mxt.anitrend.util.KeyUtils.FINISHED_AIRING;
import static com.mxt.anitrend.util.KeyUtils.FINISHED_PUBLISHING;
import static com.mxt.anitrend.util.KeyUtils.MangaStatusTypes;
import static com.mxt.anitrend.util.KeyUtils.NOT_YET_AIRED;
import static com.mxt.anitrend.util.KeyUtils.NOT_YET_PUBLISHED;
import static com.mxt.anitrend.util.KeyUtils.UserAnimeStatus;
import static com.mxt.anitrend.util.KeyUtils.UserMangaStatus;

/**
 * Created by Maxwell on 10/31/2015.
 */
public class DialogManager {

    private ApplicationPrefs app_prefs;
    private ApiPreferences api_prefs;
    private final ProgressDialog progress;
    private Context mContext;

    /**
     * Note: This constructor is used by more than one class
     * <br/>
     * @param mContext any valid application context
     */
    public DialogManager(Context mContext) {
        this.mContext = mContext;
        app_prefs = new ApplicationPrefs(mContext);
        api_prefs = new ApiPreferences(mContext);
        progress = new ProgressDialog(mContext);
        progress.setMessage(mContext.getString(R.string.text_processing_request));
    }

    private String getAnimeTitle(Anime anime) {
        switch (api_prefs.getTitleLanguage()) {
            case "romaji":
                return anime.getTitle_romaji();
            case "english":
                return anime.getTitle_english();
            case "japanese":
                return anime.getTitle_japanese();
        }
        return "";
    }

    private String getMangaTitle(Manga manga) {
        switch (api_prefs.getTitleLanguage()) {
            case "romaji":
                return manga.getTitle_romaji();
            case "english":
                return manga.getTitle_english();
            case "japanese":
                return manga.getTitle_japanese();
        }
        return "";
    }

    private String getSeriesTitle(Series series) {
        switch (api_prefs.getTitleLanguage()) {
            case "romaji":
                return series.getTitle_romaji();
            case "english":
                return series.getTitle_english();
            case "japanese":
                return series.getTitle_japanese();
        }
        return "";
    }

    /**
     * Automatically updates the episode count without opening a dialog
     */
    public void episodeAutoUpdate(ListItem item, final RemoteChangeListener mListener) {

        if(item != null) {
            int episodes = item.getEpisodes_watched() + 1;
            int total_episodes = item.getAnime().getTotal_episodes();
            
            if(item.getAnime() != null && (total_episodes != 0 && total_episodes < episodes)) {
                Toast.makeText(mContext, R.string.text_unable_to_increment_episodes, Toast.LENGTH_LONG).show();
                return;
            }

            Payload.ListAnimeAction animeAction = new Payload.ListAnimeAction();
            if(episodes == total_episodes) {
                animeAction.setList_status(UserAnimeStatus[KeyUtils.COMPLETED]);
            } else {
                animeAction.setList_status(item.getList_status());
            }
            animeAction.setId(item.getSeries_id());
            animeAction.setEpisodes_watched(episodes);
            animeAction.setHidden_default(item.getHidden_default());
            animeAction.setNotes(item.getNotes());
            animeAction.setRewatched(item.getRewatched());
            animeAction.setScore(item.getScore());
            animeAction.setScore_raw(item.getScore_raw());

            progress.show();
            RequestApiAction.AnimeListActions userPostActions = new RequestApiAction.AnimeListActions(mContext, ANIME_LIST_EDIT, animeAction, new Callback<Object>() {
                @Override
                public void onResponse(Call<Object> call, Response<Object> response) {
                    if (response.errorBody() == null) try {
                        progress.dismiss();
                        mListener.onResultSuccess();
                        Toast.makeText(mContext, R.string.text_changes_saved, Toast.LENGTH_SHORT).show();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    else try {
                        progress.dismiss();
                        Toast.makeText(mContext, response.errorBody().string(), Toast.LENGTH_SHORT).show();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onFailure(Call<Object> call, Throwable t) {
                    Toast.makeText(mContext, t.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                    progress.dismiss();
                }
            });
            userPostActions.execute();
        } else {
            Toast.makeText(mContext, R.string.dialog_action_null, Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Automatically updates the chapter count without opening a dialog
     */
    public void chapterAutoUpdate(ListItem item, final RemoteChangeListener mListener) {
        if(item != null) {
            int chapters_read = item.getChapters_read() + 1;
            int total_chapters = item.getManga().getTotal_chapters();

            if(item.getManga() != null && (total_chapters != 0 && total_chapters < chapters_read)) {
                Toast.makeText(mContext, R.string.text_unable_to_increment_chapters, Toast.LENGTH_LONG).show();
                return;
            }

            Payload.ListMangaAction mangaAction = new Payload.ListMangaAction();
            if(chapters_read == total_chapters) {
                mangaAction.setList_status(UserMangaStatus[KeyUtils.COMPLETED]);
            } else {
                mangaAction.setList_status(item.getList_status());
            }
            mangaAction.setId(item.getSeries_id());
            mangaAction.setChapters_read(chapters_read);
            mangaAction.setVolumes_read(item.getVolumes_read());
            mangaAction.setHidden_default(item.getHidden_default());
            mangaAction.setNotes(item.getNotes());
            mangaAction.setScore(item.getScore());
            mangaAction.setScore_raw(item.getScore_raw());

            progress.show();
            RequestApiAction.MangaListActions userPostActions = new RequestApiAction.MangaListActions(mContext, MANGA_LIST_EDIT, mangaAction, new Callback<Object>() {
                @Override
                public void onResponse(Call<Object> call, Response<Object> response) {
                    if (response.errorBody() == null) try {
                        progress.dismiss();
                        mListener.onResultSuccess();
                        Toast.makeText(mContext, R.string.text_changes_saved, Toast.LENGTH_SHORT).show();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    else try {
                        progress.dismiss();
                        Toast.makeText(mContext, response.errorBody().string(), Toast.LENGTH_LONG).show();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onFailure(Call<Object> call, Throwable t) {
                    Toast.makeText(mContext, t.getLocalizedMessage(), Toast.LENGTH_LONG).show();
                    progress.dismiss();
                }
            });
            userPostActions.execute();
        } else {
            Toast.makeText(mContext, R.string.dialog_action_null, Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Only to be used by users anime lists
     */
    public void animeEditDialogSmall(final ListItem item, final RemoteChangeListener mListener) {
        final Payload.ListAnimeAction animeAction = new Payload.ListAnimeAction();
        progress.setIcon(R.drawable.ic_border_color_blue_600_24dp);
        MaterialDialog.Builder md = new MaterialDialog.Builder(mContext)
                .typeface(Typeface.SANS_SERIF,Typeface.SANS_SERIF)
                .title(Html.fromHtml(mContext.getString(R.string.dialog_edit_title, getAnimeTitle(item.getAnime()))))
                .autoDismiss(false)
                .iconRes(R.drawable.ic_border_color_blue_600_24dp)
                .customView(R.layout.dialog_listing_action_anime, true)
                .buttonRippleColorRes(R.color.colorAccent)
                .positiveColorRes(R.color.colorStateBlue)
                .negativeColorRes(R.color.colorStateOrange)
                .theme(app_prefs.isLightTheme()?Theme.LIGHT:Theme.DARK)
                .positiveText(R.string.Update)
                .neutralText(R.string.Cancel)
                .negativeText(R.string.Delete);

        MaterialDialog matD = md.build();

        final EditText mRating = (EditText) matD.findViewById(R.id.dia_current_score);
        final Spinner mStatus = (Spinner) matD.findViewById(R.id.dia_current_status);
        final EditText mProgress = (EditText) matD.findViewById(R.id.dia_current_progress);
        final CheckBox mPrivate = (CheckBox) matD.findViewById(R.id.dia_current_privacy);
        final TextView mIncrement = (TextView) matD.findViewById(R.id.dia_current_progress_increment);
        final EditText mRewatch = (EditText) matD.findViewById(R.id.dia_current_rewatch);
        final TextInputEditText mNotes = (TextInputEditText) matD.findViewById(R.id.dia_current_notes);

        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(mContext, R.array.anime_listing_status, R.layout.adapter_spinner_item);
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        mStatus.setAdapter(adapter);

        animeAction.setId(item.getSeries_id());
        animeAction.setScore_raw(item.getScore_raw());
        animeAction.setEpisodes_watched(item.getEpisodes_watched());
        animeAction.setRewatched(item.getRewatched());
        animeAction.setNotes(item.getNotes());
        animeAction.setHidden_default(item.getHidden_default());
        animeAction.setList_status(item.getList_status());

        mStatus.setSelection(Arrays.asList(UserAnimeStatus).indexOf(item.getList_status()),true);
        mPrivate.setChecked(item.isPrivate());
        if(item.getScore_raw() != 0)
            mRating.setText(String.valueOf(item.getScore_raw()));
        if(item.getEpisodes_watched() != 0)
            mProgress.setText(String.valueOf(item.getEpisodes_watched()));
        if(item.getRewatched() != 0)
            mRewatch.setText(String.valueOf(item.getRewatched()));
        if(item.getNotes() != null)
            mNotes.setText(item.getNotes());

        mIncrement.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int current = animeAction.getEpisodes_watched() != null ?animeAction.getEpisodes_watched()+1:1;
                animeAction.setEpisodes_watched(current);
                mProgress.setText(String.valueOf(current));
            }
        });

        mPrivate.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                animeAction.setHidden_default(b ? 1 : 0);
            }
        });

        mStatus.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                animeAction.setList_status(UserAnimeStatus[i]);
                //"watching","plan to watch","completed","on hold","dropped"
                    switch (UserAnimeStatus[i]) {
                        case "watching":
                            if (item.getAnime().getAiring_status().equals(AnimeStatusTypes[NOT_YET_AIRED]))
                                Toast.makeText(mContext, R.string.warning_anime_not_airing, Toast.LENGTH_LONG).show();
                            break;
                        case "plan to watch":
                            break;
                        case "completed":
                            if (!item.getAnime().getAiring_status().equals(AnimeStatusTypes[FINISHED_AIRING])) {
                                Toast.makeText(mContext, R.string.warning_anime_is_airing, Toast.LENGTH_LONG).show();
                            } else {
                                int total = item.getAnime().getTotal_episodes();
                                mProgress.setText(String.valueOf(total));
                            }
                            break;
                        case "on hold":
                            if (item.getAnime().getAiring_status().equals(AnimeStatusTypes[NOT_YET_AIRED]))
                                Toast.makeText(mContext, R.string.warning_anime_not_airing, Toast.LENGTH_LONG).show();
                            break;
                        case "dropped":
                            if (item.getAnime().getAiring_status().equals(AnimeStatusTypes[NOT_YET_AIRED]))
                                Toast.makeText(mContext, R.string.warning_anime_not_airing, Toast.LENGTH_LONG).show();
                            break;
                    }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        md.onNeutral(new MaterialDialog.SingleButtonCallback() {
            @Override
            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                dialog.dismiss();
            }
        });

        md.onNegative(new MaterialDialog.SingleButtonCallback() {
            @Override
            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                //Destination Fucked :'D if you did this by mistake
                progress.show();
                final MaterialDialog dia = dialog;
                RequestApiAction.AnimeListActions userPostActions = new RequestApiAction.AnimeListActions(mContext, ANIME_LIST_DELETE, animeAction, new Callback<Object>() {
                    @Override
                    public void onResponse(Call<Object> call, Response<Object> response) {
                        if (response.errorBody() == null) try {
                            dia.dismiss();
                            progress.dismiss();
                            mListener.onResultSuccess();
                            Toast.makeText(mContext, R.string.completed_success, Toast.LENGTH_SHORT).show();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        else try {
                                progress.dismiss();
                                Toast.makeText(mContext, response.errorBody().string(), Toast.LENGTH_SHORT).show();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                    }

                    @Override
                    public void onFailure(Call<Object> call, Throwable t) {
                        Toast.makeText(mContext, t.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                        progress.dismiss();
                    }
                });
                userPostActions.execute();
            }
        });

        md.onPositive(new MaterialDialog.SingleButtonCallback() {
            @Override
            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                progress.show();
                animeAction.setEpisodes_watched(mProgress.getText().length() > 0? Integer.valueOf(mProgress.getText().toString()): 0);
                animeAction.setRewatched(mRewatch.getText().length() > 0? Integer.valueOf(mRewatch.getText().toString()): 0);
                animeAction.setScore_raw(mRating.getText().length() > 0? Integer.valueOf(mRating.getText().toString()): 0);
                animeAction.setNotes(mNotes.getText().toString());
                animeAction.setList_status(UserAnimeStatus[mStatus.getSelectedItemPosition()]);

                final MaterialDialog dia = dialog;
                RequestApiAction.AnimeListActions userPostActions = new RequestApiAction.AnimeListActions(mContext, ANIME_LIST_EDIT, animeAction, new Callback<Object>() {
                    @Override
                    public void onResponse(Call<Object> call, Response<Object> response) {
                        if (response.errorBody() == null) try {
                            dia.dismiss();
                            progress.dismiss();
                            mListener.onResultSuccess();
                            Toast.makeText(mContext, R.string.completed_success, Toast.LENGTH_SHORT).show();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        else try {
                                progress.dismiss();
                                Toast.makeText(mContext, response.errorBody().string(), Toast.LENGTH_SHORT).show();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                    }

                    @Override
                    public void onFailure(Call<Object> call, Throwable t) {
                        Toast.makeText(mContext, t.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                        progress.dismiss();
                    }
                });
                userPostActions.execute();
            }
        }).show();
    }

    /**
     * Only to be used by users anime lists
     */
    public void animeAddDialogSmall(@NonNull final Series item) {
        final Payload.ListAnimeAction animeAction = new Payload.ListAnimeAction();
        progress.setIcon(R.drawable.ic_fiber_new_blue_600_24dp);

        MaterialDialog.Builder md = new MaterialDialog.Builder(mContext)
                .typeface(Typeface.SANS_SERIF,Typeface.SANS_SERIF)
                .title(Html.fromHtml(mContext.getString(R.string.dialog_add_title, getSeriesTitle(item))))
                .autoDismiss(false)
                .iconRes(R.drawable.ic_fiber_new_blue_600_24dp)
                .customView(R.layout.dialog_listing_action_anime, true)
                .buttonRippleColorRes(R.color.colorAccent)
                .positiveColorRes(R.color.colorStateBlue)
                .theme(app_prefs.isLightTheme()?Theme.LIGHT:Theme.DARK)
                .positiveText(R.string.Add)
                .neutralText(R.string.Cancel);

        MaterialDialog matD = md.build();

        final EditText mRating = (EditText) matD.findViewById(R.id.dia_current_score);
        final Spinner mStatus = (Spinner) matD.findViewById(R.id.dia_current_status);
        final EditText mProgress = (EditText) matD.findViewById(R.id.dia_current_progress);
        final CheckBox mPrivate = (CheckBox) matD.findViewById(R.id.dia_current_privacy);
        final TextView mIncrement = (TextView) matD.findViewById(R.id.dia_current_progress_increment);
        final EditText mRewatch = (EditText) matD.findViewById(R.id.dia_current_rewatch);
        final TextInputEditText mNotes = (TextInputEditText) matD.findViewById(R.id.dia_current_notes);

        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(mContext, R.array.anime_listing_status, R.layout.adapter_spinner_item);
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        mStatus.setAdapter(adapter);

        animeAction.setId(item.getId());

        mIncrement.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int current = animeAction.getEpisodes_watched() != null ?animeAction.getEpisodes_watched()+1:1;
                animeAction.setEpisodes_watched(current);
                mProgress.setText(String.valueOf(current));
            }
        });

        mPrivate.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                animeAction.setHidden_default(b ? 1 : 0);
            }
        });

        mStatus.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                animeAction.setList_status(UserAnimeStatus[i]);
                //"watching","plan to watch","completed","on hold","dropped"
                switch (UserAnimeStatus[i]) {
                    case "watching":
                        /*if (item.getAiring_status().equals(AnimeStatusTypes[KeyUtils.AnimeStatusType.CURRENTLY_AIRING.ordinal()])) {
                            if(item.getNextAiring() != null) {
                                int total = item.getNextAiring().getNext_episode() - 1;
                                mProgress.setText(String.valueOf(total));
                            }
                        }*/
                        break;
                    case "plan to watch":
                        break;
                    case "completed":
                        if (!item.getAiring_status().equals(AnimeStatusTypes[FINISHED_AIRING])) {
                            Toast.makeText(mContext, R.string.warning_anime_is_airing, Toast.LENGTH_SHORT).show();
                        } else {
                            int total = item.getTotal_episodes();
                            mProgress.setText(String.valueOf(total));
                        }
                        break;
                    case "on hold":
                        if (item.getAiring_status().equals(AnimeStatusTypes[NOT_YET_AIRED]))
                            Toast.makeText(mContext, R.string.warning_anime_not_airing, Toast.LENGTH_LONG).show();
                        break;
                    case "dropped":
                        if (item.getAiring_status().equals(AnimeStatusTypes[NOT_YET_AIRED]))
                            Toast.makeText(mContext, R.string.warning_anime_not_airing, Toast.LENGTH_LONG).show();
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        md.onNeutral(new MaterialDialog.SingleButtonCallback() {
            @Override
            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                dialog.dismiss();
            }
        });

        md.onPositive(new MaterialDialog.SingleButtonCallback() {
            @Override
            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                progress.show();
                animeAction.setEpisodes_watched(mProgress.getText().length() > 0? Integer.valueOf(mProgress.getText().toString()): null);
                animeAction.setRewatched(mRewatch.getText().length() > 0? Integer.valueOf(mRewatch.getText().toString()): null);
                animeAction.setScore_raw(mRating.getText().length() > 0? Integer.valueOf(mRating.getText().toString()): null);
                animeAction.setNotes(mNotes.getText().toString());
                animeAction.setList_status(UserAnimeStatus[mStatus.getSelectedItemPosition()]);
                final MaterialDialog dia = dialog;
                RequestApiAction.AnimeListActions userPostActions = new RequestApiAction.AnimeListActions(mContext, ANIME_LIST_ADD, animeAction, new Callback<Object>() {
                    @Override
                    public void onResponse(Call<Object> call, Response<Object> response) {
                        if (response.errorBody() == null) {
                            dia.dismiss();
                            progress.dismiss();
                            Toast.makeText(mContext, R.string.text_changes_saved, Toast.LENGTH_SHORT).show();
                        } else
                            try {
                                progress.dismiss();
                                Toast.makeText(mContext, response.errorBody().string(), Toast.LENGTH_LONG).show();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                    }

                    @Override
                    public void onFailure(Call<Object> call, Throwable t) {
                        Toast.makeText(mContext, t.getLocalizedMessage(), Toast.LENGTH_LONG).show();
                        progress.dismiss();
                    }
                });
                userPostActions.execute();
            }
        }).show();
    }

    /**
     * Only to be used by users anime lists when browsing an external users lists
     */
    public void animeAddDialogSmall(@NonNull final Anime item) {
        final Payload.ListAnimeAction animeAction = new Payload.ListAnimeAction();
        progress.setIcon(R.drawable.ic_fiber_new_blue_600_24dp);

        MaterialDialog.Builder md = new MaterialDialog.Builder(mContext)
                .typeface(Typeface.SANS_SERIF,Typeface.SANS_SERIF)
                .title(Html.fromHtml(mContext.getString(R.string.dialog_add_title, getAnimeTitle(item))))
                .autoDismiss(false)
                .iconRes(R.drawable.ic_fiber_new_blue_600_24dp)
                .customView(R.layout.dialog_listing_action_anime, true)
                .buttonRippleColorRes(R.color.colorAccent)
                .positiveColorRes(R.color.colorStateBlue)
                .theme(app_prefs.isLightTheme()?Theme.LIGHT:Theme.DARK)
                .positiveText(R.string.Add)
                .neutralText(R.string.Cancel);

        MaterialDialog matD = md.build();

        final EditText mRating = (EditText) matD.findViewById(R.id.dia_current_score);
        final Spinner mStatus = (Spinner) matD.findViewById(R.id.dia_current_status);
        final EditText mProgress = (EditText) matD.findViewById(R.id.dia_current_progress);
        final CheckBox mPrivate = (CheckBox) matD.findViewById(R.id.dia_current_privacy);
        final TextView mIncrement = (TextView) matD.findViewById(R.id.dia_current_progress_increment);
        final EditText mRewatch = (EditText) matD.findViewById(R.id.dia_current_rewatch);
        final TextInputEditText mNotes = (TextInputEditText) matD.findViewById(R.id.dia_current_notes);

        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(mContext, R.array.anime_listing_status, R.layout.adapter_spinner_item);
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        mStatus.setAdapter(adapter);

        animeAction.setId(item.getId());

        mIncrement.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int current = animeAction.getEpisodes_watched() != null ?animeAction.getEpisodes_watched()+1:1;
                animeAction.setEpisodes_watched(current);
                mProgress.setText(String.valueOf(current));
            }
        });

        mPrivate.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                animeAction.setHidden_default(b ? 1 : 0);
            }
        });

        mStatus.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                animeAction.setList_status(UserAnimeStatus[i]);
                //"watching","plan to watch","completed","on hold","dropped"
                switch (UserAnimeStatus[i]) {
                    case "watching":
                        /*if (item.getAiring_status().equals(AnimeStatusTypes[KeyUtils.AnimeStatusType.CURRENTLY_AIRING.ordinal()])) {
                            if(item.getNextAiring() != null) {
                                int total = item.getNextAiring().getNext_episode() - 1;
                                mProgress.setText(String.valueOf(total));
                            }
                        }*/
                        break;
                    case "plan to watch":
                        break;
                    case "completed":
                        if (!item.getAiring_status().equals(AnimeStatusTypes[FINISHED_AIRING])) {
                            Toast.makeText(mContext, R.string.warning_anime_is_airing, Toast.LENGTH_SHORT).show();
                        } else {
                            int total = item.getTotal_episodes();
                            mProgress.setText(String.valueOf(total));
                        }
                        break;
                    case "on hold":
                        if (item.getAiring_status().equals(AnimeStatusTypes[NOT_YET_AIRED]))
                            Toast.makeText(mContext, R.string.warning_anime_not_airing, Toast.LENGTH_LONG).show();
                        break;
                    case "dropped":
                        if (item.getAiring_status().equals(AnimeStatusTypes[NOT_YET_AIRED]))
                            Toast.makeText(mContext, R.string.warning_anime_not_airing, Toast.LENGTH_LONG).show();
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        md.onNeutral(new MaterialDialog.SingleButtonCallback() {
            @Override
            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                dialog.dismiss();
            }
        });

        md.onPositive(new MaterialDialog.SingleButtonCallback() {
            @Override
            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                progress.show();
                animeAction.setEpisodes_watched(mProgress.getText().length() > 0? Integer.valueOf(mProgress.getText().toString()): null);
                animeAction.setRewatched(mRewatch.getText().length() > 0? Integer.valueOf(mRewatch.getText().toString()): null);
                animeAction.setScore_raw(mRating.getText().length() > 0? Integer.valueOf(mRating.getText().toString()): null);
                animeAction.setNotes(mNotes.getText().toString());
                animeAction.setList_status(UserAnimeStatus[mStatus.getSelectedItemPosition()]);
                final MaterialDialog dia = dialog;
                RequestApiAction.AnimeListActions userPostActions = new RequestApiAction.AnimeListActions(mContext, ANIME_LIST_ADD, animeAction, new Callback<Object>() {
                    @Override
                    public void onResponse(Call<Object> call, Response<Object> response) {
                        if (response.errorBody() == null) {
                            dia.dismiss();
                            progress.dismiss();
                            Toast.makeText(mContext, R.string.text_changes_saved, Toast.LENGTH_SHORT).show();
                        } else
                            try {
                                progress.dismiss();
                                Toast.makeText(mContext, response.errorBody().string(), Toast.LENGTH_LONG).show();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                    }

                    @Override
                    public void onFailure(Call<Object> call, Throwable t) {
                        Toast.makeText(mContext, t.getLocalizedMessage(), Toast.LENGTH_LONG).show();
                        progress.dismiss();
                    }
                });
                userPostActions.execute();
            }
        }).show();
    }

    /**
     * Only to be used by users manga lists
     */
    public void mangaEditDialogSmall(final ListItem item, final RemoteChangeListener mListener) {
        final Payload.ListMangaAction mangaAction = new Payload.ListMangaAction();
        progress.setIcon(R.drawable.ic_border_color_teal_600_24dp);
        MaterialDialog.Builder md = new MaterialDialog.Builder(mContext)
                .typeface(Typeface.SANS_SERIF,Typeface.SANS_SERIF)
                .title(Html.fromHtml(mContext.getString(R.string.dialog_edit_title, getMangaTitle(item.getManga()))))
                .autoDismiss(false)
                .iconRes(R.drawable.ic_border_color_teal_600_24dp)
                .customView(R.layout.dialog_listing_action_manga, true)
                .buttonRippleColorRes(R.color.colorAccent)
                .positiveColorRes(R.color.colorStateBlue)
                .negativeColorRes(R.color.colorStateOrange)
                .theme(app_prefs.isLightTheme()?Theme.LIGHT:Theme.DARK)
                .positiveText(R.string.Update)
                .neutralText(R.string.Cancel)
                .negativeText(R.string.Delete);

        MaterialDialog matD = md.build();

        final EditText mRating = (EditText) matD.findViewById(R.id.dia_current_score);
        final Spinner mStatus = (Spinner) matD.findViewById(R.id.dia_current_status);
        final EditText mVolumes = (EditText) matD.findViewById(R.id.dia_current_volumes);
        final EditText mChapters = (EditText) matD.findViewById(R.id.dia_current_chapters);
        final EditText mReread = (EditText) matD.findViewById(R.id.dia_current_reread);
        final TextInputEditText mNotes = (TextInputEditText) matD.findViewById(R.id.dia_current_notes);
        final CheckBox mPrivate = (CheckBox) matD.findViewById(R.id.dia_current_privacy);
        final TextView mIncrement = (TextView) matD.findViewById(R.id.dia_current_progress_increment);

        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(mContext, R.array.manga_listing_status, R.layout.adapter_spinner_item);
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        mStatus.setAdapter(adapter);

        mangaAction.setId(item.getSeries_id());
        mangaAction.setScore_raw(item.getScore_raw());
        mangaAction.setChapters_read(item.getChapters_read());
        mangaAction.setVolumes_read(item.getVolumes_read());
        mangaAction.setReread(item.getReread());
        mangaAction.setNotes(item.getNotes());
        mangaAction.setHidden_default(item.getHidden_default());
        mangaAction.setList_status(item.getList_status());

        mStatus.setSelection(Arrays.asList(UserMangaStatus).indexOf(item.getList_status()),true);
        mPrivate.setChecked(item.isPrivate());
        if(item.getScore_raw() != 0)
            mRating.setText(String.valueOf(item.getScore_raw()));
        if(item.getVolumes_read() != 0)
            mVolumes.setText(String.valueOf(item.getVolumes_read()));
        if(item.getChapters_read() != 0)
            mChapters.setText(String.valueOf(item.getChapters_read()));
        if(item.getReread() != 0)
            mReread.setText(String.valueOf(item.getReread()));
        if(item.getNotes() != null)
            mNotes.setText(item.getNotes());
        

        mIncrement.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int current = mangaAction.getChapters_read() != null ?mangaAction.getChapters_read()+1:1;
                mangaAction.setChapters_read(current);
                mChapters.setText(String.valueOf(current));
            }
        });

        mPrivate.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                mangaAction.setHidden_default(b ? 1 : 0);
            }
        });

        mStatus.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                mangaAction.setList_status(UserMangaStatus[i]);
                //"watching","plan to watch","completed","on hold","dropped"
                
                    switch (UserMangaStatus[i]) {
                        case "reading":
                            if (item.getManga().getPublishing_status().equals(MangaStatusTypes[NOT_YET_PUBLISHED])) {
                                Toast.makeText(mContext, R.string.warning_manga_not_publishing, Toast.LENGTH_SHORT).show();
                            }
                            break;
                        case "plan to read":
                            break;
                        case "completed":
                            if (!item.getManga().getPublishing_status().equals(MangaStatusTypes[FINISHED_PUBLISHING])) {
                                Toast.makeText(mContext, R.string.warning_manga_publishing, Toast.LENGTH_SHORT).show();
                            } else {
                                int total = item.getManga().getTotal_chapters();
                                mChapters.setText(String.valueOf(total));
                                total = item.getManga().getTotal_volumes();
                                if(total > 0)
                                    mVolumes.setText(String.valueOf(total));
                            }
                            break;
                        case "on hold":
                            if (item.getManga().getPublishing_status().equals(MangaStatusTypes[NOT_YET_PUBLISHED])) {
                                Toast.makeText(mContext, R.string.warning_manga_not_publishing, Toast.LENGTH_SHORT).show();
                            }
                            break;
                        case "dropped":
                            if (item.getManga().getPublishing_status().equals(MangaStatusTypes[NOT_YET_PUBLISHED])) {
                                Toast.makeText(mContext, R.string.warning_manga_not_publishing, Toast.LENGTH_SHORT).show();
                            }
                            break;
                    }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        md.onNeutral(new MaterialDialog.SingleButtonCallback() {
            @Override
            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                dialog.dismiss();
            }
        });

        md.onNegative(new MaterialDialog.SingleButtonCallback() {
            @Override
            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                //Destination Fucked :'D if you did this by mistake
                progress.show();
                final MaterialDialog dia = dialog;
                RequestApiAction.MangaListActions userPostActions = new RequestApiAction.MangaListActions(mContext, MANGA_LIST_DELETE, mangaAction, new Callback<Object>() {
                    @Override
                    public void onResponse(Call<Object> call, Response<Object> response) {
                        if (response.errorBody() == null) try {
                            dia.dismiss();
                            progress.dismiss();
                            mListener.onResultSuccess();
                            Toast.makeText(mContext, R.string.completed_success, Toast.LENGTH_SHORT).show();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        else try {
                                progress.dismiss();
                                Toast.makeText(mContext, response.errorBody().string(), Toast.LENGTH_LONG).show();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                    }

                    @Override
                    public void onFailure(Call<Object> call, Throwable t) {
                        Toast.makeText(mContext, t.getLocalizedMessage(), Toast.LENGTH_LONG).show();
                        progress.dismiss();
                    }
                });
                userPostActions.execute();
            }
        });

        md.onPositive(new MaterialDialog.SingleButtonCallback() {
            @Override
            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                progress.show();
                mangaAction.setNotes(mNotes.getText().toString());
                mangaAction.setVolumes_read(mVolumes.getText().length() > 0 ? Integer.valueOf(mVolumes.getText().toString()) : 0);
                mangaAction.setChapters_read(mChapters.getText().length() > 0?Integer.valueOf(mChapters.getText().toString()): 0);
                mangaAction.setReread(mReread.getText().length() > 0? Integer.valueOf(mReread.getText().toString()): 0);
                mangaAction.setScore_raw(mRating.getText().length() > 0? Integer.valueOf(mRating.getText().toString()): 0);
                mangaAction.setList_status(UserMangaStatus[mStatus.getSelectedItemPosition()]);

                final MaterialDialog dia = dialog;
                RequestApiAction.MangaListActions userPostActions = new RequestApiAction.MangaListActions(mContext, MANGA_LIST_EDIT, mangaAction, new Callback<Object>() {
                    @Override
                    public void onResponse(Call<Object> call, Response<Object> response) {
                        if (response.errorBody() == null) try {
                            dia.dismiss();
                            progress.dismiss();
                            mListener.onResultSuccess();
                            Toast.makeText(mContext, R.string.text_changes_saved, Toast.LENGTH_SHORT).show();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        else try {
                                progress.dismiss();
                                Toast.makeText(mContext, response.errorBody().string(), Toast.LENGTH_LONG).show();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                    }

                    @Override
                    public void onFailure(Call<Object> call, Throwable t) {
                        Toast.makeText(mContext, t.getLocalizedMessage(), Toast.LENGTH_LONG).show();
                        progress.dismiss();
                    }
                });
                userPostActions.execute();
            }
        }).show();
    }

    /**
     * Only to be used by users manga lists
     */
    public void mangaAddDialogSmall(@NonNull final Series item) {
        final Payload.ListMangaAction mangaAction = new Payload.ListMangaAction();
        progress.setIcon(R.drawable.ic_fiber_new_teal_600_24dp);
        MaterialDialog.Builder md = new MaterialDialog.Builder(mContext)
                .typeface(Typeface.SANS_SERIF,Typeface.SANS_SERIF)
                .title(Html.fromHtml(mContext.getString(R.string.dialog_add_title, getSeriesTitle(item))))
                .autoDismiss(false)
                .iconRes(R.drawable.ic_fiber_new_teal_600_24dp)
                .customView(R.layout.dialog_listing_action_manga, true)
                .buttonRippleColorRes(R.color.colorAccent)
                .positiveColorRes(R.color.colorStateBlue)
                .theme(app_prefs.isLightTheme()?Theme.LIGHT:Theme.DARK)
                .positiveText(R.string.Add)
                .neutralText(R.string.Cancel);

        MaterialDialog matD = md.build();

        final EditText mRating = (EditText) matD.findViewById(R.id.dia_current_score);
        final Spinner mStatus = (Spinner) matD.findViewById(R.id.dia_current_status);
        final EditText mVolumes = (EditText) matD.findViewById(R.id.dia_current_volumes);
        final EditText mChapters = (EditText) matD.findViewById(R.id.dia_current_chapters);
        final EditText mReread = (EditText) matD.findViewById(R.id.dia_current_reread);
        final TextInputEditText mNotes = (TextInputEditText) matD.findViewById(R.id.dia_current_notes);
        final CheckBox mPrivate = (CheckBox) matD.findViewById(R.id.dia_current_privacy);
        final TextView mIncrement = (TextView) matD.findViewById(R.id.dia_current_progress_increment);

        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(mContext, R.array.manga_listing_status, R.layout.adapter_spinner_item);
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        mStatus.setAdapter(adapter);

        mangaAction.setId(item.getId());

        mIncrement.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int current = mangaAction.getChapters_read() != null? mangaAction.getChapters_read()+1:1;
                mangaAction.setChapters_read(current);
                mVolumes.setText(String.valueOf(current));
            }
        });

        mPrivate.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                mangaAction.setHidden_default(b ? 1 : 0);
            }
        });

        mStatus.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                mangaAction.setList_status(UserMangaStatus[i]);
                //"watching","plan to watch","completed","on hold","dropped"
                switch (UserMangaStatus[i]) {
                    case "reading":
                        if (item.getPublishing_status().equals(MangaStatusTypes[NOT_YET_PUBLISHED])) {
                            Toast.makeText(mContext, R.string.warning_manga_not_publishing, Toast.LENGTH_SHORT).show();
                        }
                        break;
                    case "plan to read":
                        break;
                    case "completed":
                        if (!item.getPublishing_status().equals(MangaStatusTypes[FINISHED_PUBLISHING])) {
                            Toast.makeText(mContext, R.string.warning_manga_publishing, Toast.LENGTH_SHORT).show();
                        } else {
                            int total = item.getTotal_chapters();
                            mVolumes.setText(String.valueOf(total));
                        }
                        break;
                    case "on hold":
                        if (item.getPublishing_status().equals(MangaStatusTypes[NOT_YET_PUBLISHED])) {
                            Toast.makeText(mContext, R.string.warning_manga_not_publishing, Toast.LENGTH_SHORT).show();
                        }
                        break;
                    case "dropped":
                        if (item.getPublishing_status().equals(MangaStatusTypes[NOT_YET_PUBLISHED])) {
                            Toast.makeText(mContext, R.string.warning_manga_not_publishing, Toast.LENGTH_SHORT).show();
                        }
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        md.onNeutral(new MaterialDialog.SingleButtonCallback() {
            @Override
            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                dialog.dismiss();
            }
        });

        md.onPositive(new MaterialDialog.SingleButtonCallback() {
            @Override
            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                progress.show();
                mangaAction.setNotes(mNotes.getText().toString());
                if(mVolumes.getText().length() > 0)
                    mangaAction.setVolumes_read(Integer.valueOf(mVolumes.getText().toString()));
                if(mChapters.getText().length() > 0)
                    mangaAction.setChapters_read(Integer.valueOf(mChapters.getText().toString()));
                if(mReread.getText().length() > 0)
                    mangaAction.setReread(Integer.valueOf(mReread.getText().toString()));
                if(mRating.getText().length() > 0)
                    mangaAction.setScore_raw(Integer.valueOf(mRating.getText().toString()));
                mangaAction.setList_status(UserMangaStatus[mStatus.getSelectedItemPosition()]);
                final MaterialDialog dia = dialog;
                RequestApiAction.MangaListActions userPostActions = new RequestApiAction.MangaListActions(mContext, MANGA_LIST_ADD, mangaAction, new Callback<Object>() {
                    @Override
                    public void onResponse(Call<Object> call, Response<Object> response) {
                        if (response.errorBody() == null) {
                            dia.dismiss();
                            progress.dismiss();
                            Toast.makeText(mContext, R.string.text_changes_saved, Toast.LENGTH_SHORT).show();
                        } else
                            try {
                                progress.dismiss();
                                Toast.makeText(mContext, response.errorBody().string(), Toast.LENGTH_SHORT).show();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                    }

                    @Override
                    public void onFailure(Call<Object> call, Throwable t) {
                        Toast.makeText(mContext, t.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                        progress.dismiss();
                    }
                });
                userPostActions.execute();
            }
        }).show();
    }

    /**
     * Only to be used by users manga lists when browsing an external users lists
     */
    public void mangaAddDialogSmall(@NonNull final Manga item) {
        final Payload.ListMangaAction mangaAction = new Payload.ListMangaAction();
        progress.setIcon(R.drawable.ic_fiber_new_teal_600_24dp);
        MaterialDialog.Builder md = new MaterialDialog.Builder(mContext)
                .typeface(Typeface.SANS_SERIF,Typeface.SANS_SERIF)
                .title(Html.fromHtml(mContext.getString(R.string.dialog_add_title, getMangaTitle(item))))
                .autoDismiss(false)
                .iconRes(R.drawable.ic_fiber_new_teal_600_24dp)
                .customView(R.layout.dialog_listing_action_manga, true)
                .buttonRippleColorRes(R.color.colorAccent)
                .positiveColorRes(R.color.colorStateBlue)
                .theme(app_prefs.isLightTheme()?Theme.LIGHT:Theme.DARK)
                .positiveText(R.string.Add)
                .neutralText(R.string.Cancel);

        MaterialDialog matD = md.build();

        final EditText mRating = (EditText) matD.findViewById(R.id.dia_current_score);
        final Spinner mStatus = (Spinner) matD.findViewById(R.id.dia_current_status);
        final EditText mVolumes = (EditText) matD.findViewById(R.id.dia_current_volumes);
        final EditText mChapters = (EditText) matD.findViewById(R.id.dia_current_chapters);
        final EditText mReread = (EditText) matD.findViewById(R.id.dia_current_reread);
        final TextInputEditText mNotes = (TextInputEditText) matD.findViewById(R.id.dia_current_notes);
        final CheckBox mPrivate = (CheckBox) matD.findViewById(R.id.dia_current_privacy);
        final TextView mIncrement = (TextView) matD.findViewById(R.id.dia_current_progress_increment);

        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(mContext, R.array.manga_listing_status, R.layout.adapter_spinner_item);
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        mStatus.setAdapter(adapter);

        mangaAction.setId(item.getId());

        mIncrement.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int current = mangaAction.getChapters_read() != null? mangaAction.getChapters_read()+1:1;
                mangaAction.setChapters_read(current);
                mVolumes.setText(String.valueOf(current));
            }
        });

        mPrivate.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                mangaAction.setHidden_default(b ? 1 : 0);
            }
        });

        mStatus.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                mangaAction.setList_status(UserMangaStatus[i]);
                //"watching","plan to watch","completed","on hold","dropped"
                switch (UserMangaStatus[i]) {
                    case "reading":
                        if (item.getPublishing_status().equals(MangaStatusTypes[NOT_YET_PUBLISHED])) {
                            Toast.makeText(mContext, R.string.warning_manga_not_publishing, Toast.LENGTH_SHORT).show();
                        }
                        break;
                    case "plan to read":
                        break;
                    case "completed":
                        if (!item.getPublishing_status().equals(MangaStatusTypes[FINISHED_PUBLISHING])) {
                            Toast.makeText(mContext, R.string.warning_manga_publishing, Toast.LENGTH_SHORT).show();
                        } else {
                            int total = item.getTotal_chapters();
                            mVolumes.setText(String.valueOf(total));
                        }
                        break;
                    case "on hold":
                        if (item.getPublishing_status().equals(MangaStatusTypes[NOT_YET_PUBLISHED])) {
                            Toast.makeText(mContext, R.string.warning_manga_not_publishing, Toast.LENGTH_SHORT).show();
                        }
                        break;
                    case "dropped":
                        if (item.getPublishing_status().equals(MangaStatusTypes[NOT_YET_PUBLISHED])) {
                            Toast.makeText(mContext, R.string.warning_manga_not_publishing, Toast.LENGTH_SHORT).show();
                        }
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        md.onNeutral(new MaterialDialog.SingleButtonCallback() {
            @Override
            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                dialog.dismiss();
            }
        });

        md.onPositive(new MaterialDialog.SingleButtonCallback() {
            @Override
            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                progress.show();
                mangaAction.setNotes(mNotes.getText().toString());
                if(mVolumes.getText().length() > 0)
                    mangaAction.setVolumes_read(Integer.valueOf(mVolumes.getText().toString()));
                if(mChapters.getText().length() > 0)
                    mangaAction.setChapters_read(Integer.valueOf(mChapters.getText().toString()));
                if(mReread.getText().length() > 0)
                    mangaAction.setReread(Integer.valueOf(mReread.getText().toString()));
                if(mRating.getText().length() > 0)
                    mangaAction.setScore_raw(Integer.valueOf(mRating.getText().toString()));
                mangaAction.setList_status(UserMangaStatus[mStatus.getSelectedItemPosition()]);
                final MaterialDialog dia = dialog;
                RequestApiAction.MangaListActions userPostActions = new RequestApiAction.MangaListActions(mContext, MANGA_LIST_ADD, mangaAction, new Callback<Object>() {
                    @Override
                    public void onResponse(Call<Object> call, Response<Object> response) {
                        if (response.errorBody() == null) {
                            dia.dismiss();
                            progress.dismiss();
                            Toast.makeText(mContext, R.string.text_changes_saved, Toast.LENGTH_SHORT).show();
                        } else
                            try {
                                progress.dismiss();
                                Toast.makeText(mContext, response.errorBody().string(), Toast.LENGTH_SHORT).show();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                    }

                    @Override
                    public void onFailure(Call<Object> call, Throwable t) {
                        Toast.makeText(mContext, t.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                        progress.dismiss();
                    }
                });
                userPostActions.execute();
            }
        }).show();
    }

    public void createDialogActivityPost(MaterialDialog.SingleButtonCallback callback) {
        createDialogInput(mContext.getString(R.string.create_status_title), mContext.getString(R.string.create_status_text),
                new MaterialDialog.InputCallback() {
                    @Override
                    public void onInput(@NonNull MaterialDialog dialog, CharSequence input) {
                        //on input
                    }
                }, callback);
    }

    public void createDialogActivityEdit(MaterialDialog.SingleButtonCallback callback, Spanned value) {
        
        MaterialDialog dialog = new MaterialDialog.Builder(mContext)
                .positiveText(R.string.Ok)
                .negativeText(R.string.Cancel)
                //.neutralText(R.string.attach_media)
                .onAny(callback)
                .buttonRippleColorRes(R.color.colorAccent)
                .positiveColorRes(R.color.colorStateBlue)
                .negativeColorRes(R.color.colorStateOrange)
                .theme(app_prefs.isLightTheme()?Theme.LIGHT:Theme.DARK)
                .title(R.string.edit_status_title)
                .content(R.string.edit_status_text)
                .inputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_IME_MULTI_LINE | InputType.TYPE_TEXT_FLAG_MULTI_LINE)
                .input(mContext.getString(R.string.text_enter_text), null, new MaterialDialog.InputCallback() {
                    @Override
                    public void onInput(@NonNull MaterialDialog dialog, CharSequence input) {

                    }
                }).show();
        if(dialog.getInputEditText() != null) {
            dialog.getInputEditText().setText(value);
        }
        else
            Toast.makeText(mContext, R.string.text_unable_set_text, Toast.LENGTH_LONG).show();
    }

    public void createDialogAttachMedia(@IdRes int action, final TextInputEditText editor) {

        MaterialDialog.Builder builder = new MaterialDialog.Builder(mContext)
                .positiveText(R.string.Ok)
                .negativeText(R.string.Cancel)
                .autoDismiss(false)
                .buttonRippleColorRes(R.color.colorAccent)
                .positiveColorRes(R.color.colorStateBlue)
                .negativeColorRes(R.color.colorStateOrange)
                .theme(app_prefs.isLightTheme()?Theme.LIGHT:Theme.DARK)
                .inputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_IME_MULTI_LINE | InputType.TYPE_TEXT_FLAG_MULTI_LINE)
                .input(mContext.getString(R.string.text_enter_text), null, new MaterialDialog.InputCallback() {
                    @Override
                    public void onInput(@NonNull MaterialDialog dialog, CharSequence input) {
                        // on input
                    }
                });

        switch (action) {
            case R.id.action_link:
                builder.title(R.string.attach_link_title)
                        .content(R.string.attach_link_text)
                        .onAny(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                switch (which) {
                                    case POSITIVE:
                                        EditText editText = dialog.getInputEditText();
                                        if(editText != null) {
                                            if(!TextUtils.isEmpty(editText.getText())) {
                                                editor.append(MarkDown.convertLink(editText.getText()));
                                                dialog.dismiss();
                                            } else {
                                                Toast.makeText(mContext, R.string.input_empty_warning, Toast.LENGTH_SHORT).show();
                                            }
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
                break;
            case R.id.action_image:
                builder.title(R.string.attach_image_title)
                        .content(R.string.attach_image_text)
                        .onAny(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                switch (which) {
                                    case POSITIVE:
                                        EditText editText = dialog.getInputEditText();
                                        if(editText != null) {
                                            if(!TextUtils.isEmpty(editText.getText())) {
                                                editor.append(MarkDown.convertImage(editText.getText()));
                                                dialog.dismiss();
                                            } else {
                                                Toast.makeText(mContext, R.string.input_empty_warning, Toast.LENGTH_SHORT).show();
                                            }
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
                break;
            case R.id.action_youtube:
                builder.title(R.string.attach_youtube_title)
                        .content(R.string.attach_youtube_text)
                        .onAny(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                switch (which) {
                                    case POSITIVE:
                                        EditText editText = dialog.getInputEditText();
                                        if(editText != null) {
                                            if(!TextUtils.isEmpty(editText.getText())) {
                                                editor.append(MarkDown.convertYoutube(editText.getText()));
                                                dialog.dismiss();
                                            } else {
                                                Toast.makeText(mContext, R.string.input_empty_warning, Toast.LENGTH_SHORT).show();
                                            }
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
                break;
            case R.id.action_webm:
                builder.title(R.string.attach_webm_title)
                        .content(R.string.attach_webm_text)
                        .onAny(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                switch (which) {
                                    case POSITIVE:
                                        EditText editText = dialog.getInputEditText();
                                        if(editText != null) {
                                            if(!TextUtils.isEmpty(editText.getText())) {
                                                editor.append(MarkDown.convertVideo(editText.getText()));
                                                dialog.dismiss();
                                            } else {
                                                Toast.makeText(mContext, R.string.input_empty_warning, Toast.LENGTH_SHORT).show();
                                            }
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
                break;
        }
        builder.show();
    }

    /**
     * Creates a dialog to type in text
     */
    public void createDialogInput(String header, String content, MaterialDialog.InputCallback callback, MaterialDialog.SingleButtonCallback buttonCallback) {
        new MaterialDialog.Builder(mContext)
                .positiveText(R.string.Ok)
                .negativeText(R.string.Cancel)
                //.neutralText(R.string.attach_media)
                .autoDismiss(false)
                .onAny(buttonCallback)
                .buttonRippleColorRes(R.color.colorAccent)
                .positiveColorRes(R.color.colorStateBlue)
                .negativeColorRes(R.color.colorStateOrange)
                .theme(app_prefs.isLightTheme()?Theme.LIGHT:Theme.DARK)
                .title(header)
                .content(content)
                .inputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_IME_MULTI_LINE | InputType.TYPE_TEXT_FLAG_MULTI_LINE)
                .input(mContext.getString(R.string.text_enter_text), null, callback)
                .show();
    }

    /**
     * Creates a dialog to display a message
     */
    public void createDialogMessage(String header, Spanned body) {
        // Build the alert dialog
        new MaterialDialog.Builder(mContext)
                .positiveText(R.string.Close)
                .typeface(Typeface.SANS_SERIF,Typeface.SANS_SERIF)
                .iconRes(app_prefs.isLightTheme()?R.drawable.ic_new_releases_black_24dp:R.drawable.ic_new_releases_white_24dp)
                .buttonRippleColorRes(R.color.colorAccent)
                .positiveColorRes(R.color.colorStateBlue)
                .negativeColorRes(R.color.colorStateOrange)
                .theme(app_prefs.isLightTheme()?Theme.LIGHT:Theme.DARK)
                .title(header)
                .content(body)
                .show();
    }

    /**
     * Creates a dialog to display a message
     */
    public void createDialogMessage(String header, String body) {
        // Build the alert dialog
        new MaterialDialog.Builder(mContext)
                .positiveText(R.string.Close)
                .typeface(Typeface.SANS_SERIF,Typeface.SANS_SERIF)
                .iconRes(app_prefs.isLightTheme()?R.drawable.ic_new_releases_black_24dp:R.drawable.ic_new_releases_white_24dp)
                .buttonRippleColorRes(R.color.colorAccent)
                .positiveColorRes(R.color.colorStateBlue)
                .negativeColorRes(R.color.colorStateOrange)
                .theme(app_prefs.isLightTheme()?Theme.LIGHT:Theme.DARK)
                .title(header)
                .content(body)
                .show();
    }

    /**
     * Creates a dialog to display a message
     */
    public void createDialogMessage(String header, String body, String pos, String neg, MaterialDialog.SingleButtonCallback buttonCallback) {
        // Build the alert dialog
        new MaterialDialog.Builder(mContext)
                .onAny(buttonCallback)
                .positiveText(pos)
                .negativeText(neg)
                .typeface(Typeface.SANS_SERIF,Typeface.SANS_SERIF)
                .iconRes(app_prefs.isLightTheme()?R.drawable.ic_new_releases_black_24dp:R.drawable.ic_new_releases_white_24dp)
                .buttonRippleColorRes(R.color.colorAccent)
                .positiveColorRes(R.color.colorStateBlue)
                .negativeColorRes(R.color.colorStateOrange)
                .theme(app_prefs.isLightTheme()?Theme.LIGHT:Theme.DARK)
                .title(header)
                .content(body)
                .show();
    }

    /**
     * Creates a dialog to display a message
     */
    public void createDialogMessage(String header, Spanned body, String pos, String neg, MaterialDialog.SingleButtonCallback buttonCallback) {
        // Build the alert dialog
        new MaterialDialog.Builder(mContext)
                .onAny(buttonCallback)
                .positiveText(pos)
                .negativeText(neg)
                .typeface(Typeface.SANS_SERIF,Typeface.SANS_SERIF)
                .iconRes(app_prefs.isLightTheme()?R.drawable.ic_new_releases_black_24dp:R.drawable.ic_new_releases_white_24dp)
                .buttonRippleColorRes(R.color.colorAccent)
                .positiveColorRes(R.color.colorStateBlue)
                .negativeColorRes(R.color.colorStateOrange)
                .theme(app_prefs.isLightTheme()?Theme.LIGHT:Theme.DARK)
                .title(header)
                .content(body)
                .show();
    }

    /**
     * Will not auto dismiss
     */
    public void createDialogMessage(String header, Spanned body, String pos, String neg, String neu, MaterialDialog.SingleButtonCallback buttonCallback) {
        // Build the alert dialog
        new MaterialDialog.Builder(mContext)
                .onAny(buttonCallback)
                .positiveText(pos)
                .negativeText(neg)
                .neutralText(neu)
                .autoDismiss(false)
                .typeface(Typeface.SANS_SERIF,Typeface.SANS_SERIF)
                .iconRes(app_prefs.isLightTheme()?R.drawable.ic_new_releases_black_24dp:R.drawable.ic_new_releases_white_24dp)
                .buttonRippleColorRes(R.color.colorAccent)
                .positiveColorRes(R.color.colorStateBlue)
                .negativeColorRes(R.color.colorStateOrange)
                .theme(app_prefs.isLightTheme()?Theme.LIGHT:Theme.DARK)
                .title(header)
                .content(body)
                .show();
    }

    public void createDialogChecks(String title, Collection selection, MaterialDialog.ListCallbackMultiChoice callbackMultiChoice, MaterialDialog.SingleButtonCallback mButtonCallback, Integer[] checked){
        new MaterialDialog.Builder(mContext)
                .title(title)
                .items(selection)
                .typeface(Typeface.SANS_SERIF,Typeface.SANS_SERIF)
                .buttonRippleColorRes(R.color.colorAccent)
                .positiveColorRes(R.color.colorStateBlue)
                .negativeColorRes(R.color.colorStateOrange)
                .theme(app_prefs.isLightTheme()?Theme.LIGHT:Theme.DARK)
                .itemsCallbackMultiChoice(checked, callbackMultiChoice)
                .positiveText(R.string.Apply)
                .neutralText(R.string.Cancel)
                .negativeText(R.string.Reset)
                .onAny(mButtonCallback).show();
    }

    public void createDialogSelection(String title , int selection, MaterialDialog.ListCallbackSingleChoice callbackSingleChoice, MaterialDialog.SingleButtonCallback positive, int index){
        new MaterialDialog.Builder(mContext)
                .title(title)
                .items(selection)
                .typeface(Typeface.SANS_SERIF,Typeface.SANS_SERIF)
                .iconRes(app_prefs.isLightTheme()?R.drawable.ic_view_list_black_24dp:R.drawable.ic_view_list_white_24dp)
                .buttonRippleColorRes(R.color.colorAccent)
                .positiveColorRes(R.color.colorStateBlue)
                .negativeColorRes(R.color.colorStateOrange)
                .theme(app_prefs.isLightTheme()?Theme.LIGHT:Theme.DARK)
                .itemsCallbackSingleChoice(index, callbackSingleChoice)
                .positiveText(R.string.Apply)
                .neutralText(R.string.Cancel)
                .onPositive(positive)
                .onNeutral(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        dialog.dismiss();
                    }
                })
                .show();
    }

    public void createDialogSelection(String title, Collection selection, MaterialDialog.ListCallbackSingleChoice callbackSingleChoice, MaterialDialog.SingleButtonCallback positive, int index){
        new MaterialDialog.Builder(mContext)
                .title(title)
                .items(selection)
                .typeface(Typeface.SANS_SERIF,Typeface.SANS_SERIF)
                .iconRes(app_prefs.isLightTheme()?R.drawable.ic_view_list_black_24dp:R.drawable.ic_view_list_white_24dp)
                .buttonRippleColorRes(R.color.colorAccent)
                .positiveColorRes(R.color.colorStateBlue)
                .negativeColorRes(R.color.colorStateOrange)
                .theme(app_prefs.isLightTheme()?Theme.LIGHT:Theme.DARK)
                .itemsCallbackSingleChoice(index, callbackSingleChoice)
                .positiveText(R.string.Apply)
                .neutralText(R.string.Cancel)
                .onPositive(positive)
                .onNeutral(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        dialog.dismiss();
                    }
                })
                .show();
    }

    public void createChangeLog() {

        LayoutInflater layoutInflater = (LayoutInflater) mContext.getSystemService(
                Context.LAYOUT_INFLATER_SERVICE);
        ChangeLogRecyclerView chgList = (ChangeLogRecyclerView) layoutInflater.inflate(R.layout.fragment_changelog, null);
        if(!app_prefs.isLightTheme())
            chgList.setBackgroundResource(R.color.cardview_dark_background);

        new MaterialDialog.Builder(mContext)
                .title(R.string.text_what_is_new)
                .autoDismiss(true)
                .typeface(Typeface.SANS_SERIF,Typeface.SANS_SERIF)
                .iconRes(app_prefs.isLightTheme()?R.drawable.ic_new_releases_black_24dp:R.drawable.ic_new_releases_white_24dp)
                .customView(chgList ,true)
                .buttonRippleColorRes(R.color.colorAccent)
                .positiveColorRes(R.color.colorStateBlue)
                .negativeColorRes(R.color.colorStateOrange)
                .theme(app_prefs.isLightTheme()?Theme.LIGHT:Theme.DARK)
                .positiveText(R.string.Close)
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        dialog.dismiss();
                    }
                })
                .show();
    }


}
