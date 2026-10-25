package com.mxt.anitrend.viewmodel

import com.mxt.anitrend.model.entity.base.UserBase
import org.junit.Assert.assertEquals
import org.junit.Test

class UserSheetFilterViewModelTest {

    @Test
    fun `query matches user names case insensitively by containment`() {
        val viewModel = viewModel(user(id = 1L, name = "MiXeD User"))

        viewModel.setQuery("XED")

        assertEquals(listOf(1L), viewModel.filteredRows.value.map { it.model.id })
    }

    @Test
    fun `query with no matching name returns no rows`() {
        val viewModel = viewModel(user(id = 1L, name = "Other User"))

        viewModel.setQuery("missing")

        assertEquals(emptyList<Long>(), viewModel.filteredRows.value.map { it.model.id })
    }

    @Test
    fun `nonempty query excludes users with null names`() {
        val viewModel = viewModel(user(id = 1L, name = null))

        viewModel.setQuery("user")

        assertEquals(emptyList<Long>(), viewModel.filteredRows.value.map { it.model.id })
    }

    @Test
    fun `clearing query restores all source rows`() {
        val viewModel = viewModel(
            user(id = 1L, name = "Target User"),
            user(id = 2L, name = null),
        )

        viewModel.setQuery("target")
        viewModel.setQuery("")

        assertEquals(listOf(1L, 2L), viewModel.filteredRows.value.map { it.model.id })
    }

    private fun viewModel(vararg users: UserBase) = BottomSheetUsersViewModel().apply {
        setSourceUsers(users.toList())
    }

    private fun user(id: Long, name: String?): UserBase = UserBase(name = name).apply {
        this.id = id
    }
}
