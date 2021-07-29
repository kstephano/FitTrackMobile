package com.example.fittrackmobile.fragments


import android.app.Dialog
import android.content.ContentResolver
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
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.bumptech.glide.Glide

import com.example.fittrackmobile.R
import com.google.android.gms.tasks.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.UploadTask
import de.hdodenhof.circleimageview.CircleImageView

/**
 * A fragment for the user settings page.
 */
class SettingsFragment : Fragment() {

    interface GetUserInfo {
        fun getUserInfo(urlOfProfilePic: String, userName: String)
    }

    // XML variables.
    private lateinit var parent: View
    private lateinit var profileIV: CircleImageView
    private lateinit var updateBioBtn: Button
    private lateinit var updateProfilePicBtn: Button
    private lateinit var userNameET: EditText
    private lateinit var bioET: EditText
    private lateinit var cityET: EditText
    private lateinit var countryET: EditText
    private lateinit var waitDialog: Dialog

    // Class variables.
    private val TAG = "SettingsFragment"
    private lateinit var newProfilePicUri: Uri
    private val REQUEST_CODE = 1
    private var isUpdatedProfilePic = false
    private lateinit var extractedURL: String
    private lateinit var extractedUserName: String

    // Firebase variables.
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firebaseFirestore: FirebaseFirestore
    private lateinit var documentReference: DocumentReference
    private lateinit var storageReference: StorageReference
    private lateinit var firebaseStorage: FirebaseStorage

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        parent = inflater.inflate(R.layout.fragment_settings, container, false)
        waitDialog = Dialog(context!!)
        waitDialog.setContentView(R.layout.please_wait_dialog)
        waitDialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        initializeObjects()
        loadProfileInformationAtStart(object : GetUserInfo {
            override fun getUserInfo(urlOfProfilePic: String, userName: String) {
                extractedURL = urlOfProfilePic
                extractedUserName = userName
            }
        })

        // Set the OnClickListener for the update profile picture button.
        updateProfilePicBtn.setOnClickListener { openGallery() }

        // Set the OnClickListener for the update bio button.
        updateBioBtn.setOnClickListener { updateUserInfo() }

