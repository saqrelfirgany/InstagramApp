package com.example.instagram.Model

data class Notification(
    val userId: String = "",
    val text: String = "",
    val postId: String = "",
    val ispost: Boolean = false
)