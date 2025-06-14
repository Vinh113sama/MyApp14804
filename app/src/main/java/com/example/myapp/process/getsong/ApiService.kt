package com.example.myapp.process.getsong

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.HTTP
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {
    @GET("api/song")
    suspend fun getSongs(
        @Query("q") keyword: String? = null,
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 10
    ): SongResponse

    @GET("api/song/play/{songId}")
    suspend fun getLink(@Path("songId") songId: Int): SongUrlResponse

    @GET("api/song/history")
    suspend fun getHistorySongs(
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 10
    ) : HistoryResponse

    @GET("api/song/favorite")
    suspend fun getFavoriteSongs(
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 10
    ) : FavoriteResponse

    @POST("api/song/favorite")
    suspend fun postFavoriteSong(@Body request: FavoriteRequest): Response<BaseResponse>

    @HTTP(method = "DELETE", path = "api/song/favorite", hasBody = true)
    suspend fun deleteFavoriteSong(@Body request: FavoriteRequest): Response<BaseResponse>

}