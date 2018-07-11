package com.avaya.mobilevideo

import android.app.ProgressDialog
import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast

import com.avaya.mobilevideo.api.DialActivity
import com.avaya.mobilevideo.impl.DialActivityImpl
import com.avaya.mobilevideo.utils.Constants

class ExampleDialActivity : DialActivityImpl(), DialActivity {

    private var mProgress: ProgressDialog? = null

    private val number: String
        get() {
            var myPreferences = "myPrefs"
            var sharedPreferences = getSharedPreferences(myPreferences, Context.MODE_PRIVATE)
            var numero = sharedPreferences.getString("numero", "Doe")
            val tv = numero
            return tv
        }

    private val uui: String
        get() {
            val tv = findViewById(R.id.uui_field) as TextView
            var uui = tv.text.toString()

            if (uui.length > Constants.MAX_CONTEXT_ID_LENGTH) {
                uui = uui.substring(0, Constants.MAX_CONTEXT_ID_LENGTH)
            }

            return uui
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_dial)
        setReferencedClass()

        val extras = intent.extras
        setServer(extras!!.getString(Constants.DATA_KEY_SERVER))
    }

    override fun onResume() {
        super.onResume()
        dismissProgressDialog()
    }

    override fun onStop() {
        super.onStop()
        dismissProgressDialog()
    }

    override fun dialVideo(v: View) {
        displayProgressDialog()

        if (validateNumber(number) && validateUui(uui)) {
            dialVideo(number, uui)
        } else {
            dismissProgressDialog()
        }
    }

    override fun dialOneWayVideo(v: View) {
        displayProgressDialog()

        if (validateNumber(number) && validateUui(uui)) {
            dialOneWayVideo(number, uui)
        } else {
            dismissProgressDialog()
        }
    }

    override fun dialAudioOnly(v: View) {
        displayProgressDialog()

        if (validateNumber(number) && validateUui(uui)) {
            dialAudioOnly(number, uui)
        } else {
            dismissProgressDialog()
        }
    }

    private fun displayProgressDialog() {
        mProgress = ProgressDialog.show(this, "Llamando",
                "Esperando a respuesta", true)
    }

    override fun logout(v: View) {
        logout()
    }

    override fun setReferencedClass() {
        setCallActivityClass(ExampleCallActivity::class.java)
    }

    private fun dismissProgressDialog() {
        if (mProgress != null) {
            mProgress!!.dismiss()
        }
    }

    override fun validateNumber(number: String?): Boolean {
        var isValid = true

        if (!super.validateNumber(number)) {
            Toast.makeText(this, resources.getString(R.string.specify_number), Toast.LENGTH_LONG).show()
            isValid = false
        }

        return isValid
    }

    override fun validateUui(uui: String?): Boolean {
        var isValid = true

        if (!super.validateUui(uui)) {
            Toast.makeText(this, resources.getString(R.string.enter_valid_uui), Toast.LENGTH_LONG).show()
            isValid = false
        }

        return isValid
    }
}
