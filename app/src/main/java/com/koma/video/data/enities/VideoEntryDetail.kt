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

import android.content.Context
import android.support.annotation.Keep
import android.text.format.Formatter
import java.text.SimpleDateFormat
import java.util.*

@Keep
data class VideoEntryDetail(
    val title: String,
    private val duration: Int,
    private val dateTaken: Long,
    private val size: Long,
    val path: String
) {
    fun formatDateTaken(): String {
        return SimpleDateFormat.getDateInstance().format(Date(dateTaken))
    }

    fun formatSize(context: Context): String {
        return Formatter.formatFileSize(context, size)
    }

    var formatDuration: String = "00:00"
        get() = VideoEntry.stringForTime(duration)

    var format: String = ""
        get() = path.substringAfterLast(".", "mp4")
}