package com.mxt.anitrend.data.forum

import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.api.Optional
import com.mxt.anitrend.data.graphql.ThreadBrowseQuery
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

data class ThreadItem(
    val id: Long,
    val title: String,
    val body: String,
    val createdAt: Int,
    val replyCount: Int,
    val viewCount: Int,
    val userName: String,
    val userAvatarMedium: String?,
    val categories: List<String>,
)

interface ThreadRepository {
    fun observeThreads(page: Int = 1): Flow<List<ThreadItem>>
}

class ApolloThreadRepository(
    private val apolloClient: ApolloClient,
) : ThreadRepository {

    override fun observeThreads(page: Int): Flow<List<ThreadItem>> = flow {
        val response = apolloClient.query(
            ThreadBrowseQuery(
                page = Optional.present(page),
                perPage = Optional.present(20),
            )
        ).execute()

        val items = response.data?.Page?.threads
            ?.mapNotNull { thread ->
                thread?.let {
                    ThreadItem(
                        id = it.id.toLong(),
                        title = it.title.orEmpty(),
                        body = it.body.orEmpty(),
                        createdAt = it.createdAt,
                        replyCount = it.replyCount ?: 0,
                        viewCount = it.viewCount ?: 0,
                        userName = it.user?.name.orEmpty(),
                        userAvatarMedium = it.user?.avatar?.medium,
                        categories = it.categories?.mapNotNull { cat -> cat?.name }.orEmpty(),
                    )
                }
            }
            .orEmpty()

        emit(items)
    }
}
