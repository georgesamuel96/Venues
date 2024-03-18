package com.example.venues.utils

import android.content.Context
import androidx.constraintlayout.widget.ConstraintLayout
import com.google.android.material.snackbar.Snackbar

object Util {

    fun ConstraintLayout.showSnackBar(message: String) {
        Snackbar.make(this, message, Snackbar.LENGTH_LONG).show()
    }
}