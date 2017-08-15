package com.mxt.anitrend.view.base.activity;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.Toolbar;
import android.text.Spanned;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.github.rubensousa.bottomsheetbuilder.BottomSheetBuilder;
import com.github.rubensousa.bottomsheetbuilder.adapter.BottomSheetItemClickListener;
import com.mxt.anitrend.R;
import com.mxt.anitrend.base.custom.Payload;
import com.mxt.anitrend.base.custom.async.RequestApiAction;
import com.mxt.anitrend.base.custom.view.editor.MarkdownEditor;
import com.mxt.anitrend.presenter.detail.GenericPresenter;
import com.mxt.anitrend.util.ApplicationPrefs;
import com.mxt.anitrend.util.DialogManager;
import com.mxt.anitrend.util.ErrorHandler;
import com.mxt.anitrend.util.ImeAction;
import com.mxt.anitrend.util.KeyUtils;
import com.mxt.anitrend.util.MarkDown;
import com.mxt.anitrend.view.index.activity.LoginActivity;
import com.mxt.anitrend.viewmodel.activity.DefaultActivity;

import butterknife.BindView;
import butterknife.ButterKnife;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by max on 2017/08/12.
 */

public class ComposerActivity extends DefaultActivity implements BottomSheetItemClickListener, Callback<ResponseBody> {

    public static final String ARG_ACTION_TYPE = "arg_action_type";
    public static final String ARG_ACTION_ID = "arg_action_id";
    public static final String ARG_ACTION_PAYLOAD = "arg_action_payload";

