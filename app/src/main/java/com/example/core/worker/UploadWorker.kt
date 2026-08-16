package com.example.core.worker

import android.content.Context
import android.net.Uri
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.google.firebase.storage.FirebaseStorage
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.tasks.await

@HiltWorker
class UploadWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val storage: FirebaseStorage
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        val fileUriString = inputData.getString(KEY_FILE_URI) ?: return Result.failure()
        val destinationPath = inputData.getString(KEY_DESTINATION_PATH) ?: return Result.failure()
        
        return try {
            val fileUri = Uri.parse(fileUriString)
            val storageRef = storage.reference.child(destinationPath)
            
            storageRef.putFile(fileUri).await()
            val downloadUrl = storageRef.downloadUrl.await().toString()
            
            val outputData = workDataOf(KEY_DOWNLOAD_URL to downloadUrl)
            Result.success(outputData)
        } catch (e: Exception) {
            if (runAttemptCount < 3) {
                Result.retry()
            } else {
                Result.failure(workDataOf(KEY_ERROR to (e.message ?: "Upload failed")))
            }
        }
    }

    companion object {
        const val KEY_FILE_URI = "file_uri"
        const val KEY_DESTINATION_PATH = "destination_path"
        const val KEY_DOWNLOAD_URL = "download_url"
        const val KEY_ERROR = "error"
    }
}
