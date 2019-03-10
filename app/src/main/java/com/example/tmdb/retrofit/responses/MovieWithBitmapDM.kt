package com.example.tmdb.retrofit.responses

import android.graphics.Bitmap

class MovieWithBitmapDM(
    val title: String,
    val release_date: String,
    val overview: String,
    val poster_path: String?,
    var poster: Bitmap
)