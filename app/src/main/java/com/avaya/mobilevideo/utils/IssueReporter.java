/**
 * IssueReporter.java <br>
 * Copyright 2015 Avaya Inc. <br>
 * All rights reserved. Usage of this source is bound to the terms described the file
 * MOBILE_VIDEO_SDK_LICENSE_AGREEMENT.txt, included in this SDK.<br>
 * Avaya – Confidential & Proprietary. Use pursuant to your signed agreement or Avaya Policy.
 */
package com.avaya.mobilevideo.utils;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.widget.Toast;

import com.avaya.mobilevideo.R;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Class which can be used to email the app log files to a given email address
 *
 * @author Avaya Inc
 */
public class IssueReporter {

    private static final String TAG = IssueReporter.class.getSimpleName();
    private static final String FILE_PREFIX = "file://";
    private Logger mLogger = Logger.getLogger(TAG);

    private Activity mActivity;
    private String mEmailAddress;
    private String mLogFileName;
    private String mSdkLogFileName;
    private String mIntentTitle;
    private String mSdkVersion;

    public IssueReporter(Activity activity, String sdkVersion) {
        this(activity, sdkVersion, false);
    }

    public IssueReporter(Activity activity, String sdkVersion, boolean uncaughtException) {
        mActivity = activity;

        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(activity);

        mEmailAddress = sharedPref.getString(Constants.PREFERENCE_EMAIL_ADDRESS, "");
        mLogFileName = sharedPref.getString(Constants.PREFERENCE_LOG_FILE_NAME, "");
        mSdkLogFileName = activity.getResources().getString(R.string.sdk_log_file_name);

        mSdkVersion = (sdkVersion == null ? "" : sdkVersion);

        if (uncaughtException) {
            mIntentTitle = mActivity.getResources().getString(R.string.uncaught_exception_email_log_files);
        } else {
            mIntentTitle = mActivity.getResources().getString(R.string.email_log_files);
        }
    }

    public void reportIssue() {
        try {
            mLogger.d("Report issue");

            boolean sendEmail = true;

            Intent emailIntent;

            String idTag = "[" + UUID.randomUUID() + "]";
            String subject = Constants.REPORT_ISSUE_SUBJECT.replace(Constants.PLACEHOLDER1, idTag);

            String regularLogFile = Environment.getExternalStorageDirectory() + File.separator + mLogFileName;
            boolean regularLogFileExists = false;

            String fullSDKLogs = extractLogToFile();

            File logFile = new File(regularLogFile);
            if (logFile.exists()) {
                regularLogFileExists = true;
            }

            if (fullSDKLogs != null) {
                List<String> filePaths = new ArrayList<String>();

                if (regularLogFileExists) {
                    mLogger.d("Send regular and SKD log files");
                    filePaths.add(regularLogFile);
                } else {
                    mLogger.i("Regular log file doesn't exist, send SDK logs only");
                }

                filePaths.add(fullSDKLogs);

                emailIntent = new Intent(
                        Intent.ACTION_SEND_MULTIPLE);

                ArrayList<Uri> uris = new ArrayList<Uri>();

                for (String file : filePaths) {
                    File fileIn = new File(file);
                    Uri u = Uri.fromFile(fileIn);
                    uris.add(u);
                }
                emailIntent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris);
            } else {
                mLogger.i("Can't send SDK logs, send regular logs only");
                emailIntent = new Intent(
                        Intent.ACTION_SEND);

                if (regularLogFileExists) {
                    emailIntent.putExtra(android.content.Intent.EXTRA_STREAM, Uri.parse(FILE_PREFIX + regularLogFile));
                } else {
                    mLogger.w("No log files to send");
                    sendEmail = false;
                }
            }

            emailIntent.setType("plain/text");
            emailIntent.putExtra(android.content.Intent.EXTRA_EMAIL,
                    new String[]{mEmailAddress});
            emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT,
                    subject);

            if (sendEmail) {
                mActivity.startActivity(Intent.createChooser(emailIntent, mIntentTitle));
            } else {
                mLogger.i("Neither log file could be found or generated, nothing to send");
                GeneralDialogFragment.displayMessage(mActivity, R.string.no_log_files_found);
            }
        } catch (Throwable t) {
            mLogger.e("reportIssue() exception", t);

            Toast.makeText(mActivity,
                    "Request failed try again: " + t.toString(),
                    Toast.LENGTH_LONG).show();
        }
    }

    protected String extractLogToFile() {
        String fullName;
        InputStreamReader reader = null;
        FileWriter writer = null;

        try {
            fullName = Environment.getExternalStorageDirectory() + File.separator + mSdkLogFileName;
            File file = new File(fullName);

            // For Android 4.0 and earlier, you will get all app's log output, so filter it to
            // mostly limit it to your app's output.  In later versions, the filtering isn't needed.
            String cmd = (Build.VERSION.SDK_INT <= Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1) ?
                    "logcat -d -v time MobileVideoApplication:v dalvikvm:v System.err:v *:s" :
                    "logcat -d -v time";

            // get input stream
            Process process = Runtime.getRuntime().exec(cmd);
            reader = new InputStreamReader(process.getInputStream());

            // write output stream
            writer = new FileWriter(file);
            addDeviceInformation(writer);

            char[] buffer = new char[10000];
            do {
                int n = reader.read(buffer, 0, buffer.length);
                if (n == -1)
                    break;
                writer.write(buffer, 0, n);
            } while (true);
        } catch (IOException e) {
            mLogger.e("IOException extractLogToFile", e);
            fullName = null;
        } catch (Exception e) {
            mLogger.e("Exception extractLogToFile", e);
            fullName = null;
        } finally {
            if (writer != null)
                try {
                    writer.close();
                } catch (IOException e1) {
                    mLogger.w("Exception closing resource");
                }
            if (reader != null)
                try {
                    reader.close();
                } catch (IOException e1) {
                    mLogger.w("Exception closing resource");
                }
        }

        return fullName;
    }

    private void addDeviceInformation(FileWriter writer) throws IOException, PackageManager.NameNotFoundException {
        PackageInfo info = mActivity.getPackageManager().getPackageInfo(mActivity.getPackageName(), 0);
        String model = Build.MODEL;

        if (!model.startsWith(Build.MANUFACTURER)) {
            model = Build.MANUFACTURER + " " + model;
        }

        writer.write("Android version: " + Build.VERSION.SDK_INT + "\n");
        writer.write("Device: " + model + "\n");
        writer.write("SDK version: " + mSdkVersion + "\n");
        writer.write("App version: " + (info == null ? "(null)" : info.versionCode) + "\n");
    }

    protected String getSdkLogFileName() {
        return mSdkLogFileName;
    }

    protected void setSdkLogFileName(String sdkLogFileName) {
        mSdkLogFileName = sdkLogFileName;
    }
}
