package com.example.service.utils

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import java.util.Arrays


object PermissionUtils {
    fun havePermission(context: Context): Boolean {
        for (permission in getNeedPermissions()) {
            if (context.checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED) {
                return false
            }

        }
        return true
    }

    private fun getNeedPermissions(): Array<String> {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return arrayOf(
                Manifest.permission.READ_MEDIA_AUDIO,
                Manifest.permission.POST_NOTIFICATIONS,
            )
        } else {

            return arrayOf(
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
        }

    }


    fun requestPermission(activity: Activity) {
        if (!havePermission(activity)) {
            val allPerms: ArrayList<Array<String>> = ArrayList(listOf(getNeedPermissions()))

            activity.requestPermissions(allPerms.flatMap { it.asList() }.toTypedArray(), 0)
        }
    }
}