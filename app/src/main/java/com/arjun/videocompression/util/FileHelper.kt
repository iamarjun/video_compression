package com.arjun.videocompression.util

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import com.arjun.videocompression.BuildConfig
import org.apache.commons.io.FileUtils
import timber.log.Timber
import java.io.File
import java.io.IOException
import javax.inject.Inject

class FileHelper @Inject constructor(private val context: Context) {

    fun getFileProviderUri(file: File): Uri {
        return FileProvider.getUriForFile(
            context, BuildConfig.APPLICATION_ID + ".fileprovider",
            file
        )
    }

    @Throws(IOException::class)
    fun createImageFile(): File {
        return createTempFile("photos", "IMG", ".jpg")
    }

    @Throws(IOException::class)
    fun createCsvFile(name: String): File {
        return createTempFile("leaderboard", name + "_", ".csv")
    }

    @Throws(IOException::class)
    fun createTosFile(): File {
        return createTempFile("tos", "TOS", ".htm")
    }

    @Throws(IOException::class)
    private fun createTempFile(dir: String, prefix: String, suffix: String): File {
        // Create an image file name
        var base = context.externalCacheDir
        if (base == null) {
            base = context.cacheDir
        }
        val fileDir = File(base, dir)
        return if (fileDir.exists() || fileDir.mkdir()) {
            File.createTempFile(prefix, suffix, fileDir)
        } else {
            throw IOException("Cannot create dir $dir")
        }
    }

    @Throws(IOException::class)
    fun copyToLocalFile(url: Uri): File {
        val suffix = ".mp4"
        val file = createTempFile("videos", "", suffix)
        Timber.d("Uri: file name : %s", file.name)
        val input = context.contentResolver.openInputStream(url)
        FileUtils.copyInputStreamToFile(input, file)
        return file
    }

    companion object {
        fun getFolderSizeLabel(file: File): String {
            val size = getFolderSize(file) / 1024 // Get size and convert bytes into Kb.
            return if (size >= 1024) {
                (size / 1024).toString() + " Mb"
            } else {
                "$size Kb"
            }
        }

        private fun getFolderSize(file: File): Long {
            var size: Long = 0
            if (file.isDirectory) {
                for (child in file.listFiles()) {
                    size += getFolderSize(child)
                }
            } else {
                size = file.length()
            }
            return size
        }

        fun deleteCache(context: Context) {
            try {
                val dir = context.cacheDir
                deleteDir(dir)
            } catch (e: Exception) {
                Timber.e(e)
            }
        }

        private fun deleteDir(dir: File?): Boolean {
            return if (dir != null && dir.isDirectory) {
                val children = dir.list()
                if (children != null) {
                    for (child in children) {
                        val success = deleteDir(File(dir, child))
                        if (!success) {
                            return false
                        }
                    }
                }
                dir.delete()
            } else if (dir != null && dir.isFile) {
                dir.delete()
            } else {
                false
            }
        }
    }
}