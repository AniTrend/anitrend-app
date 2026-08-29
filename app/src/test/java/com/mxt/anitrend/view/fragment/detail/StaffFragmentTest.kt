package com.mxt.anitrend.view.fragment.detail

import com.mxt.anitrend.navigation.extension.ARG_STAFF_SCREEN
import com.mxt.anitrend.navigation.extension.screenParamKey
import com.mxt.anitrend.navigation.model.StaffScreenParam
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class StaffFragmentTest {
    @Test
    fun `resolve rejects missing and non-positive legacy ids`() {
        assertNull(StaffFragment.resolve(null, -1L))
        assertNull(StaffFragment.resolve(null, 0L))
        assertNull(StaffFragment.resolve(null, -5L))
    }

    @Test
    fun `resolve bridges legacy identity`() {
        assertEquals(StaffScreenParam(123L), StaffFragment.resolve(null, 123L))
    }

    @Test
    fun `resolve prefers valid typed identity`() {
        assertEquals(StaffScreenParam(77L), StaffFragment.resolve(StaffScreenParam(77L), 5L))
    }

    @Test
    fun `invalid typed identity falls back to valid legacy identity`() {
        assertEquals(StaffScreenParam(5L), StaffFragment.resolve(StaffScreenParam(0L), 5L))
    }

    @Test
    fun `staff screen parameter remains identity only`() {
        assertEquals(listOf("long"), StaffScreenParam::class.java.constructors[0].parameterTypes.map { it.simpleName })
        assertEquals("arg.staff.screen", ARG_STAFF_SCREEN)
        assertEquals(ARG_STAFF_SCREEN, screenParamKey<StaffScreenParam>())
    }
}
