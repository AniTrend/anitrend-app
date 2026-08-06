package com.mxt.anitrend.view.activity.detail

import com.mxt.anitrend.navigation.extension.ARG_STAFF_SCREEN
import com.mxt.anitrend.navigation.extension.screenParamKey
import com.mxt.anitrend.navigation.model.StaffScreenParam
import com.mxt.anitrend.util.KeyUtil
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class StaffActivityTest {

    // ── production resolve(): missing/invalid ids ──

    @Test
    fun `resolve returns null when no identity is supplied`() {
        // Mirrors an intent without extras: getLongExtra(KeyUtil.arg_id, -1) → -1.
        assertNull(StaffActivity.resolve(typed = null, legacyId = -1))
    }

    @Test
    fun `resolve returns null when legacy id is negative`() {
        assertNull(StaffActivity.resolve(typed = null, legacyId = -5))
    }

    @Test
    fun `resolve returns null when legacy id is zero`() {
        assertNull(StaffActivity.resolve(typed = null, legacyId = 0))
    }

    // ── production resolve(): legacy bridge ──

    @Test
    fun `resolve bridges positive legacy id`() {
        val result = StaffActivity.resolve(typed = null, legacyId = 123L)
        assertNotNull(result)
        assertEquals(123L, result!!.staffId)
    }

    // ── production resolve(): typed-first precedence ──

    @Test
    fun `resolve returns typed param when present and positive`() {
        val result = StaffActivity.resolve(typed = StaffScreenParam(staffId = 77L), legacyId = 5L)
        assertNotNull(result)
        assertEquals(77L, result!!.staffId)
    }

    @Test
    fun `resolve returns null when typed param is present but invalid, even with valid legacy id`() {
        // Typed parameter wins: an invalid typed param must not fall back to legacy.
        assertNull(StaffActivity.resolve(typed = StaffScreenParam(staffId = 0), legacyId = 5L))
    }

    // ── wire keys ──

    @Test
    fun `legacy wire key matches KeyUtil arg_id constant`() {
        assertEquals("id", KeyUtil.arg_id)
    }

    @Test
    fun `stable staff screen key uses destination-owned namespace`() {
        assertEquals("arg.staff.screen", ARG_STAFF_SCREEN)
    }

    @Test
    fun `screenParamKey resolves staff param to its stable key`() {
        assertEquals(ARG_STAFF_SCREEN, screenParamKey<StaffScreenParam>())
    }

    // ── parameter construction ──

    @Test
    fun `StaffScreenParam holds staffId correctly`() {
        val param = StaffScreenParam(staffId = 456L)
        assertEquals(456L, param.staffId)
    }

    @Test
    fun `StaffScreenParam is identity-only and does not carry the onList filter`() {
        // The tri-state onList filter stays on the legacy arg_onList channel
        // (read directly by the activity) until the pager fragments migrate.
        val paramTypes = StaffScreenParam::class.java.constructors[0].parameterTypes.map { it.simpleName }
        assertEquals(listOf("long"), paramTypes)
    }
}
