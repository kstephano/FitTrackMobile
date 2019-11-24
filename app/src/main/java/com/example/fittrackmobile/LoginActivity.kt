package com.example.fittrackmobile

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.lang.Exception

class LoginActivity: AppCompatActivity() {

    // XML object variables.
    private lateinit var emailET: EditText
    private lateinit var passwordET: EditText
    private lateinit var loginButton: Button
    private lateinit var registerButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        attachJavaToXML()

        // Set the tool bar.
        setSupportActionBar(findViewById(R.id.login_toolbar))
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setTitle(R.string.login_title)
    }

    private fun attachJavaToXML() {
        try {
            emailET = findViewById(R.id.login_email_ET)
            passwordET = findViewById(R.id.login_password_ET)
            loginButton = findViewById(R.id.login_loginButton)
            registerButton = findViewById(R.id.login_registerButton)

        } catch (e: Exception) {
            Toast.makeText(this, "LoginActivity: " + e.message, Toast.LENGTH_SHORT).show()
        }
    }

    fun registerButtonOnClick(view: View) {
        try {
            val registerIntent = Intent(applicationContext, RegisterActivity::class.java)
            startActivity(registerIntent)
        } catch (e: Exception) {
            Toast.makeText(this, "LoginActivity: " + e.message, Toast.LENGTH_SHORT).show()
        }
    }
}