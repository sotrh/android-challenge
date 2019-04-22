package com.owletcare.androidtest

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.LruCache
import kotlinx.coroutines.*
import java.net.URL

class BitmapCache(val cacheSize: Int): CoroutineScope {
    private val job = Job()
    override val coroutineContext = Dispatchers.IO + job

    private val memoryCache: LruCache<String, Bitmap>
    private val activeBitmapRequests = mutableMapOf<String, Deferred<Bitmap>>()

    init {
        memoryCache = object : LruCache<String, Bitmap>(cacheSize) {
            override fun sizeOf(key: String?, value: Bitmap?): Int {
                return (value?.byteCount ?: 0) / 1024
            }
        }
    }

    fun cancelAllBitmapRequests() {
        job.cancel()
        activeBitmapRequests.clear()
    }

    fun getBitmapAsync(key: String): Deferred<Bitmap> {
        synchronized(activeBitmapRequests) {
            return activeBitmapRequests[key] ?: async {
                memoryCache[key].let { cachedBitmap ->
                    if (cachedBitmap != null) cachedBitmap
                    else {
                        val url = URL(key)
                        val connection = url.openConnection().apply {
                            doInput = true
                            connect()
                        }
                        val input = connection.getInputStream()

                        BitmapFactory.decodeStream(input).also {
                            memoryCache.put(key, it)
                        }
                    }
                }
            }.also {
                activeBitmapRequests[key] = it
            }
        }
    }
}