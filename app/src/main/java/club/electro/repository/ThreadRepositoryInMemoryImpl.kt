package club.electro.repository

import android.app.Application
import club.electro.dto.Post
import club.electro.dto.SubscriptionArea
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class ThreadRepositoryInMemoryImpl(application: Application) : ThreadRepository {
    override val data: Flow<List<Post>> = flow {
        emit (
            listOf(
                Post(
                    id = 1,
                    threadId = 1,
                    authorId = 1,
                    authorName = "Author",
                    authorAvatar = "",
                    content = "По отношению к языку лингвистический термин «текст» представляет собой единство значимых единиц речи — предложений. Наша речь состоит не только из слов как минимальных значимых единиц, а из предложений, которые объединяются в высказывание и образуют более крупную единицу речи — текст. Единство предложений в тексте оформляется общим содержанием и грамматически. С этой точки зрения дадим следующее определение, что такое текст.",
                    published = 0,
                    likes = 0,
                    views = 1,
                ),
            )
        )
    }

    override suspend fun getThreadPosts() {

    }


}