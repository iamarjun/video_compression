package com.arjun.videocompression

import android.net.Uri
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.arjun.videocompression.databinding.ActivityMainBinding
import com.arjun.videocompression.util.FileHelper
import com.arjun.videocompression.util.viewBinding
import com.arthenica.mobileffmpeg.Config.RETURN_CODE_CANCEL
import com.arthenica.mobileffmpeg.Config.RETURN_CODE_SUCCESS
import com.arthenica.mobileffmpeg.FFmpeg
import com.google.android.exoplayer2.DefaultLoadControl
import com.google.android.exoplayer2.DefaultRenderersFactory
import com.google.android.exoplayer2.ExoPlayerFactory
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.upstream.DataSource
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.util.Util
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber
import java.io.File
import javax.inject.Inject


@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    @Inject
    internal lateinit var fileHelper: FileHelper

    private val binding by viewBinding(ActivityMainBinding::inflate)
    private lateinit var player: SimpleExoPlayer

    private val getContent = registerForActivityResult(ActivityResultContracts.GetContent()) {
        Timber.d(it.toString())
        lifecycleScope.launch(Dispatchers.IO) {
            val file = copyToLocalFile(it)
            Timber.d(file.path)
            Timber.d(file.absolutePath)
            runCompression(file.path)
        }
    }

    private fun copyToLocalFile(uri: Uri): File {
        return fileHelper.copyToLocalFile(uri)
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        player = ExoPlayerFactory.newSimpleInstance(
                this,
                DefaultRenderersFactory(this),
                DefaultTrackSelector(),
                DefaultLoadControl())


        binding.pickVideo.setOnClickListener {
            getContent.launch("video/*")
        }

        binding.playerView.player = player

    }

    private fun runCompression(path: String) {
        FFmpeg.executeAsync("-y -i $path -c:v mpeg4 ${fileHelper.getBaseDirectory()}/videos/output.mp4") { executionId, returnCode ->
            when (returnCode) {
                RETURN_CODE_SUCCESS -> {
                    val uri = fileHelper.getFileProviderUri(File("${fileHelper.getBaseDirectory()}/videos/output.mp4"))
                    Timber.d(uri.path)
                    Timber.d("Async command execution completed successfully.")
                    val mediaSource = buildMediaSourceNew(uri)
                    player.prepare(mediaSource, true, false)
                }
                RETURN_CODE_CANCEL -> {
                    Timber.d("Async command execution cancelled by user.")
                }
                else -> {
                    Timber.d("Async command execution failed with rc=%d.", returnCode)
                }
            }
        }
    }

    private fun buildMediaSourceNew(uri: Uri): MediaSource? {
        val dataSourceFactory: DataSource.Factory = DefaultDataSourceFactory(this, Util.getUserAgent(this, getString(R.string.app_name)))
        return ProgressiveMediaSource.Factory(dataSourceFactory).createMediaSource(uri)
    }
}