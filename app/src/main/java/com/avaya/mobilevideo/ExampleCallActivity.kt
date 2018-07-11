/**
 * ExampleCallActivity.java <br></br>
 * Copyright 2014-2015 Avaya Inc. <br></br>
 * All rights reserved. Usage of this source is bound to the terms described the file
 * MOBILE_VIDEO_SDK_LICENSE_AGREEMENT.txt, included in this SDK.<br></br>
 * Avaya – Confidential & Proprietary. Use pursuant to your signed agreement or Avaya Policy.
 */
package com.avaya.mobilevideo

import android.media.AudioManager
import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.ToggleButton

import com.avaya.mobilevideo.api.CallActivity
import com.avaya.mobilevideo.impl.CallActivityImpl
import com.avaya.mobilevideo.utils.Constants

class ExampleCallActivity : CallActivityImpl(), CallActivity {

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        volumeControlStream = AudioManager.STREAM_VOICE_CALL

        setReferencedClass()

        setContentView(R.layout.activity_call)
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)

        setControls()
    }

    override fun onResume() {
        super.onResume()

        val extras = intent.extras
        val enableVideo = extras!!.getBoolean(Constants.KEY_ENABLE_VIDEO)

        val toggleButton = findViewById(R.id.btnEnableVideo) as ToggleButton
        toggleButton.isChecked = enableVideo
    }

    override fun toggleHold(v: View) {
        if (callOnHold) {
            showControls()
        } else {
            hideControls()
        }

        super.toggleHold(v)
    }

    private fun hideControls() {
        setButtonsVisibility(View.INVISIBLE)
    }

    private fun showControls() {
        setButtonsVisibility(View.VISIBLE)
    }

    private fun setButtonsVisibility(visibility: Int) {
        setVisibility(findViewById(R.id.btnMuteAudio), visibility)
        setVisibility(findViewById(R.id.btnEnableVideo), visibility)
        setVisibility(findViewById(R.id.mute_video), visibility)
        setVisibility(findViewById(R.id.btnSwitchVideo), visibility)
        setVisibility(findViewById(R.id.btnDtmf), visibility)
        setVisibility(findViewById(R.id.end_call), visibility)
    }

    private fun setVisibility(button: View, visibility: Int) {
        if (visibility == View.VISIBLE || visibility == View.INVISIBLE) {
            button.visibility = visibility
        }
    }
    override fun setControls() {
        setDisplayNameField(findViewById(R.id.displayNameField) as TextView)
        setCalleeNumberDisplay(findViewById(R.id.callee_number_display) as TextView)
        setMuteVideo(findViewById(R.id.mute_video) as ToggleButton)
    }
    override fun setReferencedClass() {}
}
