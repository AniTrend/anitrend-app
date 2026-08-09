package com.mxt.anitrend.repository

import co.anitrend.retrofit.graphql.model.attribute.GraphError
import co.anitrend.retrofit.graphql.model.body.GraphContainer
import com.mxt.anitrend.graphql.generated.StaffBase
import com.mxt.anitrend.graphql.generated.StaffBaseData
import com.mxt.anitrend.model.api.retro.anilist.StaffService
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import retrofit2.Response

@OptIn(ExperimentalCoroutinesApi::class)
class StaffRepositoryTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private val service = mock(StaffService::class.java)
    private val repository = StaffRepository(
        staffService = service,
        ioDispatcher = testDispatcher,
    )

    @Test
    fun `getStaffBase success maps GraphContainer data to StaffRecord`() = runTest {
        val request = StaffBase.request(id = 5)
        `when`(service.getStaffBase(request)).thenReturn(
            Response.success(
                GraphContainer(
                    data = staffBaseData(),
                    errors = null,
                ),
            ),
        )

        val result = repository.getStaffBase(id = 5L)

        assertTrue(result.isSuccess)
        val staff = result.getOrThrow()
        assertEquals(5L, staff.id)
        assertEquals("Shinichiro Watanabe", staff.name)
        assertEquals("https://anilist.co/staff/5", staff.siteUrl)
        assertTrue(staff.isFavourite)
    }

    @Test
    fun `getStaffBase GraphQL error returns failed Result with message`() = runTest {
        val request = StaffBase.request(id = 5)
        `when`(service.getStaffBase(request)).thenReturn(
            Response.success(
                GraphContainer<StaffBaseData>(
                    data = null,
                    errors = listOf(GraphError(message = "Staff failed")),
                ),
            ),
        )

        val result = repository.getStaffBase(id = 5L)

        assertTrue(result.isFailure)
        assertEquals("Staff failed", result.exceptionOrNull()?.message)
    }

    @Test
    fun `getStaffBase null body returns failed Result`() = runTest {
        val request = StaffBase.request(id = 5)
        `when`(service.getStaffBase(request)).thenReturn(Response.success(null))

        val result = repository.getStaffBase(id = 5L)

        assertTrue(result.isFailure)
        assertEquals("Empty response body", result.exceptionOrNull()?.message)
    }

    @Test
    fun `getStaffBase null root returns failed Result`() = runTest {
        val request = StaffBase.request(id = 5)
        `when`(service.getStaffBase(request)).thenReturn(
            Response.success(
                GraphContainer(
                    data = StaffBaseData(staff = null),
                    errors = null,
                ),
            ),
        )

        val result = repository.getStaffBase(id = 5L)

        assertTrue(result.isFailure)
        assertEquals("Empty response body", result.exceptionOrNull()?.message)
    }

    @Test
    fun `getStaffBase unmapped staff name falls back correctly`() = runTest {
        val request = StaffBase.request(id = 6)
        `when`(service.getStaffBase(request)).thenReturn(
            Response.success(
                GraphContainer(
                    data = StaffBaseData(
                        staff = StaffBaseData.Staff(
                            id = 6,
                            image = null,
                            isFavourite = false,
                            language = null,
                            name = null,
                            siteUrl = null,
                        ),
                    ),
                    errors = null,
                ),
            ),
        )

        val result = repository.getStaffBase(id = 6L)

        assertTrue(result.isSuccess)
        val staff = result.getOrThrow()
        assertEquals(6L, staff.id)
        assertNull(staff.name)
        assertNull(staff.siteUrl)
        assertFalse(staff.isFavourite)
    }

    private fun staffBaseData(): StaffBaseData = StaffBaseData(
        staff = StaffBaseData.Staff(
            id = 5,
            name = StaffBaseData.StaffName(
                first = "Shinichiro",
                last = "Watanabe",
                full = "Shinichiro Watanabe",
                native = "渡辺信一郎",
                alternative = null,
            ),
            image = null,
            isFavourite = true,
            language = null,
            siteUrl = "https://anilist.co/staff/5",
        ),
    )
}
