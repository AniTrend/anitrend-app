package com.mxt.anitrend.view.detail.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.github.johnpersano.supertoasts.library.Style;
import com.github.johnpersano.supertoasts.library.SuperActivityToast;
import com.github.johnpersano.supertoasts.library.utils.PaletteUtils;
import com.mxt.anitrend.R;
import com.mxt.anitrend.api.model.Character;
import com.mxt.anitrend.api.model.CharacterSmall;
import com.mxt.anitrend.util.KeyUtils;
import com.mxt.anitrend.async.AsyncTaskFetch;
import com.mxt.anitrend.async.RequestApiAction;
import com.mxt.anitrend.custom.Payload;
import com.mxt.anitrend.util.ApplicationPrefs;
import com.mxt.anitrend.util.ErrorHandler;
import com.mxt.anitrend.util.TransitionHelper;
import com.mxt.anitrend.view.base.activity.ImagePreviewActivity;
import com.mxt.anitrend.viewmodel.activity.DefaultActivity;

import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import uk.co.samuelwall.materialtaptargetprompt.MaterialTapTargetPrompt;

public class CharacterActivity extends DefaultActivity implements Callback<Character>, View.OnClickListener {

    public static final String CHARACTER_OBJECT_PARAM = "character_obj_value";

    private final String REQ_KEY = "char_req";
    private final String SM_KEY = "char_sm_key";
    private final String CH_KEY = "char_data_key";

    private boolean requesting;
    private Character character;
    private CharacterSmall character_temp;
    private ApplicationPrefs prefs;
    private MenuItem favMenuItem;

    @BindView(R.id.parent_coordinator) CoordinatorLayout coordinatorLayout;
    @BindView(R.id.toolbar) Toolbar toolbar;

