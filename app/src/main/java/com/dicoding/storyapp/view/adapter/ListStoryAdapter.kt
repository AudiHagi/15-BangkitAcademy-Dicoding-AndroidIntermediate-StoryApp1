package com.dicoding.storyapp.view.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.dicoding.storyapp.data.remote.response.Story
import com.dicoding.storyapp.databinding.ItemStoryBinding
import com.dicoding.storyapp.utils.formatDate
import java.util.*

class ListStoryAdapter : RecyclerView.Adapter<ListStoryAdapter.StoryViewHolder>() {
    private val listStory = ArrayList<Story>()
    private lateinit var onItemClickCallback: OnItemClickCallback

    fun setOnItemClickCallback(onItemClickCallback: OnItemClickCallback) {
        this.onItemClickCallback = onItemClickCallback
    }

    @SuppressLint("NotifyDataSetChanged")
    fun setList(stories: ArrayList<Story>) {
        listStory.clear()
        listStory.addAll(stories)
        notifyDataSetChanged()
    }

    inner class StoryViewHolder(private val binding: ItemStoryBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(story: Story) {
            binding.apply {
                Glide.with(itemView)
                    .load(story.photoUrl)
                    .into(storyImages)
                storyName.text = story.name
                storyDate.text = formatDate(story.createdAt, TimeZone.getDefault().id)
                storyDesc.text = story.description

                root.setOnClickListener {
                    onItemClickCallback.onItemClicked(story)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StoryViewHolder {
        val binding = ItemStoryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return StoryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: StoryViewHolder, position: Int) {
        holder.bind(listStory[position])
    }

    override fun getItemCount(): Int = listStory.size

    interface OnItemClickCallback {
        fun onItemClicked(data: Story)
    }
}