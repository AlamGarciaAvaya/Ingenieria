
package com.avaya.mobilevideo.api;

import android.view.View;

public interface DialActivity {
    void dialVideo(View v);
    /**
     * Make one-way video call, app receives video but doesn't broadcast it
     */
    void dialOneWayVideo(View v);
    void dialAudioOnly(View v);
    void logout(View v);
}
