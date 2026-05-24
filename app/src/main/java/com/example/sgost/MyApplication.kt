package com.example.sgost

import android.app.Application
import com.example.sgost.api.ApiAndroid

class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // Inicializa Retrofit
        ApiAndroid.init(this)
    }
}