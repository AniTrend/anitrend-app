package com.mxt.anitrend.ui.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mxt.anitrend.data.media.MediaCharacter
import com.mxt.anitrend.data.media.MediaRelation
import com.mxt.anitrend.data.media.MediaRepository
import com.mxt.anitrend.data.media.MediaSocialItem
import com.mxt.anitrend.data.media.MediaStaffMember
import com.mxt.anitrend.data.media.Ranking
import com.mxt.anitrend.data.media.RecommendationItem
import com.mxt.anitrend.data.media.ScoreDistribution
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch

enum class MediaTab { Overview, Characters, Staff, Relations, Stats, Social, Recommendations }

class MediaDetailViewModel(
    private val mediaRepository: MediaRepository,
    private val mediaId: Int,
) : ViewModel() {

    private val _uiState = MutableStateFlow<MediaDetailUiState>(MediaDetailUiState.Loading)
    val uiState: StateFlow<MediaDetailUiState> = _uiState

    private val _selectedTab = MutableStateFlow(MediaTab.Overview)
    val selectedTab: StateFlow<MediaTab> = _selectedTab

    private val _characters = MutableStateFlow<List<MediaCharacter>>(emptyList())
    val characters: StateFlow<List<MediaCharacter>> = _characters

    private val _staff = MutableStateFlow<List<MediaStaffMember>>(emptyList())
    val staff: StateFlow<List<MediaStaffMember>> = _staff

    private val _relations = MutableStateFlow<List<MediaRelation>>(emptyList())
    val relations: StateFlow<List<MediaRelation>> = _relations

    private val _scoreDistribution = MutableStateFlow<List<ScoreDistribution>>(emptyList())
    val scoreDistribution: StateFlow<List<ScoreDistribution>> = _scoreDistribution

    private val _rankings = MutableStateFlow<List<Ranking>>(emptyList())
    val rankings: StateFlow<List<Ranking>> = _rankings

    private val _social = MutableStateFlow<List<MediaSocialItem>>(emptyList())
    val social: StateFlow<List<MediaSocialItem>> = _social

    private val _recommendations = MutableStateFlow<List<RecommendationItem>>(emptyList())
    val recommendations: StateFlow<List<RecommendationItem>> = _recommendations

    init {
        loadMedia()
    }

    fun selectTab(tab: MediaTab) {
        _selectedTab.value = tab
        when (tab) {
            MediaTab.Characters -> if (_characters.value.isEmpty()) loadCharacters()
            MediaTab.Staff -> if (_staff.value.isEmpty()) loadStaff()
            MediaTab.Relations -> if (_relations.value.isEmpty()) loadRelations()
            MediaTab.Stats -> if (_scoreDistribution.value.isEmpty()) loadStats()
            MediaTab.Social -> if (_social.value.isEmpty()) loadSocial()
            MediaTab.Recommendations -> if (_recommendations.value.isEmpty()) loadRecommendations()
            else -> {}
        }
    }

    private fun loadMedia() {
        viewModelScope.launch {
            mediaRepository.observeMedia(mediaId)
                .onStart { _uiState.value = MediaDetailUiState.Loading }
                .catch { e -> _uiState.value = MediaDetailUiState.Error(e.message ?: "Unknown error") }
                .collect { media ->
                    if (media != null) _uiState.value = MediaDetailUiState.Success(media)
                    else _uiState.value = MediaDetailUiState.Error("Media not found")
                }
        }
    }

    private fun loadCharacters() {
        viewModelScope.launch {
            mediaRepository.observeCharacters(mediaId).collect { _characters.value = it }
        }
    }

    private fun loadStaff() {
        viewModelScope.launch {
            mediaRepository.observeStaff(mediaId).collect { _staff.value = it }
        }
    }

    private fun loadRelations() {
        viewModelScope.launch {
            mediaRepository.observeRelations(mediaId).collect { _relations.value = it }
        }
    }

    private fun loadStats() {
        viewModelScope.launch {
            mediaRepository.observeStats(mediaId).collect { (scores, ranks) ->
                _scoreDistribution.value = scores
                _rankings.value = ranks
            }
        }
    }

    private fun loadSocial() {
        viewModelScope.launch {
            mediaRepository.observeSocial(mediaId).collect { _social.value = it }
        }
    }

    private fun loadRecommendations() {
        viewModelScope.launch {
            mediaRepository.observeRecommendations(mediaId).collect { _recommendations.value = it }
        }
    }
}
