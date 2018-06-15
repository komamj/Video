/*
 * Copyright 2018 Koma
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
package com.koma.video.folder

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.support.v7.util.DiffUtil
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.koma.video.R
import com.koma.video.base.BaseAdapter
import com.koma.video.data.enities.BucketEntry
import com.koma.video.folder.detail.FolderDetailActivity
import com.koma.video.util.GlideApp

class FoldersAdapter(context: Context) :
    BaseAdapter<BucketEntry, FoldersAdapter.FoldersVH>(context = context,
        diffCallback = object : DiffUtil.ItemCallback<BucketEntry>() {
            override fun areItemsTheSame(oldItem: BucketEntry, newItem: BucketEntry): Boolean {
                return oldItem.buketId == newItem.buketId
            }

            override fun areContentsTheSame(oldItem: BucketEntry, newItem: BucketEntry): Boolean {
                return oldItem.equals(newItem)
            }

            override
            fun getChangePayload(oldItem: BucketEntry, newItem: BucketEntry): Any? {
                return null
            }
        }) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = FoldersVH(
        LayoutInflater.from(context).inflate(
            R.layout.folder_item, parent, false
        )
    )

    override fun onBindViewHolder(holder: FoldersVH, position: Int) {
        val entry = getItem(position)

        bind(holder, entry)
    }

    private fun bind(holder: FoldersVH, entry: BucketEntry) {
        GlideApp.with(context)
            .asBitmap()
            .placeholder(ColorDrawable(Color.GRAY))
            .thumbnail(0.1f)
            .load(entry.uri)
            .into(holder.image)

        holder.name.text = entry.name

        holder.count.text = context.getString(R.string.folder_item_count, entry.count)
    }

    inner class FoldersVH(itemView: View) : RecyclerView.ViewHolder(itemView),
        View.OnClickListener {
        val image: ImageView
        val name: TextView
        val count: TextView

        init {
            itemView.setOnClickListener(this)
            image = itemView.findViewById(R.id.iv_video)
            name = itemView.findViewById(R.id.tv_name)
            count = itemView.findViewById(R.id.tv_count)
        }

        override fun onClick(view: View) {
            val intent = Intent(context, FolderDetailActivity::class.java)
            val entry = getItem(adapterPosition)

            intent.putExtra(
                FolderDetailActivity.BUCKET_ID,
                entry.buketId
            )
            intent.putExtra(FolderDetailActivity.BUCKET_NAME, entry.name)
            context.startActivity(intent)
        }
    }
}