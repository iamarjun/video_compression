package com.arjun.videocompression

import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.upstream.DataSource
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.util.Util
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class MainActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

//        player = ExoPlayerFactory.newSimpleInstance(
//                this,
//                DefaultRenderersFactory(this),
//                DefaultTrackSelector(),
//                DefaultLoadControl())
//
//
//        binding.pickVideo.setOnClickListener {
//
//        }
//
//        binding.playerView.player = player

    }


    private fun buildMediaSourceNew(uri: Uri): MediaSource? {
        val dataSourceFactory: DataSource.Factory = DefaultDataSourceFactory(this, Util.getUserAgent(this, getString(R.string.app_name)))
        return ProgressiveMediaSource.Factory(dataSourceFactory).createMediaSource(uri)
    }
}