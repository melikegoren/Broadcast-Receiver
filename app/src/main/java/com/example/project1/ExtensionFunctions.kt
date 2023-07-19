package com.example.project1

import android.content.Context
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat



    fun Context.has(permission: String ): Boolean =
        ActivityCompat.checkSelfPermission(this, permission) ==
                PackageManager.PERMISSION_GRANTED

