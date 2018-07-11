/**
 * LoginHandlerImpl.java <br>
 * Copyright 2014-2015 Avaya Inc. <br>
 * All rights reserved. Usage of this source is bound to the terms described the file
 * MOBILE_VIDEO_SDK_LICENSE_AGREEMENT.txt, included in this SDK.<br>
 * Avaya – Confidential & Proprietary. Use pursuant to your signed agreement or Avaya Policy.
 */
package com.avaya.mobilevideo.impl;

import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;

import com.avaya.mobilevideo.api.LoginHandler;
import com.avaya.mobilevideo.utils.Constants;
import com.avaya.mobilevideo.utils.Logger;
import com.avaya.mobilevideo.utils.NullHostNameVerifier;
import com.avaya.mobilevideo.utils.TrustAllCerts;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.UnknownHostException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;

public final class LoginHandlerImpl implements LoginHandler {
    private static LoginHandler sLoginHandler = null;
    private static final String TAG = LoginHandlerImpl.class.getSimpleName();
    private boolean mTrustAllCerts;
    private static final String SELF_SIGNED_CERTIFICATE_NAME = "";
    private static final String HTTP = "http";
    private static final String TLS = "TLS";
    private static final String X509 = "X.509";
    private static final String CA = "ca";
    private static final String CONTENT_TYPE_HEADER_NAME = "Content-Type";
    private static final String CONTENT_TYPE_HEADER_VALUE = "application/json";
    private static final String USER_AGENT_HEADER_NAME = "User-Agent";
    private static final String USER_AGENT_HEADER_VALUE = "Android" + Build.DEVICE + "/" + Build.VERSION.CODENAME;
    private static final String SESSION_ID_PROPERTY = "sessionid";
    private static final int READ_BUF_SIZE = 256;
    private static final int CONNECTION_TIMEOUT = 10000;
    private HttpURLConnection mHttpConnection;
    /**
     * Code received from the server in response to a given request.
     */
    private int mResponseCode;

    /**
     * Response message received from the server in response to a given request.
     */
    private String mResponseMessage;

    /**
     * Message from exception thrown when attempting to login
     */
    private String mExceptionMessage;

    private String mResponseBody;

    private Logger mLogger = Logger.getLogger(TAG);

    private LoginHandlerImpl() {
    }

    public static LoginHandler getInstance() {
        if (sLoginHandler == null) {
            sLoginHandler = new LoginHandlerImpl();
        }
        return sLoginHandler;
    }
    @Override
    public Bundle login(final String url, final String data, final boolean trustAllCerts) {
        return login(url, data, trustAllCerts, true);
    }

    @Override
    public Bundle login(final String url, final String data, final boolean trustAllCerts, final boolean doAsPost) {
        mLogger.d("Login");

        final Bundle bundle = new Bundle();

        mTrustAllCerts = trustAllCerts;

        final int loginOutcome = sendMessage(url, data, doAsPost);

        if (loginOutcome < 0) {
            bundle.putInt(Constants.DATA_KEY_ERROR, loginOutcome);
            bundle.putInt(Constants.DATA_KEY_RESPONSE_CODE, mResponseCode);
            bundle.putString(Constants.DATA_KEY_RESPONSE_MESSAGE, mResponseMessage);
            bundle.putString(Constants.DATA_KEY_EXCEPTION_MESSAGE, mExceptionMessage);
        } else if (TextUtils.isEmpty(mResponseBody)) {
            bundle.putInt(Constants.DATA_KEY_ERROR, LoginHandler.ERROR_LOGIN_FAILED);
            bundle.putInt(Constants.DATA_KEY_RESPONSE_CODE, mResponseCode);
            bundle.putString(Constants.DATA_KEY_RESPONSE_MESSAGE, mResponseMessage);
        } else {
            mLogger.d("Login response: " + mResponseBody);
            bundle.putString(Constants.DATA_SESSION_KEY, mResponseBody);

            // Parse the response body for the session key

            try {
                final JSONObject responseData = new JSONObject(mResponseBody);
                String key = responseData.getString(SESSION_ID_PROPERTY);
                bundle.putString(Constants.DATA_KEY_SESSION_ID, key);
            } catch (JSONException e) {
                mLogger.e("Error parsing JSON string " + mResponseBody + " - " + e.getMessage(), e);
                bundle.putInt(Constants.DATA_KEY_ERROR, LoginHandler.ERROR_LOGIN_FAILED);
                bundle.putInt(Constants.DATA_KEY_RESPONSE_CODE, mResponseCode);
                bundle.putString(Constants.DATA_KEY_RESPONSE_MESSAGE, mResponseMessage);
            }
        }

        // reset response codes and messages before next login attempt
        mResponseCode = 0;
        mResponseMessage = "";
        mExceptionMessage = "";

        return bundle;
    }

