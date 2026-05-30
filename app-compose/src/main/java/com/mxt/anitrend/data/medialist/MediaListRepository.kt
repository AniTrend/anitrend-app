package com.mxt.anitrend.data.medialist

import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.api.Optional
import com.mxt.anitrend.data.graphql.DeleteMediaListEntryMutation
import com.mxt.anitrend.data.graphql.SaveMediaListEntryMutation
import com.mxt.anitrend.data.graphql.type.MediaListStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

interface MediaListRepository {
    fun saveEntry(mediaId: Int, status: String?, score: Double?, progress: Int?, progressVolumes: Int?): Flow<Unit>
    fun deleteEntry(id: Int): Flow<Unit>
}

class ApolloMediaListRepository(
    private val apolloClient: ApolloClient,
) : MediaListRepository {

    override fun saveEntry(mediaId: Int, status: String?, score: Double?, progress: Int?, progressVolumes: Int?): Flow<Unit> = flow {
        val mutation = SaveMediaListEntryMutation(
            mediaId = Optional.Present(mediaId),
            status = Optional.presentIfNotNull(status?.let { MediaListStatus.safeValueOf(it).takeUnless { it == MediaListStatus.UNKNOWN__ } }),
            score = Optional.presentIfNotNull(score),
            progress = Optional.presentIfNotNull(progress),
            progressVolumes = Optional.presentIfNotNull(progressVolumes),
        )
        apolloClient.mutation(mutation).execute()
        emit(Unit)
    }

    override fun deleteEntry(id: Int): Flow<Unit> = flow {
        val mutation = DeleteMediaListEntryMutation(id = id)
        apolloClient.mutation(mutation).execute()
        emit(Unit)
    }
}
