package com.example.fittrackmobile

import android.app.DatePickerDialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.content.Intent
import android.net.Uri
import android.provider.MediaStore
import android.view.View
import android.webkit.MimeTypeMap
import android.widget.*
import androidx.annotation.NonNull
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

    // Uniform Resource Identifier for retrieving profile picture.
    private var profileImageURL: Uri? = null

    // Initialize an instance of the calendar.
    private var cal = Calendar.getInstance()

    // Firebase variables.
    private lateinit var firebaseFirestore: FirebaseFirestore
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firebaseStorageRef: StorageReference

    // User data variables.
    private lateinit var validPassword: String
    private var radioID: Int = 0

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
        dateET = findViewById<EditText>(R.id.register_dob_ET) as EditText

        // Set the tool bar.
        setSupportActionBar(findViewById(R.id.register_toolbar))
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setTitle(R.string.register_title)

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
                dateET.text = sdf.format(cal.time)
            }
        }

        // Set the data picker dialog to show when the date TextView object is clicked.
        dateET.setOnClickListener(object : View.OnClickListener {
            override fun onClick(view: View) {
                val dpd = DatePickerDialog(this@RegisterActivity,
                    R.style.MyDialogTheme,
                    dateSetListener,
                    cal.get(Calendar.YEAR),
                    cal.get(Calendar.MONTH),
                    cal.get(Calendar.DAY_OF_MONTH))
                dpd.show()
            }
        })

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
                if (userPasswordET.text.toString().equals(userConfirmPasswordET.text.toString())) {
                    Toast.makeText(this, "Attempting to register user", Toast.LENGTH_SHORT).show()
                    validPassword =  userPasswordET.text.toString()

                    // Create new user and add success/failure listeners to check if successful or not.
                    firebaseAuth.createUserWithEmailAndPassword(
                        userEmailET.text.toString(), validPassword
                    ).addOnSuccessListener(object : OnSuccessListener<AuthResult> {
                        // Executes upon successful authentication of user account.
                        override fun onSuccess(authResult: AuthResult) {
                            // Upload new user data to the database.
                            uploadToFirebase()
                        }
                    }).addOnFailureListener(object : OnFailureListener {
                        // Executes upon failure to authenticate user account.
                        override fun onFailure(e: Exception) {
                            Toast.makeText(this@RegisterActivity,
                                "Failed to create user: " + e.message, Toast.LENGTH_SHORT).show()
                        }
                    })
                } else {
                    Toast.makeText(this, "Passwords did not match", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Please make sure all fields are not empty", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Toast.makeText(this,
                "Could not create user account:" + e.message, Toast.LENGTH_SHORT).show()
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

                Toast.makeText(this, "Uploading profile picture", Toast.LENGTH_SHORT).show()

                // Returns a new task with the specified Continuation applied to the Task.
                // A Continuation is a function called to continue after completion of a Task.
                // In this case the Continuation function passes through an encapsulation of the
                // state regarding the running UploadTask to check if it is successful or not.
                uploadTask.continueWithTask(object : Continuation<UploadTask.TaskSnapshot, Task<Uri>> {
                    override fun then (@NonNull task: Task<UploadTask.TaskSnapshot>): Task<Uri> {
                        if (!task.isSuccessful) {
                            Toast.makeText(this@RegisterActivity, "Couldn't upload image"
                                    + task.exception, Toast.LENGTH_SHORT).show()
                        }
                        return imageRef.downloadUrl
                    }
                // Listener to execute code and upload data to database upon completion of task.
                }).addOnCompleteListener(object : OnCompleteListener<Uri> {
                    override fun onComplete(@NonNull task: Task<Uri>) {
                        // If the execution of the upload task is successful.
                        if (task.isSuccessful) {

                            Toast.makeText(this@RegisterActivity,
                                "Uploading user data", Toast.LENGTH_SHORT).show()

                            // HashMap used to store objects in database.
                            val hashMap: HashMap<String, Any> = HashMap()

                            // Obtain the checked radio button (male/female).
                            radioID = radioGroup.checkedRadioButtonId
                            radioButton = findViewById(radioID)

                            // Add user data to the HashMap.
                            hashMap.put("profileimageurl", task.result.toString())
                            hashMap.put("username", userNameET.text.toString())
                            hashMap.put("useremail", userEmailET.text.toString())
                            hashMap.put("firstname", userFirstNameET.text.toString())
                            hashMap.put("lastname", userLastNameET.text.toString())
                            hashMap.put("dob", dateET.text.toString())
                            hashMap.put("userpassword", userPasswordET.text.toString())
                            hashMap.put("gender", radioButton.text.toString())

                            hashMap.put("numberoflikes", 0)
                            hashMap.put("numberofimagestatus", 0)
                            hashMap.put("numberoftextstatus", 0)
                            hashMap.put("streakvalue", 0)

                            // Create a new Firebase collection to store documents.
                            // Each user is represented by a document, which contains
                            // all the data about that user.
                            firebaseFirestore.collection("Users")
                                .document(userEmailET.text.toString())
                                .set(hashMap)
                                .addOnSuccessListener(object : OnSuccessListener<Void> {
                                    override fun onSuccess(void: Void?) {
                                        // Sign out the user.
                                        if (firebaseAuth.currentUser != null) {
                                            firebaseAuth.signOut()
                                        }

                                        Toast.makeText(this@RegisterActivity,
                                            "Successfully registered user",
                                            Toast.LENGTH_SHORT).show()

                                        val loginIntent = Intent(
                                            applicationContext, LoginActivity::class.java)
                                        startActivity(loginIntent)
                                    }
                                })
                                .addOnFailureListener(object : OnFailureListener {
                                    override fun onFailure(@NonNull e: Exception) {
                                        Toast.makeText(this@RegisterActivity,
                                            "Failed to upload user data: " + e.message,
                                            Toast.LENGTH_SHORT).show()
                                    }
                                })

                        } else if (!task.isSuccessful) {
                            Toast.makeText(this@RegisterActivity,
                                "RegisterActivity: " + task.exception.toString(),
                                Toast.LENGTH_SHORT).show()
                        }
                    }
                })
            } else {
                Toast.makeText(this,
                    "Please select a profile picture", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Toast.makeText(this,
                "RegisterActivity: " + e.message, Toast.LENGTH_SHORT).show()
        }
    }

    /**
     *
     */
    private fun getExtension(uri: Uri): String? {
        val extension: String?
        try {
            val contentResolver = contentResolver
            val mimeTypeMap = MimeTypeMap.getSingleton()

            extension = mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(uri))

            return extension
        } catch (e: Exception) {
            Toast.makeText(this, "RegisterActivity: " + e.message, Toast.LENGTH_SHORT).show()
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
            startActivityForResult(galleryIntent, REQUEST_CODE)
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
