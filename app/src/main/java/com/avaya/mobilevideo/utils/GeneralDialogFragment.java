/**
 * GeneralDialogFragment.java <br>
 * Copyright 2014-2015 Avaya Inc. <br>
 * All rights reserved. Usage of this source is bound to the terms described the file
 * MOBILE_VIDEO_SDK_LICENSE_AGREEMENT.txt, included in this SDK.<br>
 * Avaya – Confidential & Proprietary. Use pursuant to your signed agreement or Avaya Policy.
 */
package com.avaya.mobilevideo.utils;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;

import com.avaya.mobilevideo.R;

/**
 * Used to display error messages back to the user
 *
 * @author Avaya Inc
 */
public class GeneralDialogFragment extends DialogFragment {
    private static final String TAG = GeneralDialogFragment.class.getSimpleName();
    private static Logger logger = Logger.getLogger(TAG);

    private boolean mIsErrorMessage = true;
    private String mMessage = "";
    private boolean mFinishActivity = false;
    private Activity mActivity;
    private String mTitle = null;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        if (mTitle == null || mTitle.equals("")) {
            if (mIsErrorMessage) {
                builder.setTitle(R.string.error_dialog_title);
            } else {
                builder.setTitle(R.string.information_dialog_title);
            }
        } else {
            builder.setTitle(mTitle);
        }

        builder.setMessage(mMessage)
                .setPositiveButton(R.string.error_dialog_acknowledge, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        if (mFinishActivity) {
                            mActivity.finish();
                        }
                    }
                });
        // Create the AlertDialog object and return it
        return builder.create();
    }

    private void setMessage(boolean isErrorMessage, String message) {
        mIsErrorMessage = isErrorMessage;
        mMessage = message;
    }

    public void setTitle(String title) {
        mTitle = title;
    }

    private void setActivity(Activity activity) {
        mActivity = activity;
    }

    private void setFinishActivity(boolean finishActivity) {
        mFinishActivity = finishActivity;
    }

    public static void displayMessage(Activity activity, String message) {
        displayMessage(activity, message, false);
    }

    public static void displayMessage(Activity activity, String message, String title) {
        displayMessage(activity, message, false, true, title);
    }

    public static void displayMessage(boolean isErrorMessage, Activity activity, String message) {
        displayMessage(activity, message, false, isErrorMessage);
    }

    public static void displayMessage(Activity activity, String message, boolean finishActivity) {
        displayMessage(activity, message, finishActivity, true);
    }

    public static void displayMessage(Activity activity, String message, boolean finishActivity, boolean isErrorMessage) {
        displayMessage(activity, message, finishActivity, isErrorMessage, null);
    }

    public static void displayMessage(Activity activity, String message, boolean finishActivity, boolean isErrorMessage, String title) {
        try {
            GeneralDialogFragment generalDialogFragment = new GeneralDialogFragment();
            generalDialogFragment.setMessage(isErrorMessage, message);
            generalDialogFragment.show(activity.getFragmentManager(), "");
            generalDialogFragment.setActivity(activity);
            generalDialogFragment.setFinishActivity(finishActivity);
            generalDialogFragment.setTitle(title);
        } catch (Exception e) {
            logger.e("Exception occurred in displayMessage()", e);
        }
    }

    public static void displayMessage(Activity activity, int stringId) {
        displayMessage(activity, stringId, false);
    }

    public static void displayMessage(Activity activity, int stringId, boolean finishActivity) {
        displayMessage(activity, activity.getResources().getString(stringId), finishActivity);
    }
}