    /**
     * @param address The address that we want to send our logout message to.
     */
    @Override
    public void logout(final String address) {
        mLogger.d("Logout");

        new Thread(new Runnable() {

            @Override
            public void run() {
                try {
                    final HttpURLConnection connection = (HttpURLConnection) new URL(address).openConnection();
                    connection.setRequestMethod("DELETE");
                    connection.connect();
                    connection.disconnect();
                } catch (IOException e) {
                    mLogger.e("Sending logout message failed: " + e.getLocalizedMessage(), e);
                }
            }
        }, "Logging out").start();
    }

    /**
     * Create a HTTP connection to the given address.
     *
     * @param address Address we want to make a connection to
     * @return A valid {@link HttpURLConnection}, or <code><b>null</b></code> if the connection failed.
     */
    private HttpURLConnection createConnection(final String address, boolean doAsPost) {
        return createConnection(address, true, doAsPost);
    }

    /**
     * Create connection
     *
     * @param address
     * @param retry
     * @param doAsPost
     * @return
     */
    private HttpURLConnection createConnection(final String address, final boolean retry, boolean doAsPost) {
        mLogger.d("Create connection");

        // attempt authentication against a network service.
        URL url;
        try {
            url = new URL(address);
        } catch (MalformedURLException e) {
            mLogger.e("MalformedURLException: " + e, e);
            mExceptionMessage = e.getMessage();
            return null;
        }

        mLogger.d("URL: " + url.toExternalForm());

        HttpURLConnection connection;
        try {
            connection = getHttpOrHttpsConnection(url);

        } catch (Exception e) {
            mLogger.e("Could not getHttpOrHttpsConnection: " + url + " - " + e.getMessage(), e);
            mExceptionMessage = e.getMessage();
            return null;
        }

        try {
            connection.setDoOutput(doAsPost);
            connection.setRequestProperty(USER_AGENT_HEADER_NAME, USER_AGENT_HEADER_VALUE);
            connection.setRequestProperty(CONTENT_TYPE_HEADER_NAME, CONTENT_TYPE_HEADER_VALUE);
            connection.setConnectTimeout(CONNECTION_TIMEOUT);
            connection.connect();
        } catch (SocketTimeoutException ste) {
            mExceptionMessage = ste.getMessage();
            if (retry) {
                return handleConnectionFailed(address, url, ste, doAsPost);
            } else {
                mLogger.w("Connection timed out, we waited for 10 seconds. Is "
                        + "the device connected to the network correctly?");
                return null;
            }
        } catch (ConnectException ce) {
            mExceptionMessage = ce.getMessage();
            if (retry) {
                return handleConnectionFailed(address, url, ce, doAsPost);
            } else {
                mLogger.w("Server refused the connection");
                return null;
            }
        } catch (Exception e) {
            mExceptionMessage = e.getMessage();
            mLogger.e("Exception: " + e.getMessage(), e);
            return null;
        }

        return connection;
    }

