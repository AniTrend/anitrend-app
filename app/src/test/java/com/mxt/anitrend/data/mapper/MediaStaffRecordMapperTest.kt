package com.mxt.anitrend.data.mapper

import com.mxt.anitrend.domain.model.StaffRecord
import com.mxt.anitrend.graphql.generated.MediaStaffData
import com.mxt.anitrend.graphql.generated.StaffLanguage
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class MediaStaffRecordMapperTest {

    @Test
    fun `maps generated Media to MediaStaffRecord preserving all values`() {
        val record = media(
            staff = MediaStaffData.MediaStaff(
                edges = listOf(
                    MediaStaffData.MediaStaffEdges(
                        role = "MAIN",
                        node = node(
                            id = 123,
                            image = MediaStaffData.MediaStaffEdgesNodeImage(
                                large = "https://cdn.example.com/large.jpg",
                                medium = "https://cdn.example.com/medium.jpg",
                            ),
                            isFavourite = true,
                            language = StaffLanguage.JAPANESE,
                            name = MediaStaffData.MediaStaffEdgesNodeName(
                                alternative = listOf("Alt Name"),
                                first = "Shinichiro",
                                full = "Shinichiro Watanabe",
                                last = "Watanabe",
                                native = "渡辺信一郎",
                            ),
                            siteUrl = "https://anilist.co/staff/123",
                        ),
                    ),
                ),
                pageInfo = MediaStaffData.MediaStaffPageInfo(
                    currentPage = 1,
                    hasNextPage = true,
                    lastPage = 3,
                    perPage = 25,
                    total = 61,
                ),
            ),
        ).toMediaStaffRecord()

        assertEquals(1, record.edges?.size)
        val edge = record.edges?.first()
        assertEquals("MAIN", edge?.role)
        val node = edge?.node
        assertEquals(123L, node?.id)
        assertEquals("Shinichiro Watanabe", node?.name)
        assertEquals("https://anilist.co/staff/123", node?.siteUrl)
        assertTrue(node?.isFavourite == true)
        assertEquals(1, record.pageInfo?.currentPage)
        assertEquals(3, record.pageInfo?.lastPage)
        assertEquals(25, record.pageInfo?.perPage)
        assertEquals(61, record.pageInfo?.total)
        assertTrue(record.pageInfo?.hasNextPage == true)
        assertFalse(record.pageInfo?.hasPreviousPage == true)
    }

    @Test
    fun `maps generated String roles through unchanged`() {
        val record = media(
            staff = MediaStaffData.MediaStaff(
                edges = listOf(
                    MediaStaffData.MediaStaffEdges(role = "MAIN", node = node(id = 1)),
                    MediaStaffData.MediaStaffEdges(role = "DIRECTOR", node = node(id = 2)),
                    MediaStaffData.MediaStaffEdges(role = null, node = node(id = 3)),
                ),
                pageInfo = null,
            ),
        ).toMediaStaffRecord()

        assertEquals("MAIN", record.edges?.get(0)?.role)
        assertEquals("DIRECTOR", record.edges?.get(1)?.role)
        assertNull(record.edges?.get(2)?.role)
    }

    @Test
    fun `converts generated Int staff ids to domain Longs`() {
        val record = media(
            staff = MediaStaffData.MediaStaff(
                edges = listOf(
                    MediaStaffData.MediaStaffEdges(
                        role = null,
                        node = node(id = Int.MAX_VALUE),
                    ),
                ),
                pageInfo = null,
            ),
        ).toMediaStaffRecord()

        assertEquals(Int.MAX_VALUE.toLong(), record.edges?.first()?.node?.id)
    }

    @Test
    fun `maps StaffCoreFragment node preserving name site and favourite semantics`() {
        val record = media(
            staff = MediaStaffData.MediaStaff(
                edges = listOf(
                    MediaStaffData.MediaStaffEdges(
                        role = "MAIN",
                        node = node(
                            id = 42,
                            image = MediaStaffData.MediaStaffEdgesNodeImage(
                                large = "https://cdn.example.com/large.jpg",
                                medium = "https://cdn.example.com/medium.jpg",
                            ),
                            isFavourite = false,
                            language = StaffLanguage.ENGLISH,
                            name = MediaStaffData.MediaStaffEdgesNodeName(
                                alternative = null,
                                first = "Maka",
                                full = "Maka Albarn",
                                last = "Albarn",
                                native = null,
                            ),
                            siteUrl = "https://anilist.co/staff/42",
                        ),
                    ),
                ),
                pageInfo = null,
            ),
        ).toMediaStaffRecord()

        val node: StaffRecord? = record.edges?.first()?.node
        assertEquals(42L, node?.id)
        assertEquals("Maka Albarn", node?.name)
        assertEquals("https://anilist.co/staff/42", node?.siteUrl)
        assertFalse(node?.isFavourite == true)
    }

    @Test
    fun `derives full name like legacy TitleBase fullName`() {
        val firstOnly = node(name = name(first = "Shinichiro", last = null))
        val lastOnly = node(name = name(first = null, last = "Watanabe"))
        val both = node(name = name(first = "Shinichiro", last = "Watanabe"))
        val none = node(name = name(first = null, last = null))

        val record = media(
            staff = MediaStaffData.MediaStaff(
                edges = listOf(
                    MediaStaffData.MediaStaffEdges(role = null, node = firstOnly),
                    MediaStaffData.MediaStaffEdges(role = null, node = lastOnly),
                    MediaStaffData.MediaStaffEdges(role = null, node = both),
                    MediaStaffData.MediaStaffEdges(role = null, node = none),
                ),
                pageInfo = null,
            ),
        ).toMediaStaffRecord()

        assertEquals("Shinichiro", record.edges?.get(0)?.node?.name)
        assertEquals("Watanabe", record.edges?.get(1)?.node?.name)
        assertEquals("Shinichiro Watanabe", record.edges?.get(2)?.node?.name)
        assertNull(record.edges?.get(3)?.node?.name)
    }

    @Test
    fun `maps generated PageInfo to PageInfoRecord preserving page metadata`() {
        val record = media(
            staff = MediaStaffData.MediaStaff(
                edges = null,
                pageInfo = MediaStaffData.MediaStaffPageInfo(
                    currentPage = 2,
                    hasNextPage = false,
                    lastPage = 4,
                    perPage = 50,
                    total = 200,
                ),
            ),
        ).toMediaStaffRecord()

        assertEquals(2, record.pageInfo?.currentPage)
        assertEquals(4, record.pageInfo?.lastPage)
        assertEquals(50, record.pageInfo?.perPage)
        assertEquals(200, record.pageInfo?.total)
        assertFalse(record.pageInfo?.hasNextPage == true)
        assertTrue(record.pageInfo?.hasPreviousPage == true)
    }

    @Test
    fun `preserves nullable semantics for optional blocks and lists`() {
        val record = media(
            staff = MediaStaffData.MediaStaff(
                edges = null,
                pageInfo = null,
            ),
        ).toMediaStaffRecord()

        assertNull(record.edges)
        assertNull(record.pageInfo)
    }

    @Test
    fun `collapses null staff block into null edges and page info`() {
        val record = media(staff = null).toMediaStaffRecord()

        assertNull(record.edges)
        assertNull(record.pageInfo)
    }

    @Test
    fun `maps empty edges list to empty list`() {
        val record = media(
            staff = MediaStaffData.MediaStaff(
                edges = emptyList(),
                pageInfo = null,
            ),
        ).toMediaStaffRecord()

        assertEquals(0, record.edges?.size)
    }

    @Test
    fun `drops null list elements in edges`() {
        val record = media(
            staff = MediaStaffData.MediaStaff(
                edges = listOf(
                    null,
                    MediaStaffData.MediaStaffEdges(
                        role = "MAIN",
                        node = node(id = 1),
                    ),
                    null,
                ),
                pageInfo = null,
            ),
        ).toMediaStaffRecord()

        assertEquals(1, record.edges?.size)
        assertEquals("MAIN", record.edges?.first()?.role)
        assertEquals(1L, record.edges?.first()?.node?.id)
    }

    @Test
    fun `preserves null node blocks within edges`() {
        val record = media(
            staff = MediaStaffData.MediaStaff(
                edges = listOf(
                    MediaStaffData.MediaStaffEdges(
                        role = "MAIN",
                        node = null,
                    ),
                ),
                pageInfo = null,
            ),
        ).toMediaStaffRecord()

        assertEquals(1, record.edges?.size)
        assertEquals("MAIN", record.edges?.first()?.role)
        assertNull(record.edges?.first()?.node)
    }

    @Test
    fun `preserves null role and nullable node fields`() {
        val record = media(
            staff = MediaStaffData.MediaStaff(
                edges = listOf(
                    MediaStaffData.MediaStaffEdges(
                        role = null,
                        node = node(
                            id = 1,
                            image = null,
                            isFavourite = false,
                            language = null,
                            name = null,
                            siteUrl = null,
                        ),
                    ),
                ),
                pageInfo = null,
            ),
        ).toMediaStaffRecord()

        assertNull(record.edges?.first()?.role)
        assertNull(record.edges?.first()?.node?.name)
        assertNull(record.edges?.first()?.node?.siteUrl)
    }

    private fun media(
        staff: MediaStaffData.MediaStaff? = null,
    ): MediaStaffData.Media = MediaStaffData.Media(
        staff = staff,
    )

    private fun node(
        id: Int = 1,
        image: MediaStaffData.MediaStaffEdgesNodeImage? = null,
        isFavourite: Boolean = false,
        language: StaffLanguage? = null,
        name: MediaStaffData.MediaStaffEdgesNodeName? = null,
        siteUrl: String? = null,
    ): MediaStaffData.MediaStaffEdgesNode = MediaStaffData.MediaStaffEdgesNode(
        id = id,
        image = image,
        isFavourite = isFavourite,
        language = language,
        name = name,
        siteUrl = siteUrl,
    )

    private fun name(
        first: String? = null,
        last: String? = null,
    ): MediaStaffData.MediaStaffEdgesNodeName = MediaStaffData.MediaStaffEdgesNodeName(
        alternative = null,
        first = first,
        full = null,
        last = last,
        native = null,
    )
}
