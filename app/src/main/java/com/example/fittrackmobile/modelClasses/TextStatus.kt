package com.example.fittrackmobile.modelClasses

import com.google.firebase.Timestamp
import com.google.firebase.firestore.ServerTimestamp

/**
 * Class representing a text status object.
 */
class TextStatus {

    @ServerTimestamp lateinit var timestamp: Timestamp
    lateinit var statusID: String
    lateinit var useremail: String
    lateinit var firstname: String
    lateinit var status: String
    lateinit var profileurl: String
    var numberofcomments: Int = 0
    var numberoflaughreacts: Int = 0
    var numberoflovereacts: Int = 0
    var numberofsadreacts: Int = 0

    // Empty constructor needed when retrieving data from Firebase.
    constructor()

    /**
     * Main constructor used to initialize class variables.
     */
    constructor(
        useremail: String,
        firstname: String,
        timestamp: Timestamp,
        status: String,
        profileurl: String,
        numberofcomments: Int,
        numberoflaughreacts: Int,
        numberoflovereacts: Int,
        numberofsadreacts: Int
    ) {
        this.useremail = useremail
        this.firstname = firstname
        this.timestamp = timestamp
        this.status = status
        this.profileurl = profileurl
        this.numberofcomments = numberofcomments
        this.numberoflaughreacts = numberoflaughreacts
        this.numberoflovereacts = numberoflovereacts
        this.numberofsadreacts = numberofsadreacts
    }
}