package com.example.myapp.repository


import com.example.myapp.process.getsong.ApiService
import com.example.myapp.process.getsong.Song


class SongRepository(private val apiService: ApiService) {

    suspend fun getSongs(keyword: String?, page: Int, limit: Int = 8): List<Song> {
        return apiService.getSongs(keyword, page, limit).data
    }

    suspend fun getFavoriteSongs(page: Int, limit: Int = 10): List<Song> {
        return apiService.getFavoriteSongs(page, limit).data
    }

    suspend fun getHistorySongs(page: Int, limit: Int = 10): List<Song> {
        return apiService.getHistorySongs(page, limit).data.map { it.song }
    }
}
