package com.geckostudio.androidyoutubevlc

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.MediaController
import android.widget.Toast
import org.videolan.libvlc.LibVLC
import org.videolan.libvlc.Media
import org.videolan.libvlc.MediaPlayer
import org.videolan.libvlc.util.VLCVideoLayout

class Player(context: Context, vlcVideoLayout: VLCVideoLayout) {
    private var mMediaPlayer: MediaPlayer? = null
    private var mLibVLC: LibVLC? = null
    var eventListener: MediaPlayer.EventListener? = null

    private val playerInterface: MediaController.MediaPlayerControl = object :
        MediaController.MediaPlayerControl {
        override fun getBufferPercentage(): Int {
            return 0
        }

        override fun getCurrentPosition(): Int {
            val pos: Float = mMediaPlayer?.position ?: 0f
            return (pos * duration).toInt()
        }

        override fun getDuration(): Int {
            return mMediaPlayer?.length?.toInt() ?:0
        }

        override fun isPlaying(): Boolean {
            return mMediaPlayer?.isPlaying ?: false
        }

        override fun pause() {
            mMediaPlayer?.pause()
        }

        override fun seekTo(pos: Int) {
            mMediaPlayer?.position = pos.toFloat() / duration
        }

        override fun start() {
            mMediaPlayer?.play()
        }

        override fun canPause(): Boolean {
            return true
        }

        override fun canSeekBackward(): Boolean {
            return true
        }

        override fun canSeekForward(): Boolean {
            return true
        }

        override fun getAudioSessionId(): Int {
            return 0
        }
    }

    init {
        val args: ArrayList<String> = ArrayList()
        args.add("--no-drop-late-frames")
        args.add("--no-skip-frames")
        args.add("--rtsp-tcp")
        args.add("-vvv")
        mLibVLC = LibVLC(context, args)
        mMediaPlayer?.stop()
        mMediaPlayer = MediaPlayer(mLibVLC)

        mMediaPlayer?.attachViews(vlcVideoLayout, null, false, true)

        val controller = MediaController(context)
        controller.setMediaPlayer(playerInterface)
        controller.setAnchorView(vlcVideoLayout)
        vlcVideoLayout.setOnClickListener {
            controller.show(10000)
        }
    }

    fun play(url: String) {
        val media = Media(mLibVLC, Uri.parse(url))
        mMediaPlayer?.media = media
        media.release()
        mMediaPlayer?.play()

        mMediaPlayer?.setEventListener(eventListener)
    }

    fun sendToVlc(activity: Activity, url: String, title: String) {
        val vlcRequestCode = 42
        val uri = Uri.parse(url)
        val vlcIntent = Intent(Intent.ACTION_VIEW)
        vlcIntent.setPackage("org.videolan.vlc")
        vlcIntent.setDataAndTypeAndNormalize(uri, "video/*")
        vlcIntent.putExtra("title", title)

        try {
            activity.startActivityForResult(vlcIntent, vlcRequestCode)
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(activity, "Je ne trouve pas vlc", Toast.LENGTH_SHORT).show()
        }
    }
}