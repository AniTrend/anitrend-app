package com.mxt.anitrend.viewmodel

import androidx.lifecycle.ViewModel
import com.mxt.anitrend.domain.model.UserRecord
import com.mxt.anitrend.model.entity.base.UserBase
import com.mxt.anitrend.ui.user.UserRowUiState
import java.util.Locale
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class BottomSheetUsersViewModel : ViewModel() {

    private val _sourceRows = MutableStateFlow<List<UserRowUiState>>(emptyList())

    private val _query = MutableStateFlow("")
    internal val query: StateFlow<String> = _query.asStateFlow()

    private val _filteredRows = MutableStateFlow<List<UserRowUiState>>(emptyList())
    internal val filteredRows: StateFlow<List<UserRowUiState>> = _filteredRows.asStateFlow()

    fun setSourceUsers(users: List<UserBase>) {
        _sourceRows.value = users.map { user ->
            UserRowUiState(
                model = user,
                name = user.name,
                avatar = user.avatar,
                isFollowing = user.isFollowing,
            )
        }
        updateFilteredRows()
    }

    fun setQuery(query: String?) {
        _query.value = query.orEmpty()
        updateFilteredRows()
    }

    /** Converges matching projections only after their authoritative store record commits. */
    fun rebindUserIfPresent(record: UserRecord): Boolean {
        val rows = _sourceRows.value
        var updatedRows: MutableList<UserRowUiState>? = null

        for (index in rows.indices) {
            val row = rows[index]
            if (row.model.id != record.id) continue

            row.model.isFollowing = record.isFollowing
            if (row.isFollowing != record.isFollowing) {
                val updated = updatedRows ?: rows.toMutableList().also { updatedRows = it }
                updated[index] = row.copy(isFollowing = record.isFollowing)
            }
        }

        val updated = updatedRows ?: return false
        _sourceRows.value = updated
        updateFilteredRows()
        return true
    }

    private fun updateFilteredRows() {
        val rows = _sourceRows.value
        val filter = _query.value.lowercase(Locale.getDefault())
        _filteredRows.value =
            if (filter.isBlank()) {
                rows
            } else {
                rows.filter { row ->
                    row.name?.lowercase(Locale.getDefault())?.contains(filter) == true
                }
            }
    }
}
