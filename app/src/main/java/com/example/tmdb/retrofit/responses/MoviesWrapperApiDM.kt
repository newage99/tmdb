package com.example.tmdb.retrofit.responses

class MoviesWrapperApiDM(
    val page: Int,
    val total_pages: Int,
    val results: List<MovieWithBitmapDM>
)