
package com.avaya.mobilevideo.impl

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.Toast
import com.avaya.clientplatform.api.CameraType
import com.avaya.clientplatform.api.ClientPlatform
import com.avaya.clientplatform.api.Device
import com.avaya.clientplatform.api.User
import com.avaya.clientplatform.api.VideoResolution
import com.avaya.clientplatform.api.VideoSpecification
import com.avaya.mobilevideo.R
import com.avaya.mobilevideo.utils.Constants
import com.avaya.mobilevideo.utils.GeneralDialogFragment
import com.avaya.mobilevideo.utils.Logger

import java.util.ArrayList
import java.util.Collections
import java.util.HashMap

abstract class DialActivityImpl : MobileVideoActivity(), AdapterView.OnItemSelectedListener {
    private val mLogger = Logger.getLogger(TAG)
    private var mServer = ""
    private var mSupportedCameraCaptureResolutionMap: Map<String, VideoResolution> = HashMap()
    private var mCallActivityClass: Class<out CallActivityImpl>? = null
    private var mVideoResolution: VideoResolution? = null

    override fun onResume() {
        super.onResume()
        val thread = object : Thread() {
            override fun run() {
                setSupportedCameraCaptureResolutions()
            }
        }
        thread.start()
    }
    protected fun setCallActivityClass(callActivityClass: Class<out CallActivityImpl>) {
        mLogger.d("Set the call activity")
        mCallActivityClass = callActivityClass
    }
    private fun setSupportedCameraCaptureResolutions() {
        try {
            mLogger.d("Set supported camera capture resolutions")

            val cameraType = CameraType.FRONT
            val token = intent.extras!!.getString(Constants.DATA_SESSION_KEY)

            val platform = ClientPlatformManager.getClientPlatform(applicationContext)

            val user = platform.user

            val tokenAccepted = user.setSessionAuthorizationToken(token)
            when {
                tokenAccepted -> {
                    user.acceptAnyCertificate(true)
                    val device = platform.device
                    val videoSpecifications = device.getSupportedCameraCaptureResolutions(cameraType)
                    mSupportedCameraCaptureResolutionMap = mapVideoSpecificationsToResolutions(videoSpecifications)
                    val resolutionList = ArrayList<String>()
                    resolutionList.addAll(mSupportedCameraCaptureResolutionMap.keys)
                    resolutionList.sort()
                    resolutionList.reverse()

                    val dataAdapter = ArrayAdapter(this,
                            android.R.layout.simple_spinner_item, resolutionList)

                    val onItemSelectedListener = this
                    runOnUiThread { updateResolutionSpinner(dataAdapter, onItemSelectedListener) }
                }
                else -> {
                    mLogger.w("Invalid token used")
                    displayMessage(resources.getString(R.string.invalid_token))
                }
            }
        } catch (e: Exception) {
            mLogger.e("Exception occurred in setSupportedCameraCaptureResolutions(): " + e.javaClass + ": " + e.message, e)
            displayToast("Set supported camera capture resolutions exception: " + e.message)
        }

    }

    private fun updateResolutionSpinner(dataAdapter: ArrayAdapter<String>, onItemSelectedListener: AdapterView.OnItemSelectedListener) {
        val spinner = findViewById(R.id.resolution_spinner) as Spinner

        spinner.adapter = dataAdapter
        spinner.onItemSelectedListener = onItemSelectedListener
    }

    override fun onItemSelected(parent: AdapterView<*>, view: View,
                                pos: Int, id: Long) {

        val item = parent.getItemAtPosition(pos)
        setCameraCaptureResolution(item)
    }


    private fun setCameraCaptureResolution(item: Any?) {
        mLogger.d("Update camera capture resolution")

        if (item != null) {
            try {
                val itemValue = item as String?

                if (!mSupportedCameraCaptureResolutionMap.containsKey(itemValue)) {
                    mLogger.i("Item not found in resolution map")
                } else {
                    mVideoResolution = mSupportedCameraCaptureResolutionMap[itemValue]

                    mLogger.d("Video resolution to use: " + mVideoResolution!!)
                    Log.d("SDK","Se utilizará : " + mVideoResolution!!)

                }

            } catch (e: Exception) {
                mLogger.e("Exception: " + e.message, e)
                displayMessage("Set camera capture resolutions exception: " + e.message)
            }

        }
    }

