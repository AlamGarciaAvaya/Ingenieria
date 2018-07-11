/**
 * Constants.java <br>
 * Copyright 2014-2015 Avaya Inc. <br>
 * All rights reserved. Usage of this source is bound to the terms described the file
 * MOBILE_VIDEO_SDK_LICENSE_AGREEMENT.txt, included in this SDK.<br>
 * Avaya – Confidential & Proprietary. Use pursuant to your signed agreement or Avaya Policy.
 */
package com.avaya.mobilevideo.utils;

public interface Constants {

    // App version will be appended to the SDK version
    int APP_VERSION = 1;

    int MAX_CONTEXT_ID_LENGTH = 32;
    String HTTP = "http://";
    String HTTPS = "https://";
    String SERVER_PLACEHOLDER = "{server}";
    String LOGIN_EXTENSION = "/avayatest/auth";
    String LOGOUT_EXTENSION = "/avayatest/auth/id/";
    String SESSION_ID_PLACEHOLDER = "{session_id}";
    String PORT_PLACEHOLDER = "{port}";

    String REGULAR_LOGIN_URL = HTTP + SERVER_PLACEHOLDER + ":" + PORT_PLACEHOLDER + LOGIN_EXTENSION;
    String SECURE_LOGIN_URL = HTTPS + SERVER_PLACEHOLDER + ":" + PORT_PLACEHOLDER + LOGIN_EXTENSION;

    String REGULAR_LOGOUT_URL = HTTP + SERVER_PLACEHOLDER + ":" + PORT_PLACEHOLDER + LOGOUT_EXTENSION
            + SESSION_ID_PLACEHOLDER;
    String SECURE_LOGOUT_URL = HTTPS + SERVER_PLACEHOLDER + ":" + PORT_PLACEHOLDER + LOGOUT_EXTENSION
            + SESSION_ID_PLACEHOLDER;

    /**
     * Key for the session key value.
     */
    String DATA_SESSION_KEY = "_session_key";

    String DATA_KEY_SESSION_ID = "_session_id";
    String DATA_KEY_ERROR = "_error";
    String DATA_KEY_SERVER = "_server";
    String DATA_KEY_SECURE = "_secure";
    String DATA_KEY_PORT = "_port";
    String DATA_KEY_RESPONSE_CODE = "_responseCode";
    String DATA_KEY_RESPONSE_MESSAGE = "_responseMessage";
    String DATA_KEY_EXCEPTION_MESSAGE = "_exceptionMessage";

    String KEY_ENABLE_VIDEO = "key_enableVideo";
    String KEY_START_MUTED_AUDIO = "key_startMutedAudio";
    String KEY_START_MUTED_VIDEO = "key_startMutedVideo";
    String KEY_NUMBER_TO_DIAL = "key_numberToDial";
    String KEY_CONTEXT = "key_context";
    String KEY_PREFERRED_VIDEO_RESOLUTION = "key_preferredVideoResolution";

    String PLACEHOLDER1 = "%1";

    long TIMER_INTERVAL = 100L;
    String CALL_TIME_ELAPSED_FORMAT = "%s - %s%02d:%02d%s";
    String CALL_TIME_ELAPSED_SEPARATOR = "(";
    String CALL_TIME_ELAPSED_END = ")";

    String RESOLUTION_176x144 = "176x144";
    String RESOLUTION_320x180 = "320x180";
    String RESOLUTION_352x288 = "352x288";
    String RESOLUTION_640x360 = "640x360";
    String RESOLUTION_640x480 = "640x480";
    String RESOLUTION_960x720 = "960x720";
    String RESOLUTION_1280x720 = "1280x720";

    String REPORT_ISSUE_SUBJECT = "Reporting issue " + PLACEHOLDER1;

    String PREFERENCE_EMAIL_ADDRESS = "preference_email_address";
    String PREFERENCE_LOG_TO_DEVICE = "preference_log_to_device";
    String PREFERENCE_LOG_LEVEL = "preference_log_level";
    String PREFERENCE_LOG_FILE_NAME = "preference_log_file_name";
    String PREFERENCE_MAX_FILE_SIZE = "preference_max_file_size";
    String PREFERENCE_MAX_BACK_UPS = "preference_max_back_ups";
    String PREFERENCE_SECURE_LOGIN = "preference_secure_login";
    String PREFERENCE_PORT = "preference_port";
    String PREFERENCE_TRUST_ALL_CERTS = "preference_trust_all_certs";

    String LOG_LEVEL_DEBUG = "debug";
    String LOG_LEVEL_INFO = "info";
    String LOG_LEVEL_WARN = "warn";
    String LOG_LEVEL_ERROR = "error";

    int RESULT_LOGOUT = 3;

    int MINIMUM_CALL_QUALITY = 0;
    int MAXIMUM_CALL_QUALITY = 100;

    String ALPHA_NUMERIC_REGEX = "^[a-zA-Z0-9\\s]*$";
}
