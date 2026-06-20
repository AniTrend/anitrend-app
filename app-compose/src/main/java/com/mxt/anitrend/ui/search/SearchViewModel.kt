package com.mxt.anitrend.ui.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mxt.anitrend.data.search.CharacterResult
import com.mxt.anitrend.data.search.SearchRepository
import com.mxt.anitrend.data.search.SearchResult
import com.mxt.anitrend.data.search.StaffResult
import com.mxt.anitrend.data.search.StudioResult
import com.mxt.anitrend.data.search.UserResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch

enum class SearchType { Media, Characters, Staff, Studios, Users }

sealed class TypedSearchUiState<out T> {
    data object Idle : TypedSearchUiState<Nothing>()
    data object Loading : TypedSearchUiState<Nothing>()
    data class Results<T>(val items: List<T>) : TypedSearchUiState<T>()
    data class Error(val message: String) : TypedSearchUiState<Nothing>()
}

class SearchViewModel(
    private val searchRepository: SearchRepository,
) : ViewModel() {

    private val _selectedType = MutableStateFlow(SearchType.Media)
    val selectedType: StateFlow<SearchType> = _selectedType

    private val _mediaState = MutableStateFlow<TypedSearchUiState<SearchResult>>(TypedSearchUiState.Idle)
    val mediaState: StateFlow<TypedSearchUiState<SearchResult>> = _mediaState

    private val _characterState = MutableStateFlow<TypedSearchUiState<CharacterResult>>(TypedSearchUiState.Idle)
    val characterState: StateFlow<TypedSearchUiState<CharacterResult>> = _characterState

    private val _staffState = MutableStateFlow<TypedSearchUiState<StaffResult>>(TypedSearchUiState.Idle)
    val staffState: StateFlow<TypedSearchUiState<StaffResult>> = _staffState

    private val _studioState = MutableStateFlow<TypedSearchUiState<StudioResult>>(TypedSearchUiState.Idle)
    val studioState: StateFlow<TypedSearchUiState<StudioResult>> = _studioState

    private val _userState = MutableStateFlow<TypedSearchUiState<UserResult>>(TypedSearchUiState.Idle)
    val userState: StateFlow<TypedSearchUiState<UserResult>> = _userState

    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query

    fun onQueryChange(newQuery: String) {
        _query.value = newQuery
        if (newQuery.length >= 3) {
            searchCurrentType(newQuery)
        } else if (newQuery.isEmpty()) {
            clearAll()
        }
    }

    fun selectType(type: SearchType) {
        _selectedType.value = type
        if (_query.value.length >= 3) {
            searchCurrentType(_query.value)
        }
    }

    private fun searchCurrentType(query: String) {
        when (_selectedType.value) {
            SearchType.Media -> searchMedia(query)
            SearchType.Characters -> searchCharacters(query)
            SearchType.Staff -> searchStaff(query)
            SearchType.Studios -> searchStudios(query)
            SearchType.Users -> searchUsers(query)
        }
    }

    private fun searchMedia(query: String) {
        viewModelScope.launch {
            searchRepository.search(query)
                .onStart { _mediaState.value = TypedSearchUiState.Loading }
                .catch { e -> _mediaState.value = TypedSearchUiState.Error(e.message ?: "Unknown error") }
                .collect { items ->
                    _mediaState.value = if (items.isEmpty()) {
                        TypedSearchUiState.Error("No results found")
                    } else {
                        TypedSearchUiState.Results(items)
                    }
                }
        }
    }

    private fun searchCharacters(query: String) {
        viewModelScope.launch {
            searchRepository.searchCharacters(query)
                .onStart { _characterState.value = TypedSearchUiState.Loading }
                .catch { e -> _characterState.value = TypedSearchUiState.Error(e.message ?: "Unknown error") }
                .collect { items ->
                    _characterState.value = if (items.isEmpty()) {
                        TypedSearchUiState.Error("No results found")
                    } else {
                        TypedSearchUiState.Results(items)
                    }
                }
        }
    }

    private fun searchStaff(query: String) {
        viewModelScope.launch {
            searchRepository.searchStaff(query)
                .onStart { _staffState.value = TypedSearchUiState.Loading }
                .catch { e -> _staffState.value = TypedSearchUiState.Error(e.message ?: "Unknown error") }
                .collect { items ->
                    _staffState.value = if (items.isEmpty()) {
                        TypedSearchUiState.Error("No results found")
                    } else {
                        TypedSearchUiState.Results(items)
                    }
                }
        }
    }

    private fun searchStudios(query: String) {
        viewModelScope.launch {
            searchRepository.searchStudios(query)
                .onStart { _studioState.value = TypedSearchUiState.Loading }
                .catch { e -> _studioState.value = TypedSearchUiState.Error(e.message ?: "Unknown error") }
                .collect { items ->
                    _studioState.value = if (items.isEmpty()) {
                        TypedSearchUiState.Error("No results found")
                    } else {
                        TypedSearchUiState.Results(items)
                    }
                }
        }
    }

    private fun searchUsers(query: String) {
        viewModelScope.launch {
            searchRepository.searchUsers(query)
                .onStart { _userState.value = TypedSearchUiState.Loading }
                .catch { e -> _userState.value = TypedSearchUiState.Error(e.message ?: "Unknown error") }
                .collect { items ->
                    _userState.value = if (items.isEmpty()) {
                        TypedSearchUiState.Error("No results found")
                    } else {
                        TypedSearchUiState.Results(items)
                    }
                }
        }
    }

    private fun clearAll() {
        _mediaState.value = TypedSearchUiState.Idle
        _characterState.value = TypedSearchUiState.Idle
        _staffState.value = TypedSearchUiState.Idle
        _studioState.value = TypedSearchUiState.Idle
        _userState.value = TypedSearchUiState.Idle
    }
}
