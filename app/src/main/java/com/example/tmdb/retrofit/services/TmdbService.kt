package com.example.tmdb.retrofit.services

import com.example.tmdb.retrofit.responses.MoviesWrapperApiDM
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Query

interface TmdbService {
    @Headers("Authorization: Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJhdWQiOiI1MzM1ZTMzODg4ZjVmZGFkNTY0ZTRlNDY0NWU5Yjk0NCIsInN1YiI6IjVjODNmOTdiMGUwYTI2NDMwYTYzMGRlMCIsInNjb3BlcyI6WyJhcGlfcmVhZCJdLCJ2ZXJzaW9uIjoxfQ.XJEfCEcXZ3YlibCQhVqhUm5Hh5E5C92EDjvUPDRFgak")
    @GET("/4/discover/movie?sort_by=popularity.desc&language=es")
    fun getMostPopularMovies(@Query("page") page: Int): Call<MoviesWrapperApiDM>
}