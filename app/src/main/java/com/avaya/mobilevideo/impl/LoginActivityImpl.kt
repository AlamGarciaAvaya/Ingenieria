
package com.avaya.mobilevideo.impl

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.AsyncTask
import android.os.Bundle
import android.preference.PreferenceManager
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast

import com.avaya.clientplatform.api.ClientPlatform
import com.avaya.mobilevideo.Ajustes
import com.avaya.mobilevideo.MobileVideoApplication
import com.avaya.mobilevideo.R
import com.avaya.mobilevideo.api.LoginHandler
import com.avaya.mobilevideo.utils.Constants
import com.avaya.mobilevideo.utils.GeneralDialogFragment
import com.avaya.mobilevideo.utils.IssueReporter
import com.avaya.mobilevideo.utils.Logger

import java.net.URLEncoder
import java.util.HashMap

abstract class LoginActivityImpl : MobileVideoActivity() {
    fun obtenerajustes() {
        var myPreferences = "myPrefs"
        var sharedPreferences = getSharedPreferences(myPreferences, Context.MODE_PRIVATE)
        var displayname = sharedPreferences.getString("displayname", "John")
        var username = sharedPreferences.getString("username", "Doe")
        var host = sharedPreferences.getString("host", "amv.collaboratory.avaya.com")
        var puerto = sharedPreferences.getString("puerto", "8443")
        var numero = sharedPreferences.getString("numero", "2682132950")
        var debug = sharedPreferences.getString("debug", "1")
        Log.d("Valores", "Key1 $displayname")
        Log.d("Valores", "Key2 $username")
        Log.d("Valores", "Key3 $host")
        Log.d("Valores", "Key4 $puerto")
        Log.d("Valores", "Key5 $numero")
        Log.d("Valores", "Key6 $debug")

    }

    private var mDialActivityClass: Class<out DialActivityImpl>? = null
    private val mLogger = Logger.getLogger(TAG)

    private var mServerAddress: String? = null

    private var mServer = ""

