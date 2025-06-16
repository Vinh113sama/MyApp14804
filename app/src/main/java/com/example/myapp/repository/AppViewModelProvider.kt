package com.example.myapp.util

import android.app.Application
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStore
import com.example.myapp.repository.MusicPlayerViewModel

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
