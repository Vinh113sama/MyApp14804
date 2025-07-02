package com.example.myapp.process

import android.content.Context
import com.example.myapp.process.getsong.ApiService
import com.example.myapp.process.login.AuthApiService
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    private const val BASE_URL = "http://3.1.202.198:8081/"
    private lateinit var appContext: Context

    fun init(context: Context) {
        appContext = context.applicationContext
    }

    private val authInterceptor = Interceptor { chain ->
        val prefs = appContext.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
        val token = prefs.getString("access_token", null)
        android.util.Log.d("API_CALL", "Token đang gửi: $token")
        val requestBuilder = chain.request().newBuilder()
        if (!token.isNullOrEmpty()) {
            requestBuilder.addHeader("Authorization", "Bearer $token")
        }

        chain.proceed(requestBuilder.build())
    }

    private val okHttpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .build()
    }

    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val apiService: ApiService by lazy {
        retrofit.create(ApiService::class.java)
    }

    val authService: AuthApiService by lazy {
        retrofit.create(AuthApiService::class.java)
    }
}
