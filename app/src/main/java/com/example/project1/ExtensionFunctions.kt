package com.example.project1

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat

class ExtensionFunctions {

    fun Context.has(context: Context, permission: String ): Boolean =
        ActivityCompat.checkSelfPermission(context, permission) ==
                PackageManager.PERMISSION_GRANTED

}