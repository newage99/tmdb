package com.example.tmdb.misc

import android.app.Activity
import android.graphics.Bitmap
import android.support.v7.widget.RecyclerView
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import android.view.LayoutInflater
import android.widget.LinearLayout
import com.example.tmdb.R
import com.example.tmdb.retrofit.responses.MovieWithBitmapDM
import kotlinx.android.synthetic.main.list_item.view.*
import java.lang.Exception

fun bitmapIsNotEmpty(bm: Bitmap?): Boolean {
    return bm != null && bm.width > 1 && bm.height > 1
}

class ViewHolder(view: View): RecyclerView.ViewHolder(view) {
    var image =         view.findViewById(R.id.movieImage)       as ImageView
    var year =          view.findViewById(R.id.movieYear)        as TextView
    var title =         view.findViewById(R.id.movieTitle)       as TextView
    var description =   view.findViewById(R.id.movieDescription) as TextView
}

class MovieCustomAdapter(val data: ArrayList<MovieWithBitmapDM>)
    : RecyclerView.Adapter<ViewHolder>()
{
    //private val layoutInflater = LayoutInflater.from(context)
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.list_item, parent, false) as LinearLayout)
    }
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.year.text = "(" + data[position].release_date.substring(0, 4) + ")"
        holder.title.text = data[position].title
        holder.description.text = data[position].overview
        if (bitmapIsNotEmpty(data[position].poster))
            holder.image.setImageBitmap(data[position].poster)
    }
    override fun getItemCount() = data.size
}