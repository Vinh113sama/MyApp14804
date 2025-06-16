package com.example.myapp.process.getsong

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.HTTP
import retrofit2.http.POST
import retrofit2.http.PUT
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

    @POST("api/playlist")
    suspend fun createPlaylist(@Body request: CreatePlaylistRequest): PlaylistResponse

    @PUT("api/playlist/{playlistId}")
    suspend fun updatePlaylist(
        @Path("playlistId") playlistId: Int,
        @Body request: UpdatePlaylistRequest
    ): PlaylistResponse

    @DELETE("api/playlist/{playlistId}")
    suspend fun deletePlaylist(@Path("playlistId") playlistId: Int): PlaylistResponse

    @POST("api/playlist/{playlistId}")
    suspend fun addSongToPlaylist(
        @Path("playlistId") playlistId: Int,
        @Body request: AddSongToPlaylistRequest
    ): AddSongResponse

    @HTTP(method = "DELETE", path = "api/playlist/delete/{playlistId}", hasBody = true)
    suspend fun removeSongFromPlaylist(
        @Path("playlistId") playlistId: Int,
        @Body request: RemoveSongFromPlaylistRequest
    ): RemoveSongResponse

    @GET("api/playlist/{id}")
    suspend fun getUserPlaylists(@Path("id") userId: Int): PlaylistListResponse

    @GET("api/playlist/songs/{playlistId}")
    suspend fun getPlaylistSongs(
        @Path("playlistId") playlistId: Int,
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 10
    ): PlaylistSongsResponse
}