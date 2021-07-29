package com.example.fittrackmobile.fragments

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.MimeTypeMap
import android.widget.*
import com.example.fittrackmobile.activities.HomeActivity
import com.example.fittrackmobile.R
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.UploadTask

/**
 * Fragment for users to create and upload image statuses.
 */
class AddImageStatusFragment : Fragment() {

    // Class variables.
    val TAG = "AddImageStatusFragment"
    private lateinit var objectView: View
    private val preCode: Int = 1000
    private lateinit var selectedImageUri: Uri
    private lateinit var currentUserEmail: String
    private lateinit var currentUserFirstName: String
    private lateinit var currentUserProfileUrl: String

    // XML variables.
    private lateinit var statusIV: ImageView
    private lateinit var statusET: EditText
    private lateinit var publishStatusBtn: Button
    private lateinit var goBackBtn: FloatingActionButton
    private lateinit var progressBar: ProgressBar

    // Firebase objects.
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firebaseFirestore: FirebaseFirestore
    private lateinit var documentReference: DocumentReference
    private lateinit var storageReference: StorageReference

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        objectView = inflater.inflate(R.layout.fragment_add_image_status, container, false)
        attachJavaToXMLViews()

        storageReference = FirebaseStorage.getInstance().getReference("ImageStatusFolder")

        // Set the OnClickListener for the status image view.
        statusIV.setOnClickListener { openMobileGallery() }

        // Set the OnClickListener for the publish status button.
        publishStatusBtn.setOnClickListener { publishStatus() }

