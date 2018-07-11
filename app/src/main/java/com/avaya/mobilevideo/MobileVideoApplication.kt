package com.avaya.mobilevideo

import android.app.Activity
import android.app.Application

import com.avaya.clientplatform.api.ClientPlatformFactory
import com.avaya.mobilevideo.utils.IssueReporter
import com.avaya.mobilevideo.utils.Logger


class MobileVideoApplication : Application() {
    private val mLogger = Logger.getLogger(TAG)

    var currentActivity: Activity? = null

    override fun onCreate() {
        super.onCreate()
        mLogger.d("onCreate()")
        sdkVersion = ClientPlatformFactory.getClientPlatformInterface(this).version
        Thread.setDefaultUncaughtExceptionHandler { thread, e -> handleUncaughtException(thread, e) }
    }
    fun handleUncaughtException(thread: Thread, e: Throwable) {
        mLogger.e("Uncaught exception thrown", e)

        launchReportIssue()

        System.exit(1) // kill off the crashed app
    }

    fun launchReportIssue() {
        mLogger.d("launchReportIssue")

        try {
            if (currentActivity != null) {
                mLogger.i("Report issue")
                val issueReporter = IssueReporter(currentActivity, sdkVersion, true)
                issueReporter.reportIssue()
            }
        } catch (e: Exception) {
            mLogger.e("Exception in launchReportIssue()", e)
        }

    }

    companion object {
        private val TAG = MobileVideoApplication::class.java.simpleName

        var sdkVersion = ""
            private set
    }
}

