package com.example.sgost

import android.app.Application
import com.example.sgost.api.ApiAndroid

class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        ApiAndroid.init(this)
        // CartManager no necesita init porque no usa base de datos
    }
}