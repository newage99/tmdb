package com.example.tmdb.activities

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
import java.lang.Exception
import java.util.*
import kotlin.collections.ArrayList
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import com.example.tmdb.retrofit.responses.MovieWithBitmapDM

const val NUMBER_OF_IMAGES_THAT_THE_API_GIVES_US: Int = 20
const val NUMBER_OF_IMAGES_TO_DOWNLOAD_ASYNCRONOUSLY: Int = 20

class MainActivity : AppCompatActivity() {

    // VARIABLES
    private lateinit var timestampOfTheLastApiCall: Date
    private lateinit var retrofit: Retrofit
    private lateinit var service: TmdbService
    private var moviesList: ArrayList<MovieWithBitmapDM> = ArrayList()
    private var actualPage: Int = 1
    private lateinit var viewAdapter: RecyclerView.Adapter<*>
    private lateinit var viewManager: LinearLayoutManager

    // INTERFACES
    interface DownloadImageTaskResponse {
        fun onResponse(moviesListIndex: Int, output: ArrayList<Bitmap>)
    }

    // ASYNC TASKS
    class DownloadImageTask(val moviesListIndex: Int, var asyncResponse: DownloadImageTaskResponse) : AsyncTask<ArrayList<String>, Void, Pair<Int, ArrayList<Bitmap>>>()
    {
        override fun doInBackground(vararg urls: ArrayList<String>): Pair<Int, ArrayList<Bitmap>> {
            val imagesArray: ArrayList<Bitmap> = ArrayList()
            try {
                for (i in 0 until NUMBER_OF_IMAGES_TO_DOWNLOAD_ASYNCRONOUSLY) {
                    imagesArray.add(BitmapFactory.decodeStream(java.net.URL
                        ("http://image.tmdb.org/t/p/w92" + urls[0][i]).openStream()))
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            return Pair(moviesListIndex, imagesArray)
        }
        override fun onPostExecute(result: Pair<Int, ArrayList<Bitmap>>) {
            asyncResponse.onResponse(result.first, result.second)
        }
    }

    // PRIVATE METHODS
    fun setPosterImages() {
        try {
            for(i: Int in moviesList.size-NUMBER_OF_IMAGES_THAT_THE_API_GIVES_US
                    until moviesList.size-1 step NUMBER_OF_IMAGES_TO_DOWNLOAD_ASYNCRONOUSLY) {
                val paths: ArrayList<String> = ArrayList()
                for(j: Int in 0 until NUMBER_OF_IMAGES_TO_DOWNLOAD_ASYNCRONOUSLY) {
                    if (i+j < moviesList.size) {
                        paths.add(moviesList[i+j].poster_path)
                    }
                }
                val callback = object : DownloadImageTaskResponse {
                    override fun onResponse(moviesListIndex: Int, output: ArrayList<Bitmap>) {
                        for(z in 0 until NUMBER_OF_IMAGES_TO_DOWNLOAD_ASYNCRONOUSLY) {
                            moviesList[moviesListIndex+z].poster = output[z]
                        }
                        viewAdapter.notifyDataSetChanged()
                    }
                }
                DownloadImageTask(i, callback).execute(paths)
            }
        } catch (e: Exception) {
            val toast = Toast.makeText(this, "setPosterImages 1: " + e.toString(), Toast.LENGTH_LONG)
            toast.show()
        }
    }
    fun enqueueMostPopularMoviesCall() {
        try {
            moviesProgressbar.visibility = View.VISIBLE
            moviesListGreyFilter.visibility = View.VISIBLE
            timestampOfTheLastApiCall = Date()
            val timestampOfTheActualApiCall = timestampOfTheLastApiCall
            service.getMostPopularMovies(actualPage).enqueue(object: Callback<MoviesWrapperApiDM> {
                override fun onResponse(call: Call<MoviesWrapperApiDM>, response: Response<MoviesWrapperApiDM>) {
                    if (timestampOfTheActualApiCall === timestampOfTheLastApiCall) {
                        val result: MoviesWrapperApiDM? = response.body()
                        if (result != null) {
                            if (actualPage == 1)
                                moviesList.clear()
                            moviesList.addAll(result.results)
                            viewAdapter.notifyDataSetChanged()
                            setPosterImages()
                            moviesListCenterText.visibility = View.GONE
                            //mainLayout.setBackgroundColor(Color.WHITE)
                            moviesListGreyFilter.visibility = View.GONE
                        } else {
                            moviesListCenterText.text = getString(R.string.error_retrieving_movies)
                            moviesListCenterText.visibility = View.VISIBLE
                        }
                        moviesProgressbar.visibility = View.GONE
                    }
                }
                override fun onFailure(call: Call<MoviesWrapperApiDM>, t: Throwable) {
                    if (timestampOfTheActualApiCall === timestampOfTheLastApiCall) {
                        moviesList.clear()
                        viewAdapter.notifyDataSetChanged()
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
            //moviesListAdapter = MovieCustomAdapter(moviesList)
            viewManager = LinearLayoutManager(this)
            viewAdapter = MovieCustomAdapter(moviesList)
            recyclerView.apply {
                layoutManager = viewManager
                adapter = viewAdapter
            }
            val scrollListener = object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    if (viewManager.findLastCompletelyVisibleItemPosition() > viewManager.itemCount - 2) {
                        actualPage += 1
                        enqueueMostPopularMoviesCall()
                    }
                }
            }
            recyclerView.addOnScrollListener(scrollListener)
            searchMoviesEditTextWrapper.bringToFront()
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