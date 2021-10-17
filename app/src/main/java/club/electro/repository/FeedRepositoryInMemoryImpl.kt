package club.electro.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asFlow
import club.electro.dto.FeedPost
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

class FeedRepositoryInMemoryImpl: FeedRepository {
    override val data: Flow<List<FeedPost>> = flow {
        emit(
            listOf (
                FeedPost(
                    id = 1,
                    title = "Post 1",
                    image = "https://electro.club/data/forum/messages/219561/images/60052-1280.jpg",
                    text = "Post text 1"
                ),
                FeedPost(
                    id = 2,
                    title = "Title 2",
                    image = "https://electro.club/data/users/4216/files/Attachment-1.jpeg",
                    text = "Post text 2"
                ),
                FeedPost(
                    id = 3,
                    title = "Title 3",
                    image = "https://electro.club/data/forum/messages/135242/images/41216-1280.jpg",
                    text = "Post text 3"
                ),
                FeedPost(
                    id = 4,
                    title = "Title 4",
                    image = "https://electro.club/data/forum/messages/191676/images/54433-1280-h480.jpg",
                    text = "Post text 4"
                ),
            )
        )
    }


    override fun getAll() {

    }
}