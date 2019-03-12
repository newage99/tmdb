package com.example.tmdb.activities

import android.os.AsyncTask
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
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
import android.support.v4.content.ContextCompat
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.text.Editable
import android.text.TextWatcher
import com.example.tmdb.retrofit.responses.MovieWithBitmapDM

const val NUMBER_OF_IMAGES_THAT_THE_API_GIVES_US: Int = 20
const val NUMBER_OF_IMAGES_TO_DOWNLOAD_ASYNCRONOUSLY: Int = 10

class MainActivity : AppCompatActivity() {

    // VARIABLES
    private lateinit var timestampOfTheLastApiCall: Date
    private lateinit var retrofit: Retrofit
    private lateinit var service: TmdbService
    private var moviesList: ArrayList<MovieWithBitmapDM> = ArrayList()
    private var actualPage: Int = 1
    private lateinit var viewAdapter: RecyclerView.Adapter<*>
    private lateinit var viewManager: LinearLayoutManager
    private var searchText: String = ""

    // INTERFACES
    interface DownloadImageTaskResponse {
        fun onResponse(moviesListIndex: Int, output: MutableMap<Int, Bitmap>)
    }

    // ASYNC TASKS
    class DownloadImageTask(val moviesListIndex: Int, var asyncResponse: DownloadImageTaskResponse) : AsyncTask<MutableMap<Int, String>, Void, Pair<Int, MutableMap<Int, Bitmap>>>()
    {
        override fun doInBackground(vararg urls: MutableMap<Int, String>): Pair<Int, MutableMap<Int, Bitmap>> {
            //val imagesArray: ArrayList<Bitmap> = ArrayList()
            val imagesMap: MutableMap<Int, Bitmap> = mutableMapOf()
            urls[0].forEach {
                try {
                    imagesMap[it.key] = BitmapFactory.decodeStream(java.net.URL
                        ("http://image.tmdb.org/t/p/w92" + it.value).openStream())
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            return Pair(moviesListIndex, imagesMap)
        }
        override fun onPostExecute(result: Pair<Int, MutableMap<Int, Bitmap>>) {
            asyncResponse.onResponse(result.first, result.second)
        }
    }

    // PRIVATE METHODS
    fun setPosterImages(timestampOfTheActualApiCall: Date) {
        try {
            loop@ for(i: Int in moviesList.size-NUMBER_OF_IMAGES_THAT_THE_API_GIVES_US
                    until moviesList.size-1 step NUMBER_OF_IMAGES_TO_DOWNLOAD_ASYNCRONOUSLY) {
                if (timestampOfTheActualApiCall != timestampOfTheLastApiCall) {
                    break@loop
                }
                val paths: MutableMap<Int, String> = mutableMapOf()
                //val paths: ArrayList<String> = ArrayList()
                for(j: Int in 0 until NUMBER_OF_IMAGES_TO_DOWNLOAD_ASYNCRONOUSLY) {
                    if (i+j < moviesList.size) {
                        val path: String? = moviesList[i+j].poster_path
                        if (path != null && !path.isEmpty())
                            paths[i+j] = path
                    }
                }
                val callback = object : DownloadImageTaskResponse {
                    override fun onResponse(moviesListIndex: Int, output: MutableMap<Int, Bitmap>) {
                        if (timestampOfTheActualApiCall === timestampOfTheLastApiCall) {
                            output.forEach {
                                moviesList[it.key].poster = it.value
                            }
                            viewAdapter.notifyItemRangeChanged(moviesListIndex, NUMBER_OF_IMAGES_TO_DOWNLOAD_ASYNCRONOUSLY)
                        }
                    }
                }
                DownloadImageTask(i, callback).execute(paths)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    fun enqueueMostPopularMoviesCall() {
        // TODO: Refactor along with 'enqueueSeachMovieCall' function
        try {
            moviesProgressbar.visibility = View.VISIBLE
            moviesListGreyFilter.visibility = View.VISIBLE
            timestampOfTheLastApiCall = Date()
            val timestampOfTheActualApiCall = timestampOfTheLastApiCall
            service.getMostPopularMovies(actualPage).enqueue(object: Callback<MoviesWrapperApiDM> {
                override fun onResponse(call: Call<MoviesWrapperApiDM>, response: Response<MoviesWrapperApiDM>) {
                    if (timestampOfTheActualApiCall === timestampOfTheLastApiCall) {
                        val result: MoviesWrapperApiDM? = response.body()
                        if (result == null || result.total_results == 0 || result.results.size == 0) {
                            moviesList.clear()
                            moviesListCenterText.visibility = View.VISIBLE
                            if (result == null)
                                moviesListCenterText.text = getString(R.string.error_retrieving_movies)
                            else
                                moviesListCenterText.text = getString(R.string.no_movies_founded)
                        } else {
                            if (actualPage == 1)
                                moviesList.clear()
                            moviesList.addAll(result.results)
                            setPosterImages(timestampOfTheActualApiCall)
                            moviesListCenterText.visibility = View.GONE
                            moviesListGreyFilter.visibility = View.GONE
                        }
                        viewAdapter.notifyDataSetChanged()
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
            e.printStackTrace()
        }
    }
    fun enqueueSeachMovieCall() {
        // TODO: Refactor along with 'enqueueMostPopularMoviesCall' function
        try {
            moviesProgressbar.visibility = View.VISIBLE
            moviesListGreyFilter.visibility = View.VISIBLE
            timestampOfTheLastApiCall = Date()
            val timestampOfTheActualApiCall = timestampOfTheLastApiCall
            service.searchMovie(searchText, actualPage).enqueue(object: Callback<MoviesWrapperApiDM> {
                override fun onResponse(call: Call<MoviesWrapperApiDM>, response: Response<MoviesWrapperApiDM>) {
                    if (timestampOfTheActualApiCall === timestampOfTheLastApiCall) {
                        val result: MoviesWrapperApiDM? = response.body()
                        moviesList.clear()
                        if (result == null) {
                            moviesListCenterText.text = getString(R.string.error_retrieving_movies)
                            moviesListCenterText.visibility = View.VISIBLE
                        } else if (result.total_results == 0 || result.results.size == 0) {
                            moviesListCenterText.text = getString(R.string.no_movies_founded)
                            moviesListCenterText.visibility = View.VISIBLE
                        } else {
                            moviesList.addAll(result.results)
                            setPosterImages(timestampOfTheActualApiCall)
                            moviesListCenterText.visibility = View.GONE
                            moviesListGreyFilter.visibility = View.GONE
                        }
                        viewAdapter.notifyDataSetChanged()
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
            e.printStackTrace()
        }
    }
    fun processPaginationButtonClick() {
        pageTextView.text = actualPage.toString()
        if (actualPage == 1) {
            backBackButtonWrapper.background = ContextCompat.getDrawable(this, R.drawable.pagination_disabled_button_background)
            backButtonWrapper.background = ContextCompat.getDrawable(this, R.drawable.pagination_disabled_button_background)
        } else {
            backBackButtonWrapper.background = ContextCompat.getDrawable(this, R.drawable.pagination_button_background)
            backButtonWrapper.background = ContextCompat.getDrawable(this, R.drawable.pagination_button_background)
        }
        enqueueSeachMovieCall()
    }
    fun processBackBackButtonClick() {
        if (searchText.length > 0 && actualPage > 1) {
            if (actualPage >= 11)
                actualPage -= 10
            else
                actualPage = 1
            processPaginationButtonClick()
        }
    }
    fun processBackButtonClick() {
        if (searchText.length > 0 && actualPage > 1) {
            actualPage -= 1
            processPaginationButtonClick()
        }
    }
    fun processNextButtonClick() {
        if (searchText.length > 0) {
            actualPage += 1
            processPaginationButtonClick()
        }
        // TODO: Consider 'total_pages' parameter
    }
    fun processNextNextButtonClick() {
        if (searchText.length > 0) {
            actualPage += 10
            processPaginationButtonClick()
        }
        // TODO: Consider 'total_pages' parameter
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
                    if (searchText.length == 0 && viewManager.findLastCompletelyVisibleItemPosition() > viewManager.itemCount - 2) {
                        actualPage += 1
                        enqueueMostPopularMoviesCall()
                    }
                }
            }
            recyclerView.addOnScrollListener(scrollListener)
            searchMoviesEditTextWrapper.bringToFront()
            searchMoviesEditText.addTextChangedListener(object : TextWatcher {
                override fun afterTextChanged(p0: Editable?) {}
                override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
                override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                    try {
                        if (p0 != null && p0.length >= 0) {
                            actualPage = 1
                            searchText = p0.toString()
                            moviesList.clear()
                            if (p0.length > 0) {
                                paginationWrapper.visibility = View.VISIBLE
                                recyclerViewBottom.visibility = View.VISIBLE
                                processPaginationButtonClick()
                            } else {
                                paginationWrapper.visibility = View.GONE
                                recyclerViewBottom.visibility = View.GONE
                                enqueueMostPopularMoviesCall()
                            }
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            })
            backBackButton.setOnClickListener { processBackBackButtonClick() }
            backButton.setOnClickListener { processBackButtonClick() }
            nextButton.setOnClickListener { processNextButtonClick() }
            nextNextButton.setOnClickListener { processNextNextButtonClick() }

            retrofit = Retrofit.Builder().baseUrl("https://api.themoviedb.org/")
                .addConverterFactory(GsonConverterFactory.create()).build()
            service = retrofit.create(TmdbService::class.java)
            enqueueMostPopularMoviesCall()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}