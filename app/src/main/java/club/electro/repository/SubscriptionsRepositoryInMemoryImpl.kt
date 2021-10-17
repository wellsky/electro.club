package club.electro.repository

import androidx.lifecycle.MutableLiveData
import club.electro.dto.FeedPost
import club.electro.dto.SubscriptionArea
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class SubscriptionsRepositoryInMemoryImpl : SubscriptionsRepository {
    override val data: Flow<List<SubscriptionArea>> = flow {
        emit (
            listOf(
                SubscriptionArea(
                    id = 1,
                    image = "https://electro.club/images/status-none.jpg",
                    name = "Название темы 1",
                    last_text = "Длинный текст который должен обрезаться при выводе в списке"
                ),
                SubscriptionArea(
                    id = 2,
                    image = "https://electro.club/images/status-none.jpg",
                    name = "Название темы 2",
                    last_text = "Длинный текст который должен обрезаться при выводе в списке"
                ),
                SubscriptionArea(
                    id = 3,
                    image = "https://electro.club/images/status-none.jpg",
                    name = "Название темы 3",
                    last_text = "Длинный текст который должен обрезаться при выводе в списке"
                ),
                SubscriptionArea(
                    id = 4,
                    image = "https://electro.club/images/status-none.jpg",
                    name = "Название темы 4",
                    last_text = "Длинный текст который должен обрезаться при выводе в списке"
                ),
                SubscriptionArea(
                    id = 5,
                    image = "https://electro.club/images/status-none.jpg",
                    name = "Название темы 5",
                    last_text = "Длинный текст который должен обрезаться при выводе в списке"
                ),
                SubscriptionArea(
                    id = 6,
                    image = "https://electro.club/images/status-none.jpg",
                    name = "Название темы 6",
                    last_text = "Длинный текст который должен обрезаться при выводе в списке"
                ),
                SubscriptionArea(
                    id = 7,
                    image = "https://electro.club/images/status-none.jpg",
                    name = "Название темы 7",
                    last_text = "Длинный текст который должен обрезаться при выводе в списке"
                ),
                SubscriptionArea(
                    id = 8,
                    image = "https://electro.club/images/status-none.jpg",
                    name = "Название темы 8",
                    last_text = "Длинный текст который должен обрезаться при выводе в списке"
                ),
                SubscriptionArea(
                    id = 9,
                    image = "https://electro.club/images/status-none.jpg",
                    name = "Название темы 9",
                    last_text = "Длинный текст который должен обрезаться при выводе в списке"
                ),
                SubscriptionArea(
                    id = 10,
                    image = "https://electro.club/images/status-none.jpg",
                    name = "Название темы 10",
                    last_text = "Длинный текст который должен обрезаться при выводе в списке"
                ),
                SubscriptionArea(
                    id = 11,
                    image = "https://electro.club/images/status-none.jpg",
                    name = "Название темы 11",
                    last_text = "Длинный текст который должен обрезаться при выводе в списке"
                ),
                SubscriptionArea(
                    id = 12,
                    image = "https://electro.club/images/status-none.jpg",
                    name = "Название темы 12",
                    last_text = "Длинный текст который должен обрезаться при выводе в списке"
                ),
                SubscriptionArea(
                    id = 13,
                    image = "https://electro.club/images/status-none.jpg",
                    name = "Название темы 13",
                    last_text = "Длинный текст который должен обрезаться при выводе в списке"
                ),
            )
        )
    }


    override suspend fun getAll() {

    }
}