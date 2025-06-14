package com.example.myapp.process.getsong



data class SongResponse(
    val data: List<Song>,
    val limit: Int,
    val total: Int,
    val totalPages: Int,
    val currentPage: Int
)