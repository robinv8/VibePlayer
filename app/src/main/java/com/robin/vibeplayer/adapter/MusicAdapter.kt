package com.robin.vibeplayer.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.robin.vibeplayer.R
import com.robin.vibeplayer.model.MusicFile

class MusicAdapter(
    private var musicFiles: List<MusicFile>,
    private val onItemClick: (MusicFile) -> Unit
) : RecyclerView.Adapter<MusicAdapter.MusicViewHolder>() {

    class MusicViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val titleText: TextView = view.findViewById(R.id.titleText)
        val artistText: TextView = view.findViewById(R.id.artistText)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MusicViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_music, parent, false)
        return MusicViewHolder(view)
    }

    override fun onBindViewHolder(holder: MusicViewHolder, position: Int) {
        val musicFile = musicFiles[position]
        holder.titleText.text = musicFile.title
        holder.artistText.text = musicFile.artist
        holder.itemView.setOnClickListener { onItemClick(musicFile) }
    }

    override fun getItemCount() = musicFiles.size

    fun updateList(newList: List<MusicFile>) {
        musicFiles = newList
        notifyDataSetChanged()
    }
}
