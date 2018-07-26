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
package com.koma.bugly.util

import android.util.Log
import com.koma.bugly.BuildConfig

class LogUtils {
    companion object {
        private const val TAG = "Bugly"

        private fun buildString(tag: String, msg: String): String {
            val stringBuilder = StringBuilder()
            stringBuilder.append(tag)
            stringBuilder.append("----")
            stringBuilder.append(msg)
            return stringBuilder.toString()
        }

        fun v(tag: String, msg: String) {
            if (BuildConfig.DEBUG) {
                Log.v(TAG, buildString(tag, msg))
            }
        }

        fun d(tag: String, msg: String) {
            if (BuildConfig.DEBUG) {
                Log.d(TAG, buildString(tag, msg))
            }
        }

        fun i(tag: String, msg: String) {
            if (BuildConfig.DEBUG) {
                Log.i(TAG, buildString(tag, msg))
            }
        }

        fun w(tag: String, msg: String) {
            if (BuildConfig.DEBUG) {
                Log.w(TAG, buildString(tag, msg))
            }
        }

        fun e(tag: String, msg: String) {
            if (BuildConfig.DEBUG) {
                Log.e(TAG, buildString(tag, msg))
            }
        }
    }
}