package com.example.venues.data.repository

import com.example.venues.data.model.SearchResponse
import com.example.venues.data.remote.APIService
import retrofit2.Response
import javax.inject.Inject

class VenuesRepoImpl @Inject constructor(
    private val apiService: APIService
): VenuesRepo {

    override suspend fun searchLocation(points: String): Response<SearchResponse>? {
        return apiService.searchLocation(
            points,
            "4EQRZPSGKBZGFSERGJY055FRW2OSPJRZYR4C3J0JN2CQQFIV",
            "AJR4B5LLRONWAJWJJOACHAFLCWS2YJAZMGQNFFZQP0IB3THR",
            "20180910"
        )
    }
}