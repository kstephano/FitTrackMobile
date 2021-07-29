package com.example.fittrackmobile.modelClasses

import com.google.firebase.Timestamp
import com.google.firebase.firestore.ServerTimestamp

/**
 * Class representing a notification object.
 */
class Notification {

    @ServerTimestamp lateinit var timestamp: Timestamp
    lateinit var action: String
    lateinit var email: String
    lateinit var type: String

    // Empty constructor needed when retrieving data from Firebase.
    constructor()

    /**
     * Main constructor used to initialize class variables.
     */
    constructor(timestamp: Timestamp, action: String, email: String, type: String) {
        this.timestamp = timestamp
        this.action = action
        this.email = email
        this.type = type
    }
}