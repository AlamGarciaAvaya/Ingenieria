/**
 * CallActivityImpl.java <br>
 * Copyright 2014-2015 Avaya Inc. <br>
 * All rights reserved. Usage of this source is bound to the terms described the file
 * MOBILE_VIDEO_SDK_LICENSE_AGREEMENT.txt, included in this SDK.<br>
 * Avaya – Confidential & Proprietary. Use pursuant to your signed agreement or Avaya Policy.
 */
package com.avaya.mobilevideo.impl;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Point;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.avaya.clientplatform.api.CameraType;
import com.avaya.clientplatform.api.ClientPlatform;
import com.avaya.clientplatform.api.DTMFType;
import com.avaya.clientplatform.api.Device;
import com.avaya.clientplatform.api.Orientation;
import com.avaya.clientplatform.api.Participant;
import com.avaya.clientplatform.api.Session;
import com.avaya.clientplatform.api.SessionError;
import com.avaya.clientplatform.api.SessionException;
import com.avaya.clientplatform.api.SessionListener2;
import com.avaya.clientplatform.api.SessionState;
import com.avaya.clientplatform.api.User;
import com.avaya.clientplatform.api.UserListener2;
import com.avaya.clientplatform.api.VideoListener;
import com.avaya.clientplatform.api.VideoResolution;
import com.avaya.clientplatform.api.VideoSurface;
import com.avaya.clientplatform.impl.SessionImpl;
import com.avaya.clientplatform.impl.VideoSurfaceImpl;
import com.avaya.mobilevideo.R;
import com.avaya.mobilevideo.utils.Constants;
import com.avaya.mobilevideo.utils.GeneralDialogFragment;
import com.avaya.mobilevideo.utils.InternetConnectionDetector;
import com.avaya.mobilevideo.utils.Logger;

public abstract class CallActivityImpl extends MobileVideoActivity implements SessionListener2, UserListener2 {

    private static final String TAG = CallActivityImpl.class.getSimpleName();
    private static VideoResolution mPreferredVideoResolution = VideoResolution.RESOLUTION_1280x720;

    private Logger mLogger = Logger.getLogger(TAG);

    private Device mDevice;
    private User mUser;
    private ClientPlatform mPlatform;
    private SessionImpl mSession;
    private VideoSurface mRemoteVideoSurface;
    private VideoSurface mPreviewView;
    private TextView mDisplayNameTextView = null;
    private TextView mCalleeNumberDisplay = null;
    private ToggleButton mMuteVideo = null;
    private boolean mCallOnHold = false;

    private boolean mEnableVideo;
    private boolean mStartAudioMuted;
    private boolean mStartVideoMuted;
    private String mContextId;
    private CameraType mCamera;

    private PopupWindow mDtmfPopupWindow = null;

    private String mStatusText = "";
    private Handler mTimerHandler;

