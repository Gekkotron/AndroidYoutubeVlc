package com.geckostudio.androidyoutubevlc

import androidx.lifecycle.MutableLiveData
import com.yausername.youtubedl_android.mapper.VideoInfo

class MainActivityRepository {
    val videoInfoLiveData: MutableLiveData<VideoInfo> by lazy {
        MutableLiveData<VideoInfo>()
    }

    fun getStreamingUrl(url: String?) {
        if(url != null) {
            val videoInfo = YoutubeDLUtils.extractVideoInfo(url)
            videoInfoLiveData.postValue(videoInfo?.videoInfo)
        } else videoInfoLiveData.postValue(null)
    }
}