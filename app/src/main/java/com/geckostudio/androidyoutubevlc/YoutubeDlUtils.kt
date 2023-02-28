package com.geckostudio.androidyoutubevlc

import android.app.Application
import android.content.Context
import android.util.Log
import android.widget.Toast
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import com.yausername.youtubedl_android.YoutubeDL
import com.yausername.youtubedl_android.YoutubeDLException
import com.yausername.youtubedl_android.YoutubeDLRequest
import com.yausername.youtubedl_android.mapper.VideoInfo
import io.reactivex.Observable

object YoutubeDLUtils {
    private var updating = false
    private val compositeDisposable: CompositeDisposable = CompositeDisposable()

    /**********************************************************************************************/
    /*                                         Extractor                                          */
    /**********************************************************************************************/

    fun initYoutubeDL(application: Application) {
        try {
            YoutubeDL.getInstance().init(application)
        } catch (e: YoutubeDLException) {
            Log.e("Utils", "failed to initialize youtubedl-android", e)
        }
    }

    fun extractVideoInfo(url: String): VideoInfoExtra? {
        if(url.isEmpty()) return null

        Log.e("Utils", "extractVideoInfo URL $url")

        repeat(2) {
            val request = YoutubeDLRequest(url)
            request.addOption("-f", "best")

            try {
                val videoInfo = YoutubeDL.getInstance().getInfo(request)
                Log.e("Utils", "extractVideoInfo title ${videoInfo.title}")
                return VideoInfoExtra(videoInfo)
            } catch (re: RuntimeException) {
                Log.e("Utils", "extractVideoInfo Error $url $re")
            } catch (e: YoutubeDLException) {
                Log.e("Utils", "failed to extractVideoInfo", e)
                val error = if(e.message?.contains("This video is not available") == true) {
                    "La musique n'est pas disponible"
                } else {
                    e.message
                }
                return VideoInfoExtra(null, error)
            } catch (e: Exception) {
                Log.e("Utils", "failed to extractVideoInfo", e)
            }
            Thread.sleep(50)
        }

        return null
    }

    fun updateYoutubeDL(context: Context?) {
        if (updating) {
            Toast.makeText(context, "update is already in progress", Toast.LENGTH_LONG)
                .show()
            return
        }
        updating = true
        val disposable: Disposable = Observable.fromCallable {
            YoutubeDL.getInstance().updateYoutubeDL(context)
        }
            .subscribeOn(Schedulers.newThread())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ status ->
                when (status) {
                    YoutubeDL.UpdateStatus.DONE -> Toast.makeText(
                        context,
                        "La mise à jour a été effectuée avec succès",
                        Toast.LENGTH_LONG
                    ).show()
                    YoutubeDL.UpdateStatus.ALREADY_UP_TO_DATE -> Toast.makeText(
                        context,
                        "La librairie est déjà à jour.",
                        Toast.LENGTH_LONG
                    ).show()
                    else -> Toast.makeText(context, status.toString(), Toast.LENGTH_LONG)
                        .show()
                }
                updating = false
            }) {
                // progressBar.setVisibility(View.GONE)
                Toast.makeText(context, "Mise à jour échoué", Toast.LENGTH_LONG).show()
                updating = false
            }
        compositeDisposable.add(disposable)
    }
}

data class VideoInfoExtra(val videoInfo: VideoInfo?, val error: String? = null)