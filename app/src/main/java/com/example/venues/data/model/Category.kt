package com.example.venues.data.model

data class Category(
    val id: String,
    val name: String,
    val pluralName: String,
    val shortName: String,
    val icon: Icon,
    val categoryCode: Int,
    val mapIcon: String,
    val primary: Boolean
)
