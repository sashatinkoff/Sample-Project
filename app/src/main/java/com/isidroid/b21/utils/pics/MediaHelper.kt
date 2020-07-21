package com.isidroid.b21.utils.pics

import android.net.Uri

object MediaHelper {
    fun isExternalStorageDocument(uri: Uri) = "com.android.externalstorage.documents" == uri.authority
    fun isDownloadsDocument(uri: Uri) = "com.android.providers.downloads.documents" == uri.authority
    fun isChromeDownload(uri: Uri) = "com.android.chrome.FileProvider" == uri.authority
    fun isMediaDocument(uri: Uri) = "com.android.providers.media.documents" == uri.authority
    fun isGooglePhotosUri(uri: Uri) = isNewGooglePhotosUri(uri) || isOldGooglePhotosUri(
        uri
    )
    fun isGoogleDrive(uri: Uri) =
        "com.google.android.apps.docs.storage" == uri.authority || "com.google.android.apps.docs.storage.legacy" == uri.authority

    fun isGoogleDriveLegacy(uri: Uri) = "com.google.android.apps.docs.storage.legacy" == uri.authority
    private fun isNewGooglePhotosUri(uri: Uri) = "com.google.android.apps.photos.contentprovider" == uri.authority
    private fun isOldGooglePhotosUri(uri: Uri) = "com.google.android.apps.photos.content" == uri.authority
}