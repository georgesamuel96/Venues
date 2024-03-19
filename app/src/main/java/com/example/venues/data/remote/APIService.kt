package com.example.venues.data.remote

import com.example.venues.data.model.SearchResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface APIService {

    @GET("v2/venues/search")
    suspend fun searchLocation(
        @Query("ll") points: String,
        @Query("client_id") clientId: String,
        @Query("client_secret") clientSecret: String,
        @Query("v") version: String
    ): Response<SearchResponse>?
}