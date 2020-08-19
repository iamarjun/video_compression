package com.arjun.videocompression

import android.net.Uri
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.arjun.videocompression.util.Event
import com.arjun.videocompression.util.FileHelper
import com.arthenica.mobileffmpeg.Config
import com.arthenica.mobileffmpeg.FFmpeg
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.File

class MainViewModel @ViewModelInject constructor(private val fileHelper: FileHelper) : ViewModel() {

    private val _compressedVideoUri by lazy { MutableLiveData<Event<Uri>>() }

    private val _videoSizeBeforeCompression by lazy { MutableLiveData<Event<String>>() }
    private val _videoSizeAfterCompression by lazy { MutableLiveData<Event<String>>() }

    val compressedVideoUri: LiveData<Event<Uri>>
        get() = _compressedVideoUri

    val videoSizeBeforeCompression: LiveData<Event<String>>
        get() = _videoSizeBeforeCompression

    val videoSizeAfterCompression: LiveData<Event<String>>
        get() = _videoSizeAfterCompression

    fun compressVideo(uri: Uri, bitrate: String) {

        viewModelScope.launch {

            runCompression(uri, bitrate)
        }

    }

    private fun copyToLocalFile(uri: Uri): File {
        return fileHelper.copyToLocalFile(uri)
    }


    private suspend fun runCompression(uri: Uri, bitrate: String) = withContext(Dispatchers.IO) {

        val inputFile = copyToLocalFile(uri)
        val inputSize = FileHelper.getFolderSizeLabel(inputFile)

        Timber.d("size before compression $inputSize")
        _videoSizeBeforeCompression.postValue(Event(inputSize))

        Timber.d(inputFile.path)
        Timber.d(inputFile.absolutePath)

        FFmpeg.executeAsync("-y -i ${inputFile.path} -c:v mpeg4 -b:v ${bitrate}k ${fileHelper.getBaseDirectory()}/videos/output.mp4") { _, returnCode ->
            when (returnCode) {
                Config.RETURN_CODE_SUCCESS -> {

                    val outputFile = File("${fileHelper.getBaseDirectory()}/videos/output.mp4")

                    val outputSize = FileHelper.getFolderSizeLabel(outputFile)
                    Timber.d("size after compression $outputSize")

                    _videoSizeAfterCompression.postValue(Event(outputSize))

                    val outputUri = fileHelper.getFileProviderUri(outputFile)
                    Timber.d(outputUri.path)
                    Timber.d("Async command execution completed successfully.")

                    _compressedVideoUri.postValue(Event(outputUri))
                }
                Config.RETURN_CODE_CANCEL -> {
                    Timber.d("Async command execution cancelled by user.")
                }
                else -> {
                    Timber.d("Async command execution failed with rc=%d.", returnCode)
                }
            }
        }
    }

}