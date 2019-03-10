package com.example.tmdb.misc

import android.app.Activity
import android.graphics.Bitmap
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import android.view.LayoutInflater
import com.example.tmdb.R
import com.example.tmdb.retrofit.responses.MovieWithBitmapDM
import java.lang.Exception

fun bitmapIsNotEmpty(bm: Bitmap?): Boolean {
    return bm != null && bm.width > 1 && bm.height > 1
}

class MovieCustomAdapter(val data: ArrayList<MovieWithBitmapDM>, context: Activity)
    : ArrayAdapter<MovieWithBitmapDM>(context, R.layout.list_item, data)
{
    private val layoutInflater = LayoutInflater.from(context)

    override fun getView(position: Int, view: View?, parent: ViewGroup?): View? {
        var rowView: View? = null
        try {
            val viewHolder: ViewHolder
            if (view == null) {
                rowView = layoutInflater.inflate(R.layout.list_item, parent, false)
                viewHolder = ViewHolder(rowView)
                rowView.tag = viewHolder
            } else {
                rowView = view
                viewHolder = rowView.tag as ViewHolder
            }
            viewHolder.year.text = "(" + data[position].release_date.substring(0, 4) + ")"
            viewHolder.title.text = data[position].title
            viewHolder.description.text = data[position].overview
            if (bitmapIsNotEmpty(data[position].poster))
                viewHolder.image.setImageBitmap(data[position].poster)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return rowView
    }
    private class ViewHolder(view: View?) {
        val image =         view?.findViewById(R.id.movieImage)       as ImageView
        val year =          view?.findViewById(R.id.movieYear)        as TextView
        val title =         view?.findViewById(R.id.movieTitle)       as TextView
        val description =   view?.findViewById(R.id.movieDescription) as TextView
    }
}