        return parent
    }

    /**
     * Method to load in user information.
     *
     * @param getUserInfo Interface used to obtain user name and profile image url.
     */
    private fun loadProfileInformationAtStart(getUserInfo: GetUserInfo) {
        try {
            if (firebaseAuth != null) {
                waitDialog.show()

                // Get the document for the current user.
                documentReference = firebaseFirestore.collection("Users")
                    .document(firebaseAuth.currentUser?.email as String)

                // Once obtained, update the relevant fields.
                documentReference.get().addOnSuccessListener { documentSnapshot ->
                    userNameET.setText(documentSnapshot?.getString("username"))
                    bioET.setText(documentSnapshot?.getString("bio"))
                    cityET.setText(documentSnapshot?.getString("city"))
                    countryET.setText(documentSnapshot?.getString("country"))

                    // Load the user's profile picture into the ImageView.
                    Glide.with(context!!).load(documentSnapshot?.getString("profileimageurl"))
                        .into(profileIV)

                    // GGet the user's profileimageurl and username.
                    getUserInfo.getUserInfo(
                        documentSnapshot?.getString("profileimageurl") as String,
                        documentSnapshot.getString("username") as String)

                    waitDialog.dismiss()
                }.addOnFailureListener { e -> Log.e(TAG, "" + e.message) }
            } else {
                Log.i(TAG, getString(R.string.logcat_no_user))
            }
        } catch (e: Exception) {
            Log.e(TAG, "" + e.message)
        }
    }

    /**
     * Method to update relevant profile information in Firebase.
     */
    private fun updateUserInfo() {
        try {
            if (isUpdatedProfilePic) {
                waitDialog.show()
                storageReference = FirebaseStorage.getInstance().getReference("ImageFolder")

                if (newProfilePicUri != null) {
                    val imageName = extractedUserName + "." + getExtension(newProfilePicUri)
                    val imgRef = storageReference.child(imageName)

                    val uploadTask = imgRef.putFile(newProfilePicUri)

                    uploadTask.continueWithTask { task ->
                        // If the task is unsuccessful, show a toast message.
                        if (!task.isSuccessful) {
                            Toast.makeText(context, getString(R.string.cant_upload_image_msg)
                                    + task.exception?.message, Toast.LENGTH_SHORT).show()
                        }

                        imgRef.downloadUrl
                    }.addOnCompleteListener { task ->
                        // If the task is successful, update Firebase variables.
                        if (task.isSuccessful) {

                            // Create the hashMap and add updated values to it.
                            val hashMap: HashMap<String, Any> = HashMap()
                            hashMap["profileimageurl"] = task.result.toString()
                            hashMap["username"] = userNameET.text.toString()
                            hashMap["bio"] = bioET.text.toString()
                            hashMap["city"] = cityET.text.toString()
                            hashMap["country"] = countryET.text.toString()

                            firebaseFirestore.collection("Users")
                                .document(firebaseAuth.currentUser?.email as String)
                                .update(hashMap).addOnSuccessListener {
                                    Toast.makeText(context,
                                        getString(R.string.profile_successfully_updated_msg),
                                        Toast.LENGTH_SHORT).show()

                                    waitDialog.dismiss()
                                }.addOnFailureListener { e ->
                                    waitDialog.dismiss()
                                    Log.e(TAG, "" + e.message)
                                }
                        } else if (!task.isSuccessful) {
                            waitDialog.dismiss()
                            Log.e(TAG, "" + task.exception)
                        }
                    }
                } else {
                    Toast.makeText(context,
                        getString(R.string.no_image_selected_msg),
                        Toast.LENGTH_SHORT).show()
                }
            // Otherwise, if the user profile picture isn't being changed,
            // only update the other data.
            } else {
                waitDialog.show()

                // Create the hashMap and add updated values to it.
                val hashMap: HashMap<String, Any> = HashMap()
                hashMap["username"] = userNameET.text.toString()
                hashMap["bio"] = bioET.text.toString()
                hashMap["city"] = cityET.text.toString()
                hashMap["country"] = countryET.text.toString()

                firebaseFirestore.collection("Users")
                    .document(firebaseAuth.currentUser?.email as String)
                    .update(hashMap).addOnSuccessListener {
                        Toast.makeText(context,
                            getString(R.string.profile_successfully_updated_msg),
                            Toast.LENGTH_SHORT).show()

                        waitDialog.dismiss()
                    }.addOnFailureListener { e ->
                        waitDialog.dismiss()
                        Log.e(TAG, "" + e.message)
                    }

            }
        } catch (e: Exception) {
            Log.e(TAG, "" + e.message)
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
            val contentResolver = activity?.contentResolver as ContentResolver
            val mimeTypeMap = MimeTypeMap.getSingleton()

            extension = mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(uri))

            return extension
        } catch (e: java.lang.Exception) {
            Log.e(TAG, "" + e.message)
        }
        return null
    }

    /**
     * Method to set the ImageView with the new profile picture selected from the media gallery.
     */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (data?.data != null && data != null) {
            newProfilePicUri = data.data as Uri
            profileIV.setImageURI(newProfilePicUri)
            isUpdatedProfilePic = true
        } else {
            Toast.makeText(context, getString(R.string.no_image_selected_msg), Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Method to open the device's image gallery.
     */
    private fun openGallery() {
        try {
            val galleryIntent = Intent(Intent.ACTION_GET_CONTENT)
            galleryIntent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*")
            startActivityForResult(galleryIntent, REQUEST_CODE)
        } catch (e: Exception) {
            Log.e(TAG, "" + e.message)
        }
    }

    /**
     * Method to initialize variables and map Kotlin/Java objects to XML objects.
     */
    private fun initializeObjects() {
        try {
            firebaseAuth = FirebaseAuth.getInstance()
            firebaseFirestore = FirebaseFirestore.getInstance()
            firebaseStorage = FirebaseStorage.getInstance()

            profileIV = parent.findViewById(R.id.frag_settings_profilePicIV)
            updateProfilePicBtn = parent.findViewById(R.id.frag_settings_editProfilePicBtn)
            updateBioBtn = parent.findViewById(R.id.frag_settings_updateProfileBtn)
            userNameET = parent.findViewById(R.id.frag_settings_userNameET)
            bioET = parent.findViewById(R.id.frag_settings_userBioET)
            cityET = parent.findViewById(R.id.frag_settings_userCityET)
            countryET = parent.findViewById(R.id.frag_settings_userCountryET)
        } catch (e: Exception) {
            Log.e(TAG, "" + e.message)
        }
    }
}
