package com.example.myapp.process.getsong

import com.example.myapp.process.getplaylist.IdSong
import com.example.myapp.process.getplaylist.NamePlaylistRequest
import com.example.myapp.process.getplaylist.PlaylistAllResponse
import com.example.myapp.process.getplaylist.PlaylistResponse
import com.example.myapp.process.getplaylist.PlaylistSongsResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.HTTP
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {
    @GET("api/songs")
    suspend fun getSongs(
        @Query("q") keyword: String? = null,
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 10
    ): SongResponse

    @GET("api/songs/genres/{genreName}/top-listens")
    suspend fun getSongsByGenre(
        @Path("genreName") genreName: String,
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 10
    ): TopSongListResponse

    @GET("api/songs/history")
    suspend fun getHistorySongs(
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 10
    ): HistoryResponse

    @GET("api/songs/favorites")
    suspend fun getFavoriteSongs(
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 10
    ): FavoriteResponse

    @POST("api/songs/favorites")
    suspend fun postFavoriteSong(@Body request: FavoriteRequest): Response<PlaylistAllResponse>

    @HTTP(method = "DELETE", path = "api/songs/favorites/{songId}", hasBody = false)
    suspend fun deleteFavoriteSong(@Path("songId") songId: Int): Response<PlaylistAllResponse>

    @GET("api/users")
    suspend fun getUserInfor(): UserInfor

    @POST("api/playlists")
    suspend fun createPlaylist(@Body request: NamePlaylistRequest): Response<PlaylistAllResponse>

    @PUT("api/playlists/{playlistId}")
    suspend fun updatePlaylist(
        @Path("playlistId") playlistId: Int,
        @Body request: NamePlaylistRequest
    ): Response<PlaylistAllResponse>

    @HTTP(method = "DELETE", path = "api/playlists/{playlistId}", hasBody = false)
    suspend fun deletePlaylist(@Path("playlistId") playlistId: Int): Response<PlaylistAllResponse>

    @POST("api/playlists/{playlistId}/songs")
    suspend fun addSongToPlaylist(
        @Path("playlistId") playlistId: Int,
        @Body request: IdSong
    ): Response<PlaylistAllResponse>

    @HTTP(method = "DELETE", path = "api/playlists/{playlistId}/songs/{songId}", hasBody = false)
    suspend fun removeSongFromPlaylist(
        @Path("playlistId") playlistId: Int,
        @Path("songId") songId: Int
    ): Response<PlaylistAllResponse>

    @GET("api/playlists")
    suspend fun getPlaylists(): Response<PlaylistResponse>

    @POST("api/songs/{songId}/play")
    suspend fun postPlaySong(@Path("songId") songId: Int)

    @GET("api/playlists/{playlistId}/songs")
    suspend fun getPlaylistSongs(
        @Path("playlistId") playlistId: Int,
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 10
    ): PlaylistSongsResponse

//    @GET("api/songs/top-listens")
//    suspend fun fetchTopSongs(
//        @Query("page") page: Int = 1,
//        @Query("limit") limit: Int = 10
//    ): TopSongListResponse

    @POST("api/auth/logout")
    suspend fun logout()
}