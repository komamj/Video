/*
 * Copyright 2018 koma_mj
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.koma.video.util

import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import android.text.TextUtils
import com.bumptech.glide.Priority
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.data.DataFetcher
import com.koma.video.data.enities.BucketEntry
import java.io.File
import java.io.FileNotFoundException
import java.io.IOException
import java.io.InputStream

class VideoThumbFetcher(private val context: Context, private val bucketEntry: BucketEntry) :
    DataFetcher<InputStream> {
    private var inputStream: InputStream? = null

    override fun getDataClass(): Class<InputStream> = InputStream::class.java

    override fun cleanup() {
        try {
            inputStream?.close()
        } catch (e: IOException) {
            // Ignored.
        }
    }

    override fun getDataSource(): DataSource {
        return DataSource.LOCAL
    }

    override fun cancel() {
        // Do nothing.
    }

    override fun loadData(priority: Priority, callback: DataFetcher.DataCallback<in InputStream>) {
        val projection = arrayOf(MediaStore.Video.Media._ID)
        val selection = MediaStore.Video.Media.BUCKET_ID + " = " + bucketEntry.bucketId
        val sortOrder = MediaStore.Video.Media.DATE_TAKEN + " DESC"
        val resolver = context.applicationContext.contentResolver
        val cursor = resolver.query(
            MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
            projection,
            selection,
            null,
            sortOrder
        )
        val thumbSelection = MediaStore.Video.Thumbnails.KIND + " = " +
                MediaStore.Video.Thumbnails.MINI_KIND + " AND " +
                MediaStore.Video.Thumbnails.VIDEO_ID + " = ?"
        cursor?.use {
            cursor.moveToFirst()
            do {
                val id = cursor.getInt(0)
                val thumbCursor = resolver.query(
                    MediaStore.Video.Thumbnails.EXTERNAL_CONTENT_URI,
                    arrayOf(MediaStore.Video.Thumbnails.DATA),
                    thumbSelection,
                    arrayOf(id.toString()),
                    null,
                    null
                )
                thumbCursor.use {
                    val path = it.getString(0)
                    if (TextUtils.isEmpty(path)) {
                        inputStream = null
                        callback.onLoadFailed(FileNotFoundException())
                        return
                    }
                    val file = File(path)
                    if (!isValid(file)) {
                        inputStream = null
                        callback.onLoadFailed(FileNotFoundException())
                        return
                    }
                    val thumbnailUri = Uri.fromFile(file)
                    inputStream = resolver.openInputStream(thumbnailUri)
                    callback.onDataReady(inputStream)
                }
                break
            } while (cursor.moveToNext())
        }
    }

    private fun isValid(file: File): Boolean {
        return file.exists() && 0 < file.length()
    }

    companion object {
        private const val TAG = "VideoThumbFetcher"
    }
}