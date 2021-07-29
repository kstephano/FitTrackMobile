package com.example.fittrackmobile.modelClasses

import com.google.firebase.Timestamp
import com.google.firebase.firestore.ServerTimestamp

/**
 * Class representing a favourited image status object.
 */
class FavouriteImageStatus {

    @ServerTimestamp lateinit var timestamp: Timestamp
    lateinit var useremail: String
     lateinit var firstname: String
    lateinit var statusimageurl: String
    lateinit var status: String
    lateinit var profileurl: String

    // Empty constructor needed when retrieving data from Firebase.
    constructor()

    constructor(
        useremail: String,
        firstname: String,
        status: String,
        statusimageurl: String,
        profileurl: String,
        timestamp: Timestamp
    ) {
        this.useremail = useremail
        this.firstname = firstname
        this.timestamp = timestamp
        this.status = status
        this.statusimageurl = statusimageurl
        this.profileurl = profileurl
    }
}