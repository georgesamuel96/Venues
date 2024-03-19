package com.example.venues.data.repository

import com.example.venues.data.model.SearchResponse
import retrofit2.Response

interface VenuesRepo {

    suspend fun searchLocation(points: String): Response<SearchResponse>?
}