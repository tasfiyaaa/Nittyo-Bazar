package com.example.data

import android.content.Context
import android.util.Log
import com.google.firebase.FirebaseApp

object FirebaseConfig {
    private const val TAG = "FirebaseConfig"

    fun isFirebaseAvailable(context: Context): Boolean {
        return try {
            val apps = FirebaseApp.getApps(context)
            if (apps.isNotEmpty()) {
                true
            } else {
                val app = FirebaseApp.initializeApp(context)
                app != null
            }
        } catch (e: Exception) {
            Log.w(TAG, "Firebase is not initialized or google-services.json is missing: ${e.message}. Falling back to Demo Mode.")
            false
        }
    }
}
