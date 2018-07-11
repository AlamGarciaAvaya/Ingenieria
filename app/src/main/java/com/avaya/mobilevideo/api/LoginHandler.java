/**
 * LoginHandler.java <br>
 * Copyright 2014-2015 Avaya Inc. <br>
 * All rights reserved. Usage of this source is bound to the terms described the file
 * MOBILE_VIDEO_SDK_LICENSE_AGREEMENT.txt, included in this SDK.<br>
 * Avaya – Confidential & Proprietary. Use pursuant to your signed agreement or Avaya Policy.
 */
package com.avaya.mobilevideo.api;

import android.os.Bundle;

public interface LoginHandler {
    static final int ERROR_CONNECTION_FAILED = -1;
    static final int ERROR_LOGIN_FAILED = -2;
    Bundle login(final String url, final String data, final boolean trustAllCerts);
    Bundle login(final String url, final String data, final boolean trustAllCerts, final boolean doAsPost);
    void logout(final String address);
}
