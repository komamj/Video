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
package com.koma.bugly

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Environment
import com.koma.bugly.util.LogUtils
import java.io.*
import java.text.SimpleDateFormat
import java.util.*

@SuppressLint("StaticFieldLeak")
class CrashHandler private constructor() : Thread.UncaughtExceptionHandler {
    private lateinit var context: Context

    private val path: String

    private val defaultCrashHandler = Thread.getDefaultUncaughtExceptionHandler()

    init {
        Thread.setDefaultUncaughtExceptionHandler(this)

        path = Environment.getExternalStorageDirectory().path + "/bugly/log/"
    }

    private fun init(context: Context) {
        this.context = context.applicationContext
    }

    override
    fun uncaughtException(thread: Thread, ex: Throwable) {
        LogUtils.e(TAG, "uncaughtException thread ${thread.id},error ${ex.message}")

        try {
            dumpExceptionToExternalStorage(ex)

            uploadExceptionToServer()
        } catch (e: IOException) {
            e.printStackTrace()
        }

        ex.printStackTrace()

        defaultCrashHandler.uncaughtException(thread, ex)
    }

    private fun dumpExceptionToExternalStorage(ex: Throwable) {
        if (Environment.getExternalStorageState() != Environment.MEDIA_MOUNTED) {
            LogUtils.i(TAG, "sdcard unmounted,skip dump exception")

            return
        }

        val dir = File(path)
        if (!dir.exists()) {
            dir.mkdirs()
        }

        val current = System.currentTimeMillis()
        val time = SimpleDateFormat.getDateTimeInstance().format(Date(current))
        val file = File(path + FILE_NAME + time + FILE_NAME_SUFFIX)
        try {
            val pw = PrintWriter(BufferedWriter(FileWriter(file)))
            pw.println(time)

            dumpPhoneInfo(pw)
            pw.println()
            ex.printStackTrace(pw)
            pw.close()
        } catch (e: Exception) {
            LogUtils.e(TAG, "dump crash info failed")
        }
    }

    @Throws(PackageManager.NameNotFoundException::class)
    private fun dumpPhoneInfo(pw: PrintWriter) {
        val packageManager = context.packageManager
        val packageInfo =
            packageManager.getPackageInfo(context.packageName, PackageManager.GET_ACTIVITIES)
        with(pw) {
            print("App version: ${packageInfo.versionName}_")
            println(packageInfo.versionCode)

            print("OS version: ${Build.VERSION.RELEASE}_")
            println(Build.VERSION.SDK_INT)

            print("Vendor: ")
            println(Build.MANUFACTURER)

            print("Model: ")
            println(Build.MODEL)

            print("CPU ABI: ")
            println(Build.CPU_ABI)
        }
    }

    private fun uploadExceptionToServer() {

    }

    companion object {
        private const val TAG = "CrashHandler"
        private const val FILE_NAME = "crash"
        private const val FILE_NAME_SUFFIX = ".trace"

        fun init(context: Context) {
            SingletonHolder.holder.init(context)
        }
    }

    private object SingletonHolder {
        val holder = CrashHandler()
    }
}