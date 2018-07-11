package com.avaya.mobilevideo

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.preference.PreferenceManager
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast

import com.avaya.clientplatform.api.ClientPlatformFactory
import com.avaya.mobilevideo.api.LoginActivity
import com.avaya.mobilevideo.impl.LoginActivityImpl
import com.avaya.mobilevideo.impl.ManualSessionActivity
import com.avaya.mobilevideo.utils.Constants
import com.avaya.mobilevideo.utils.GeneralDialogFragment
import com.avaya.mobilevideo.utils.InternetConnectionDetector
import kotlinx.android.synthetic.main.activity_login.*
import android.graphics.BitmapFactory
import android.graphics.Bitmap
import java.io.InputStream
import java.net.URL
import android.R.attr.data
import kotlinx.android.synthetic.main.activity_ajustes.*
import java.io.FileNotFoundException


class ExampleLoginActivity:LoginActivityImpl(), LoginActivity {
    override fun onCreate(savedInstanceState:Bundle?) {
        super.onCreate(savedInstanceState)
setContentView(R.layout.activity_login)
    }



protected override fun onPostCreate(savedInstanceState:Bundle?) {


super.onPostCreate(savedInstanceState)
    setReferencedClass()
    val tv = findViewById(R.id.txtViewVersion) as TextView
    tv.text = (getResources().getString(R.string.sdk_version) + ClientPlatformFactory.getClientPlatformInterface(this).version + "." + Constants.APP_VERSION)



//Do connectivity test
displayConnectivityInformation()
}

public override fun login(v:View) {


    var myPreferences = "myPrefs"
    var sharedPreferences = getSharedPreferences(myPreferences, Context.MODE_PRIVATE)
    var displayname = sharedPreferences.getString("displayname", "John")
    var usuario = sharedPreferences.getString("username", "Doe")
    var host = sharedPreferences.getString("host", "amv.collaboratory.avaya.com")
    var puerto = sharedPreferences.getString("puerto", "8443")
    var numero = sharedPreferences.getString("numero", "2682132950")
    var debug = sharedPreferences.getString("debug", "1")
    var imguri = sharedPreferences.getString("ImgUri", "1")

    Log.d("Valores", "Key1 $displayname")
    Log.d("Valores", "Key2 $usuario")
    Log.d("Valores", "Key3 $host")
    Log.d("Valores", "Key4 $puerto")
    Log.d("Valores", "Key5 $numero")
    Log.d("Valores", "Key6 $debug")


val username = usuario

val server = host

val displayName = displayname
    login(username, displayName, server)
}


protected fun displayConnectivityInformation() {
val internetConnectionDetector = InternetConnectionDetector(this)
val internetConnection = internetConnectionDetector.connectionInformation

when (internetConnection) {
InternetConnectionDetector.InternetConnection.WIFI -> toastConnectionType(getResources().getString(R.string.wifi_connection))
InternetConnectionDetector.InternetConnection.MOBILE_DATA -> toastConnectionType(getResources().getString(R.string.mobile_connection))
InternetConnectionDetector.InternetConnection.NO_CONNECTION -> GeneralDialogFragment.displayMessage(this, getResources().getString(R.string.no_internet_connection_detected))
else -> GeneralDialogFragment.displayMessage(this, getResources().getString(R.string.no_internet_connection_detected))
}
}

private fun toastConnectionType(connectionType:String) {
var connectionTypeMessage = getResources().getString(R.string.internet_connection_type)
connectionTypeMessage = connectionTypeMessage.replace(Constants.PLACEHOLDER1, connectionType)
val message = getResources().getString(R.string.internet_connection_detected) + " " + connectionTypeMessage
Toast.makeText(this, message, Toast.LENGTH_LONG).show()
}
protected override fun setReferencedClass() {
setDialActivityClass(ExampleDialActivity::class.java)
ManualSessionActivity.setDialActivityClass(ExampleDialActivity::class.java)
}
}
