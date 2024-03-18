package com.example.venues.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.venues.data.model.User
import com.example.venues.data.remote.Resource
import com.example.venues.utils.Constants.USERS_COLLECTION
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val firebaseFireStore: FirebaseFirestore
) : ViewModel() {

    private val loadingState = MutableLiveData<Boolean>()
    val loadingStateLiveData: LiveData<Boolean> = loadingState

    private val currentUser = MutableLiveData<Resource<FirebaseUser>>()
    val currentUserLiveData: LiveData<Resource<FirebaseUser>> = currentUser

    private val newUser = MutableLiveData<Resource<User>>()
    val newUserLiveData: LiveData<Resource<User>> = newUser

    fun register(user: User) {
        viewModelScope.launch(Dispatchers.IO) {
            loadingState.postValue(true)

            firebaseAuth.createUserWithEmailAndPassword(user.email, user.password!!)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        currentUser.postValue(Resource.success(firebaseAuth.currentUser))
                    } else {
                        Log.e("LoginViewModel", "createUserWithEmail:failure", task.exception)
                        currentUser.postValue(Resource.error("Authentication failed."))

                        loadingState.postValue(false)
                    }
                }
        }
    }

    fun saveUserData(user: User) {
        viewModelScope.launch(Dispatchers.IO) {
            firebaseFireStore.collection(USERS_COLLECTION)
                .add(user)
                .addOnCompleteListener {
                    if(it.isSuccessful) {
                        newUser.postValue(Resource.success(user))
                    } else {
                        Log.e("LoginViewModel", "createUserFireStore:failure", it.exception)
                        newUser.postValue(Resource.error("Create create new user."))
                    }

                    loadingState.postValue(false)
                }
        }
    }

    fun login(user: User) {
        loadingState.postValue(true)

        firebaseAuth.signInWithEmailAndPassword(user.email, user.password!!)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    currentUser.postValue(Resource.success(firebaseAuth.currentUser))
                } else {
                    Log.e("LoginViewModel", "createUserWithEmail:failure", task.exception)
                    currentUser.postValue(Resource.error("Authentication failed."))
                }

                loadingState.postValue(false)
            }
    }
}