    override fun onNothingSelected(adapterView: AdapterView<*>) {

    }

    private fun mapVideoSpecificationsToResolutions(videoSpecifications: Array<VideoSpecification>?): Map<String, VideoResolution> {
        val supportedCameraCaptureResolutionMap = HashMap<String, VideoResolution>()
        when {
            videoSpecifications != null -> {
                mLogger.d("Number of video specifications: " + videoSpecifications.size)
                Log.d("SDK","Se encontraron " + videoSpecifications.size + " Resoluciones para tu dispositivo")
                for (videoSpecification in videoSpecifications) {
                    Log.d("SDK","Añadiendo: " + videoSpecification.fps + ", " + videoSpecification.width + ", " + videoSpecification.height)
                    mLogger.d("Video specification: " + videoSpecification.fps + ", " + videoSpecification.width + ", " + videoSpecification.height)
                    when {
                        videoSpecification.width == 176 && videoSpecification.height == 144 -> if (!supportedCameraCaptureResolutionMap.containsKey(Constants.RESOLUTION_176x144)) {
                            supportedCameraCaptureResolutionMap[Constants.RESOLUTION_176x144] = VideoResolution.RESOLUTION_176x144
                            mLogger.d("Adding: " + Constants.RESOLUTION_176x144)
                            Log.d("SDK","Añadiendo" + Constants.RESOLUTION_176x144)
                        }
                        videoSpecification.width == 320 && videoSpecification.height == 180 -> if (!supportedCameraCaptureResolutionMap.containsKey(Constants.RESOLUTION_320x180)) {
                            supportedCameraCaptureResolutionMap[Constants.RESOLUTION_320x180] = VideoResolution.RESOLUTION_320x180
                            mLogger.d("Adding: " + Constants.RESOLUTION_320x180)
                            Log.d("SDK","Añadiendo" + Constants.RESOLUTION_176x144)

                        }
                        videoSpecification.width == 352 && videoSpecification.height == 288 -> if (!supportedCameraCaptureResolutionMap.containsKey(Constants.RESOLUTION_352x288)) {
                            supportedCameraCaptureResolutionMap[Constants.RESOLUTION_352x288] = VideoResolution.RESOLUTION_352x288
                            mLogger.d("Adding: " + Constants.RESOLUTION_352x288)
                            Log.d("SDK","Añadiendo" + Constants.RESOLUTION_176x144)

                        }
                        videoSpecification.width == 640 && videoSpecification.height == 360 -> if (!supportedCameraCaptureResolutionMap.containsKey(Constants.RESOLUTION_640x360)) {
                            supportedCameraCaptureResolutionMap[Constants.RESOLUTION_640x360] = VideoResolution.RESOLUTION_640x360
                            mLogger.d("Adding: " + Constants.RESOLUTION_640x360)
                            Log.d("SDK","Añadiendo" + Constants.RESOLUTION_176x144)

                        }
                        videoSpecification.width == 640 && videoSpecification.height == 480 -> if (!supportedCameraCaptureResolutionMap.containsKey(Constants.RESOLUTION_640x480)) {
                            supportedCameraCaptureResolutionMap[Constants.RESOLUTION_640x480] = VideoResolution.RESOLUTION_640x480
                            mLogger.d("Adding: " + Constants.RESOLUTION_640x480)
                            Log.d("SDK","Añadiendo" + Constants.RESOLUTION_176x144)

                        }
                        videoSpecification.width == 960 && videoSpecification.height == 720 -> if (!supportedCameraCaptureResolutionMap.containsKey(Constants.RESOLUTION_960x720)) {
                            supportedCameraCaptureResolutionMap[Constants.RESOLUTION_960x720] = VideoResolution.RESOLUTION_960x720
                            mLogger.d("Adding: " + Constants.RESOLUTION_960x720)
                            Log.d("SDK","Añadiendo" + Constants.RESOLUTION_176x144)

                        }
                        videoSpecification.width == 1280 && videoSpecification.height == 720 -> if (!supportedCameraCaptureResolutionMap.containsKey(Constants.RESOLUTION_1280x720)) {
                            supportedCameraCaptureResolutionMap[Constants.RESOLUTION_1280x720] = VideoResolution.RESOLUTION_1280x720
                            mLogger.d("Adding: " + Constants.RESOLUTION_1280x720)
                            Log.d("SDK","Añadiendo" + Constants.RESOLUTION_176x144)

                        }
                    }
                }
            }
            else -> mLogger.d("Video specifications null")
        }

        return supportedCameraCaptureResolutionMap
    }

