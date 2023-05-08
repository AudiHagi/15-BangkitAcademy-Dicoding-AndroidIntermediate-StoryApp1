package com.dicoding.storyapp.view.liststory

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.dicoding.storyapp.data.remote.response.Story
import com.dicoding.storyapp.data.remote.response.StoryResponse
import com.dicoding.storyapp.data.remote.retrofit.ApiConfig
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ListStoryViewModel : ViewModel() {

    val listStories = MutableLiveData<ArrayList<Story>>()

    fun getStories(TOKEN: String) {
        ApiConfig.getApiService().getStories(
            "Bearer $TOKEN"
        ).enqueue(object : Callback<StoryResponse> {
            override fun onResponse(call: Call<StoryResponse>, response: Response<StoryResponse>) {
                if (response.isSuccessful) {
                    listStories.postValue(response.body()?.listStory)
                }
            }
            override fun onFailure(call: Call<StoryResponse>, t: Throwable) {
            }

        })
    }

    fun getListStories(): LiveData<ArrayList<Story>> {
        return listStories
    }

}