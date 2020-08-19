package com.arjun.videocompression

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.navArgs
import com.arjun.videocompression.databinding.FragmentVideoPlayBackBinding
import com.arjun.videocompression.util.viewBinding
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
import com.google.android.material.snackbar.Snackbar
import timber.log.Timber

class VideoPlayBackFragment : Fragment() {

    private val binding by viewBinding(FragmentVideoPlayBackBinding::bind)
    private val args: VideoPlayBackFragmentArgs by navArgs()
    private lateinit var player: SimpleExoPlayer
    private val viewModel: MainViewModel by activityViewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        player = ExoPlayerFactory.newSimpleInstance(
            requireContext(),
            DefaultRenderersFactory(requireContext()),
            DefaultTrackSelector(),
            DefaultLoadControl()
        )

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_video_play_back, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.playerView.player = player

        val mediaSource = buildMediaSourceNew(args.videoUri.toUri())
        player.prepare(mediaSource, true, false)

        binding.compressVideo.setOnClickListener {
            val bitrate = binding.bitrateField.text.toString()

            if (bitrate.isEmpty())
                Snackbar.make(requireView(), "Enter a value for bitrate", Snackbar.LENGTH_LONG)
                    .show()
            else {
                viewModel.compressVideo(args.videoUri.toUri(), bitrate)
                Snackbar.make(
                    requireView(),
                    "Compression in progress......",
                    Snackbar.LENGTH_INDEFINITE
                ).show()
            }
        }

        viewModel.compressedVideoUri.observe(viewLifecycleOwner) {
            Snackbar.make(
                requireView(),
                "Video Compressed...",
                Snackbar.LENGTH_INDEFINITE
            ).setAction("View Video") {

            }
                .show()
            Timber.d(it.toString())
        }

    }

    private fun buildMediaSourceNew(uri: Uri): MediaSource? {
        val dataSourceFactory: DataSource.Factory = DefaultDataSourceFactory(
            requireContext(),
            Util.getUserAgent(requireContext(), getString(R.string.app_name))
        )
        return ProgressiveMediaSource.Factory(dataSourceFactory).createMediaSource(uri)
    }

    companion object {

    }
}