    private var mAuthTask: LoginTask? = null
    private var mSecureLogin: Boolean = false
    private var mPort: String? = null
    private var mTrustALlCerts: Boolean = false

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.main, menu)

        setReferencedClass()
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId

        when (id) {
            R.id.settings_item -> {
                val intent = Intent(applicationContext, Ajustes::class.java)
                startActivity(intent)
                return true
            }

            else -> return super.onOptionsItemSelected(item)
        }

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        sContext = applicationContext
    }

    private fun configureLogger() {
        val sharedPref = PreferenceManager.getDefaultSharedPreferences(this)

        val logToDisk = sharedPref.getBoolean(Constants.PREFERENCE_LOG_TO_DEVICE, true)
        val logLevel = sharedPref.getString(Constants.PREFERENCE_LOG_LEVEL, "")
        val logFileName = sharedPref.getString(Constants.PREFERENCE_LOG_FILE_NAME, "")
        val maxFileSize = sharedPref.getString(Constants.PREFERENCE_MAX_FILE_SIZE, "0")
        val maxBackUps = sharedPref.getString(Constants.PREFERENCE_MAX_BACK_UPS, "0")

        Logger.configure(logToDisk, logLevel, logFileName, maxFileSize, maxBackUps)
    }

    private fun readSecurityPreferences() {
        val myPreferences = "myPrefs"
        val sharedPreferences = getSharedPreferences(myPreferences, Context.MODE_PRIVATE)
        val sharedPref = PreferenceManager.getDefaultSharedPreferences(this)
        var puerto = sharedPref.getString("puerto", "8443")

        mSecureLogin = sharedPref.getBoolean(Constants.PREFERENCE_SECURE_LOGIN, true)
        mPort = sharedPreferences.getString("puerto", "8443")
        mTrustALlCerts = sharedPref.getBoolean(Constants.PREFERENCE_TRUST_ALL_CERTS, true)

        mLogger.d("Security preferences, secure login: $mSecureLogin")
        mLogger.d("Security preferences, port: " + mPort!!)
        mLogger.d("Security preferences, trust all certs: $mTrustALlCerts")
    }

    protected fun setDialActivityClass(dialActivityClass: Class<out DialActivityImpl>) {
        mLogger.d("Set the dial activity")
        mDialActivityClass = dialActivityClass
    }

    fun login(username: String, displayName: String, server: String) {
        var displayName = displayName
        mLogger.d("Login")

        configureLogger()

        readSecurityPreferences()

        if (validateInput(username, server)) {
            this.mServer = server

            if (mSecureLogin) {
                mServerAddress = Constants.SECURE_LOGIN_URL.replace(Constants.SERVER_PLACEHOLDER, server)
            } else {
                mServerAddress = Constants.REGULAR_LOGIN_URL.replace(Constants.SERVER_PLACEHOLDER, server)
            }

            mServerAddress = mServerAddress!!.replace(Constants.PORT_PLACEHOLDER, mPort!!)

            val params = HashMap<String, String>()

            if (displayName == null) {
                displayName = ""
            }
            params["displayName"] = displayName
            params["userName"] = username

            mAuthTask = LoginTask()

            mAuthTask!!.execute(params)
        }
    }
    private fun moveToDialActivity(loginData: Bundle) {
        val intent = Intent(this, mDialActivityClass)
        intent.putExtras(loginData)
        intent.putExtra(Constants.DATA_KEY_SERVER, mServer)
        intent.putExtra(Constants.DATA_KEY_SECURE, mSecureLogin)
        intent.putExtra(Constants.DATA_KEY_PORT, mPort)

        startActivityForResult(intent, Constants.RESULT_LOGOUT)
    }
    private fun validateInput(username: String, server: String): Boolean {
        var isValid = false

        if (validateStringInput(username, resources.getString(R.string.username)) && validateStringInput(server, resources.getString(R.string.server))) {
            isValid = true
        }

        return isValid
    }

    private fun validateStringInput(value: String?, valueName: String): Boolean {
        var isValid = true

        if (value == null || value.trim { it <= ' ' }.length == 0) {
            Toast.makeText(this, resources.getString(R.string.specify) + " " + valueName, Toast.LENGTH_LONG).show()
            isValid = false
        }

        return isValid
    }
    private inner class LoginTask : AsyncTask<Map<String, String>, Void, Bundle>() {

        override fun doInBackground(vararg params: Map<String, String>): Bundle {
            mLogger.d("LoginTask doInBackground")
            val stringBuilder = StringBuilder(mServerAddress)

            val param = params[0]
            val keySet = param.keys

            if (keySet.isNotEmpty()) {
                stringBuilder.append("?")

                for (key in keySet) {
                    val value = param[key]
                    stringBuilder.append(key).append("=").append(URLEncoder.encode(value)).append("&")
                }
                stringBuilder.deleteCharAt(stringBuilder.length - 1)
            }

            return LoginHandlerImpl.getInstance().login(stringBuilder.toString(), null, mTrustALlCerts, false)
        }

        override fun onPostExecute(loginData: Bundle) {
            mLogger.d("LoginTask onPostExecute")
            mAuthTask = null
            val loginResponse = loginData.getInt(Constants.DATA_KEY_ERROR)

            if (loginResponse == null || loginResponse == 0) {
                mLogger.i("Login success")
                moveToDialActivity(loginData)

            } else {
                var detailedMessage: String? = null

                val responseCode = loginData.getInt(Constants.DATA_KEY_RESPONSE_CODE)
                var responseMessage = loginData.getString(Constants.DATA_KEY_RESPONSE_MESSAGE)
                val exceptionMessage = loginData.getString(Constants.DATA_KEY_EXCEPTION_MESSAGE)
                if (responseMessage == null) {
                    responseMessage = ""
                }

                if (responseCode > 0) {
                    detailedMessage = responseCode.toString() + ":" + responseMessage
                } else if (exceptionMessage != null && exceptionMessage.trim { it <= ' ' } != "") { // add exception message if there is one
                    detailedMessage = exceptionMessage
                }

                when (loginResponse) {
                    LoginHandler.ERROR_CONNECTION_FAILED -> if (detailedMessage != null) {
                        GeneralDialogFragment.displayMessage(this@LoginActivityImpl, detailedMessage, resources.getString(R.string.failed_to_connect))
                    } else {
                        GeneralDialogFragment.displayMessage(this@LoginActivityImpl, resources.getString(R.string.failed_to_connect))
                    }

                    LoginHandler.ERROR_LOGIN_FAILED -> if (detailedMessage != null) {
                        GeneralDialogFragment.displayMessage(this@LoginActivityImpl, detailedMessage, resources.getString(R.string.failed_to_login))
                    } else {
                        GeneralDialogFragment.displayMessage(this@LoginActivityImpl, resources.getString(R.string.failed_to_login))
                    }

                    else -> GeneralDialogFragment.displayMessage(this@LoginActivityImpl, R.string.unknown_failure)
                }
            }
        }

        override fun onCancelled() {
            mAuthTask = null
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        if (Constants.RESULT_LOGOUT == requestCode) {
            val clientPlatform = ClientPlatformManager.getClientPlatform(applicationContext)
            clientPlatform.user.sessionAuthorizationToken = null
        }
    }

    private fun moveToDebugModeActivity() {
        val intent = Intent(this, ManualSessionActivity::class.java)

        startActivity(intent)
    }

    companion object {

        private val TAG = LoginActivityImpl::class.java!!.getSimpleName()

        var sContext: Context? = null
    }
}