    /*Controls*/
    @BindView(R.id.character_holder) View mCard_Holder;
    @BindView(R.id.actor_image) CircleImageView mActor_Avatar;
    @BindView(R.id.character_details_image) ImageView mCharacter_Image;
    @BindView(R.id.character_native) TextView mNative;
    @BindView(R.id.character_first_name) TextView mFirst_Name;
    @BindView(R.id.character_last_name) TextView mLast_Name;
    @BindView(R.id.actor_first_name) TextView mActor_First_Name;
    @BindView(R.id.actor_last_name) TextView mActor_Last_Name;
    @BindView(R.id.character_info) TextView mInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_character);
        ButterKnife.bind(this);
        setSupportActionBar(toolbar);
        mCharacter_Image.setOnClickListener(this);
        mCard_Holder.setOnClickListener(this);
        mActor_Avatar.setOnClickListener(this);
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        if(savedInstanceState == null) {
            character_temp = getIntent().getParcelableExtra(CHARACTER_OBJECT_PARAM);
        }
        else {
            requesting = savedInstanceState.getBoolean(REQ_KEY);
            character_temp = savedInstanceState.getParcelable(SM_KEY);
            character = savedInstanceState.getParcelable(CH_KEY);
        }
        startInit();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_generic_detail, menu);
        favMenuItem = menu.findItem(R.id.action_favor_state);
        setFavIcon();
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
            case R.id.action_favor_state:
                if(!prefs.isAuthenticated()) {
                    Snackbar.make(coordinatorLayout, R.string.info_login_req, Snackbar.LENGTH_LONG).show();
                    return super.onOptionsItemSelected(item);
                }
                Payload.ActionIdBased actionIdBased = new Payload.ActionIdBased(character.getId());
                RequestApiAction.IdActions userPostActions = new RequestApiAction.IdActions(getApplicationContext(), new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                        if(!isDestroyed() || !isFinishing()) {
                            if (response.isSuccessful() && response.body() != null) {
                                displayMessage(character.isFavourite() ? getString(R.string.text_removed_from_favourites): getString(R.string.text_add_to_favourites), CharacterActivity.this);
                                character.setFavourite(!character.isFavourite());
                                setFavIcon();
                            } else
                                displayMessage(ErrorHandler.getError(response).toString(), CharacterActivity.this);
                        }
                    }

                    @Override
                    public void onFailure(Call<ResponseBody> call, Throwable t) {
                        if(!isDestroyed() || !isFinishing())
                            displayMessage(t.getCause().getMessage(), CharacterActivity.this);
                    }
                }, KeyUtils.ActionType.CHARACTER_FAVOURITE, actionIdBased);
                userPostActions.execute();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void startInit() {
        prefs = new ApplicationPrefs(getApplicationContext());

        Glide.with(this)
             .load(character_temp.getImage_url_lge())
             .centerCrop()
             .diskCacheStrategy(DiskCacheStrategy.ALL)
             .into(mCharacter_Image);

        mActionBar.setTitle(R.string.Loading);
        mNative.setText(R.string.Loading);
        mFirst_Name.setText(character_temp.getName_first());
        mLast_Name.setText(character_temp.getName_last());
        mInfo.setText(R.string.Loading);
        if(character_temp.getActor() != null && character_temp.getActor().size() > 0){
            mActor_First_Name.setText(character_temp.getActor().get(0).getName_first());
            mActor_Last_Name.setText(character_temp.getActor().get(0).getName_last());
            Glide.with(this)
                 .load(character_temp.getActor().get(0).getImage_url_lge())
                 .centerCrop()
                 .diskCacheStrategy(DiskCacheStrategy.ALL)
                 .into(mActor_Avatar);
        }
        if(character == null)
            requestCharacterInfo();
        else
            updateUI();
    }

    @Override
    protected void updateUI() {
        mActionBar.setTitle(character.getName_japanese());
        mNative.setText(character.getName_alt());
        mInfo.setText(character.getInfo());
        setFavIcon();
    }

    protected void setFavIcon() {
        if(favMenuItem != null && character != null) {
            favMenuItem.setVisible(true);
            favMenuItem.setIcon(
                    character.isFavourite()?
                            ContextCompat.getDrawable(this, R.drawable.ic_favorite_white_24dp) :
                            ContextCompat.getDrawable(this, R.drawable.ic_favorite_border_white_24dp));

            if(prefs.getCharacterTip()){
                new MaterialTapTargetPrompt.Builder(CharacterActivity.this)
                        //or use ContextCompat.getColor(this, R.color.colorAccent)
                        .setFocalColourFromRes(R.color.colorAccent)
                        .setBackgroundColourFromRes(R.color.colorDarkKnight)
                        .setTarget(toolbar.getChildAt(toolbar.getChildCount() - 1))
                        .setPrimaryText(R.string.tip_character_options_title)
                        .setSecondaryText(R.string.tip_character_options_message)
                        .setOnHidePromptListener(new MaterialTapTargetPrompt.OnHidePromptListener() {
                            @Override
                            public void onHidePrompt(MotionEvent event, boolean tappedTarget) {

                            }

                            @Override
                            public void onHidePromptComplete() {
                                prefs.setCharacterTip();
                            }
                        }).show();
            }
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putBoolean(REQ_KEY, requesting);
        outState.putParcelable(SM_KEY, character_temp);
        outState.putParcelable(CH_KEY, character);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onResponse(Call<Character> call, Response<Character> response) {
        character = response.body();
        updateUI();
    }

    @Override
    public void onFailure(Call<Character> call, Throwable t) {
        final Snackbar snackbar = Snackbar.make(coordinatorLayout, R.string.text_error_request, Snackbar.LENGTH_INDEFINITE);
        snackbar.setAction(R.string.Retry, new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                requestCharacterInfo();
                snackbar.dismiss();
            }
        });
        snackbar.show();
    }

    private void requestCharacterInfo() {
        new AsyncTaskFetch<>(this, getApplicationContext(), character_temp.getId()).execute(AsyncTaskFetch.RequestType.CHARACTER_INFO_REQ);
    }

    public void displayMessage(String message, Context activity) {
        SuperActivityToast.create(activity, new com.github.johnpersano.supertoasts.library.Style(), Style.TYPE_STANDARD)
                          .setText(String.format(Locale.getDefault(), " %s", message))
                          .setTypefaceStyle(Typeface.NORMAL)
                          .setIconPosition(Style.ICONPOSITION_LEFT)
                          .setIconResource(R.drawable.ic_info_outline_white_18dp)
                          .setDuration(Style.DURATION_VERY_SHORT)
                          .setFrame(Style.FRAME_STANDARD)
                          .setColor(PaletteUtils.getSolidColor(PaletteUtils.MATERIAL_BLUE))
                          .setAnimations(Style.ANIMATIONS_FADE).show();
    }

    private void navigateToStaff() {
        if(character_temp.getActor() != null && character_temp.getActor().size() > 0) {
            Intent intent = new Intent(this, StaffActivity.class);
            intent.putExtra(StaffActivity.STAFF_INTENT_KEY, character_temp.getActor().get(0));
            startActivity(intent);
        } else {
            Toast.makeText(this, R.string.layout_empty_response, Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Called when a view has been clicked.
     *
     * @param v The view that was clicked.
     */
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.character_details_image:
                Intent intent = new Intent(CharacterActivity.this, ImagePreviewActivity.class);
                intent.putExtra(ImagePreviewActivity.IMAGE_SOURCE, character_temp.getImage_url_lge());
                TransitionHelper.startSharedImageTransition(CharacterActivity.this, v, getString(R.string.transition_image_preview), intent);
                break;
            case R.id.character_holder:
                navigateToStaff();
                break;
            case R.id.actor_image:
                navigateToStaff();
                break;
        }
    }
}
