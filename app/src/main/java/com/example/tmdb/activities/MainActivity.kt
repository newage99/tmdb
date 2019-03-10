package com.example.tmdb.activities

import android.app.Activity
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.os.AsyncTask
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import com.example.tmdb.R
import com.example.tmdb.misc.MovieCustomAdapter
import com.example.tmdb.retrofit.responses.MovieApiDM
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
import java.net.URL
import java.util.*
import kotlin.collections.ArrayList

class MainActivity : AppCompatActivity() {

    // VARIABLES
    private lateinit var timestampOfTheLastApiCall: Date
    private lateinit var retrofit: Retrofit
    private lateinit var service: TmdbService
    private val context: Activity = this
    private var moviesList: ArrayList<MovieApiDM> = ArrayList()
    private lateinit var moviesListAdapter: MovieCustomAdapter

    // ASYNC TASKS
    class GetHttpImageTask(private var imageView: ImageView) : AsyncTask<String, Void, Drawable>() {
        override fun doInBackground(vararg args: String): Drawable? {
            try {
                val src = "http://image.tmdb.org/t/p/w92" + args[0]
                val inputStream = URL(src).getContent() as InputStream
                return Drawable.createFromStream(inputStream, src)
            } catch (e: Exception) {
                var a = e
            }
            return null
        }
        override fun onPostExecute(result: Drawable?) {
            super.onPostExecute(result)
            var a = imageView
            imageView.setImageDrawable(result)
            var b = 0
        }
    }

    // PRIVATE METHODS
    fun setPosterImages() {
        try {
            var rowView: View
            for (i in 0 until listView.count-1) {
                rowView = listView.adapter.getView(i, null, listView)
                val imgView: ImageView = rowView.findViewById(R.id.movieImage)
                GetHttpImageTask(imgView).execute(imgView.tag as String)
            }
        } catch (e: Exception) {
            val toast = Toast.makeText(this, "setPosterImages: " + e.toString(), Toast.LENGTH_LONG)
            toast.show()
        }
    }
    fun enqueueMostPopularMoviesCall(page: Int = 1) {
        try {
            timestampOfTheLastApiCall = Date()
            val timestampOfTheActualApiCall = timestampOfTheLastApiCall
            service.getMostPopularMovies(page).enqueue(object: Callback<MoviesWrapperApiDM> {
                override fun onResponse(call: Call<MoviesWrapperApiDM>, response: Response<MoviesWrapperApiDM>) {
                    if (timestampOfTheActualApiCall === timestampOfTheLastApiCall) {
                        val result: MoviesWrapperApiDM? = response.body()
                        if (result != null) {
                            val resultMoviesList: ArrayList<MovieApiDM> = ArrayList()
                            result.results.forEach {
                                resultMoviesList.add(it)
                            }
                            moviesList.clear()
                            moviesList.addAll(resultMoviesList)
                            moviesListAdapter.notifyDataSetChanged()
                            //setPosterImages()
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