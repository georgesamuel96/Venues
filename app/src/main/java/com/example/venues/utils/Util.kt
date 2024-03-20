package com.example.venues.utils

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.constraintlayout.widget.ConstraintLayout
import com.example.venues.R
import com.example.venues.data.model.Venues
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar

object Util {

    fun ConstraintLayout.showSnackBar(message: String) {
        Snackbar.make(this, message, Snackbar.LENGTH_LONG).show()
    }

    fun Activity.showAlert(
        title: String,
        message: String,
        canCancel: Boolean = true,
        textPositiveButton: String = getString(R.string.okay),
        onClick: (() -> Unit)? = null
    ) {
        val alert = MaterialAlertDialogBuilder(this)
            .setTitle(title)
            .setMessage(message)
            .setCancelable(canCancel)
            .setPositiveButton(textPositiveButton) { _, _ ->
                onClick?.invoke()
            }

        if(canCancel) {
            alert.setNegativeButton(getString(R.string.cancel)) { dialog, _ ->
                dialog.dismiss()
            }
        }

        alert.show()
    }

    fun goToAppDetailsSettings(activity: Activity) {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.fromParts("package", activity.packageName, null)
        }
        activity.startActivityForResult(intent, 0)
    }

    fun getAddress(venues: Venues): String {
        val stringBuilder = StringBuilder()
        venues.location.formattedAddress.forEach {
            if (it.isNotEmpty()) {
                stringBuilder.append(it)
                stringBuilder.append(", ")
            }
        }
        val fullAddress = stringBuilder.toString()
        return fullAddress.substring(
            0,
            if (fullAddress.length >= 2) fullAddress.length - 2 else fullAddress.length
        )
    }

    fun getIconURL(venues: Venues): String {
        return "${venues.categories[0].icon.prefix}bg_64${venues.categories[0].icon.suffix}"
    }
}