    @BindView(R.id.coordinator)
    CoordinatorLayout coordinatorLayout;
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.composer_edit)
    MarkdownEditor editText;

    private MenuItem mPreviewAction;

    private final String arg_key_editor = "arg_key_editor";
    private final String arg_key_is_preview = "arg_key_is_preview";

    private RequestApiAction.ActivityActions<ResponseBody> activityActions;

    private int id;
    private KeyUtils.ActionType mActionType;

    private boolean isPreviewMode;
    private String original;
    private Spanned preview;

    private ApplicationPrefs preferences;
    private BottomSheetBehavior mBehavior;

    private void handleIntent() {
        if(intentReader != null) {
            if(intentReader.getSubject() == null || intentReader.getText().equals(intentReader.getSubject()))
                original = intentReader.getText().toString();
            else
                original = String.format("%s %s",intentReader.getSubject(), intentReader.getText());
        } else {
            if(getIntent() != null) {
                if(getIntent().hasExtra(ARG_ACTION_TYPE))
                    mActionType = (KeyUtils.ActionType) getIntent().getSerializableExtra(ARG_ACTION_TYPE);
                if(getIntent().hasExtra(ARG_ACTION_ID))
                    id = getIntent().getIntExtra(ARG_ACTION_ID, 0);
                if(getIntent().hasExtra(ARG_ACTION_PAYLOAD))
                    original = getIntent().getStringExtra(ARG_ACTION_PAYLOAD);
            }
        }
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_composer);
        ButterKnife.bind(this);
        setSupportActionBar(toolbar);
        preferences = new ApplicationPrefs(this);
        if(!preferences.isAuthenticated()) {
            Toast.makeText(this, R.string.text_please_sign_in, Toast.LENGTH_LONG).show();
            finish();
        } else
            handleIntent();
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        getDelegate().setHandleNativeActionModesEnabled(false);
        mPresenter = new GenericPresenter(this);
        updateUI();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_composer, menu);
        mPreviewAction = menu.findItem(R.id.action_preview);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        if(!isPreviewMode)
            original = editText.getText().toString();
        outState.putSerializable(ARG_ACTION_TYPE, mActionType);
        outState.putInt(ARG_ACTION_ID, id);
        outState.putBoolean(arg_key_is_preview, isPreviewMode);
        outState.putString(arg_key_editor, original);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        if(savedInstanceState != null) {
            mActionType = (KeyUtils.ActionType) savedInstanceState.getSerializable(ARG_ACTION_TYPE);
            id = savedInstanceState.getInt(ARG_ACTION_ID, 0);
            isPreviewMode = savedInstanceState.getBoolean(arg_key_is_preview);
            editText.setText(savedInstanceState.getString(arg_key_editor));
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
            case R.id.action_attach:
                mBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                break;
            case R.id.action_send:
                if(!TextUtils.isEmpty(editText.getText())) {
                    ImeAction.hideSoftKeyboard(this);
                    startAction();
                } else
                    Toast.makeText(this, R.string.warning_empty_input, Toast.LENGTH_SHORT).show();
                break;
            case R.id.action_preview:
                toggleModes();
                break;
            case R.id.action_help:
                new DialogManager(ComposerActivity.this)
                        .createDialogMessage(getString(R.string.menu_title_help),
                                MarkDown.convert(getString(R.string.menu_title_help)));
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        if(activityActions != null && activityActions.getStatus() == AsyncTask.Status.RUNNING)
            activityActions.cancel(true);
        super.onDestroy();
    }

    /**
     * Take care of popping the fragment back stack or finishing the activity
     * as appropriate.
     */
    @Override
    public void onBackPressed() {
        if(mBehavior.getState() == BottomSheetBehavior.STATE_EXPANDED) {
            mBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
            return;
        }
        super.onBackPressed();
    }

    private void setIcon() {
        if(mPreviewAction != null){
            if(!isPreviewMode)
                mPreviewAction.setIcon(R.drawable.ic_remove_red_eye_white_24dp);
            else
                mPreviewAction.setIcon(R.drawable.ic_border_color_white_24dp);
        }
    }

    private void startAction() {
        Payload.ActivityStruct activityStruct = null;
        switch (mActionType) {
            case ACTIVITY_CREATE:
                activityStruct = new Payload.ActivityStruct(editText.getText().toString());
                activityActions = new RequestApiAction.ActivityActions<>(getApplicationContext(), this, mActionType, activityStruct);
                break;
            case ACTIVITY_EDIT:
                activityStruct = new Payload.ActivityStruct(id, editText.getText().toString(), mPresenter.getCurrentUser().getId(), -1);
                activityActions = new RequestApiAction.ActivityActions<>(getApplicationContext(), this, mActionType, activityStruct);
                break;
        }
        activityActions = new RequestApiAction.ActivityActions<>(getApplicationContext(), this, mActionType, activityStruct);
        activityActions.execute();
        Toast.makeText(this, R.string.Sending, Toast.LENGTH_SHORT).show(); //replace with dialog
    }

    private void toggleModes() {
        if(!isPreviewMode) {
            Toast.makeText(this, R.string.text_edit_mode, Toast.LENGTH_SHORT).show();
            editText.setText(original);
        } else {
            ImeAction.hideSoftKeyboard(this);
            Toast.makeText(this, R.string.text_preview_mode, Toast.LENGTH_SHORT).show();
            original = editText.getText().toString();
            preview = MarkDown.convert(original);
            editText.setText(preview);
        }
        setIcon();
        isPreviewMode = !isPreviewMode;
    }

    @Override
    protected void updateUI() {
        editText.setBackgroundColor(ContextCompat.getColor(getApplicationContext(),android.R.color.transparent));
        mBehavior = BottomSheetBehavior.from(new BottomSheetBuilder(this, coordinatorLayout)
                .setMode(BottomSheetBuilder.MODE_GRID)
                .setBackgroundColorResource(R.color.colorDarkKnight)
                .setItemTextColorResource(R.color.white)
                .setMenu(R.menu.menu_attachments)
                .setItemClickListener(this).createView());
        mBehavior.setBottomSheetCallback(null);
        toggleModes();
    }

    @Override
    public void onBottomSheetItemClick(MenuItem item) {
        mBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        new DialogManager(ComposerActivity.this).createDialogAttachMedia(item.getItemId(), editText);
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
    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
        if (isAlive()) {
            if (response.isSuccessful() && response.body() != null) {
                Toast.makeText(this, R.string.text_compose_success, Toast.LENGTH_SHORT).show();
                finish();
            }
            else
                Toast.makeText(this, ErrorHandler.getError(response).toString(), Toast.LENGTH_LONG).show();
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
    public void onFailure(Call<ResponseBody> call, Throwable t) {
        if(isAlive()) {
            t.printStackTrace();
            Toast.makeText(this, t.getLocalizedMessage(), Toast.LENGTH_LONG).show();
        }
    }
}
