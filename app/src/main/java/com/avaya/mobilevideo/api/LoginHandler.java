/**
 * LoginHandler.java <br>
 * Copyright 2014-2015 Avaya Inc. <br>
 * All rights reserved. Usage of this source is bound to the terms described the file
 * MOBILE_VIDEO_SDK_LICENSE_AGREEMENT.txt, included in this SDK.<br>
 * Avaya – Confidential & Proprietary. Use pursuant to your signed agreement or Avaya Policy.
 */
package com.avaya.mobilevideo.api;

import android.os.Bundle;

/**
 * Interface that your login handler should implement
 * @author Avaya Inc
 */
public interface LoginHandler {

    /**
     * Error code telling us that we were unable to make a valid connection.
     */
    static final int ERROR_CONNECTION_FAILED = -1;

    /**
     * Error code telling us that something went wrong with the login.
     */
    static final int ERROR_LOGIN_FAILED = -2;

    /**
     * Login to the web-app server passing the given data. Create a connection to the server, send the POST request and
     * read the response to get the session key.
     * @param url The url of the server we want to login to
     * @param data The data we want to login with
     * @return A {@link Bundle} that either contains an <code>int</code> error code, or a valid <code>String</code>
     *         session ID.
     */
    Bundle login(final String url, final String data, final boolean trustAllCerts);

    /**
     * Login to the web-app server passing the given data. Create a connection to the server, send the POST request and
     * read the response to get the session key.
     * @param url The url of the server we want to login to
     * @param data The data we want to login with
     * @return A {@link Bundle} that either contains an <code>int</code> error code, or a valid <code>String</code>
     *         session ID.
     */
    Bundle login(final String url, final String data, final boolean trustAllCerts, final boolean doAsPost);

    /**
     * @param address The address that we want to send our logout message to.
     */
    void logout(final String address);
}
