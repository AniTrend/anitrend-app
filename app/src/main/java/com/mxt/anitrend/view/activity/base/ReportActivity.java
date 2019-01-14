package com.mxt.anitrend.view.activity.base;

import android.os.Bundle;

import com.mxt.anitrend.R;
import com.mxt.anitrend.base.custom.activity.ActivityBase;
import com.mxt.anitrend.presenter.base.BasePresenter;

import android.support.annotation.Nullable;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class ReportActivity extends ActivityBase<Void, BasePresenter> {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report_test);
        try {
            Process process = Runtime.getRuntime().exec("logcat -d");
            BufferedReader bufferedReader = new BufferedReader(
                    new InputStreamReader(process.getInputStream()));

            StringBuilder log = new StringBuilder();
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                log.append(line).append("\n");
            }
            ((TextView)findViewById(R.id.report_display)).setText(log.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onActivityReady() {
    }

    @Override
    protected void updateUI() {
    }

    @Override
    protected void makeRequest() {
    }
}
