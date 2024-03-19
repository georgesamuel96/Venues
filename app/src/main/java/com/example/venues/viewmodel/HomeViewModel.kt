package com.example.venues.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.venues.R
import com.example.venues.data.model.Venues
import com.example.venues.data.remote.Resource
import com.example.venues.data.repository.VenuesRepo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val venuesRepo: VenuesRepo
) : ViewModel() {

    private val loadingState = MutableLiveData<Boolean>()
    val loadingStateLiveData: LiveData<Boolean> = loadingState

    private val venuesList = MutableLiveData<Resource<List<Venues>>>()
    val venuesListLiveData: LiveData<Resource<List<Venues>>> = venuesList

    fun searchLocation(lat: Double, long: Double) {
        viewModelScope.launch(Dispatchers.IO) {
            loadingState.postValue(true)

            val response = venuesRepo.searchLocation("27.444442064288378,30.82587573991425")

            if(response?.isSuccessful == true) {
                val body = response.body()
                if(body != null && response.body()?.response != null) {
                    val data = response.body()?.response?.venues
                    venuesList.postValue(Resource.success(data))
                } else {
                    venuesList.postValue(Resource.error(R.string.no_venues_near_you))
                }
            } else {
                Log.e("HomeViewModel", response?.message()?: "")
                venuesList.postValue(Resource.error(R.string.something_went_wrong))
            }

            loadingState.postValue(false)
        }
    }
}