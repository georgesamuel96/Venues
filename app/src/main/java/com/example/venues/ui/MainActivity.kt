package com.example.venues.ui

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import com.example.venues.R
import com.example.venues.databinding.ActivityMainBinding
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    @Inject
    lateinit var firebaseAuth: FirebaseAuth

    private lateinit var navHostFragment: NavHostFragment
    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)

        setContentView(binding.root)

        initialization()
        listeners()
    }

    private fun initialization() {
        checkUserLoggedIn()

        binding.apply {
            setSupportActionBar(topAppBar)

            setGraph()

            val appBarConfiguration = AppBarConfiguration(navController.graph, drawerLayout)
            setupActionBarWithNavController(navController, appBarConfiguration)
        }
    }

    private fun setGraph() {
        navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController
        val navGraph = navController.navInflater.inflate(R.navigation.main_navigation)
        navController.graph = navGraph
    }

    private fun listeners() {
        binding.apply {
            navView.setNavigationItemSelectedListener { menuItem ->

                uncheckItems()

                menuItem.isChecked = true
                drawerLayout.closeDrawers()

                when (menuItem.itemId) {
                    R.id.homeFragment -> {
                        navController.navigate(R.id.homeFragment)
                        true
                    }

                    R.id.profileFragment -> {
                        navController.navigate(R.id.profileFragment)
                        true
                    }

                    R.id.item_logout -> {
                        firebaseAuth.signOut()
                        openLoginActivity()

                        true
                    }

                    else -> false
                }
            }

            topAppBar.setNavigationOnClickListener {
                drawerLayout.open()
            }
        }
    }

    private fun uncheckItems() {
        binding.apply {
            for(i in 0..2) {
                navView.menu.getItem(i).isChecked = false
            }
        }
    }

    private fun openLoginActivity() {
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }

    private fun checkUserLoggedIn() {
        val currentUser = firebaseAuth.currentUser
        if (currentUser == null) {
            openLoginActivity()
        }
    }

}