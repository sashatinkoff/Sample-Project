package com.isidroid.b21.domain

import android.content.ContentUris
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.os.Environment
import android.provider.BaseColumns
import android.provider.DocumentsContract
import android.provider.MediaStore
import android.webkit.MimeTypeMap
import com.isidroid.b21.utils.pics.MediaHelper
import com.isidroid.b21.utils.pics.MediaParserUtils
import java.io.File
import java.io.IOException
import java.io.InputStream
import java.util.*

class PictureInteractor(private val context: Context) : IPictureUseCase {
    private var imageInfo: String? = null

    override fun parse(uri: Uri): String {
        var cursor: Cursor? = null
        try {
            when {
                MediaHelper.isGooglePhotosUri(uri) -> googlePhotos(uri)
                MediaHelper.isGoogleDrive(uri) -> googleDrive(uri)
                MediaHelper.isExternalStorageDocument(uri) -> externalStorage(uri)
                MediaHelper.isDownloadsDocument(uri) -> cursor = downloadsDocument(uri)
                MediaHelper.isMediaDocument(uri) -> cursor = media(uri)
                MediaHelper.isChromeDownload(uri) -> chromeDownloads(uri)
            }

        } catch (e: Exception) {
            e.printStackTrace()
        }

        cursor?.close()
        return this.imageInfo ?: getLocal(uri)
    }

    private fun getColumn(cursor: Cursor, name: String) = cursor.getColumnIndex(name)

    @Throws(IOException::class)
    private fun googlePhotos(uri: Uri): Cursor? = googleDrive(uri)

    private fun chromeDownloads(uri: Uri): Cursor? {
        val projection = arrayOf(
            BaseColumns._ID,
            MediaStore.Files.FileColumns.DATA,
            MediaStore.Files.FileColumns.MEDIA_TYPE,
            MediaStore.Files.FileColumns.PARENT,
            MediaStore.Files.FileColumns._ID

        )

        val cursor: Cursor? = null
        try {
            getData(uri, null, projection)
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            cursor?.close()
        }
        return null
    }

    @Throws(IOException::class)
    private fun googleDrive(uri: Uri): Cursor? {
        val inputStream = context.contentResolver.openInputStream(uri)
        val projection = arrayOf(
            MediaStore.Images.ImageColumns.DISPLAY_NAME,
            MediaStore.Images.ImageColumns.DATE_TAKEN,
            MediaStore.Images.ImageColumns.DATA,
            MediaStore.Files.FileColumns.MIME_TYPE
        )

        val cursor = getData(uri, null, projection)
        var filename = UUID.randomUUID().toString().substring(0, 5)

        if (cursor?.moveToFirst() == true) {
            imageInfo = cursor.getString(getColumn(cursor, projection[2]))

            val mimeType = cursor.getString(getColumn(cursor, projection[3]))
            MimeTypeMap.getSingleton().getExtensionFromMimeType(mimeType)

            filename = cursor.getString(getColumn(cursor, projection[0]))
        }

        // the file is not stored locally, then download it to the device
        if (imageInfo == null) {
            try {
                val file = File(context.cacheDir, filename)
                inputStream.use { input -> file.outputStream().use { input?.copyTo(it) } }
                imageInfo = file.absolutePath

            } catch (e: Exception) {
            } finally {
                closeStream(inputStream)
            }
        }

        return cursor
    }

    private fun closeStream(inputStream: InputStream?) {
        try {
            inputStream?.close()
        } catch (e: Exception) {
        }
    }

    @Throws(Exception::class)
    private fun media(uri: Uri): Cursor? {
        var cursor: Cursor? = null

        val docId = DocumentsContract.getDocumentId(uri)
        val split = docId.split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        val type = split[0]
        var contentUri: Uri? = null
        val projection = arrayOf(
            MediaStore.Images.ImageColumns.DATA,
            MediaStore.Images.ImageColumns.DATE_TAKEN,
            MediaStore.Images.ImageColumns.DISPLAY_NAME
        )

        if ("image" == type)
            contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI

        if (contentUri != null)
            cursor = getData(contentUri, split[1], projection)

        if (cursor?.moveToFirst() == true) {
            imageInfo = cursor.getString(getColumn(cursor, projection[0]))
        }

        return cursor
    }

    private fun externalStorage(uri: Uri) {
        val docId = DocumentsContract.getDocumentId(uri)
        val split = docId.split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        val type = split[0]

        if ("primary".equals(type, ignoreCase = true)) {
            imageInfo = Environment.getExternalStorageDirectory().toString() + "/" + split[1]
        }
    }

    private fun downloadsDocument(uri: Uri): Cursor? {
        val cursor: Cursor?
        val id = DocumentsContract.getDocumentId(uri)

        return try {
            val contentUri =
                ContentUris.withAppendedId(
                    Uri.parse("content://downloads/public_downloads"),
                    id.toLong()
                )

            val projection = arrayOf("_data", "lastmod")
            cursor = getData(contentUri, id, projection)
            if (cursor?.moveToFirst() == true) {
                imageInfo = cursor.getString(getColumn(cursor, projection[0]))
            }

            cursor
        } catch (e: Exception) {
            e.printStackTrace()
            imageInfo = getRaw(id)
            null
        }
    }

    private fun getRaw(id: String): String? {
        val uri = Uri.parse(id)
        val path = uri.path
        val file = File(path)

        return file.absolutePath
    }

    private fun getData(uri: Uri, id: String?, projection: Array<String>): Cursor? {
        val selection = "_id=?"
        val selectionArgs = arrayOf(id ?: "")
        return context.contentResolver.query(uri, projection, selection, selectionArgs, null)
    }

    private fun getLocal(uri: Uri): String {
        val filepath = MediaParserUtils.getPath(context, uri)
        val file = File(filepath ?: "")
        return file.absolutePath
    }

}