package com.example.fittrackmobile.modelClasses

import com.google.firebase.Timestamp

/**
 * Class representing a comment.
 */
class Comment {

    lateinit var useremail: String
    lateinit var comment: String
    lateinit var commenter: String
    lateinit var timestamp: Timestamp
    lateinit var profilepicurl: String

    constructor()

    /**
     * Main constructor used to initialize class variables.
     */
    constructor(
        useremail: String,
        comment: String,
        commenter: String,
        currentDateTime: Timestamp,
        profilePicURL: String) {
        this.useremail = useremail
        this.comment = comment
        this.commenter = commenter
        this.timestamp = currentDateTime
        this.profilepicurl = profilePicURL
    }
}