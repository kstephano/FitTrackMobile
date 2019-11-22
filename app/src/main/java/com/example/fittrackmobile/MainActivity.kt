package com.example.fittrackmobile

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.content.Intent
import android.net.Uri
import android.provider.MediaStore
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.*
import de.hdodenhof.circleimageview.CircleImageView
import java.lang.Exception


class MainActivity : AppCompatActivity() {

    // Companion object to hold static class variables.
    companion object {
        private var REQUEST_CODE: Int = 1
    }

    // XML objects initialization.
    lateinit var profileIV: CircleImageView
    lateinit var userNameET: EditText
    lateinit var userEmailET: EditText
    lateinit var userFirstNameET: EditText
    lateinit var userLastNameET: EditText
    lateinit var userPasswordET: EditText
    lateinit var userConfirmPasswordET: EditText

    lateinit var registerButton: Button
    lateinit var radioGroup: RadioGroup
    lateinit var dateTV: TextView

    // Class variables.
    private var profileImageURL: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        profileIV = findViewById<CircleImageView>(R.id.register_user_profilepic_IV) as CircleImageView
        userNameET = findViewById<EditText>(R.id.register_username_ET) as EditText
        userEmailET = findViewById<EditText>(R.id.register_email_ET) as EditText
        userFirstNameET = findViewById<EditText>(R.id.register_firstname_ET) as EditText
        userLastNameET = findViewById<EditText>(R.id.register_lastname_ET) as EditText
        userPasswordET = findViewById<EditText>(R.id.register_password_ET) as EditText
        userConfirmPasswordET = findViewById<EditText>(R.id.register_reenter_ET) as EditText

        registerButton = findViewById<Button>(R.id.register_button) as Button
        radioGroup = findViewById<RadioGroup>(R.id.register_radioGroup) as RadioGroup
        dateTV = findViewById<TextView>(R.id.register_dob_TV) as TextView

        setSupportActionBar(findViewById(R.id.register_toolbar))
    }

    fun profileIVonClick(view: View) {
        selectImageFromGallery()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.register_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item!!.itemId) {
            R.id.back_button -> {
                true
            }
        }
        return super.onOptionsItemSelected(item)
    }


    private fun openImageGallery() {
        try {
            val galleryIntent = Intent(Intent.ACTION_GET_CONTENT)
            galleryIntent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*")

            startActivityForResult(galleryIntent, REQUEST_CODE)
        } catch (e: Exception) {
            Toast.makeText(this, "RegisterActivity:" + e.message, Toast.LENGTH_SHORT).show()
        }
    }

    private fun selectImageFromGallery() {
        try {
            openImageGallery()
        } catch (e: Exception) {
            Toast.makeText(this, "RegisterPage:" + e.message, Toast.LENGTH_SHORT).show()
        }
    }

    @Override
    internal fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        super.onActivityResult(requestCode, resultCode, data)
        if(data.data != null && data != null) {
            profileImageURL = data.data
            profileIV?.setImageURI(profileImageURL)
        } else {
            Toast.makeText(this, "No image selected", Toast.LENGTH_SHORT).show()
        }
    }
}
