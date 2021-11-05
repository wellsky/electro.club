package club.electro.workers

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import club.electro.di.DependencyContainer
import club.electro.repository.PostRepositoryServerImpl

class SavePostWorker(
    applicationContext: Context,
    params: WorkerParameters
) : CoroutineWorker(applicationContext, params) {

    companion object {
        const val localPostId = "localPostId"
    }

    override suspend fun doWork(): Result {
        val diContainer: DependencyContainer = DependencyContainer.getInstance()

        val id = inputData.getLong(localPostId, 0L)
        if (id == 0L) {
            return Result.failure()
        }

        val repository = PostRepositoryServerImpl(diContainer)

        return try {
            repository.savePostWork(id)
            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }
}