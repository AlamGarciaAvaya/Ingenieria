/**
 * TrustAllCerts.java <br>
 * Copyright 2014-2015 Avaya Inc. <br>
 * All rights reserved. Usage of this source is bound to the terms described the file
 * MOBILE_VIDEO_SDK_LICENSE_AGREEMENT.txt, included in this SDK.<br>
 * Avaya – Confidential & Proprietary. Use pursuant to your signed agreement or Avaya Policy.
 */
package com.avaya.mobilevideo.utils;

import javax.net.ssl.X509TrustManager;

/**
 * Create a trust manager that does not validate the certificate chain. This trust manager should only be used for
 * testing. A better solution would be to implement a trust manager that enhances the default Android X509 TrustManager
 * behaviour by providing alternative authentication logic when the default TrustManager fails.
 * http://docs.oracle.com/javase/1.5.0/docs/guide/security/jsse/JSSERefGuide.html#X509TrustManager
 * <p/>
 * * @author Avaya Inc
 */
public class TrustAllCerts implements X509TrustManager {

    private static final String TAG = TrustAllCerts.class.getSimpleName();
    private Logger mLogger = Logger.getLogger(TAG);

    @Override
    public java.security.cert.X509Certificate[] getAcceptedIssuers() {
        mLogger.d("Get accepted issuers");
        return null;
    }

    @Override
    public void checkClientTrusted(java.security.cert.X509Certificate[] certs, String authType) {
        mLogger.d("Check client trusted: " + authType);
        if (certs != null) {
            mLogger.d("Number of certs : " + certs.length);
        }
    }

    @Override
    public void checkServerTrusted(java.security.cert.X509Certificate[] certs, String authType) {
        mLogger.d("Check server trusted; " + authType);
        if (certs != null) {
            mLogger.d("Number of certs : " + certs.length);
        }
    }
}
