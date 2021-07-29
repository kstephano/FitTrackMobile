package com.example.fittrackmobile.modelClasses

import com.google.firebase.Timestamp
import com.google.firebase.firestore.ServerTimestamp

/**
 * Class representing a favourited text status object.
 */
class FavouriteTextStatus {

    @ServerTimestamp lateinit var timestamp: Timestamp
    lateinit var useremail: String
    lateinit var firstname: String
    lateinit var status: String
    lateinit var profileurl: String

    // Empty constructor needed when retrieving data from Firebase.
    constructor()

    constructor(
        useremail: String,
        firstname: String,
        status: String,
        profileurl: String,
        timestamp: Timestamp
    ) {
        this.useremail = useremail
        this.firstname = firstname
        this.timestamp = timestamp
        this.status = status
        this.profileurl = profileurl
    }
}