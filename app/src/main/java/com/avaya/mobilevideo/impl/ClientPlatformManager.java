/**
 * ClientPlatformManager.java <br>
 * Copyright 2015 Avaya Inc. <br>
 * All rights reserved. Usage of this source is bound to the terms described the file
 * MOBILE_VIDEO_SDK_LICENSE_AGREEMENT.txt, included in this SDK.<br>
 * Avaya – Confidential & Proprietary. Use pursuant to your signed agreement or Avaya Policy.
 */
package com.avaya.mobilevideo.impl;

import android.content.Context;

import com.avaya.clientplatform.api.ClientPlatform;
import com.avaya.clientplatform.api.ClientPlatformFactory;

/**
 * Client platform factory class
 *
 * @author Avaya Inc
 */
public class ClientPlatformManager {

    private static ClientPlatform sClientPlatform;

    public static synchronized ClientPlatform getClientPlatform(Context context) {

        if (sClientPlatform != null) {
            return sClientPlatform;
        }

        sClientPlatform = ClientPlatformFactory.getClientPlatformInterface(context);

        return sClientPlatform;
    }

}

