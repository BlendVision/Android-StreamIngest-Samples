package com.blendvision.stream.ingest.sample.base

import android.app.Application
import android.net.http.HttpResponseCache
import android.util.Log
import java.io.File
import java.io.IOException

class App : Application() {

    override fun onCreate() {
        super.onCreate()

        initHttpCacheSize()
    }

    private fun initHttpCacheSize() {
        try {
            val httpCacheDir = File(cacheDir, "http")
            val httpCacheSize = (50 * 1024 * 1024).toLong() // 50 MB
            HttpResponseCache.install(httpCacheDir, httpCacheSize)
            Log.i(TAG, "HTTP response cache installation done")
        } catch (e: IOException) {
            Log.w(TAG, "HTTP response cache installation failed:$e")
        }
    }

    companion object {
        private val TAG = this::class.java.simpleName
    }

}