    protected fun dialVideo(number: String, uui: String) {
        dial(number, uui, true)
    }


    protected fun dialOneWayVideo(number: String, uui: String) {
        dial(number, uui, true, true)
    }

    protected fun dialAudioOnly(number: String, uui: String) {
        dial(number, uui, false)
    }
    protected fun logout() {
        try {
            mLogger.d("Logout")

            val extras = intent.extras
            val secure = extras!!.getBoolean(Constants.DATA_KEY_SECURE)
            val sessionKey = extras.getString(Constants.DATA_KEY_SESSION_ID)
            val port = extras.getString(Constants.DATA_KEY_PORT)

            var address: String

            if (secure) {
                address = Constants.SECURE_LOGOUT_URL.replace(Constants.SERVER_PLACEHOLDER, mServer)
            } else {
                address = Constants.REGULAR_LOGOUT_URL.replace(Constants.SERVER_PLACEHOLDER, mServer)
            }

            address = address.replace(Constants.PORT_PLACEHOLDER, port!!)
            address = address.replace(Constants.SESSION_ID_PLACEHOLDER, sessionKey!!)

            LoginHandlerImpl.getInstance().logout(address)

            val returning = Intent()
            setResult(Constants.RESULT_LOGOUT, returning)
        } catch (e: Exception) {
            mLogger.w("Exception in logout(): " + e.message, e)
            displayMessage("Logout exception: " + e.message)
        }

        finish()
    }

    private fun dial(number: String, uui: String, enableVideo: Boolean, mutedVideo: Boolean = false) {
        moveToCallActivity(number, uui, enableVideo, false, mutedVideo)
    }

    private fun moveToCallActivity(numberToDial: String, uui: String, enableVideo: Boolean, startMutedAudio: Boolean,
                                   startMutedVideo: Boolean) {

        try {
            val intent = Intent(this, mCallActivityClass)

            intent.putExtras(getIntent().extras!!)
            intent.putExtra(Constants.KEY_NUMBER_TO_DIAL, numberToDial)
            intent.putExtra(Constants.KEY_ENABLE_VIDEO, enableVideo)
            intent.putExtra(Constants.KEY_START_MUTED_AUDIO, startMutedAudio)
            intent.putExtra(Constants.KEY_START_MUTED_VIDEO, startMutedVideo)
            intent.putExtra(Constants.DATA_KEY_SERVER, mServer)
            intent.putExtra(Constants.KEY_CONTEXT, uui)
            intent.putExtra(Constants.KEY_PREFERRED_VIDEO_RESOLUTION, mVideoResolution)

            CallActivityImpl.setPreferredVideoResolution(mVideoResolution)
            Log.d("Calls", mServer)
            startActivity(intent)
        } catch (e: Exception) {
            displayMessage("Move to call activity exception: " + e.message)
        }

    }

    protected open fun validateNumber(number: String?): Boolean {
        var isValid = true

        if (number == null || number.trim { it <= ' ' }.length == 0) {
            isValid = false
        }

        return isValid
    }

    protected open fun validateUui(uui: String?): Boolean {
        var isValid = true

        if (uui != null && uui.trim { it <= ' ' }.isNotEmpty()) {
            if (!uui.matches(Constants.ALPHA_NUMERIC_REGEX.toRegex())) {
                isValid = false
            }
        }

        return isValid
    }

    protected fun setServer(server: String) {
        mServer = server
    }

    private fun displayToast(message: String) {
        val activity = this

        try {
            runOnUiThread { Toast.makeText(activity, message, Toast.LENGTH_LONG).show() }

        } catch (e: Exception) {
            mLogger.e("Exception in displayToast()", e)
        }

    }

    private fun displayMessage(message: String, finishActivity: Boolean = false) {
        mLogger.d("Display message: $message")
        val activity = this

        try {
            runOnUiThread { GeneralDialogFragment.displayMessage(activity, message, finishActivity) }

        } catch (e: Exception) {
            mLogger.e("Exception in displayMessage", e)
        }

    }

    companion object {
        private val TAG = DialActivityImpl::class.java.simpleName
    }
}
