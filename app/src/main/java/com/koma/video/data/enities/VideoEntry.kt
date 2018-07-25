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
package com.koma.video.data.enities

import android.content.ContentUris
import android.net.Uri
import android.provider.MediaStore
import android.support.annotation.Keep
import java.util.*

@Keep
data class VideoEntry constructor(
    val id: Long,
    val displayName: String,
    private val duration: Int
) {
    var uri: Uri? = null
        get() = ContentUris.withAppendedId(
            MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
            id
        )

    var formatDuration: String = "00:00"
        get() = stringForTime(duration)

    companion object {
        fun stringForTime(time: Int): String {
            val formatBuilder = StringBuilder()
            val formatter = Formatter(formatBuilder, Locale.getDefault())
            val totalSeconds = time / 1000

            val seconds = totalSeconds % 60
            val minutes = totalSeconds / 60 % 60
            val hours = totalSeconds / 3600

            formatBuilder.setLength(0)
            return if (hours > 0) {
                formatter.format("%d:%02d:%02d", hours, minutes, seconds).toString()
            } else {
                formatter.format("%02d:%02d", minutes, seconds).toString()
            }
        }
    }
}