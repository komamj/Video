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
package com.koma.video.search

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.support.v4.content.ContextCompat
import android.support.v7.widget.PopupMenu
import android.support.v7.widget.RecyclerView
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.koma.video.R
import com.koma.video.data.enities.VideoEntry
import com.koma.video.play.PlayActivity
import com.koma.video.util.GlideApp

class SearchAdapter constructor(private val context: Context) :
    RecyclerView.Adapter<SearchAdapter.SearchVH>() {
    private val data = ArrayList<VideoEntry>()

    private var keyWord = ""

    fun updateData(newData: List<VideoEntry>, newKeyWord: String) {
        keyWord = newKeyWord

        data.clear()
        data.addAll(newData)

        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SearchVH {
        return SearchVH(
            LayoutInflater.from(context).inflate(
                R.layout.video_item, parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: SearchVH, position: Int) {
        val videoEntry = data[position]

        bind(holder, videoEntry)
    }

    private fun bind(holder: SearchAdapter.SearchVH, entry: VideoEntry) {
        GlideApp.with(context)
            .asBitmap()
            .placeholder(ColorDrawable(Color.GRAY))
            .thumbnail(0.1f)
            .load(entry.uri)
            .into(holder.image)

        val spannableStringBuilder = SpannableStringBuilder(entry.displayName)
        val foregroundColorSpan =
            ForegroundColorSpan(ContextCompat.getColor(context, R.color.colorAccent))
        val start = entry.displayName.toLowerCase().indexOf(keyWord)
        val end = start + keyWord.length
        spannableStringBuilder.setSpan(
            foregroundColorSpan,
            start,
            end,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        holder.name.text = spannableStringBuilder

        holder.duration.text = entry.formatDuration
    }

    override fun getItemCount(): Int {
        return data.size
    }

    inner class SearchVH(itemView: View) : RecyclerView.ViewHolder(itemView), View.OnClickListener {
        val image: ImageView
        val duration: TextView
        val name: TextView

        init {
            itemView.setOnClickListener(this)
            image = itemView.findViewById(R.id.iv_video)
            name = itemView.findViewById(R.id.tv_name)
            duration = itemView.findViewById(R.id.tv_duration)
            (itemView.findViewById(R.id.iv_more) as ImageView).setOnClickListener(this)
        }


        override fun onClick(view: View) {
            when (view.id) {
                R.id.iv_more -> {
                    val popupMenu = PopupMenu(view.context, view)
                    popupMenu.menuInflater.inflate(R.menu.item_video_menu, popupMenu.menu)
                    popupMenu.show()
                }
                else -> {
                    val intent = Intent(context, PlayActivity::class.java)
                    intent.putExtra(PlayActivity.KEY_MEDIA_ID, data[adapterPosition].id)
                    context.startActivity(intent)
                }
            }
        }
    }
}