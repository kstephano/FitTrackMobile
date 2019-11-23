package com.example.fittrackmobile

import android.app.DatePickerDialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.content.Intent
import android.net.Uri
import android.provider.MediaStore
import android.view.View
import android.widget.*
import de.hdodenhof.circleimageview.CircleImageView
import java.lang.Exception
import java.text.SimpleDateFormat
import java.util.*


class MainActivity : AppCompatActivity() {

    // Companion object to hold static class variables.
    companion object {
        private var REQUEST_CODE: Int = 1
    }

    // XML objects initialization.
    private lateinit var profileIV: CircleImageView
    private lateinit var userNameET: EditText
    private lateinit var userEmailET: EditText
    private lateinit var userFirstNameET: EditText
    private lateinit var userLastNameET: EditText
    private lateinit var userPasswordET: EditText
    private lateinit var userConfirmPasswordET: EditText

    private lateinit var registerButton: Button
    private lateinit var radioGroup: RadioGroup
    private lateinit var dateTV: TextView

    // Initialize Uniform Resource Identifier for retrieving profile picture.
    private var profileImageURL: Uri? = null

    // Initialize an instance of the calendar.
    private var cal = Calendar.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        // Attach XML objects to Kotlin objects.
        profileIV = findViewById<CircleImageView>(R.id.register_user_profilepic_IV) as CircleImageView
        userNameET = findViewById<EditText>(R.id.register_username_ET) as EditText
        userEmailET = findViewById<EditText>(R.id.register_email_ET) as EditText
        userFirstNameET = findViewById<EditText>(R.id.register_firstname_ET) as EditText
        userLastNameET = findViewById<EditText>(R.id.register_lastname_ET) as EditText
        userPasswordET = findViewById<EditText>(R.id.register_password_ET) as EditText
        userConfirmPasswordET = findViewById<EditText>(R.id.register_reenter_ET) as EditText

        registerButton = findViewById<Button>(R.id.register_button) as Button
        radioGroup = findViewById<RadioGroup>(R.id.register_radioGroup) as RadioGroup
        dateTV = findViewById<EditText>(R.id.register_dob_ET) as EditText

        // Set the tool bar.
        setSupportActionBar(findViewById(R.id.register_toolbar))
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setTitle("Register an account")

        // Initialize the date picker listener.
        val dateSetListener = object : DatePickerDialog.OnDateSetListener {
            override fun onDateSet(view: DatePicker, year: Int, month: Int, day: Int) {
                // Sets the calendar object with the values selected by the user.
                cal.set(Calendar.YEAR, year)
                cal.set(Calendar.MONTH, month)
                cal.set(Calendar.DAY_OF_MONTH, day)

                // Initialize the date format and sets the text in the date TextView
                // object with the new calendar values.
                val dateFormat = "dd/MM/yyyy"
                val sdf = SimpleDateFormat(dateFormat, Locale.UK)
                dateTV.text = sdf.format(cal.time)
            }
        }

        // Set the data picker dialog to show when the date TextView object is clicked.
        dateTV.setOnClickListener(object : View.OnClickListener {
            override fun onClick(view: View) {
                val dpd = DatePickerDialog(this@MainActivity,
                    R.style.MyDialogTheme,
                    dateSetListener,
                    cal.get(Calendar.YEAR),
                    cal.get(Calendar.MONTH),
                    cal.get(Calendar.DAY_OF_MONTH))
                dpd.show()
            }
        })
    }

    fun profileIVonClick(view: View) {
        selectImageFromGallery()
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
            Toast.makeText(this, "RegisterActivity:" + e.message, Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Sets the user's profile with their chosen image from the gallery.
     */
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
