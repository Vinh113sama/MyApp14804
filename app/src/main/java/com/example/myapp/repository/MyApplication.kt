package com.example.myapp.repository

import android.app.Application


class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        AppViewModelProvider.application = this
    }
}
