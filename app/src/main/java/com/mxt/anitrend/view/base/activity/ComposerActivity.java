package com.mxt.anitrend.view.base.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.app.ShareCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextUtils;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.github.rubensousa.bottomsheetbuilder.BottomSheetBuilder;
import com.github.rubensousa.bottomsheetbuilder.adapter.BottomSheetItemClickListener;
import com.mxt.anitrend.R;
import com.mxt.anitrend.util.ApiPreferences;
import com.mxt.anitrend.util.ApplicationPrefs;
import com.mxt.anitrend.util.DialogManager;
import com.mxt.anitrend.util.ImeAction;
import com.mxt.anitrend.util.MarkDown;
import com.mxt.anitrend.util.PatternMatcher;
import com.mxt.anitrend.view.detail.activity.UserReplyActivity;
import com.mxt.anitrend.view.index.activity.LoginActivity;
import com.mxt.anitrend.viewmodel.activity.DefaultActivity;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by max on 2017/08/12.
 */

public class ComposerActivity extends DefaultActivity implements ActionMode.Callback, BottomSheetItemClickListener, View.OnClickListener {

    @BindView(R.id.coordinator)
    CoordinatorLayout coordinatorLayout;
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.composer_edit)
    EditText editText;

    private ApplicationPrefs preferences;
    private BottomSheetBehavior mBehavior;

    private void handleIntent() {
        if(intentReader != null) {
            if(intentReader.getSubject() == null || intentReader.getText().equals(intentReader.getSubject()))
                editText.setText(intentReader.getText());
            else
                editText.setText(String.format("%s %s",intentReader.getSubject(), intentReader.getText()));
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
            startActivity(new Intent(ComposerActivity.this, LoginActivity.class));
            finish();
        } else
            handleIntent();
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        getDelegate().setHandleNativeActionModesEnabled(false);
        editText.setCustomSelectionActionModeCallback(this);
        updateUI();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_composer, menu);
        return super.onCreateOptionsMenu(menu);
    }

    /**
     * This hook is called whenever an item in your options menu is selected.
     * The default implementation simply returns false to have the normal
     * processing happen (calling the item's Runnable or sending a message to
     * its Handler as appropriate).  You can use this method for any items
     * for which you would like to do processing without those other
     * facilities.
     * <p>
     * <p>Derived classes should call through to the base class for it to
     * perform the default menu handling.</p>
     *
     * @param item The menu item that was selected.
     * @return boolean Return false to allow normal menu processing to
     * proceed, true to consume it here.
     * @see #onCreateOptionsMenu
     */
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
                // TODO: 2017/08/13 check if there is an int to tell us what to send, review or just activity post
                String replyText = editText.getText().toString();
                if(!TextUtils.isEmpty(replyText)) {
                    ImeAction.hideSoftKeyboard(this);
                    switch (0) {
                        case 0:
                            break;
                        case 1:
                            break;
                    }
                } else
                    Toast.makeText(this, R.string.warning_empty_input, Toast.LENGTH_SHORT).show();
                break;
            case R.id.action_preview:
                String temporary = editText.getText().toString();
                new DialogManager(ComposerActivity.this)
                        .createDialogMessage(getString(R.string.action_preview), MarkDown.convert(
                                PatternMatcher.removeTags(temporary.replaceAll("\n","<br/>"))));
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
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
    }

    @Override
    protected void onDestroy() {
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

    @Override
    protected void updateUI() {
        editText.setBackgroundColor(ContextCompat.getColor(getApplicationContext(),android.R.color.transparent));
        View bottomSheet = new BottomSheetBuilder(this, coordinatorLayout)
                .setMode(BottomSheetBuilder.MODE_GRID)
                .setBackgroundColorResource(R.color.colorDarkKnight)
                .setItemTextColorResource(R.color.white)
                .setMenu(R.menu.menu_attachments)
                .setItemClickListener(this).createView();
        mBehavior = BottomSheetBehavior.from(bottomSheet);
        mBehavior.setBottomSheetCallback(null);
    }

    /**
     * Called when action mode is first created. The menu supplied will be used to
     * generate action buttons for the action mode.
     *
     * @param mode ActionMode being created
     * @param menu Menu used to populate action buttons
     * @return true if the action mode should be created, false if entering this
     * mode should be aborted.
     */
    @Override
    public boolean onCreateActionMode(ActionMode mode, Menu menu) {
        MenuInflater inflater = mode.getMenuInflater();
        inflater.inflate(R.menu.editor_menu, menu);
        return true;
    }

    /**
     * Called to refresh an action mode's action menu whenever it is invalidated.
     *
     * @param mode ActionMode being prepared
     * @param menu Menu used to populate action buttons
     * @return true if the menu or action mode was updated, false otherwise.
     */
    @Override
    public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
        return false;
    }

    /**
     * Called to report a user click on an action button.
     *
     * @param mode The current ActionMode
     * @param item The item that was clicked
     * @return true if this callback handled the event, false if the standard MenuItem
     * invocation should continue.
     */
    @Override
    public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
        int start = editText.getSelectionStart();
        int end = editText.getSelectionEnd();
        Editable editable = editText.getText();
        switch (item.getItemId()) {
            case R.id.menu_bold:
                editable.insert(start,"__");
                editable.insert(end, "__");
                editText.setText(editable);
                mode.finish();
                return true;
            case R.id.menu_italic:
                editable.insert(start,"_");
                editable.insert(end, "_");
                editText.setText(editable);
                mode.finish();
                return true;
            case R.id.menu_strike:
                editable.insert(start,"~~");
                editable.insert(end, "~~");
                editText.setText(editable);
                mode.finish();
                return true;
            case R.id.menu_list:
                editable.insert(start,"1. ");
                editText.setText(editable);
                mode.finish();
                return true;
            case R.id.menu_bullet:
                editable.insert(start,"- ");
                editText.setText(editable);
                mode.finish();
                return true;
            case R.id.menu_heading:
                editable.insert(start,"#");
                editText.setText(editable);
                mode.finish();
                return true;
            case R.id.menu_center:
                editable.insert(start,"~~~");
                editable.insert(end, "~~~");
                editText.setText(editable);
                mode.finish();
                return true;
            case R.id.menu_quote:
                editable.insert(start,">");
                editText.setText(editable);
                mode.finish();
                return true;
            case R.id.menu_code:
                editable.insert(start,"`");
                editable.insert(end, "`");
                editText.setText(editable);
                mode.finish();
                return true;
        }
        return false;
    }

    /**
     * Called when an action mode is about to be exited and destroyed.
     *
     * @param mode The current ActionMode being destroyed
     */
    @Override
    public void onDestroyActionMode(ActionMode mode) {

    }

    @Override
    public void onBottomSheetItemClick(MenuItem item) {
        mBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        new DialogManager(ComposerActivity.this).createDialogAttachMedia(item.getItemId(), editText);
    }

    /**
     * Called when a view has been clicked.
     *
     * @param v The view that was clicked.
     */
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
        }
    }
}
