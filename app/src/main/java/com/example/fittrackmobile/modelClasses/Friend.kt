package com.example.fittrackmobile.modelClasses

/**
 * Class representing a friend object to be viewed in a friends list RecyclerView.
 */
class Friend {

    lateinit var useremail: String
    lateinit var profileimageurl: String
    lateinit var firstname: String
    lateinit var lastname: String

    // Empty constructor needed when retrieving data from Firebase.
    constructor()

    /**
     * Main constructor used to initialize class variables.
     */
    constructor(
        useremail: String,
        profileurl: String,
        firstname: String,
        lastname: String
    ) {
        this.useremail = useremail
        this.profileimageurl = profileurl
        this.firstname = firstname
        this.lastname = lastname
    }
}