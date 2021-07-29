package com.example.fittrackmobile.appClasses

import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore

/**
 * Generates notifications in the app for when an action is performed.
 */
class AddNotifications {

    /**
     * Generate the notification and upload it to Firebase.
     */
    fun generateNotification(currentUserEmail: String, userAction: String,
                             statusType: String, recipientEmail: String) {

        val hashMap: HashMap<String, Any> = HashMap()
        val firebaseFirestore = FirebaseFirestore.getInstance()

        hashMap["timestamp"] = Timestamp.now()
        hashMap["email"] = currentUserEmail
        hashMap["action"] = userAction
        hashMap["type"] = statusType

        firebaseFirestore.collection("Users")
            .document(recipientEmail)
            .collection("Notifications")
            .document(System.currentTimeMillis().toString())
            .set(hashMap)
    }
}