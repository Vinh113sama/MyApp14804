package com.example.myapp.repository

import android.app.Application
import com.example.myapp.util.AppViewModelProvider

class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        AppViewModelProvider.application = this
    }
}
