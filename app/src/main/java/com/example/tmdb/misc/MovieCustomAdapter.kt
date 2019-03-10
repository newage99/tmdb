package com.example.tmdb.misc

import android.app.Activity
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import com.example.tmdb.retrofit.responses.MovieApiDM
import android.view.LayoutInflater
import com.example.tmdb.R

class MovieCustomAdapter(val data: ArrayList<MovieApiDM>, context: Activity)
    : ArrayAdapter<MovieApiDM>(context, R.layout.list_item, data)
{
    private val layoutInflater = LayoutInflater.from(context)

    override fun getView(position: Int, view: View?, parent: ViewGroup?): View? {
        val viewHolder: ViewHolder
        val rowView: View?
        if (view == null) {
            rowView = layoutInflater.inflate(R.layout.list_item, parent, false)
            viewHolder = ViewHolder(rowView)
            rowView.tag = viewHolder
        } else {
            rowView = view
            viewHolder = rowView.tag as ViewHolder
        }
        viewHolder.image.tag = data[position].poster_path
        viewHolder.year.text = "(" + data[position].release_date.substring(0, 4) + ")"
        viewHolder.title.text = data[position].title
        viewHolder.description.text = data[position].overview
        return rowView
    }
    private class ViewHolder(view: View?) {
        val image =         view?.findViewById(R.id.movieImage)       as ImageView
        val year =          view?.findViewById(R.id.movieYear)        as TextView
        val title =         view?.findViewById(R.id.movieTitle)       as TextView
        val description =   view?.findViewById(R.id.movieDescription) as TextView
    }
}