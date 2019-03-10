package com.example.tmdb.misc

import android.graphics.drawable.Drawable
import android.os.AsyncTask
import java.io.InputStream
import java.net.URL

internal class GetHttpImageTask : AsyncTask<String, Void, Drawable>() {
    override fun doInBackground(vararg urls: String): Drawable? {
        try {
            val src = "http://image.tmdb.org/t/p/w92" + urls[0]
            val inputStream = URL(src).getContent() as InputStream
            return Drawable.createFromStream(inputStream, src)
        } catch (e: Exception) {
            return null
        }
    }
}