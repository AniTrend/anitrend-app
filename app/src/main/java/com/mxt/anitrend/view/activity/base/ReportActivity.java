package com.mxt.anitrend.view.activity.base;

import android.Manifest;
import android.os.Bundle;

import com.mxt.anitrend.R;
import com.mxt.anitrend.base.custom.activity.ActivityBase;
import com.mxt.anitrend.presenter.base.BasePresenter;

import android.os.Environment;
import android.support.annotation.Nullable;
import android.widget.Button;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;

public class ReportActivity extends ActivityBase<Void, BasePresenter> {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report_test);
        StringBuilder log = new StringBuilder();
        try {
            Process process = Runtime.getRuntime().exec("logcat -d");
            BufferedReader bufferedReader = new BufferedReader(
                    new InputStreamReader(process.getInputStream()));

            String line;
            while ((line = bufferedReader.readLine()) != null) {
                log.append(line).append("\n");
            }
            ((TextView) findViewById(R.id.report_display)).setText(log.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
        Button saveLogcat = findViewById(R.id.save_logcat_button);
        saveLogcat.setOnClickListener(view -> {
            if (requestPermissionIfMissing(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                try {
                    File root = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
                            "AniTrend Logcat.txt");
                    FileWriter writer = new FileWriter(root);
                    writer.append(log.toString());
                    writer.flush();
                    writer.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }});
        }

        @Override
        protected void onActivityReady () {
        }

        @Override
        protected void updateUI () {
        }

        @Override
        protected void makeRequest () {
        }
    }
