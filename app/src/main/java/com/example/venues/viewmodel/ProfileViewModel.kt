package com.example.venues.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.venues.R
import com.example.venues.data.model.User
import com.example.venues.data.remote.Resource
import com.example.venues.utils.Constants
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
open class ProfileViewModel @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val firebaseFireStore: FirebaseFirestore
) : ViewModel() {

    protected val loadingState = MutableLiveData<Boolean>()
    val loadingStateLiveData: LiveData<Boolean> = loadingState

    private val user = MutableLiveData<Resource<User>>()
    val userLiveData: LiveData<Resource<User>> = user

    fun getUser() {
        viewModelScope.launch(Dispatchers.IO) {
            loadingState.postValue(true)

            firebaseFireStore.collection(Constants.USERS_COLLECTION)
                .whereEqualTo(Constants.EMAIL_FIELD, firebaseAuth.currentUser!!.email)
                .get().addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        user.postValue(Resource.success(mapToClass(task.result?.documents?.get(0)?.data)))
                    } else {
                        Log.e("ProfileViewModel", "createUserWithEmail:failure", task.exception)
                        user.postValue(Resource.error(R.string.auth_failed))
                    }

                    loadingState.postValue(false)
                }
        }
    }

    private fun mapToClass(data: Map<String, Any>?): User {
        return User(
            firstName = data!![Constants.FIRST_NAME_FIELD] as String,
            lastName = data[Constants.LAST_NAME_FIELD] as String,
            email = data[Constants.EMAIL_FIELD] as String,
            birthdate = data[Constants.BIRTH_DATE_FIELD] as String
        )
    }
}