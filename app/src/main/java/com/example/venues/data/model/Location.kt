package com.example.venues.data.model

data class Location(
    val address: String,
    val lat: String,
    val lng: String,
    val labeledLatLngs: List<LabeledLatLng>,
    val distance: Double,
    val postalCode: String,
    val cc: String,
    val city: String,
    val state: String,
    val country: String,
    val formattedAddress: List<String>

)
