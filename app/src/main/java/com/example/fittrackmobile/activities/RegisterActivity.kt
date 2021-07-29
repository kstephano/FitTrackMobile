package com.example.fittrackmobile.activities

import android.app.DatePickerDialog
import android.app.Dialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.content.Intent
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.webkit.MimeTypeMap
import android.widget.*
import androidx.annotation.NonNull
import com.example.fittrackmobile.R
import com.google.android.gms.tasks.*
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.UploadTask
import de.hdodenhof.circleimageview.CircleImageView
import java.lang.Exception
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.HashMap


class RegisterActivity : AppCompatActivity() {

    // Companion object to hold static class variables.
    companion object {
        private var REQUEST_CODE: Int = 1
    }

    // XML object variables.
    private lateinit var profileIV: CircleImageView
    private lateinit var userNameET: EditText
    private lateinit var userEmailET: EditText
    private lateinit var userFirstNameET: EditText
    private lateinit var userLastNameET: EditText
    private lateinit var userPasswordET: EditText
    private lateinit var userConfirmPasswordET: EditText
    private lateinit var registerButton: Button
    private lateinit var radioGroup: RadioGroup
    private lateinit var radioButton: RadioButton
    private lateinit var dateET: TextView
    private lateinit var pleaseWaitDialog: Dialog

    // Firebase variables.
    private lateinit var firebaseFirestore: FirebaseFirestore
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firebaseStorageRef: StorageReference

    // Class variables.
    private lateinit var validPassword: String
    private var radioID: Int = 0
    private var cal = Calendar.getInstance()
    private var profileImageURL: Uri? = null
    private val TAG = "RegisterActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize the dialog shown as the user is being registered.
        pleaseWaitDialog = Dialog(this)
        pleaseWaitDialog.setContentView(R.layout.please_wait_dialog)
        pleaseWaitDialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        setContentView(R.layout.activity_register)

        // Attach XML objects to Kotlin objects.
        profileIV = findViewById(R.id.register_user_profilepic_IV)
        userNameET = findViewById(R.id.register_username_ET)
        userEmailET = findViewById(R.id.register_email_ET)
        userFirstNameET = findViewById(R.id.register_firstname_ET)
        userLastNameET = findViewById(R.id.register_lastname_ET)
        userPasswordET = findViewById(R.id.register_password_ET)
        userConfirmPasswordET = findViewById(R.id.register_reenter_ET)
        registerButton = findViewById(R.id.register_button)
        radioGroup = findViewById(R.id.register_radioGroup)
        dateET = findViewById(R.id.register_dob_ET)

        // Set the tool bar.
        setSupportActionBar(findViewById(R.id.register_toolbar))
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = getString(R.string.title_register_page)

        // Initialize the date picker listener.
        val dateSetListener =
            DatePickerDialog.OnDateSetListener { view, year, month, day ->
                // Sets the calendar object with the values selected by the user.
                cal.set(Calendar.YEAR, year)
                cal.set(Calendar.MONTH, month)
                cal.set(Calendar.DAY_OF_MONTH, day)

                // Initialize the date format and sets the text in the date TextView
                // object with the new calendar values.
                val dateFormat = "dd/MM/yyyy"
                val sdf = SimpleDateFormat(dateFormat, Locale.UK)
                dateET.text = sdf.format(cal.time)
            }