    private HttpURLConnection getHttpOrHttpsConnection(URL url) throws IOException, NoSuchAlgorithmException,
            KeyManagementException, KeyStoreException, CertificateException {

        HttpURLConnection connection;

        if (url.getProtocol().equals(HTTP)) { // HTTP
            connection = (HttpURLConnection) url.openConnection();
        } else { // HTTPS
            mLogger.d("Trust all certificates: " + mTrustAllCerts);

            if (mTrustAllCerts) {
                connection = getHttpsConnectionTrustAllCerts(url);
            } else {
                connection = getHttpsConnectionCheckSelfSignedCert(url);
            }
        }

        return connection;
    }

    private HttpURLConnection getHttpsConnectionCheckSelfSignedCert(URL url) throws IOException, NoSuchAlgorithmException,
            KeyManagementException, KeyStoreException, CertificateException {
        mLogger.d("Get HTTPS connection using a self-signed certificate");

        TrustManager[] trustManagers = getTrustManagers(SELF_SIGNED_CERTIFICATE_NAME);

        return getHttpsConnection(url, trustManagers);
    }

    /**
     * Get a HTTPS connection, will accept all certificates. This approach should only be used during development and testing.
     *
     * @param url
     * @return
     * @throws IOException
     * @throws NoSuchAlgorithmException
     * @throws KeyManagementException
     * @throws KeyStoreException
     * @throws CertificateException
     */
    private HttpURLConnection getHttpsConnectionTrustAllCerts(URL url) throws IOException, NoSuchAlgorithmException,
            KeyManagementException, KeyStoreException, CertificateException {
        mLogger.d("Get HTTPS connection, trust all certificates");

        return getHttpsConnection(url, new TrustManager[]{new TrustAllCerts()});
    }
    private HttpURLConnection getHttpsConnection(URL url, TrustManager[] trustManagers) throws IOException, NoSuchAlgorithmException,
            KeyManagementException, KeyStoreException, CertificateException {
        HttpURLConnection connection;

        SSLContext context = SSLContext.getInstance(TLS);

        context.init(null, trustManagers, null);

        HttpsURLConnection.setDefaultHostnameVerifier(new NullHostNameVerifier());
        HttpsURLConnection.setDefaultSSLSocketFactory(context.getSocketFactory());

        connection = (HttpsURLConnection) url.openConnection();

        return connection;
    }
    private TrustManager[] getTrustManagers(String certName) throws CertificateException, IOException, KeyStoreException, NoSuchAlgorithmException {
        TrustManagerFactory factory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());

        try {
            CertificateFactory cf = CertificateFactory.getInstance(X509);
            InputStream caInput = new BufferedInputStream(LoginActivityImpl.Companion.getSContext().getAssets().open(certName));
            Certificate ca = cf.generateCertificate(caInput);
            mLogger.d("ca=" + ((X509Certificate) ca).getSubjectDN());

            String keyStoreType = KeyStore.getDefaultType();
            KeyStore keyStore = KeyStore.getInstance(keyStoreType);
            keyStore.load(null, null);
            keyStore.setCertificateEntry(CA, ca);

            factory.init(keyStore);
        } catch (FileNotFoundException e) {
            mLogger.e("Cert not found exception", e);
            throw new CertNotFoundException();
        }

