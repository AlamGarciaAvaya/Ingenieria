package com.avaya.mobilevideo

import android.os.Bundle
import android.app.Activity
import android.content.Context
import android.content.CursorLoader
import android.util.Log

import kotlinx.android.synthetic.main.activity_ajustes.*
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Picture
import android.net.Uri
import android.provider.MediaStore
import android.view.View
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_login.*
import java.io.File
import java.io.FileNotFoundException
import java.io.IOException
import android.graphics.Bitmap


class Ajustes : Activity() {
    var orgUri: Uri? = null
    var uriFromPath:Uri? = null
    var convertedPath: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ajustes)
        val myPreferences = "myPrefs"
        val sharedPreferences = getSharedPreferences(myPreferences, Context.MODE_PRIVATE)
        obtenerajustes()
        guardarajustes_btn.setOnClickListener {
            guardarajustes()
        }
        switch1.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                val editor = sharedPreferences.edit()
                editor.putString("debug", 1.toString())
                Log.d("Valores", "Check Si")
                editor.apply()
            } else {
                val editor = sharedPreferences.edit()
                editor.putString("debug", 0.toString())
                Log.d("Valores", "Check No")
                editor.apply()
            }
        }
    }
    fun guardarajustes() {
        val myPreferences = "myPrefs"
        val sharedPreferences = getSharedPreferences(myPreferences, Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putString("displayname", displayname_txt.text.toString())
        editor.putString("username", user_txt.text.toString())
        editor.putString("host", host_txt.text.toString())
        editor.putString("puerto", puerto_txt.text.toString())
        editor.putString("numero", num_txt.text.toString())
        editor.apply()
        finish()
    }


    fun obtenerajustes() {
        var myPreferences = "myPrefs"
        var sharedPreferences = getSharedPreferences(myPreferences, Context.MODE_PRIVATE)
        var displayname = sharedPreferences.getString("displayname", "John Doe")
        var username = sharedPreferences.getString("username", "1234")
        var host = sharedPreferences.getString("host", "amv.collaboratory.avaya.com")
        var puerto = sharedPreferences.getString("puerto", "443")
        var numero = sharedPreferences.getString("numero", "2681322102")
        var debug = sharedPreferences.getString("debug", "0")
        displayname_txt.setText(displayname)
        user_txt.setText(username)
        host_txt.setText(host)
        puerto_txt.setText(puerto)
        num_txt.setText(numero)
        Log.d("Valores", "Key1 $displayname")
        Log.d("Valores", "Key2 $username")
        Log.d("Valores", "Key3 $host")
        Log.d("Valores", "Key4 $puerto")
        Log.d("Valores", "Key5 $numero")
        Log.d("Valores", "Key6 $debug")
        when (Integer.parseInt(debug)) {
            1 -> switch1.isChecked = true
            else -> switch1.isChecked = false
        }

    }
}
