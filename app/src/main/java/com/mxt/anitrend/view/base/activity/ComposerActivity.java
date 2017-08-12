package com.mxt.anitrend.view.base.activity;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.text.Editable;
import android.text.Spanned;
import android.view.ActionMode;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.EditText;

import com.mxt.anitrend.R;
import com.mxt.anitrend.util.MarkDown;
import com.mxt.anitrend.viewmodel.activity.DefaultActivity;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by max on 2017/08/12.
 */

public class ComposerActivity extends DefaultActivity implements ActionMode.Callback {

    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.composer_edit)
    EditText editText;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_composer);
        ButterKnife.bind(this);
        setSupportActionBar(toolbar);
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        getDelegate().setHandleNativeActionModesEnabled(false);
        editText.setCustomSelectionActionModeCallback(this);
        updateUI();
    }

    /**
     * Initialize the contents of the Activity's standard options menu.  You
     * should place your menu items in to <var>menu</var>.
     * <p>
     * <p>This is only called once, the first time the options menu is
     * displayed.  To update the menu every time it is displayed, see
     * {@link #onPrepareOptionsMenu}.
     * <p>
     * <p>The default implementation populates the menu with standard system
     * menu items.  These are placed in the {@link Menu#CATEGORY_SYSTEM} group so that
     * they will be correctly ordered with application-defined menu items.
     * Deriving classes should always call through to the base implementation.
     * <p>
     * <p>You can safely hold on to <var>menu</var> (and any items created
     * from it), making modifications to it as desired, until the next
     * time onCreateOptionsMenu() is called.
     * <p>
     * <p>When you add items to the menu, you can implement the Activity's
     * {@link #onOptionsItemSelected} method to handle them there.
     *
     * @param menu The options menu in which you place your items.
     * @return You must return true for the menu to be displayed;
     * if you return false it will not be shown.
     * @see #onPrepareOptionsMenu
     * @see #onOptionsItemSelected
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_attachments, menu);
        return super.onCreateOptionsMenu(menu);
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

    @Override
    protected void updateUI() {
        editText.setBackgroundColor(ContextCompat.getColor(getApplicationContext(),android.R.color.transparent));
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
}
