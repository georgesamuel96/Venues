package com.example.venues.data.model

data class Venues(
    val id: String,
    val name: String,
    val location: Location,
    val categories: List<Category>,
    val createdAt: Long
)
