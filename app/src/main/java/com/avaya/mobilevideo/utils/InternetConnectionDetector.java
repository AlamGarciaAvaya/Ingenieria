/**
 * InternetConnectionDetector.java <br>
 * Copyright 2014-2015 Avaya Inc. <br>
 * All rights reserved. Usage of this source is bound to the terms described the file
 * MOBILE_VIDEO_SDK_LICENSE_AGREEMENT.txt, included in this SDK.<br>
 * Avaya – Confidential & Proprietary. Use pursuant to your signed agreement or Avaya Policy.
 */
package com.avaya.mobilevideo.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

/**
 * Class to check if there is a valid internet connection
 *
 * @author Avaya Inc
 */
public class InternetConnectionDetector {

    public enum InternetConnection {
        WIFI, MOBILE_DATA, NO_CONNECTION
    }

    private static final String TAG = InternetConnectionDetector.class.getSimpleName();

    private Logger mLogger = Logger.getLogger(TAG);

    private Context mContext;
    private boolean mConnected;
    private boolean mWifiConnected;
    private boolean mMobileDataConnected;

    public InternetConnectionDetector(Context context) {
        this.mContext = context;

        analyse();
    }

    /**
     * Tries to detect any internet connection
     *
     * @return boolean
     */
    public boolean isConnected() {
        ConnectivityManager connectivityManager = getConnectivityManager();

        return (connectivityManager != null && isConnected(connectivityManager));
    }

    /**
     * Returns an InternetConnection enum
     *
     * @return internetConnection
     */
    public InternetConnection getConnectionInformation() {
        InternetConnection internetConnection = InternetConnection.NO_CONNECTION;

        if (mConnected) {
            if (mWifiConnected) {
                internetConnection = InternetConnection.WIFI;
            } else if (mMobileDataConnected) {
                internetConnection = InternetConnection.MOBILE_DATA;
            }
        }

        return internetConnection;
    }

    public boolean getConnected() {
        return mConnected;
    }

    public boolean getWifiConnected() {
        return mWifiConnected;
    }

    public boolean getMobileDataConnected() {
        return mMobileDataConnected;
    }

    private boolean analyse() {
        try {
            mLogger.d("analyse()");

            ConnectivityManager connectivityManager = getConnectivityManager();
            if (connectivityManager != null) {
                mConnected = isConnected(connectivityManager);
                mWifiConnected = isWifiConnected(connectivityManager);
                mMobileDataConnected = isMobileDataConnected(connectivityManager);
            }
        } catch (Exception e) {
            mLogger.e("Exception in analyse()", e);
            return false;
        }
        return true;
    }

    private ConnectivityManager getConnectivityManager() {
        return (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
    }

    private boolean isConnected(ConnectivityManager connectivityManager) {
        boolean isConnected = false;
        NetworkInfo[] networkInfo = connectivityManager.getAllNetworkInfo();

        if (networkInfo != null) {
            for (NetworkInfo aNetworkInfo : networkInfo) {
                if (aNetworkInfo.getState() == NetworkInfo.State.CONNECTED) {
                    isConnected = true;
                    break;
                }
            }
        }

        return isConnected;
    }

    private boolean isWifiConnected(ConnectivityManager connectivityManager) {
        return isNetworkConnected(connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI));
    }

    private boolean isMobileDataConnected(ConnectivityManager connectivityManager) {
        return isNetworkConnected(connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE));
    }

    private boolean isNetworkConnected(NetworkInfo networkInfo) {
        boolean isConnected = false;
        if (networkInfo != null) {
            isConnected = networkInfo.isConnected();
        }

        return isConnected;
    }

    public String toString() {
        return "InternetConnectionDetector: [Connected: " + mConnected + ", wifi connection: " + mWifiConnected + ", mobile connected: " + mMobileDataConnected + "]";
    }
}
