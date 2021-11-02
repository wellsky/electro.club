package club.electro.workers

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import club.electro.di.DependencyContainer

class SavePostWorker(
    applicationContext: Context,
    params: WorkerParameters
) : CoroutineWorker(applicationContext, params) {

    companion object {
        const val postKey = "post"
    }

    override suspend fun doWork(): Result {
        val diContainer: DependencyContainer = DependencyContainer(applicationContext)

        val id = inputData.getLong(postKey, 0L)
        if (id == 0L) {
            return Result.failure()
        }
        //val repository = ThreadRepositoryServerImpl(diContainer)

        return try {
            //repository.processWork(id)
            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }
}