        // Set the data picker dialog to show when the date TextView object is clicked.
        dateET.setOnClickListener {
            val dpd = DatePickerDialog(this@RegisterActivity,
                R.style.MyDialogTheme,
                dateSetListener,
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH))
            dpd.show()
        }

        // Initialize Firebase database and authentication objects.
        firebaseFirestore = FirebaseFirestore.getInstance()
        firebaseAuth = FirebaseAuth.getInstance()
        firebaseStorageRef = FirebaseStorage.getInstance().getReference("ImageFolder")
    }

    /**
     * Creates a new user account in the Firebase database.
     */
    private fun createAccount() {
        try {
            // If there is currently a user signed in, log them out.
            if (firebaseAuth.currentUser != null) {
                firebaseAuth.signOut()
            }
            // If there is no current user signed in and the necessary EditText fields are empty...
            if (firebaseAuth.currentUser == null
                && userNameET.text.toString().isNotEmpty()
                && userEmailET.text.toString().isNotEmpty()
                && userPasswordET.text.toString().isNotEmpty()
            ) {
                // If the two passwords entered by the user match...
                if (userPasswordET.text.toString() == userConfirmPasswordET.text.toString()) {
                    pleaseWaitDialog.show()
                    validPassword =  userPasswordET.text.toString()

                    // Create new user and add success/failure listeners to check if successful or not.
                    firebaseAuth.createUserWithEmailAndPassword(
                        userEmailET.text.toString(), validPassword
                    ).addOnSuccessListener {
                        // Upload new user data to the database.
                        uploadToFirebase()
                    }.addOnFailureListener { e ->
                        // Executes upon failure to authenticate user account.
                        pleaseWaitDialog.dismiss()
                        Log.e(TAG, "Failed to create user: " + e.message)
                    }
                } else {
                    Toast.makeText(this, "Passwords did not match", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Please make sure all fields are not empty", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            pleaseWaitDialog.dismiss()
            Log.e(TAG, "Failed to create user: " + e.message)
        }
    }

    /**
     * Upload data to the firebase database.
     */
    private fun uploadToFirebase() {
        try {
            // Initialize local variable of profileImageURL to allow casting to type: Uri.
            val localProfileImageURL = this.profileImageURL

            // If the localProfileImageURL is not null...
            if (localProfileImageURL!= null) {
                // Obtain the image name as a string.
                val imageName: String = userNameET.text.toString() + "." + getExtension(localProfileImageURL)

                // Create a storage reference in the database for the image.
                val imageRef: StorageReference = firebaseStorageRef.child(imageName)

                // Initialize an UploadTask object representing the process of uploading the profile image object.
                val uploadTask: UploadTask = imageRef.putFile(localProfileImageURL)

                // Returns a new task with the specified Continuation applied to the Task.
                // A Continuation is a function called to continue after completion of a Task.
                uploadTask.continueWithTask { task ->
                    if (!task.isSuccessful) {
                        pleaseWaitDialog.dismiss()
                        Toast.makeText(this@RegisterActivity, "Couldn't upload image"
                                + task.exception?.message, Toast.LENGTH_SHORT).show()
                    }
                    imageRef.downloadUrl
                }.addOnCompleteListener { task ->
                    // If the execution of the upload task is successful.
                    if (task.isSuccessful) {

                        // HashMap used to store objects in database.
                        val hashMap: HashMap<String, Any> = HashMap()

                        // Obtain the checked radio button (male/female).
                        radioID = radioGroup.checkedRadioButtonId
                        radioButton = findViewById(radioID)

                        // Add user data to the HashMap.
                        hashMap["profileimageurl"] = task.result.toString()
                        hashMap["username"] = userNameET.text.toString()
                        hashMap["useremail"] = userEmailET.text.toString()
                        hashMap["firstname"] = userFirstNameET.text.toString()
                        hashMap["lastname"] = userLastNameET.text.toString()
                        hashMap["dob"] = dateET.text.toString()
                        hashMap["gender"] = radioButton.text.toString()
                        hashMap["numberoflikes"] = 0
                        hashMap["numberofimagestatus"] = 0
                        hashMap["numberoftextstatus"] = 0
                        hashMap["streakvalue"] = 0

                        // Create a new Firebase collection to store documents.
                        // Each user is represented by a document, which contains
                        // all the data about that user.
                        firebaseFirestore.collection("Users")
                            .document(userEmailET.text.toString())
                            .set(hashMap)
                            .addOnSuccessListener {
                                pleaseWaitDialog.dismiss()
                                // Sign out the user.
                                if (firebaseAuth.currentUser != null) {
                                    firebaseAuth.signOut()
                                }

                                Toast.makeText(this@RegisterActivity,
                                    "Successfully registered user",
                                    Toast.LENGTH_SHORT).show()

                                val homeIntent = Intent(
                                    applicationContext, HomeActivity::class.java)
                                startActivity(homeIntent)
                            }
                            .addOnFailureListener { e ->
                                pleaseWaitDialog.dismiss()
                                Toast.makeText(this@RegisterActivity,
                                    "Failed to upload user data: " + e.message,
                                    Toast.LENGTH_SHORT).show()
                            }

                    } else if (!task.isSuccessful) {
                        Toast.makeText(this@RegisterActivity,
                            "RegisterActivity: " + task.exception.toString(),
                            Toast.LENGTH_SHORT).show()
                    }
                }.addOnFailureListener { e ->
                    pleaseWaitDialog.dismiss()
                    Toast.makeText(this@RegisterActivity, e.message.toString(),
                        Toast.LENGTH_SHORT).show()
                }
            } else {
                pleaseWaitDialog.dismiss()
                Toast.makeText(this,
                    "Please select a profile picture", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Toast.makeText(this,
                "RegisterActivity: " + e.message, Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Get the extension for an image given a Uri.
     * @param uri The Uri of an image.
     * @return extension The file extension for the given Uri.
     * @return null Returns null
     */
    private fun getExtension(uri: Uri): String? {
        val extension: String?
        try {
            val contentResolver = contentResolver
            val mimeTypeMap = MimeTypeMap.getSingleton()

            extension = mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(uri))

            return extension
        } catch (e: Exception) {
            Log.e(TAG, "" + e.message)
        }
        return null
    }

    /**
     * Called when the CircleImageView is clicked. Opens the image gallery.
     */

    fun profileIVonClick(view: View) {
        openImageGallery()
    }

    /**
     * Called when the register button is clicked. Sends the user data to the database.
     */
    fun registerButtonOnClick(view: View) {
        createAccount()
    }

    /**
     * Opens the image gallery on the device.
     */
    private fun openImageGallery() {
        try {
            // Initialize intent to get media content and set content to show images.
            val galleryIntent = Intent(Intent.ACTION_GET_CONTENT)
            galleryIntent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*")

            // Open the image gallery in a new activity.
            startActivityForResult(galleryIntent,
                REQUEST_CODE
            )
        } catch (e: Exception) {
            Toast.makeText(this, "RegisterActivity: " + e.message, Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Sets the user's profile with their chosen image from the gallery.
     */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        // Sets the profile image if the selected data is valid (i.e. not null).
        if(data?.data != null) {
            profileImageURL = data.data
            profileIV.setImageURI(profileImageURL)
        } else {
            Toast.makeText(this, "No image selected", Toast.LENGTH_SHORT).show()
        }
    }
}
