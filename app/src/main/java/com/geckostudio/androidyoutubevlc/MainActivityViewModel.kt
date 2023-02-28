package com.geckostudio.androidyoutubevlc

import androidx.lifecycle.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivityViewModel(private val repository: MainActivityRepository) : ViewModel() {
    val videoInfoLiveData = repository.videoInfoLiveData

    fun getStreamingUrl(url: String?) = apply {
        viewModelScope.launch(Dispatchers.IO) { repository.getStreamingUrl(url) }
    }

    @Suppress("UNCHECKED_CAST")
    class Factory(private val repository: MainActivityRepository) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return MainActivityViewModel(repository) as T
        }
    }
}