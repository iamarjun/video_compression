package com.arjun.videocompression

import android.net.Uri
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.arjun.videocompression.util.FileHelper
import com.arthenica.mobileffmpeg.Config
import com.arthenica.mobileffmpeg.FFmpeg
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.File

class MainViewModel @ViewModelInject constructor(private val fileHelper: FileHelper) : ViewModel() {

    private val _compressedVideoUri by lazy { MutableLiveData<Uri>() }

    val compressedVideoUri: LiveData<Uri>
        get() = _compressedVideoUri

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

        Timber.d(inputFile.path)
        Timber.d(inputFile.absolutePath)

        FFmpeg.executeAsync("-y -i ${inputFile.path} -c:v mpeg4 -b:v ${bitrate}k ${fileHelper.getBaseDirectory()}/videos/output.mp4") { _, returnCode ->
            when (returnCode) {
                Config.RETURN_CODE_SUCCESS -> {

                    val outputFile = File("${fileHelper.getBaseDirectory()}/videos/output.mp4")

                    val outputSize = FileHelper.getFolderSizeLabel(outputFile)
                    Timber.d("size after compression $outputSize")

                    val outputUri = fileHelper.getFileProviderUri(outputFile)
                    Timber.d(outputUri.path)
                    Timber.d("Async command execution completed successfully.")

                    _compressedVideoUri.postValue(outputUri)
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