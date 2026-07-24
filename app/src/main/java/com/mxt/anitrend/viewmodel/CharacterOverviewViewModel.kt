package com.mxt.anitrend.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.anitrend.retrofit.graphql.model.attribute.GraphError
import com.mxt.anitrend.graphql.generated.CharacterOverview
import com.mxt.anitrend.model.api.retro.anilist.CharacterModel
import com.mxt.anitrend.model.entity.anilist.MediaCharacter
import com.mxt.anitrend.util.graphql.apiError
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber

class CharacterOverviewViewModel(
    private val characterService: CharacterModel,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
) : ViewModel() {

    sealed interface UiState {
        data object Loading : UiState
        data class Success(val character: MediaCharacter) : UiState
        data class Error(val message: String) : UiState
    }

    private val _state = MutableStateFlow<UiState>(UiState.Loading)
    val state: StateFlow<UiState> = _state.asStateFlow()

    private var loadedOnce = false

    /**
     * Loads the character overview by AniList ID. After the first successful load,
     * subsequent calls are ignored until a new ViewModel instance is created.
     * Failed loads remain retryable.
     */
    fun load(characterId: Long) {
        if (loadedOnce) return
        viewModelScope.launch {
            _state.value = UiState.Loading
            runCatching {
                withContext(ioDispatcher) {
                    val request = CharacterOverview.request(characterId.toInt(), asHtml = false)
                    val response = characterService.getCharacterOverview(request).execute()
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
                Timber.e(throwable, "CharacterOverviewViewModel load failed")
                _state.value = UiState.Error(
                    throwable.message ?: "Failed to load character overview",
                )
            }
        }
    }
}
