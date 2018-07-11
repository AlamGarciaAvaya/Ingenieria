package com.avaya.mobilevideo.impl;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.avaya.mobilevideo.MobileVideoApplication;
import com.avaya.mobilevideo.R;
import com.avaya.mobilevideo.utils.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;


public abstract class MobileVideoActivity extends Activity {

    private static final String TAG = MobileVideoActivity.class.getSimpleName();
    protected MobileVideoApplication mMobileVideoApplication;
    private Logger mLogger = Logger.getLogger(TAG);

    protected abstract void setReferencedClass();

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        String myPreferences = "myPrefs";
        SharedPreferences sharedPreferences = this.getSharedPreferences(myPreferences, 0);
        String debug = sharedPreferences.getString("debug", "1");
        int deb = Integer.parseInt(debug);
        switch (deb){
            case 0:
                setReferencedClass();
                break;
            case 1:
                getMenuInflater().inflate(R.menu.issue_report, menu);
                setReferencedClass();
                break;
        }

        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.report_issue_item) {
            StringBuilder logBuilder = new StringBuilder();
            try {
                Process process = Runtime.getRuntime().exec("logcat -d SDK:D VideoSurface:D VideoSurface:I CallQualityCalculator:D");
                BufferedReader bufferedReader = new BufferedReader(
                        new InputStreamReader(process.getInputStream()));

                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    logBuilder.append(line + "\n");
                }
            } catch (IOException e) {
            }


            AlertDialog.Builder builder1 = new AlertDialog.Builder(this);
            builder1.setMessage(logBuilder);
            builder1.setCancelable(true);
            builder1.setNegativeButton(
                    "No",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.cancel();
                        }
                    });

            AlertDialog alert11 = builder1.create();
            alert11.show();

        }


        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mMobileVideoApplication = (MobileVideoApplication) this.getApplicationContext();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mMobileVideoApplication.setCurrentActivity(this);
    }

    @Override
    protected void onPause() {
        clearReference();
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        clearReference();
        super.onDestroy();
    }

    private void clearReference() {
        Activity currentActivity = mMobileVideoApplication.getCurrentActivity();
        if (currentActivity != null && currentActivity.equals(this))
            mMobileVideoApplication.setCurrentActivity(null);
    }
}