        return factory.getTrustManagers();
    }
    private HttpURLConnection handleConnectionFailed(String address, URL url, IOException cause, boolean doAsPost) {
        final String host = url.getHost();
        try {
            final InetAddress[] allAddresses = InetAddress.getAllByName(host);
            if (allAddresses.length > 1) {
                HttpURLConnection connection;
                for (InetAddress retryAddress : allAddresses) {
                    connection = createConnection(address.replaceFirst(host, retryAddress.getHostAddress()), false,
                            doAsPost);
                    if (connection != null) {
                        return connection;
                    }
                }
            }

            mLogger.e("Could not connect: " + cause.getMessage());

            return null;
        } catch (UnknownHostException e) {
            /*
             * This shouldn't happen, as any UnknownHostException should have been thrown earlier, but log it in case it
             * does.
             */
            mLogger.e("Could not resolve address " + address);
            return null;
        }
    }

    /**
     * Send a POST request using the given connection and sending the given content.
     *
     * @param content {@link String} containing the data that we want to send.
     * @return <code><b>true</b></code> if the POST succeeded, <code><b>false</b></code> otherwise.
     */
    private boolean post(final String content) {
        mLogger.d("POST: " + content);

        try {
            final OutputStream out = mHttpConnection.getOutputStream();
            out.write(content.getBytes());
            out.flush();
        } catch (IOException e) {
            mLogger.e("Exception: " + e.getMessage(), e);
            return false;
        }

        return true;
    }

    /**
     * Opens an input stream on the specified connection, reads all the available data, closes the stream, returns the
     * data.
     *
     * @param in The {@link InputStream} to read fom
     * @return The data read from the stream, as a {@link String}
     * @throws IOException
     */
    private String readAllFromConnection(final InputStream in) throws IOException {
        try {
            final byte[] data = new byte[READ_BUF_SIZE];
            int len;
            final StringBuilder stringBuilder = new StringBuilder();

            while (-1 != (len = in.read(data))) {
                stringBuilder.append(new String(data, 0, len));
            }

            return stringBuilder.toString();
        } finally {
            in.close();
        }
    }

    /**
     * Get the response from the given connection.
     *
     * @return <code><b>true</b></code> if we got a valid response, <code><b>false</b></code> otherwise.
     */
    private boolean getResponse() {
        try {
            mResponseCode = mHttpConnection.getResponseCode();
            mResponseMessage = mHttpConnection.getResponseMessage();
            mLogger.d("Got response: " + mResponseCode + " " + mHttpConnection.getResponseMessage());

            final InputStream inputStream = mHttpConnection.getInputStream();
            mResponseBody = readAllFromConnection(inputStream);
        } catch (Exception e) {
            final InputStream es = mHttpConnection.getErrorStream();
            if (es != null) {
                mLogger.e("getResponse error");
                return false;
            }

            mLogger.e("Exception: " + e.getMessage(), e);

            return false;
        } finally {
            mHttpConnection.disconnect();
        }

        return true;
    }

    /**
     * @param message A {@link String} containing the message we want to send.
     * @return <code><b>true</b></code> if the message was sent successfully, <code><b>falase</b></code> if not.
     */
    private boolean doSendMessage(final String message, boolean doAsPost) {
        if (doAsPost && message != null && message.length() > 0) {
            // Send the call connect request
            if (!post(message)) {
                mLogger.e("Failed to post the message");
                mHttpConnection.disconnect();
                return false;
            }
        }

        // What's the answer? Are we good?
        if (!getResponse()) {
            mLogger.e("Failed to get the response");
            return false;
        }

        if (mResponseCode != HttpURLConnection.HTTP_OK) {
            mLogger.w("Unexpected response code: " + mResponseCode);
            return false;
        }

        return true;
    }

    /**
     * Create a connection to the given address and send the given message.
     *
     * @param url     The url to connect to
     * @param message The message to send
     * @return 0 if the message was sent successfully, or one of our error codes.
     */
    private int sendMessage(final String url, final String message, boolean doAsPost) {
        mLogger.d("send message");

        mHttpConnection = createConnection(url, doAsPost);
        if (mHttpConnection == null) {
            mLogger.e("Failed to create a connection to '" + url + "'");
            return LoginHandler.ERROR_CONNECTION_FAILED;
        }

        if (doSendMessage(message, doAsPost)) {
            return 0;
        } else {
            return LoginHandler.ERROR_LOGIN_FAILED;
        }
    }

    public class CertNotFoundException extends FileNotFoundException {

        @Override
        public String getMessage() {
            return "Certificate not found";
        }
    }
}


