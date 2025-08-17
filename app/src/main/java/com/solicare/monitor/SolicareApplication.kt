package com.solicare.monitor

import android.app.Application
import com.google.firebase.FirebaseApp

class SolicareApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)
    }
}
