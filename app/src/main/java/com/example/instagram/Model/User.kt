package com.example.instagram.Model

class User {

    private var uid: String = ""
    private var userName: String = ""
    private var fullName: String = ""
    private var bio: String = ""
    private var image: String = ""

    constructor()
    constructor(uid: String, userName: String, fullName: String, bio: String, image: String) {
        this.uid = uid
        this.userName = userName
        this.fullName = fullName
        this.bio = bio
        this.image = image
    }

    fun getUID(): String {
        return uid
    }

    fun getUserName(): String {
        return userName
    }

    fun setUserName(userName: String) {
        this.userName = userName
    }

    fun getFullName(): String {
        return fullName
    }

    fun setFullName(fullName: String) {
        this.fullName = fullName
    }

    fun getBio(): String {
        return bio
    }

    fun setBio(bio: String) {
        this.bio = bio
    }

    fun getImage(): String {
        return image
    }

    fun setB(image: String) {
        this.image = image
    }
}