package pwr.barwa.chat.services

import android.content.Context
import android.net.Uri
import android.os.Environment
import androidx.core.content.FileProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import pwr.barwa.chat.data.AppDatabase
import pwr.barwa.chat.data.model.MediaType
import pwr.barwa.chat.data.model.Message
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.UUID

class MediaStorageService(private val context: Context) {

    private val messageDao = AppDatabase.getInstance(context).messageDao()
    private val client = OkHttpClient()


    suspend fun downloadAndSaveMedia(message: Message): Result<String> = withContext(Dispatchers.IO) {
        try {
            val mediaUrl = message.mediaUrl ?: return@withContext Result.failure(
                IllegalArgumentException("Media URL nie może być null")
            )

            val fileExtension = message.mediaExtension

            val fileName = "media_${message.id}_${UUID.randomUUID()}$fileExtension"

            val mediaDir = when (message.mediaType) {
                MediaType.IMAGE -> getMediaDirectory("Images")
                MediaType.VIDEO -> getMediaDirectory("Videos")
                MediaType.AUDIO -> getMediaDirectory("Audio")
                MediaType.FILE, null -> getMediaDirectory("Files")
            }

            if (!mediaDir.exists()) {
                mediaDir.mkdirs()
            }

            val destinationFile = File(mediaDir, fileName)

            val request = Request.Builder().url(mediaUrl).build()
            val response = client.newCall(request).execute()

            if (!response.isSuccessful) {
                return@withContext Result.failure(
                    IOException("Pobieranie pliku nie powiodło się: ${response.code}")
                )
            }

            response.body?.let { body ->
                FileOutputStream(destinationFile).use { output ->
                    body.byteStream().use { input ->
                        input.copyTo(output)
                    }
                }
            } ?: return@withContext Result.failure(
                IOException("Puste ciało odpowiedzi")
            )

            val localPath = destinationFile.absolutePath
            messageDao.updateLocalMediaPath(message.id, localPath)

            Result.success(localPath)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }


    fun getMediaUri(localPath: String): Uri? {
        val file = File(localPath)
        if (!file.exists()) return null

        return FileProvider.getUriForFile(
            context,
            "pwr.barwa.chat.fileprovider",
            file
        )
    }


    suspend fun getPendingMediaDownloads(): List<Message> {
        return messageDao.getMessagesWithPendingMediaDownload()
    }


    fun downloadAllPendingMedia(): Flow<MediaDownloadProgress> = flow {
        val pendingMessages = getPendingMediaDownloads()
        val totalCount = pendingMessages.size
        var successCount = 0
        var failureCount = 0

        emit(MediaDownloadProgress(0, totalCount, successCount, failureCount, null))

        pendingMessages.forEachIndexed { index, message ->
            val result = downloadAndSaveMedia(message)

            if (result.isSuccess) {
                successCount++
            } else {
                failureCount++
            }

            emit(
                MediaDownloadProgress(
                    index + 1,
                    totalCount,
                    successCount,
                    failureCount,
                    result.exceptionOrNull()?.message
                )
            )
        }
    }


    private fun getMediaDirectory(subDir: String): File {
        val baseDir = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
            ?: context.filesDir
        return File(baseDir, "ChattApp/$subDir")
    }


    data class MediaDownloadProgress(
        val current: Int,
        val total: Int,
        val successCount: Int,
        val failureCount: Int,
        val lastError: String?
    ) {
        val progressPercent: Int
            get() = if (total > 0) (current * 100 / total) else 0
    }
}

