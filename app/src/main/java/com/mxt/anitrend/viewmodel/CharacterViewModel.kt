package com.mxt.anitrend.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.anitrend.retrofit.graphql.model.attribute.GraphError
import com.mxt.anitrend.graphql.generated.CharacterBase
import com.mxt.anitrend.model.api.retro.anilist.CharacterModel
import com.mxt.anitrend.repository.BaseRepository
import com.mxt.anitrend.util.graphql.apiError
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber

class CharacterViewModel(
    private val characterService: CharacterModel,
    private val baseRepository: BaseRepository,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
) : ViewModel() {

    sealed interface UiState {
        data object Loading : UiState
        data class Success(val character: com.mxt.anitrend.model.entity.base.CharacterBase) : UiState
        data class Error(val message: String) : UiState
    }

    private val _state = MutableStateFlow<UiState>(UiState.Loading)
    val state: StateFlow<UiState> = _state.asStateFlow()

    private var loadedOnce = false

    /**
     * Loads the character by its AniList ID. Safe to call multiple times -- skips
     * the network call after the first successful load.
     */
    fun load(characterId: Long) {
        if (loadedOnce) return
        viewModelScope.launch {
            _state.value = UiState.Loading
            runCatching {
                withContext(ioDispatcher) {
                    val request = CharacterBase.request(characterId.toInt())
                    val response = characterService.getCharacterBase(request).execute()
                    if (response.isSuccessful) {
                        val body = response.body()
                            ?: throw IllegalStateException("Empty response body")
                        val graphErrors: List<GraphError>? = body.errors
                        if (!graphErrors.isNullOrEmpty()) {
                            throw RuntimeException(
                                graphErrors.first().message
                                    ?: "GraphQL error",
                            )
                        }
                        body.data?.result
                            ?: throw IllegalStateException("Empty response body")
                    } else {
                        throw RuntimeException(response.apiError())
                    }
                }
            }.onSuccess { character ->
                _state.value = UiState.Success(character)
                loadedOnce = true
            }.onFailure { throwable ->
                Timber.e(throwable, "CharacterViewModel load failed")
                _state.value = UiState.Error(
                    throwable.message ?: "Failed to load character",
                )
            }
        }
    }

    suspend fun toggleFavourite(
        animeId: Int?,
        mangaId: Int?,
        characterId: Int?,
        staffId: Int?,
        studioId: Int?,
    ): Result<Unit> = withContext(ioDispatcher) {
        baseRepository.toggleFavourite(animeId, mangaId, characterId, staffId, studioId)
    }
}
