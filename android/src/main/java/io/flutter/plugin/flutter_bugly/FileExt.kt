package io.flutter.plugin.flutter_bugly

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import androidx.annotation.RequiresApi
import java.io.File
import java.io.IOException
private const val TAG = "FileExt"

fun downloadFile(
    context: Context,
    filename: String,
    sbf: StringBuffer
) {

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        val uri = createDownloadUri(context, filename)
        try {
            if (uri != null) {
                context.contentResolver.openOutputStream(uri, "w")?.use { outputStream ->
                    //
                    outputStream.write(sbf.toString().toByteArray())
                }
            }
        } catch (e: IOException) {
            Log.e(TAG, e.printStackTrace().toString())
            // _errorFlow.emit("Couldn't save the ${type.extension}\n$uri")
        }
    } else {
        val downloadsFolder =
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)

        // We re-generate the filename until we can confirm its uniqueness
        val file = File(downloadsFolder, filename)

        try {
            file.outputStream().use { outputStream ->
                outputStream.write(sbf.toString().toByteArray())
            }
        } catch (e: IOException) {
            Log.e(TAG, e.printStackTrace().toString())
        }
    }

}

@RequiresApi(Build.VERSION_CODES.Q)
fun createDownloadUri(context: Context, filename: String): Uri? {
    val downloadsCollection = MediaStore.Downloads.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
    val newImage = ContentValues().apply {
        put(MediaStore.Downloads.DISPLAY_NAME, filename)
    }
    // This method will perform a binder transaction which is better to execute off the main
    // thread
    return context.contentResolver.insert(downloadsCollection, newImage)

}