    private Runnable mCallTimeChecker = new Runnable() {
        @Override
        public void run() {
            updateCallTime();
            mTimerHandler.postDelayed(mCallTimeChecker, Constants.TIMER_INTERVAL);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mTimerHandler = new Handler();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mLogger.d("onDestroy()");

        mPlatform = null;
        mUser = null;
        if (mDevice != null) {
            mDevice.setLocalVideoView(null);
            mDevice = null;
        }

        mRemoteVideoSurface = null;
        mPreviewView = null;
    }

    @Override
    protected void onResume() {
        super.onResume();

        try {
            mLogger.d("onResume()");

            Bundle extras = getIntent().getExtras();
            mContextId = extras.getString(Constants.KEY_CONTEXT).trim();
            mEnableVideo = extras.getBoolean(Constants.KEY_ENABLE_VIDEO);
            mStartAudioMuted = extras.getBoolean(Constants.KEY_START_MUTED_AUDIO);
            mStartVideoMuted = extras.getBoolean(Constants.KEY_START_MUTED_VIDEO);

            mCamera = CameraType.FRONT;
            mPlatform = ClientPlatformManager.getClientPlatform(this.getApplicationContext());
            mUser = mPlatform.getUser();
            String token = extras.getString(Constants.DATA_SESSION_KEY);
            Log.d("SDK", token);

            boolean tokenAccepted = mUser.setSessionAuthorizationToken(token);

            if (tokenAccepted) {
                mUser.registerListener(this);
                mUser.acceptAnyCertificate(true);
                mPlatform.getDevice();
                Log.d("SDK", mPlatform.getDevice().toString());


                if (mSession == null) {
                    if (mUser.isServiceAvailable()) {
                        mLogger.d("service available, make call now");
                        call();
                    } else {
                        mLogger.e("service not available");
                        String message = addNetworkConnectionMessage(getResources().getString(R.string.service_unavailable));
                        hangup();
                        displayMessage(message, true);
                    }
                } else {
                    mLogger.d("session state: " + mSession.getState() + ", not placing call now");
                }
            } else {
                mLogger.w("Invalid token used");
                displayMessage(getResources().getString(R.string.invalid_token), true);
            }
        } catch (Exception e) {
            mLogger.e("Exception in onResume(): " + e.getMessage(), e);
            displayMessage("Call activity resume exception: " + e.getMessage());
        }
    }

    private void call() {
        mLogger.d("Placing call");

        try {
            mDevice = mPlatform.getDevice();

            ClientPlatform clientPlatform = ClientPlatformManager.getClientPlatform(this.getApplicationContext());

            String browser = clientPlatform.getUserAgentBrowser();
            String version = clientPlatform.getUserAgentVersion();

            mSession = (SessionImpl) mUser.createSession();
            mSession.registerListener(this);

            mSession.enableAudio(true);
            mSession.enableVideo(mEnableVideo);
            mSession.muteAudio(mStartAudioMuted);
            mSession.muteVideo(mStartVideoMuted);

            if (mStartVideoMuted) {
                uncheckMuteVideoControl();
            }

            if (mContextId != null && mContextId.length() > 0) {
                mLogger.d("Context ID:" + mContextId);
                mSession.setContextId(mContextId);
            }

            String numberToDial = getIntent().getExtras().getString(Constants.KEY_NUMBER_TO_DIAL);
            Log.d("SDK", numberToDial);

            mSession.setRemoteAddress(numberToDial);

            if (mEnableVideo) {

                mDevice.selectCamera(mCamera);
                mDevice.setCameraCaptureResolution(mPreferredVideoResolution);
            }
            mDevice.setCameraCaptureOrientation(Orientation.TWO_SEVENTY);
            mSession.start();

            setCalleeDisplayInformation(mSession.getState());
            mLogger.d("Browser: " + browser + ", version: " + version);
            mLogger.d("Orientation: " + mDevice.getCameraCaptureOrientation());
            mLogger.d("Camera capture resolution: " + mDevice.getCameraCaptureResolution());
            mLogger.d("Session authorisation token: " + mUser.getSessionAuthorizationToken());

        } catch (Exception e) {
            mLogger.e("Exception occurred in CallActivityImpl.call(): " + e.getMessage(), e);
            displayMessage(getResources().getString(R.string.call_failed) + e.getMessage());
            finish();
        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        createVideoComponents();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mLogger.d("onStop()");

        if (mDtmfPopupWindow != null) {
            mDtmfPopupWindow.dismiss();
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        mLogger.d("onBackPressed()");
        hangup();
        finish();
    }

    /**
     * @param savedInstanceState
     */
    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
    }

    /**
     * Abstract set controls method
     */
    protected abstract void setControls();

    /**
     * Set display name field
     *
     * @param displayNameTextView
     */
    protected void setDisplayNameField(TextView displayNameTextView) {
        this.mDisplayNameTextView = displayNameTextView;
    }

    /**
     * Set calee number display
     *
     * @param calleeNumberDisplay
     */
    protected void setCalleeNumberDisplay(TextView calleeNumberDisplay) {
        this.mCalleeNumberDisplay = calleeNumberDisplay;
    }
    protected void setMuteVideo(ToggleButton muteVideo) {
        this.mMuteVideo = muteVideo;
    }
    public void endCall(View V) {
        endCall();
    }

    protected void endCall() {
        try {
            hangup();

            finish();
        } catch (Throwable e) {
            mLogger.e("Throwable in endCall()", e);
        }
    }

    /**
     * Toggle mute audio
     *
     * @param v
     */
    public void toggleMuteAudio(View v) {
        try {
            mLogger.d("Toggle mute audio");
            boolean wasMuted = mSession.isAudioMuted();
            boolean nowMuted = !wasMuted;

            mSession.muteAudio(nowMuted);
        } catch (Exception e) {
            mLogger.e("Exception in toggleMuteAudio()", e);
            displayMessage("Mute audio exception: " + e.getMessage());
        }
    }

    public void toggleHold(View v) {
        try {
            mLogger.d("Toggle hold");

            if (!mCallOnHold) {
                mLogger.d("Put call on hold");
                mSession.hold();
                mCallOnHold = true;
            } else {
                mLogger.d("Take call off hold");
                mSession.resume();
                mCallOnHold = false;
            }
        } catch (Exception e) {
            mLogger.e("Exception in toggleHold()", e);
            displayMessage("Hold exception: " + e.getMessage());
        }
    }

    /**
     * Toggle mute video
     *
     * @param v
     */
    public void toggleMuteVideo(View v) {
        try {
            mLogger.d("Toggle mute video");
            boolean wasMuted = mSession.isVideoMuted();
            boolean nowMuted = !wasMuted;

            mSession.muteVideo(nowMuted);
        } catch (Exception e) {
            mLogger.e("Exception in toggleMuteVideo()", e);
            displayMessage("Mute video exception: " + e.getMessage());
        }
    }

    /**
     * Toggle enable audio
     *
     * @param v
     */
    public void toggleEnableAudio(View v) {
        try {
            mLogger.d("Toggle enable audio");
            boolean wasEnabled = mSession.isAudioEnabled();
            boolean nowEnabled = !wasEnabled;

            mSession.enableAudio(nowEnabled);
        } catch (Exception e) {
            mLogger.e("Exception in toggleEnableAudio()", e);
            displayMessage("Enable audio exception: " + e.getMessage());
        }
    }

    /**
     * Switch video between front and back camera
     *
     * @param v
     */
    public void switchVideo(View v) {
        try {
            mLogger.d("Switch camera");

            switch (mDevice.getSelectedCamera()) {
                case BACK:
                    mLogger.d("Using back camera, switch to front");
                    mCamera = CameraType.FRONT;
                    break;

                case FRONT:
                    mLogger.d("Using front camera, switch to back");
                    mCamera = CameraType.BACK;
                    break;

                default:
                    mLogger.d("Camera DEFAULT: " + mDevice.getSelectedCamera());
                    break;
            }

            mDevice.selectCamera(mCamera);
            mLogger.d("Camera after switch: " + mDevice.getSelectedCamera());
        } catch (Exception e) {
            mLogger.e("Exception in switchVideo()", e);
            displayMessage("Switch camera exception: " + e.getMessage());
        }
    }

    public void launchDtmf(View v) {
        mLogger.d("Pop a DTMF window");

        try {
            if (mDtmfPopupWindow == null) {
                final Button btnOpenPopup = (Button) findViewById(R.id.btnSwitchVideo);

                LayoutInflater layoutInflater
                        = (LayoutInflater) getBaseContext()
                        .getSystemService(LAYOUT_INFLATER_SERVICE);
                View popupView = layoutInflater.inflate(R.layout.dtmf_popup, null);

                mDtmfPopupWindow = new PopupWindow(
                        popupView,
                        LayoutParams.WRAP_CONTENT,
                        LayoutParams.WRAP_CONTENT);

                mDtmfPopupWindow.showAsDropDown(btnOpenPopup, 50, -30);
            }
        } catch (Exception e) {
            mLogger.e("Exception in launchDtmf()", e);
            displayMessage("Launch DTMF exception: " + e.getMessage());
        }
    }

    public void dismissDtmf(View v) {
        if (mDtmfPopupWindow != null) {
            mDtmfPopupWindow.dismiss();
        }

        mDtmfPopupWindow = null;
    }

    public void dtmf(View v) {
        try {
            String digit = (String) v.getTag();
            mLogger.d("DTMF: " + digit);
            mSession.sendDTMF(DTMFType.get(digit), true);
        } catch (Exception e) {
            mLogger.e("Exception in dtmf()", e);
            displayMessage("DTMF exception: " + e.getMessage());
        }
    }

    /**
     * Toggle enable video
     *
     * @param v
     */
    public void toggleEnableVideo(View v) {
        try {
            mLogger.d("Toggle enable video");
            boolean isEnabled = mSession.isVideoEnabled();
            mSession.enableVideo(!isEnabled);
        } catch (Exception e) {
            mLogger.e("Exception in toggleEnableVideo()", e);
            displayMessage("Enable video exception: " + e.getMessage());
        }
    }

    /**
     * Uncheck mute video control
     */
    private void uncheckMuteVideoControl() {
        try {
            if (mMuteVideo != null) {
                mMuteVideo.setChecked(false);
            }
        } catch (Exception e) {
            mLogger.e("Exception in uncheckMuteVideoControl()", e);
            displayMessage(e.getMessage());
        }
    }

    /**
     * Set calee display information
     */
    private void setCalleeDisplayInformation(final SessionState sessionState) {
        try {
            runOnUiThread(new Runnable() {

                @Override
                public void run() {
                    if (mCalleeNumberDisplay != null) {
                        mStatusText = convertSessionState(sessionState) + " "
                                + getIntent().getExtras().getString(Constants.KEY_NUMBER_TO_DIAL);

                        mCalleeNumberDisplay.setText(mStatusText);
                    }
                }
            });
        } catch (Exception e) {
            mLogger.e("Exception in setCalleeDisplayInformation()", e);
            displayMessage("Set callee display exception: " + e.getMessage());
        }
    }

    /**
     * Hang-up the call
     */
    private void hangup() {
        try {
            mLogger.i("Hang-up");

            if (mSession.isVideoEnabled()) {

                mDevice.setLocalVideoView(null);
                mDevice.setRemoteVideoView(null);
            }

            mSession.unregisterListener(this);
            mUser.unregisterListener(this);

            mSession.end();

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    stopCallTimer();
                }
            });
        } catch (Exception e) {
            displayMessage("Hang-up exception: " + e.getMessage());
            mLogger.e("Exception in hang-up", e);
        }
    }

    @Override
    public void onSessionRemoteAlerting(Session session, boolean hasEarlyMedia) {
        setCalleeDisplayInformation(mSession.getState());
        mLogger.i("Session remote alerting");
    }

    @Override
    public void onSessionRedirected(Session session) {
        mLogger.i("Session redirected");
    }

    @Override
    public void onSessionQueued(Session session) {
        mLogger.i("Session queued");
    }

    @Override
    public void onSessionEstablished(Session session) {
        try {
            SessionState sessionState = mSession.getState();

            setCalleeDisplayInformation(sessionState);

            mLogger.i("Session established");
            mLogger.d("Session state: " + sessionState);
            mLogger.d("Service available: " + mSession.isServiceAvailable());

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    startCallTimer();
                    onSessionRemoteAddressChanged(mSession, mSession.getRemoteAddress(), mSession.getRemoteDisplayName());
                }
            });
        } catch (Exception e) {
            mLogger.e("Exception in onSessionEstablished()", e);
            displayMessage("Session established exception: " + e.getMessage());
        }
    }

    @Override
    public void onSessionRemoteAddressChanged(Session session, String newAddress, final String newDisplayName) {
        try {
            runOnUiThread(new Runnable() {

                @Override
                public void run() {
                    if (mDisplayNameTextView != null) {
                        mDisplayNameTextView.setText(newDisplayName);
                    }
                }
            });

        } catch (Exception e) {
            mLogger.e("Exception in onSessionRemoteAddressChanged()", e);
            displayMessage("Session remote address changed exception: " + e.getMessage());
        }
    }

    @Override
    public void onSessionEnded(Session session) {
        mLogger.i("Session ended");

        hangup();

        finish();
    }

    @Override
    public void onSessionFailed(Session session, SessionError sessionError) {
        mLogger.e("Session failed: " + sessionError);

        hangup();

        displayMessage(sessionError.toString(), true);

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                stopCallTimer();
            }
        });
    }

    @Override
    public void onSessionAudioMuteStatusChanged(Session session, boolean muted) {
        mLogger.i("Session audio mute status changed");
        Log.d("SDK", "Mute status cambiado" );

    }

    @Override
    public void onSessionAudioMuteFailed(Session session, boolean requestedMuteStatus, SessionException exception) {
        mLogger.w("Session audio mute failed");
        displayMessage("Session audio mute failed: " + exception);
        Log.d("SDK", "Fallo Mute Video" + exception);

    }

    @Override
    public void onSessionVideoMuteStatusChanged(Session session, boolean muted) {
        Log.d("SDK", "Video Mute Off");
    }

    @Override
    public void onSessionVideoMuteFailed(Session session, boolean requestedMuteStatus, SessionException exception) {
        Log.d("SDK", "Video Mute Error Off");
    }

    @Override
    public void onSessionVideoRemovedRemotely(Session session) {
        Log.d("SDK", "Video Remoto Off");

    }

    @Override
    public void onSessionServiceAvailable(Session session) {
        mLogger.i("Session service available");
    }

    @Override
    public void onSessionServiceUnavailable(Session session) {
        mLogger.e("Session service unavailable");
        displayMessage("Session service unavailable");
    }

    @Override
    public void onSessionRemoteDisplayNameChanged(Session session, String s) {
        Log.d("SDK", "nombre Canbiado");

        mLogger.e("Session remote display name changed: " + s);
    }

    @Override
    public void onConnectionInProgress(User arg0) {
        mLogger.i("Connection in progress");
    }

    @Override
    public void onServiceAvailable(final User user) {
        mLogger.d("onServiceAvailable");
    }

    @Override
    public void onServiceUnavailable(User arg0) {
        Log.d("SDK", "Servicio No Disponible");

        mLogger.e("Service unavailable");

        String message = addNetworkConnectionMessage(getResources().getString(R.string.service_unavailable));

        hangup();
        displayMessage(message, true);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        Log.d("SDK", "Configuracion cambiada");
        mLogger.i("Configuration changed");
    }

    @Override
    public void onCallError(Session session, SessionError sessionError, String message, String reason) {
        displayError("Call error", sessionError, message, reason);
    }

    @Override
    public void onDialError(Session session, SessionError sessionError, String message, String reason) {
        displayError("Dial error", sessionError, message, reason);
    }

    private void displayError(String errorType, SessionError sessionError, String message, String reason) {
        if (message == null) {
            message = "";
        }

        if (reason == null) {
            reason = "";
        }

        String errorMessage = errorType + ": " + sessionError + ", " + message + ", " + reason;

        mLogger.w(errorMessage);

        displayMessage(errorMessage);
    }

    @Override
    public void onQualityChanged(Session session, final int i) {
        Log.d("SDK", "Calidad Cambio"  + i);

        mLogger.d("Quality changed: " + i);

        try {
            runOnUiThread(new Runnable() {

                @Override
                public void run() {
                    ProgressBar progress = (ProgressBar) findViewById(R.id.call_quality_bar);

                    if (i >= Constants.MINIMUM_CALL_QUALITY && i <= Constants.MAXIMUM_CALL_QUALITY) {
                        progress.setProgress(i);
                        TextView mTextView = (TextView) findViewById(R.id.textView7);
                        mTextView.setText(i + "%");

                    }
                }
            });

        } catch (Exception e) {
            mLogger.e("Exception in onQualityChanged()", e);
        }
    }

    @Override
    public void onCapacityReached(Session session) {
        mLogger.w("Capacity reached");
        displayMessage(getResources().getString(R.string.capacity_reached));
    }

    @Override
    public void onConnLost(User user) {
        mLogger.w("Connection lost");

        displayToast("Connection lost");

    }

    @Override
    public void onConnRetry(User user) {
        mLogger.i("Connection retry");
    }

    @Override
    public void onConnReestablished(User user) {
        Log.d("SDK", "conexion Restablecid");

        mLogger.i("Connection reestablished");
        displayToast("Connection reestablished");
    }

    @Override
    public void onNetworkError(User user) {
        Log.d("SDK", "Error de Red");

        mLogger.w("Network error");

        hangup();

        displayMessage("Network error", true);
    }

    @Override
    public void onCriticalError(User user) {
        mLogger.w("Critical error");
        displayMessage("Critical error");
        Log.d("SDK", "Error Critico");
    }

    private String convertSessionState(SessionState sessionState) {
        String strSessionState;

        switch (sessionState) {
            case ENDED:
                strSessionState = "Ended";
                break;
            case ESTABLISHED:
                strSessionState = "Llamada Establecida";
                break;
            case ENDING:
                strSessionState = "terminando";
                break;
            case FAILED:
                strSessionState = "Failed";
                break;
            case IDLE:
                strSessionState = "Idle";
                break;
            case INITIATING:
                strSessionState = "Iniciando";
                break;
            case REMOTE_ALERTING:
                strSessionState = "Timbrando";
                break;
            default:
                strSessionState = "Unknown session state";
        }

        return strSessionState;
    }

    private void createVideoComponents() {
        try {
            if (mRemoteVideoSurface == null) {
                mLogger.d("Creating Video Components");

                ClientPlatform clientPlatform = ClientPlatformManager.getClientPlatform(this);

                RelativeLayout rlRemote = (RelativeLayout) findViewById(R.id.remoteLayout);
                RelativeLayout rlLocal = (RelativeLayout) findViewById(R.id.localLayout);

                mDevice = clientPlatform.getDevice();
                Point remoteSize = new Point(rlRemote.getWidth(), rlRemote.getHeight());
                Point localSize = new Point(rlLocal.getWidth(), rlLocal.getHeight());

                mRemoteVideoSurface = new VideoSurfaceImpl(this, remoteSize, new VideoListenerClass());
                mPreviewView = new VideoSurfaceImpl(this, localSize, new VideoListenerClass());

                mRemoteVideoSurface.setLayoutParams(new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT));
                rlRemote.addView(mRemoteVideoSurface);

                mPreviewView.setLayoutParams(new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT));
                rlLocal.addView(mPreviewView);

                mDevice.setLocalVideoView(mPreviewView);
                mDevice.setRemoteVideoView(mRemoteVideoSurface);



                /*
                 * We need to keep the screen on to prevent it sleeping during video
                 * calls
                 */
                getWindow()
                        .addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
            }
        } catch (Exception e) {
            mLogger.e("Exception in createVideoComponents()", e);
            displayMessage("Create video components exception: " + e.getMessage());
        }
    }

    /**
     * Get display
     *
     * @return display
     */
    private Display getDisplay() {
        return ((WindowManager) getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
    }

    private class VideoListenerClass implements VideoListener {
        @Override
        public void renderingStart(VideoSurface videoSurface) {
            try {
                if (videoSurface == mPreviewView) {
                    mDevice.setLocalVideoView(videoSurface);
                } else if (videoSurface == mRemoteVideoSurface) {
                    mDevice.setRemoteVideoView(videoSurface);
                } else {
                    mLogger.e("Unknown surface");
                }
            } catch (Exception e) {
                mLogger.e("Exception in renderingStart()", e);
                displayMessage("Rendering start exception: " + e.getMessage());
            }
        }

        @Override
        public void frameSizeChanged(final int width, final int height, final Participant endpoint, final VideoSurface videoView) {
            Log.d("SDK", "Tamaño Cambiado");

        }
    }

    private void displayMessage(final String message) {
        displayMessage(message, false);
    }

    private void displayMessage(final String message, final boolean finishActivity) {
        mLogger.d("Display message: " + message);
        final Activity activity = this;

        try {
            runOnUiThread(new Runnable() {

                @Override
                public void run() {
                    GeneralDialogFragment.displayMessage(activity, message, finishActivity);
                }
            });

        } catch (Exception e) {
            mLogger.e("Exception in displayMessage()", e);
        }
    }

    private void displayToast(final String message) {
        final Activity activity = this;

        try {
            runOnUiThread(new Runnable() {

                @Override
                public void run() {
                    Toast.makeText(activity, message, Toast.LENGTH_LONG).show();
                }
            });

        } catch (Exception e) {
            mLogger.e("Exception in displayToast()", e);
        }
    }


    private String addNetworkConnectionMessage(String message) {
        try {
            InternetConnectionDetector internetConnectionDetector = new InternetConnectionDetector(this.getApplicationContext());

            if (!internetConnectionDetector.getWifiConnected()) {
                message += "\n" + getResources().getString(R.string.no_internet_connection_detected);
            }
        } catch (Exception e) {
            mLogger.e("Exception in addNetworkConnectionMessage()", e);
            displayMessage("Network connection message exception: " + e.getMessage());
        }

        return message;
    }

    private void startCallTimer() {
        try {
            mCallTimeChecker.run();
        } catch (Exception e) {
            mLogger.e("Exception in startCallTimer()", e);
            displayMessage("Call timer exception: " + e.getMessage());
        }
    }

    private void stopCallTimer() {
        try {
            mTimerHandler.removeCallbacks(mCallTimeChecker);
        } catch (Exception e) {
            mLogger.e("Exception in stopCallTimer()", e);
            displayMessage("Stop call timer exception: " + e.getMessage());
        }
    }

    private void updateCallTime() {
        try {
            long callTimeElapsed = mSession.getCallTimeElapsed();

            int callTimeSeconds;
            if (callTimeElapsed == -1L) {
                callTimeSeconds = 0;
            } else {
                callTimeSeconds = (int) (callTimeElapsed / 1000);
            }

            int seconds = callTimeSeconds % 60;
            int minutes = ((callTimeSeconds - seconds) / 60);

            String title = String.format(Constants.CALL_TIME_ELAPSED_FORMAT, mStatusText, Constants.CALL_TIME_ELAPSED_SEPARATOR, minutes, seconds, Constants.CALL_TIME_ELAPSED_END);

            updateStatus(title);
        } catch (Exception e) {
            mLogger.e("Exception in updateCallTime()", e);
            displayMessage("Update call timer exception: " + e.getMessage());
        }
    }

    private void updateStatus(final String message) {
        try {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (mCalleeNumberDisplay != null) {
                        mCalleeNumberDisplay.setText(message);
                    }
                }
            });
        } catch (Exception e) {
            mLogger.e("Exception in updateStatus()", e);
            displayMessage("Update status exception: " + e.getMessage());
        }
    }

    public static void setPreferredVideoResolution(VideoResolution preferredVideoResolution) {
        if (preferredVideoResolution != null) {
            mPreferredVideoResolution = preferredVideoResolution;
        }
    }

    protected boolean getCallOnHold() {
        return mCallOnHold;
    }


    @Override
    public void onGetMediaError(Session session) {
        mLogger.e("Get media error");
        displayMessage(getResources().getString(R.string.get_media_error));
    }
}


