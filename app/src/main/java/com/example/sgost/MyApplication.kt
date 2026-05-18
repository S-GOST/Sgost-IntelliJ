package com.example.sgost

import android.app.Application
import com.example.sgost.api.ApiClient

class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // Inicializa Retrofit
        ApiClient.init(this)
    }
}