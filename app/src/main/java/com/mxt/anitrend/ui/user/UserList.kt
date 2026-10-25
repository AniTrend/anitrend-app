package com.mxt.anitrend.ui.user

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.integerResource
import com.mxt.anitrend.R
import com.mxt.anitrend.base.custom.view.widget.FollowStateWidget
import com.mxt.anitrend.model.entity.base.UserBase

/** Stateless responsive list host for user rows. Paging and state remain with each owner. */
@Composable
internal fun UserList(
    items: List<UserRowUiState>,
    listState: LazyGridState = rememberLazyGridState(),
    modifier: Modifier = Modifier,
    currentUser: UserBase?,
    onUserClick: (Long) -> Unit,
    onToggleFollow: (Long) -> Unit,
) {
    val followListener = remember(onToggleFollow) {
        FollowStateWidget.Listener(onToggleFollow)
    }

    LazyVerticalGrid(
        columns = GridCells.Fixed(integerResource(R.integer.single_list_x1)),
        modifier = modifier.fillMaxSize(),
        state = listState,
    ) {
        items(
            items = items,
            key = { it.model.id },
        ) { item ->
            Box(modifier = Modifier.padding(bottom = dimensionResource(R.dimen.md_margin))) {
                UserRow(
                    state = item,
                    currentUser = currentUser,
                    followListener = followListener,
                    onClick = { onUserClick(item.model.id) },
                )
            }
        }
    }
}
