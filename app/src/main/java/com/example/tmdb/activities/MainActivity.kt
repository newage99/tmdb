package com.example.tmdb.activities

import android.app.Activity
import android.graphics.Color
import android.os.AsyncTask
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import com.example.tmdb.R
import com.example.tmdb.misc.MovieCustomAdapter
import com.example.tmdb.retrofit.responses.MoviesWrapperApiDM
import com.example.tmdb.retrofit.services.TmdbService
import kotlinx.android.synthetic.main.activity_main.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.InputStream
import java.lang.Exception
import java.util.*
import kotlin.collections.ArrayList
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.example.tmdb.retrofit.responses.MovieWithBitmapDM

class MainActivity : AppCompatActivity() {

    // VARIABLES
    private lateinit var timestampOfTheLastApiCall: Date
    private lateinit var retrofit: Retrofit
    private lateinit var service: TmdbService
    private val context: Activity = this
    private var moviesList: ArrayList<MovieWithBitmapDM> = ArrayList()
    private lateinit var moviesListAdapter: MovieCustomAdapter
    private var actualPage: Int = 1

    // INTERFACES
    interface DownloadImageTaskResponse {
        fun onResponse(output: Bitmap)
    }

    // ASYNC TASKS
    class DownloadImageTask(var asyncResponse: DownloadImageTaskResponse) : AsyncTask<String, Void, Bitmap>()
    {
        override fun doInBackground(vararg urls: String): Bitmap? {
            val urldisplay = "http://image.tmdb.org/t/p/w92" + urls[0]
            var mIcon11: Bitmap? = null
            try {
                val inputStream: InputStream = java.net.URL(urldisplay).openStream()
                mIcon11 = BitmapFactory.decodeStream(inputStream)
            } catch (e: Exception) {
                e.printStackTrace()
            }
            return mIcon11
        }
        override fun onPostExecute(result: Bitmap) {
            asyncResponse.onResponse(result)
        }
    }

    // PRIVATE METHODS
    fun setPosterImages() {
        try {
            moviesList.forEach {
                val callback = object : DownloadImageTaskResponse {
                    override fun onResponse(output: Bitmap) {
                        it.poster = output
                        moviesListAdapter.notifyDataSetChanged()
                    }
                }
                DownloadImageTask(callback).execute(it.poster_path)
            }
        } catch (e: Exception) {
            val toast = Toast.makeText(this, "setPosterImages: " + e.toString(), Toast.LENGTH_LONG)
            toast.show()
        }
    }
    fun enqueueMostPopularMoviesCall() {
        try {
            timestampOfTheLastApiCall = Date()
            val timestampOfTheActualApiCall = timestampOfTheLastApiCall
            service.getMostPopularMovies(actualPage).enqueue(object: Callback<MoviesWrapperApiDM> {
                override fun onResponse(call: Call<MoviesWrapperApiDM>, response: Response<MoviesWrapperApiDM>) {
                    if (timestampOfTheActualApiCall === timestampOfTheLastApiCall) {
                        val result: MoviesWrapperApiDM? = response.body()
                        if (result != null) {
                            if (actualPage == 1)
                                moviesList.clear()
                            val resultMoviesList: ArrayList<MovieWithBitmapDM> = ArrayList()
                            result.results.forEach {
                                //resultMoviesList.add(it)
                                moviesList.add(it)
                            }
                            //moviesList.addAll(resultMoviesList)
                            moviesListAdapter.notifyDataSetChanged()
                            setPosterImages()
                            moviesListCenterText.visibility = View.GONE
                            mainLayout.setBackgroundColor(Color.WHITE)
                        } else {
                            moviesListCenterText.text = getString(R.string.error_retrieving_movies)
                            moviesListCenterText.visibility = View.VISIBLE
                        }
                        moviesProgressbar.visibility = View.GONE
                    }
                }
                override fun onFailure(call: Call<MoviesWrapperApiDM>, t: Throwable) {
                    if (timestampOfTheActualApiCall === timestampOfTheLastApiCall) {
                        listView.adapter = MovieCustomAdapter(ArrayList(), context)
                        moviesProgressbar.visibility = View.GONE
                        moviesListCenterText.text = getString(R.string.error_retrieving_movies)
                    }
                }
            })
        } catch (e: Exception) {
            val toast = Toast.makeText(this, "enqueueMostPopularMoviesCall: " + e.toString(), Toast.LENGTH_LONG)
            toast.show()
        }
    }

    // OVERRIDES
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        try {
            moviesListAdapter = MovieCustomAdapter(moviesList, this)
            listView.isScrollingCacheEnabled = false
            listView.adapter = moviesListAdapter

            retrofit = Retrofit.Builder().baseUrl("https://api.themoviedb.org/")
                .addConverterFactory(GsonConverterFactory.create()).build()
            service = retrofit.create(TmdbService::class.java)
            enqueueMostPopularMoviesCall()
        } catch (e: Exception) {
            val toast = Toast.makeText(this, "onCreate: " + e.toString(), Toast.LENGTH_LONG)
            toast.show()
        }
    }
}