package com.sudoajay.pdf_viewer.helperClass

import android.annotation.SuppressLint
import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.os.Build
import android.provider.DocumentsContract
import android.provider.MediaStore
import android.provider.OpenableColumns
import android.text.TextUtils
import com.sudoajay.pdf_viewer.permission.AndroidExternalStoragePermission
import com.sudoajay.pdf_viewer.sharedPreference.SdCardPathSharedPreference
import java.io.File
import java.io.FileOutputStream
import kotlin.math.min

@SuppressLint("Recycle")
object FileUtils {
    private var contentUri: Uri? = null
    private var context: Context? = null
    /**
     * Get a file path from a Uri. This will get the the path for Storage Access
     * Framework Documents, as well as the _data field for the MediaStore and
     * other file-based ContentProviders.<br></br>
     * <br></br>
     * Callers should check whether the path is local before assuming it
     * represents a local file.
     *
     * @param context The context.
     * @param uri     The Uri to query.
     */
    fun getPath(context: Context, uri: Uri): String? {
        this.context = context
        // check here to KITKAT or new version
        val selection: String
        val selectionArgs: Array<String>
        // DocumentProvider
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            if (DocumentsContract.isDocumentUri(context, uri)) { // ExternalStorageProvider
                if (isExternalStorageDocument(uri)) {
                    val docId = DocumentsContract.getDocumentId(uri)
                    val split = docId.split(":").toTypedArray()
                    val fullPath = getPathFromExtSD(split)
                    return if (fullPath != "") {
                        fullPath
                    } else {
                        null
                    }
                } else if (isDownloadsDocument(uri)) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        context.contentResolver.query(uri, arrayOf(MediaStore.MediaColumns.DISPLAY_NAME), null, null, null).use { cursor ->
                            if (cursor != null && cursor.moveToFirst()) {
                                val fileName = cursor.getString(0)
                                val path = AndroidExternalStoragePermission.getExternalPath(context) + "Download/" + fileName
                                if (!TextUtils.isEmpty(path)) {
                                    return path
                                }
                            }
                        }
                        val id: String = DocumentsContract.getDocumentId(uri)
                        if (!TextUtils.isEmpty(id)) {
                            if (id.startsWith("raw:")) {
                                return id.replaceFirst("raw:".toRegex(), "")
                            }
                            val contentUriPrefixesToTry = arrayOf(
                                    "content://downloads/public_downloads",
                                    "content://downloads/my_downloads")
                            try {
                                for (contentUriPrefix in contentUriPrefixesToTry) {
                                    val contentUri = ContentUris.withAppendedId(Uri.parse(contentUriPrefix), java.lang.Long.valueOf(id))
                                    /*   final Uri contentUri = ContentUris.withAppendedId(
                                    Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));*/
                                    if (contentUri.encodedPath!!.isNotEmpty()) {
                                        return getDataColumn(context, contentUri, null, null)
                                    }
                                }
                            } catch (e: NumberFormatException) { //In Android 8 and Android P the id is not a number
                                return uri.path!!.replaceFirst("^/document/raw:".toRegex(), "").replaceFirst("^raw:".toRegex(), "")
                            }
                        }
                    } else {
                        val id = DocumentsContract.getDocumentId(uri)
                        if (id.startsWith("raw:")) {
                            return id.replaceFirst("raw:".toRegex(), "")
                        }
                        try {
                            contentUri = ContentUris.withAppendedId(
                                    Uri.parse("content://downloads/public_downloads"), java.lang.Long.valueOf(id))
                        } catch (e: NumberFormatException) {
                            e.printStackTrace()
                        }
                        if (contentUri != null) {
                            return getDataColumn(context, contentUri, null, null)
                        }
                    }
                } else if (isMediaDocument(uri)) {
                    val docId = DocumentsContract.getDocumentId(uri)
                    val split = docId.split(":").toTypedArray()
                    val type = split[0]
                    var contentUri: Uri? = null
                    when (type) {
                        "image" -> {
                            contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                        }
                        "video" -> {
                            contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI
                        }
                        "audio" -> {
                            contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
                        }
                    }
                    selection = "_id=?"
                    selectionArgs = arrayOf(split[1])
                    return getDataColumn(context, contentUri, selection,
                            selectionArgs)
                } else if (isGoogleDriveUri(uri)) {
                    return getDriveFilePath(uri, context)
                }
            } else if ("content".equals(uri.scheme, ignoreCase = true)) {
                if (isGooglePhotosUri(uri)) {
                    return uri.lastPathSegment
                }
                if (isGoogleDriveUri(uri)) {
                    return getDriveFilePath(uri, context)
                }
                return if (Build.VERSION.SDK_INT == Build.VERSION_CODES.N) { // return getFilePathFromURI(context,uri);
                    getMediaFilePathForN(uri, context)
                    // return getRealPathFromURI(context,uri);
                } else {
                    getDataColumn(context, uri, null, null)
                }
            } else if ("file".equals(uri.scheme, ignoreCase = true)) {
                return uri.path
            }
        }
        return null
    }

    /**
     * Check if a file exists on device
     *
     * @param filePath The absolute file path
     */
    private fun fileExists(filePath: String): Boolean {
        val file = File(filePath)
        return file.exists()
    }

    /**
     * Get full file path from external storage
     *
     * @param pathData The storage type and the relative path
     */
    private fun getPathFromExtSD(pathData: Array<String>): String {
        val type = pathData[0]
        val relativePath = "/" + pathData[1]
        var fullPath: String
        // on my Sony devices (4.4.4 & 5.1.1), `type` is a dynamic string
// something like "71F8-2C0A", some kind of unique id per storage
// don't know any API that can get the root path of that storage based on its id.
//
// so no "primary" type, but let the check here for other devices
        if ("primary".equals(type, ignoreCase = true)) {
            fullPath = removeLastChar(AndroidExternalStoragePermission.getExternalPath(context)) + relativePath
            if (fileExists(fullPath)) {
                return fullPath
            }
        }
        // Environment.isExternalStorageRemovable() is `true` for external and internal storage
// so we cannot relay on it.
//
// instead, for each possible path, check if file exists
// we'll start with secondary storage as this could be our (physically) removable sd card
        fullPath = System.getenv("SECONDARY_STORAGE") + relativePath
        if (fileExists(fullPath)) {
            return fullPath
        }
        fullPath = System.getenv("EXTERNAL_STORAGE") + relativePath
        return if (fileExists(fullPath)) {
            fullPath
        } else fullPath
    }


    private fun getDriveFilePath(uri: Uri, context: Context): String {
        val returnCursor = context.contentResolver.query(uri, null, null, null, null)!!
        val nameIndex = returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
        returnCursor.moveToFirst()
        val name = returnCursor.getString(nameIndex)
        val file = File(context.cacheDir, name)
        try {
            val inputStream = context.contentResolver.openInputStream(uri)
            val outputStream = FileOutputStream(file)
            var read: Int
            val maxBufferSize = 1024 * 1024
            assert(inputStream != null)
            val bytesAvailable = inputStream!!.available()
            //int bufferSize = 1024;
            val bufferSize = min(bytesAvailable, maxBufferSize)
            val buffers = ByteArray(bufferSize)
            while (inputStream.read(buffers).also { read = it } != -1) {
                outputStream.write(buffers, 0, read)
            }
            inputStream.close()
            outputStream.close()

        } catch (ignore: Exception) {
        }
        return file.path
    }

    private fun getMediaFilePathForN(uri: Uri, context: Context): String {
        val returnCursor = context.contentResolver.query(uri, null, null, null, null)!!
        val nameIndex = returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
        returnCursor.moveToFirst()
        val name = returnCursor.getString(nameIndex)
        val file = File(context.filesDir, name)
        try {
            val inputStream = context.contentResolver.openInputStream(uri)
            val outputStream = FileOutputStream(file)
            var read: Int
            val maxBufferSize = 1024 * 1024
            assert(inputStream != null)
            val bytesAvailable = inputStream!!.available()
            //int bufferSize = 1024;
            val bufferSize = min(bytesAvailable, maxBufferSize)
            val buffers = ByteArray(bufferSize)
            while (inputStream.read(buffers).also { read = it } != -1) {
                outputStream.write(buffers, 0, read)
            }
            inputStream.close()
            outputStream.close()
        } catch (ignored: Exception) {
        }
        return file.path
    }


    private fun getDataColumn(context: Context, uri: Uri?,
                              selection: String?, selectionArgs: Array<String>?): String? {
        val column = "_data"
        val projection = arrayOf(column)
        try {
            val cursor = context.contentResolver.query(uri!!, projection,
                    selection, selectionArgs, null)
            if (cursor != null && cursor.moveToFirst()) {
                val index = cursor.getColumnIndexOrThrow(column)
                return cursor.getString(index)
            }
        } catch (ignored: Exception) {
        }
        return null
    }

    /**
     * @param uri - The Uri to check.
     * @return - Whether the Uri authority is ExternalStorageProvider.
     */
    private fun isExternalStorageDocument(uri: Uri): Boolean {
        return "com.android.externalstorage.documents" == uri.authority
    }

    /**
     * @param uri - The Uri to check.
     * @return - Whether the Uri authority is DownloadsProvider.
     */
    private fun isDownloadsDocument(uri: Uri): Boolean {
        return "com.android.providers.downloads.documents" == uri.authority
    }

    /**
     * @param uri - The Uri to check.
     * @return - Whether the Uri authority is MediaProvider.
     */
    private fun isMediaDocument(uri: Uri): Boolean {
        return "com.android.providers.media.documents" == uri.authority
    }

    /**
     * @param uri - The Uri to check.
     * @return - Whether the Uri authority is Google Photos.
     */
    private fun isGooglePhotosUri(uri: Uri): Boolean {
        return "com.google.android.apps.photos.content" == uri.authority
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is Google Drive.
     */
    private fun isGoogleDriveUri(uri: Uri): Boolean {
        return "com.google.android.apps.docs.storage" == uri.authority || "com.google.android.apps.docs.storage.legacy" == uri.authority
    }

    @SuppressLint("SdCardPath")
    fun replaceSdCardPath(mContext: Context?, path: String): String {
        val spilt = "/sdcard/"

        if (path.startsWith(spilt)) {
            val sdCardPathSharedPreference = SdCardPathSharedPreference(mContext!!)
            return path.replace(spilt, sdCardPathSharedPreference.sdCardPath)
        }
        return path
    }

    private fun removeLastChar(s: String?): String? {
        var str = s
        if (str != null && str.isNotEmpty()) {
            str = str.substring(0, str.length - 1)
        }
        return str
    }

}