package com.arjun.videocompression

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import com.arjun.videocompression.databinding.FragmentVideoPickerBinding
import com.arjun.videocompression.util.viewBinding
import timber.log.Timber


class VideoPickerFragment : Fragment() {

    private val binding by viewBinding(FragmentVideoPickerBinding::bind)

    private val getContent = registerForActivityResult(ActivityResultContracts.GetContent()) {
        Timber.d(it.toString())
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_video_picker, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.pickVideo.setOnClickListener {
            getContent.launch("video/*")
        }
    }

    companion object {

    }
}