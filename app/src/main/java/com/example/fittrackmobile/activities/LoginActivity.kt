package com.example.fittrackmobile.activities

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.annotation.NonNull
import androidx.appcompat.app.AppCompatActivity
import com.example.fittrackmobile.R
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import java.lang.Exception
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth

class LoginActivity: AppCompatActivity() {

    // XML object variables.
    private lateinit var emailET: EditText
    private lateinit var passwordET: EditText
    private lateinit var loginButton: Button
    private lateinit var registerButton: Button
    private lateinit var pleaseWaitDialog: Dialog

    // Class variables.
    val TAG = "LoginActivity"

    // Firebase variables
    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize the dialog shown as the user is logging in.
        pleaseWaitDialog = Dialog(this)
        pleaseWaitDialog.setContentView(R.layout.please_wait_dialog)
        pleaseWaitDialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        setContentView(R.layout.activity_login)

        firebaseAuth = FirebaseAuth.getInstance()
        attachJavaToXML()

        // Set the tool bar.
        setSupportActionBar(findViewById(R.id.login_toolbar))
        supportActionBar?.setTitle(R.string.title_login_page)
    }

    /**
     * Attach Java/Kotlin objects to their XML counterparts.
     */
    private fun attachJavaToXML() {
        try {
            emailET = findViewById(R.id.login_email_ET)
            passwordET = findViewById(R.id.login_password_ET)
            loginButton = findViewById(R.id.login_loginButton)
            registerButton = findViewById(R.id.login_registerButton)

        } catch (e: Exception) {
            Log.e(TAG, "" + e.message)
        }
    }

    /**
     * Attempt to sign the user into the app.
     */
    private fun signInUser() {
        try {
            if(emailET.text.toString().isNotEmpty() && passwordET.text.toString().isNotEmpty()) {
                pleaseWaitDialog.show()

                // Check if user is already logged in.
                if(firebaseAuth.currentUser == null) {
                    firebaseAuth.signInWithEmailAndPassword(
                        emailET.text.toString(), passwordET.text.toString()).
                        addOnSuccessListener {
                            pleaseWaitDialog.dismiss()

                            Toast.makeText(this@LoginActivity, "Welcome " +
                                    emailET.text.toString(), Toast.LENGTH_SHORT).show()

                            // Open up the home page activity.
                            val intent = Intent(this@LoginActivity,
                                HomeActivity::class.java)
                            startActivity(intent)
                        }.addOnFailureListener { e ->
                        // Show error message when unsuccessful login occurs.
                        pleaseWaitDialog.dismiss()

                        Toast.makeText(this@LoginActivity, "LoginPage:" + e.message,
                            Toast.LENGTH_SHORT).show()
                    }
                } else {
                    pleaseWaitDialog.dismiss()

                    firebaseAuth.signOut()
                    Toast.makeText(this, "Previously signed in user logged out successfully," +
                            " login again please", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Please Fill Both Fields", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Toast.makeText(this, "LoginPage:"+e.message, Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * OnClick functionality for the register button. Opens the RegisterActivity.
     */
    fun registerButtonOnClick(view: View) {
        try {
            val registerIntent = Intent(applicationContext, RegisterActivity::class.java)
            startActivity(registerIntent)
        } catch (e: Exception) {
            Log.e(TAG, "" + e.message)
        }
    }

    /**
     * OnClick functionality for the login button. Logs in the user.
     */
    fun loginButtonOnClick(view: View) {
        try {
            signInUser()
        } catch (e: Exception) {
            Log.e(TAG, "" + e.message)
        }
    }
}