package com.geckostudio.androidyoutubevlc

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.ViewModelProviders
import com.geckostudio.androidyoutubevlc.YoutubeDLUtils.initYoutubeDL
import com.geckostudio.androidyoutubevlc.databinding.ActivityMainBinding
import org.videolan.libvlc.MediaPlayer


class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private var player: Player? = null

    private val repository: MainActivityRepository by lazy {
        MainActivityRepository()
    }

    private val viewModel: MainActivityViewModel by lazy {
        val factory = MainActivityViewModel.Factory(repository = repository)
        ViewModelProviders.of(this, factory)[MainActivityViewModel::class.java]
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(LayoutInflater.from(this))
        setContentView(binding.root)

        initYoutubeDL(application)

        manageShareData(intent)

        player = Player(this, binding.viewVlcLayout)

        player?.eventListener = MediaPlayer.EventListener {
            when (it.type) {
                MediaPlayer.Event.Playing -> HUDUtils.dismissDialog()
            }
        }

        viewModel.videoInfoLiveData.observeForever {
            HUDUtils.dismissDialog()
            if(it != null) {
                binding.copybtn.visibility = View.VISIBLE
                binding.playBtn.visibility = View.VISIBLE
                binding.playTvBtn.visibility = View.VISIBLE
            } else {
                Toast.makeText(this, "Une erreur est survenue", Toast.LENGTH_SHORT).show()
            }
        }

        binding.urlOfYoutube.addTextChangedListener {
            HUDUtils.showDialog(this, "Chargement...")
            viewModel.getStreamingUrl(it.toString())
        }

        binding.copybtn.setOnClickListener {
            val clipboard: ClipboardManager = getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("StreamingUrl", viewModel.videoInfoLiveData.value?.url)
            clipboard.setPrimaryClip(clip)
            Toast.makeText(this, "Le lien a été copié", Toast.LENGTH_SHORT).show()
        }

        binding.playBtn.setOnClickListener {
            val url = viewModel.videoInfoLiveData.value?.url
            if(url != null) {
                player?.play(url)
            } else {
                Toast.makeText(this, "Une erreur est survenue", Toast.LENGTH_SHORT).show()
            }
        }

        binding.playTvBtn.setOnClickListener {
            val videoInfo = viewModel.videoInfoLiveData.value
            if(videoInfo?.url != null) {
                player?.sendToVlc(this, videoInfo.url, videoInfo.title)
            } else {
                Toast.makeText(this, "Une erreur est survenue", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        manageShareData(intent)
    }

    private fun manageShareData(intent: Intent?) {
        if(intent?.action == Intent.ACTION_SEND && "text/plain" == intent.type) {
            intent.getStringExtra(Intent.EXTRA_TEXT)?.let {
                binding.urlOfYoutube.setText(it)
            }
        }
    }
}