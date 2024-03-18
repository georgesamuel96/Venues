package com.example.venues.data.model

data class User (
    val firstName: String = "",
    val lastName: String = "",
    val email: String = "",
    val birthdate: String = "",
    val password: String? = null,
    val uID: String? = null
)