        return objectView
    }

    /**
     * Uploads the image status to Firebase.
     */
    private fun publishStatus() {
        try {
            if (selectedImageUri != null) {
                firebaseAuth = FirebaseAuth.getInstance()
                firebaseFirestore = FirebaseFirestore.getInstance()

                if (firebaseAuth != null && statusET.text.toString().isNotEmpty()) {
                    progressBar.visibility = View.VISIBLE
                    publishStatusBtn.isEnabled = true
                    currentUserEmail = firebaseAuth.currentUser?.email ?: "No user logged in"

                    // Get a document reference for the current user.
                    documentReference = firebaseFirestore.collection("Users")
                        .document(currentUserEmail)

                    // Once successfully obtained document, get user profile pic url and first name.
                    documentReference.get().addOnSuccessListener { documentSnapshot ->
                        currentUserProfileUrl = documentSnapshot?.getString(
                            "profileimageurl") as String
                        currentUserFirstName = documentSnapshot.getString(
                            "firstname") as String
                    }.addOnFailureListener { e ->
                        Log.e(TAG, "Failed to get profile URL: " + e.message)

                        progressBar.visibility = View.INVISIBLE
                        publishStatusBtn.isEnabled = true
                    }

                    // Create an image reference to uniquely identify the image.
                    val statusImageRef: String = System.currentTimeMillis().toString() +
                            "." + getExtension(selectedImageUri)
                    val imgRef = storageReference.child(statusImageRef)

                    // Create an upload task for saving the image to Firebase Storage.
                    val uploadTask: UploadTask = imgRef.putFile(selectedImageUri)
                    uploadTask.continueWithTask { task ->
                        if (!task.isSuccessful) {
                            Log.e(TAG, "" + task.exception?.message)
                        }

                        imgRef.downloadUrl
                    }.addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            val statusMap: HashMap<String,Any> = HashMap()

                            statusMap["timestamp"] = Timestamp.now()
                            statusMap["useremail"] = currentUserEmail
                            statusMap["firstname"] = currentUserFirstName
                            statusMap["profileurl"] = currentUserProfileUrl
                            statusMap["status"] = statusET.text.toString()
                            statusMap["numberoflaughreacts"] = 0
                            statusMap["numberoflovereacts"] = 0
                            statusMap["numberofsadreacts"] = 0
                            statusMap["numberofcomments"] = 0
                            statusMap["currentflag"] = "none"
                            statusMap["statusimageurl"] = task.result.toString()

                            firebaseFirestore.collection("ImageStatus")
                                .document(System.currentTimeMillis().toString())
                                .set(statusMap)
                                .addOnSuccessListener {
                                    //Obtain a document reference for the current user.
                                    documentReference = firebaseFirestore.collection("Users")
                                        .document(currentUserEmail)

                                    documentReference.get()
                                        .addOnSuccessListener { documentSnapshot ->
                                            var totalNumberOfImageStatuses: Long = documentSnapshot.getLong("numberofimagestatus") ?: 0
                                            totalNumberOfImageStatuses++

                                            val updatedDataMap: HashMap<String, Any> = HashMap()
                                            updatedDataMap["numberofimagestatus"] =
                                                totalNumberOfImageStatuses

                                            // Update the current user's document.
                                            firebaseFirestore.collection("Users")
                                                .document(currentUserEmail)
                                                .update(updatedDataMap)

                                            Toast.makeText(context, resources.getString(R.string.status_published), Toast.LENGTH_SHORT).show()

                                            progressBar.visibility = View.INVISIBLE
                                            publishStatusBtn.isEnabled = true
                                            val intent = Intent(context, HomeActivity::class.java)
                                            startActivity(intent)

                                            activity?.finish()
                                        }.addOnFailureListener { e -> Log.e(TAG,"Failed to update number of image statuses" + e.message) }
                                }
                        }
                    }
                } else {
                    Toast.makeText(context,
                        resources.getString(R.string.enter_image_status_description),
                        Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(context, resources.getString(R.string.enter_image_status_msg),
                    Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Log.e(TAG, "" + e.message)
        }
    }

    /**
     * Get the extension for an image given a Uri.
     *
     * @param uri The Uri of an image.
     * @return extension The file extension for the given Uri.
     * @return null Returns null
     */
    private fun getExtension(selectedImageUri: Uri): String? {
        val extension: String?
        return try {
            val contentResolver = activity?.contentResolver
            val mimeTypeMap = MimeTypeMap.getSingleton()

            extension = mimeTypeMap.getExtensionFromMimeType(contentResolver?.getType(selectedImageUri))

            extension
        } catch (e: Exception) {
            Log.e(TAG, "" + e.message)
            "No Extension"
        }
    }

    /**
     * Retrieve the image selected by the user and upload it into the ImageView.
     */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (data!= null && data.data != null) {
            selectedImageUri= data.data as Uri
            statusIV.setImageURI(selectedImageUri)
        } else {
            Toast.makeText(context, "No image selected", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Method to open the image gallery on the user's device.
     */
    private fun openMobileGallery() {
        try {
            val galleryIntent = Intent(Intent.ACTION_GET_CONTENT)
            galleryIntent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*")
            startActivityForResult(galleryIntent, preCode)
        } catch (e: Exception) {
            Log.e(TAG, "" + e.message)
        }
    }

    /**
     * Attach Java/Kotlin objects to their XML counterparts.
     * Also set the OnClickListener for the go back button.
     */
    private fun attachJavaToXMLViews() {
        try {
            statusIV = objectView.findViewById(R.id.frag_addImageStatus_getImageIV)
            statusET = objectView.findViewById(R.id.frag_addImageStatus_descriptionET)
            publishStatusBtn = objectView.findViewById(R.id.frag_addImageStatus_publishStatusBtn)
            goBackBtn = objectView.findViewById(R.id.frag_addImageStatus_goBackFAB)
            progressBar = objectView.findViewById(R.id.frag_addImageStatus_progressBar)

            goBackBtn.setOnClickListener {
                val intent = Intent(context, HomeActivity::class.java)
                startActivity(intent)
                activity?.finish()
            }
        } catch (e: Exception) {
            Log.e(TAG, "" + e.message)
        }
    }
}
