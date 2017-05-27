package com.mxt.anitrend.view.base.activity;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.mxt.anitrend.R;
import com.mxt.anitrend.adapter.recycler.index.LibrariesAdapter;
import com.mxt.anitrend.api.structure.LibraryRep;
import com.mxt.anitrend.util.ApplicationPrefs;
import com.mxt.anitrend.viewmodel.activity.DefaultActivity;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;

public class AboutActivity extends DefaultActivity {

    private final String list_key = "list_key";

    private List<LibraryRep> libraries;

    @BindView(R.id.app_version) TextView app_version;
    @BindView(R.id.recycler_libraries) RecyclerView recyclerView;
    @BindView(R.id.toolbar) Toolbar toolbar;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        ButterKnife.bind(this);
        setSupportActionBar(toolbar);
        ActionBar mActionBar = getSupportActionBar();
        if (mActionBar != null) {
            mActionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        if(savedInstanceState != null){
            libraries = (List<LibraryRep>) savedInstanceState.getSerializable(list_key);
        }
        ApplicationPrefs applicationPrefs = new ApplicationPrefs(getApplicationContext());
        app_version.setText(String.format(Locale.getDefault(), "v%s", applicationPrefs.getSavedVersions().getVersion()));

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setNestedScrollingEnabled(false);
        recyclerView.setLayoutManager(layoutManager);
        if(libraries == null)
            new LibraryLoader().execute();
        else
            updateUI();
    }


    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putSerializable(list_key, (Serializable) libraries);
        super.onSaveInstanceState(outState);
    }

    class LibraryLoader extends AsyncTask<Void,Void,List<LibraryRep>> {

        @Override
        protected List<LibraryRep> doInBackground(Void... voids) {
            BufferedReader reader;
            StringBuilder stringBuilder = new StringBuilder();
            try {
                reader = new BufferedReader(new InputStreamReader(getAssets().open("libs/libraries.json")));
                String mLine;
                while ((mLine = reader.readLine()) != null) {
                    stringBuilder.append(mLine);
                }
                Gson jsonConverter = new Gson();
                return jsonConverter.fromJson(stringBuilder.toString(), new TypeToken<ArrayList<LibraryRep>>(){}.getType());
            } catch (IOException ex) {
                Log.e("Deserialize", ex.getMessage());
                ex.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(List<LibraryRep> result) {
            if(!isDestroyed()) {
                libraries = result;
                updateUI();
            }
        }
    }

    @Override
    protected void updateUI() {
        if(libraries != null) {
            LibrariesAdapter mAdapter = new LibrariesAdapter(libraries, this);
            recyclerView.setAdapter(mAdapter);
        } else {
            Toast.makeText(this, "The application was unable to build the list libraries used in this project!", Toast.LENGTH_LONG).show();
        }
    }
}
