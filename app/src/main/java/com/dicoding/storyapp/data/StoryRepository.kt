package com.dicoding.storyapp.data

import android.app.Application
import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.LiveData
import androidx.lifecycle.liveData
import com.dicoding.storyapp.data.remote.response.AddStoryResponse
import com.dicoding.storyapp.data.remote.response.LoginResponse
import com.dicoding.storyapp.data.remote.response.RegisterResponse
import com.dicoding.storyapp.data.remote.retrofit.ApiConfig
import com.dicoding.storyapp.data.remote.retrofit.ApiService
import com.dicoding.storyapp.view.login.LoginPreferences
import com.dicoding.storyapp.view.setting.SettingPreferences
import okhttp3.MultipartBody
import okhttp3.RequestBody

class StoryRepository(
    application: Application,
    private val loginPreferences: LoginPreferences
) {

    private val pref: SettingPreferences
    private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")
    private val apiService: ApiService = ApiConfig.getApiService()

    init {
        pref = SettingPreferences.getInstance(application.dataStore)
    }

    suspend fun saveTheme(darkMode: Boolean) = pref.saveThemeSetting(darkMode)
    fun getTheme() = pref.getThemeSetting()

    fun register(
        name: String,
        email: String,
        password: String
    ): LiveData<Result<RegisterResponse>> = liveData {
        emit(Result.Loading)
        try {
            val response = apiService.register(
                name,
                email,
                password
            )
            if (response.error) {
                emit(Result.Error(response.message))
            } else {
                emit(Result.Success(response))
            }
        } catch (e: Exception) {
            emit(Result.Error(e.message.toString()))
        }
    }

    fun login(
        email: String,
        password: String
    ): LiveData<Result<LoginResponse>> = liveData {
        emit(Result.Loading)
        try {
            val response = apiService.login(
                email,
                password
            )
            if (response.error) {
                emit(Result.Error(response.message))
            } else {
                emit(Result.Success(response))
            }
        } catch (e: Exception) {
            emit(Result.Error(e.message.toString()))
        }
    }

    fun addStory(
        imageFile: MultipartBody.Part,
        desc: RequestBody
    ): LiveData<Result<AddStoryResponse>> = liveData {
        emit(Result.Loading)
        try {
            val response = apiService.createStory(
                token = "Bearer ${loginPreferences.getUser().token}",
                file = imageFile,
                description = desc
            )
            if (response.error) {
                emit(Result.Error(response.message))
            } else {
                emit(Result.Success(response))
            }
        } catch (e: Exception) {
            emit(Result.Error(e.message.toString()))
        }
    }

}