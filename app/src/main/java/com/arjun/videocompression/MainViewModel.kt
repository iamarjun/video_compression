package com.arjun.videocompression

import android.net.Uri
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.arjun.videocompression.util.FileHelper
import com.arthenica.mobileffmpeg.Config
import com.arthenica.mobileffmpeg.FFmpeg
import kotlinx.coroutines.launch
import timber.log.Timber
import java.io.File

class MainViewModel @ViewModelInject constructor(private val fileHelper: FileHelper) : ViewModel() {

    private val videoUri by lazy { MutableLiveData<Uri>() }


    fun setVideoUri(uri: Uri) {
        viewModelScope.launch {
            val file = copyToLocalFile(uri)
            val size = FileHelper.getFolderSizeLabel(file)

            Timber.d("size before compression $size")

            Timber.d(file.path)
            Timber.d(file.absolutePath)

            runCompression(file.path)
        }
    }

    private fun copyToLocalFile(uri: Uri): File {
        return fileHelper.copyToLocalFile(uri)
    }

    private fun runCompression(path: String) {
        FFmpeg.executeAsync("-y -i $path -c:v mpeg4 -b:v 100k ${fileHelper.getBaseDirectory()}/videos/output.mp4") { executionId, returnCode ->
            when (returnCode) {
                Config.RETURN_CODE_SUCCESS -> {

                    val file = File("${fileHelper.getBaseDirectory()}/videos/output.mp4")

                    val size = FileHelper.getFolderSizeLabel(file)
                    Timber.d("size after compression $size")

                    val uri = fileHelper.getFileProviderUri(file)
                    Timber.d(uri.path)
                    Timber.d("Async command execution completed successfully.")
//                    val mediaSource = buildMediaSourceNew(uri)
//                    player.prepare(mediaSource, true, false)
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