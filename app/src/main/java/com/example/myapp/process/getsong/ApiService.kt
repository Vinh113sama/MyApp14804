package com.example.myapp.process.getsong

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.HTTP
import retrofit2.http.POST
import retrofit2.http.Path

interface ApiService {
    @GET("api/song")
    suspend fun getSongs(): SongResponse

    @GET("api/song/play/{songId}")
    suspend fun getLink(@Path("songId") songId: Int): SongUrlResponse

    @GET("api/song/history")
    suspend fun getHistorySongs() : HistoryResponse

    @GET("api/song/favorite")
    suspend fun getFavoriteSongs() : FavoriteResponse

    @POST("api/song/favorite")
    suspend fun postFavoriteSong(@Body request: FavoriteRequest): Response<BaseResponse>

    @HTTP(method = "DELETE", path = "api/song/favorite", hasBody = true)
    suspend fun deleteFavoriteSong(@Body request: FavoriteRequest): Response<BaseResponse>

}