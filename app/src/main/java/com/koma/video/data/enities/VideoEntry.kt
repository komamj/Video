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
import java.util.*

data class VideoEntry constructor(val id: Long, val displayName: String, val duration: Int) {
    var uri: Uri? = null
        get() = ContentUris.withAppendedId(
            MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
            id
        )

    var formatDuration: String = "00:00"
        get() = formatDuration(duration)

    private fun formatDuration(duration: Int): String {
        val ss = duration / 1000 % 60
        val mm = duration / 60000 % 60
        val hh = duration / 3600000
        return if (duration < 60 * 60 * 1000) {
            String.format(Locale.getDefault(), "%02d:%02d", mm, ss)
        } else {
            String.format(Locale.getDefault(), "%02d:%02d:%02d", hh, mm, ss)
        }
    }
}