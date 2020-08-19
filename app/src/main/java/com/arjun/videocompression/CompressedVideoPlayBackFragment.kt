package com.arjun.videocompression

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.findNavController
import androidx.navigation.fragment.navArgs
import com.arjun.videocompression.databinding.FragmentCompressedVideoPlayBackBinding
import com.arjun.videocompression.util.EventObserver
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


class CompressedVideoPlayBackFragment : Fragment() {

    private val binding by viewBinding(FragmentCompressedVideoPlayBackBinding::bind)
    private val args: CompressedVideoPlayBackFragmentArgs by navArgs()
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

        val callback: OnBackPressedCallback =
            object : OnBackPressedCallback(true /* enabled by default */) {
                override fun handleOnBackPressed() {
                    val action =
                        CompressedVideoPlayBackFragmentDirections.actionCompressedVideoPlayBackFragmentToVideoPickerFragment()
                    requireView().findNavController().navigate(action)
                }
            }
        requireActivity().onBackPressedDispatcher.addCallback(this, callback)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_compressed_video_play_back, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.playerView.player = player

        val mediaSource = buildMediaSourceNew(args.videoUri.toUri())
        player.prepare(mediaSource, true, false)


        viewModel.videoSizeBeforeCompression.observe(viewLifecycleOwner, EventObserver {
            binding.sizeBeforeCompression.text = "Video size before compression: $it"
        })

        viewModel.videoSizeAfterCompression.observe(viewLifecycleOwner, EventObserver {
            binding.sizeAfterCompression.text = "Video size after compression: $it"
        })

    }

    override fun onPause() {
        super.onPause()
        player.apply {
            stop()
            release()
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