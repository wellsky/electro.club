package club.electro.workers

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import club.electro.repository.post.PostRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class SavePostWorker @AssistedInject constructor(
    @Assisted applicationContext: Context,
    @Assisted params: WorkerParameters,
    private val repository: PostRepository
) : CoroutineWorker(applicationContext, params) {

    companion object {
        const val localPostId = "localPostId"
    }

    override suspend fun doWork(): Result {
        val id = inputData.getLong(localPostId, 0L)
        if (id == 0L) {
            return Result.failure()
        }

        return try {
            repository.savePostWork(id)
            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }
}