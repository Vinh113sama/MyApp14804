package com.example.myapp.repository

import android.app.Application
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStore

object AppViewModelProvider {
    lateinit var application: Application
    private val viewModelStore = ViewModelStore()

    fun provideMusicPlayerViewModel(): MusicPlayerViewModel {
        if (!::application.isInitialized) {
            throw IllegalStateException("AppViewModelProvider.application chưa được khởi tạo!")
        }

        return ViewModelProvider(viewModelStore,
            ViewModelProvider.AndroidViewModelFactory.getInstance(application)
        )[MusicPlayerViewModel::class.java]
    }

    fun clear() {
        viewModelStore.clear()
    }
}
