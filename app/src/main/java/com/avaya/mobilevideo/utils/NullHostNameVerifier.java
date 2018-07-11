/**
 * NullHostNameVerifier.java <br>
 * Copyright 2014-2015 Avaya Inc. <br>
 * All rights reserved. Usage of this source is bound to the terms described the file
 * MOBILE_VIDEO_SDK_LICENSE_AGREEMENT.txt, included in this SDK.<br>
 * Avaya – Confidential & Proprietary. Use pursuant to your signed agreement or Avaya Policy.
 */
package com.avaya.mobilevideo.utils;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSession;

/**
 * NullHostNameVerifier
 *
 * @author Avaya Inc
 */
public class NullHostNameVerifier implements HostnameVerifier {

    private static final String TAG = NullHostNameVerifier.class.getSimpleName();
    private Logger mLogger = Logger.getLogger(TAG);

    @Override
    public boolean verify(String hostname, SSLSession session) {
        mLogger.d("Approving certificate for " + hostname);
        mLogger.d("Session " + session.getProtocol() + " " + session.getPeerHost());
        return true;
    }
}
