package com.example.venues.ui

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.venues.databinding.ActivityMainBinding
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    @Inject
    lateinit var firebaseAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)

        setContentView(binding.root)

        initialization()

        binding.btnLogout.setOnClickListener {
            firebaseAuth.signOut()
            openLoginActivity()
        }
    }

    private fun openLoginActivity() {
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }

    private fun initialization() {
        checkUserLoggedIn()
    }

    private fun checkUserLoggedIn() {
        val currentUser = firebaseAuth.currentUser
        if (currentUser == null) {
            openLoginActivity()
        }
    }

}