package com.koma.video.data.source

import android.content.ContentResolver
import android.content.ContentUris
import android.content.Context
import android.provider.MediaStore
import com.koma.video.data.enities.BucketEntry
import com.koma.video.data.enities.VideoEntryDetail
import com.koma.video.data.enities.VideoEntry
import com.koma.video.util.LogUtils
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import java.io.FileNotFoundException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class VideoDataSource @Inject constructor(private val context: Context) : IVideoDataSource {
    private val resolver: ContentResolver by lazy<ContentResolver> {
        context.contentResolver
    }

    override fun getVideoEntries(): Flowable<List<VideoEntry>> {
        return Flowable.create({
            val entries = ArrayList<VideoEntry>()
            val projection = arrayOf(
                MediaStore.Video.Media._ID,
                MediaStore.Video.Media.DISPLAY_NAME,
                MediaStore.Video.Media.DURATION
            )
            val cursor =
                resolver.query(
                    MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                    projection,
                    null,
                    null,
                    SORT_ORDER
                )
            cursor?.use {
                if (cursor.count > 0) {
                    it.moveToFirst()
                    do {
                        val entry = VideoEntry(it.getLong(0), it.getString(1), it.getInt(2))
                        entries.add(entry)
                    } while (it.moveToNext())
                }
            }
            it.onNext(entries)
            it.onComplete()
        }, BackpressureStrategy.LATEST)
    }

    override fun getBucketEntries(): Flowable<List<BucketEntry>> {
        return Flowable.create({
            val entries = ArrayList<BucketEntry>()
            val projection = arrayOf(
                MediaStore.Video.VideoColumns.BUCKET_ID,
                MediaStore.Video.VideoColumns.BUCKET_DISPLAY_NAME,
                MediaStore.Video.Media.DATE_TAKEN
            )
            val selection = "1) GROUP BY (1"
            val cursor = resolver.query(
                MediaStore.Video.Media.EXTERNAL_CONTENT_URI, projection,
                selection, null, null
            )
            cursor?.use {
                if (cursor.count > 0) {
                    it.moveToFirst()
                    do {
                        val bucketId = it.getInt(0)
                        val dateTaken = it.getInt(2)
                        val entry = BucketEntry(bucketId, it.getString(1))
                        entry.dateTaken = dateTaken
                        val cur = resolver.query(
                            MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                            arrayOf(MediaStore.Video.Media._ID),
                            MediaStore.Video.Media.BUCKET_ID + " = ?",
                            arrayOf(bucketId.toString()), SORT_ORDER
                        )
                        cur?.use {
                            if (cur.count > 0) {
                                cur.moveToFirst()
                                entry.uri = ContentUris.withAppendedId(
                                    MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                                    cur.getLong(0)
                                )
                                entry.count = cur.count
                                entries.add(entry)
                            }
                        }
                    } while (it.moveToNext())
                }
            }
            it.onNext(entries)
            it.onComplete()
        }, BackpressureStrategy.LATEST)
    }

    override fun getVideoEntries(bucketId: Int): Flowable<List<VideoEntry>> {
        return Flowable.create({
            val entries = ArrayList<VideoEntry>()
            val projection = arrayOf(
                MediaStore.Video.Media._ID,
                MediaStore.Video.Media.DISPLAY_NAME,
                MediaStore.Video.Media.DURATION
            )
            val selection = MediaStore.Video.Media.BUCKET_ID + " = ?"
            val cursor =
                resolver.query(
                    MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                    projection,
                    selection,
                    arrayOf(bucketId.toString()),
                    SORT_ORDER
                )
            cursor?.use {
                if (cursor.count > 0) {
                    it.moveToFirst()
                    do {
                        val entry = VideoEntry(it.getLong(0), it.getString(1), it.getInt(2))
                        entries.add(entry)
                    } while (it.moveToNext())
                }
            }
            it.onNext(entries)
            it.onComplete()
        }, BackpressureStrategy.LATEST)
    }

    override fun getVideoDetailEntry(mediaId: Long): Flowable<VideoEntryDetail> {
        return Flowable.create({
            val projection = arrayOf(
                MediaStore.Video.Media.DISPLAY_NAME,
                MediaStore.Video.Media.DURATION,
                MediaStore.Video.Media.DATE_TAKEN,
                MediaStore.Video.Media.SIZE,
                MediaStore.Video.Media.DATA
            )
            val selection = MediaStore.Video.Media._ID + " = ?"
            val cursor =
                resolver.query(
                    MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                    projection,
                    selection,
                    arrayOf(mediaId.toString()),
                    null
                )
            if (cursor == null || cursor.count <= 0) {
                it.onError(FileNotFoundException("the media id $mediaId is not existed"))
            } else {
                val entry = cursor.use {
                    it.moveToFirst()
                    VideoEntryDetail(
                        it.getString(0),
                        it.getInt(1),
                        it.getLong(2),
                        it.getLong(3),
                        it.getString(4)
                    )
                }
                it.onNext(entry)
                it.onComplete()
            }
        }, BackpressureStrategy.LATEST)
    }

    override fun getTitle(mediaId: Long): Flowable<String> {
        return Flowable.create(
            {
                var title = ""
                val cursor =
                    resolver.query(
                        MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                        arrayOf(MediaStore.Video.Media._ID, MediaStore.Video.Media.TITLE),
                        MediaStore.Video.Media._ID + " = ?",
                        arrayOf(mediaId.toString()),
                        null
                    )
                cursor?.use {
                    it.moveToFirst()

                    title = it.getString(1)
                }
                it.onNext(title)
                it.onComplete()
            }, BackpressureStrategy.LATEST
        )
    }

    override fun getVideoEntries(keyword: String): Flowable<List<VideoEntry>> {
        return Flowable.create({
            val entries = ArrayList<VideoEntry>()

            val projection = arrayOf(
                MediaStore.Video.Media._ID,
                MediaStore.Video.Media.DISPLAY_NAME,
                MediaStore.Video.Media.DURATION
            )

            val selection = MediaStore.Video.Media.DISPLAY_NAME + " LIKE '%$keyword%'"
            LogUtils.d(TAG, "selection $selection")

            val cursor =
                resolver.query(
                    MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                    projection,
                    selection,
                    null,
                    SORT_ORDER
                )
            cursor?.use {
                if (cursor.count > 0) {
                    it.moveToFirst()
                    do {
                        val entry = VideoEntry(it.getLong(0), it.getString(1), it.getInt(2))
                        entries.add(entry)
                    } while (it.moveToNext())
                }
            }

            it.onNext(entries)
            it.onComplete()
        }, BackpressureStrategy.LATEST)
    }

    companion object {
        private const val TAG = "VideoDataSource"
        private const val SORT_ORDER = MediaStore.Video.Media.DATE_TAKEN + " DESC"
    }
}