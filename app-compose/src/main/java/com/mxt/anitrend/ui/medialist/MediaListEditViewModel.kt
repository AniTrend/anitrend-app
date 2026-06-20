package com.mxt.anitrend.ui.medialist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mxt.anitrend.data.medialist.MediaListRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

sealed class MediaListEditEvent {
    data object Saved : MediaListEditEvent()
    data object Deleted : MediaListEditEvent()
    data class Error(val message: String) : MediaListEditEvent()
}

class MediaListEditViewModel(
    private val mediaId: Long,
    private val repository: MediaListRepository,
) : ViewModel() {

    private val _events = MutableSharedFlow<MediaListEditEvent>()
    val events: SharedFlow<MediaListEditEvent> = _events.asSharedFlow()

    fun saveEntry(status: String?, score: Double?, progress: Int?, progressVolumes: Int?) {
        viewModelScope.launch {
            try {
                repository.saveEntry(mediaId.toInt(), status, score, progress, progressVolumes).collect {}
                _events.emit(MediaListEditEvent.Saved)
            } catch (e: Exception) {
                _events.emit(MediaListEditEvent.Error(e.message ?: "Failed to save"))
            }
        }
    }

    fun deleteEntry(listEntryId: Int) {
        viewModelScope.launch {
            try {
                repository.deleteEntry(listEntryId).collect {}
                _events.emit(MediaListEditEvent.Deleted)
            } catch (e: Exception) {
                _events.emit(MediaListEditEvent.Error(e.message ?: "Failed to delete"))
            }
        }
    }
}
