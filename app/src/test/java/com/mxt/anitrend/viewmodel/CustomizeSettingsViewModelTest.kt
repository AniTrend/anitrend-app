package com.mxt.anitrend.viewmodel

import android.content.Context
import android.content.SharedPreferences
import android.content.res.Resources
import com.mxt.anitrend.util.KeyUtil
import com.mxt.anitrend.util.Settings
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.ArgumentMatchers.anyString
import org.mockito.ArgumentMatchers.eq
import org.mockito.Mockito.mock
import org.mockito.Mockito.never
import org.mockito.Mockito.verify
import org.mockito.Mockito.verifyNoMoreInteractions
import org.mockito.Mockito.`when`

/**
 * Focused tests for the Customize settings state reduction and persistence.
 *
 * The ViewModel talks to [Settings] only through the delegated
 * [SharedPreferences] API (getString / edit), so mocked prefs exercise the
 * exact production path without Android runtime dependencies.
 */
class CustomizeSettingsViewModelTest {

    private lateinit var preferences: SharedPreferences
    private lateinit var editor: SharedPreferences.Editor

    private val keys = CustomizePreferenceKeys(
        themeKey = "pref_theme",
        languageKey = "pref_language",
        listViewStyleKey = "pref_list_style",
    )

    @Before
    fun setUp() {
        preferences = mock(SharedPreferences::class.java)
        editor = mock(SharedPreferences.Editor::class.java)
        `when`(preferences.edit()).thenReturn(editor)
        `when`(editor.putString(anyString(), anyString())).thenReturn(editor)
    }

    private fun createViewModel(): CustomizeSettingsViewModel = CustomizeSettingsViewModel(
        settings = Settings(
            context = mock(Context::class.java),
            resources = mock(Resources::class.java),
            preferences = preferences,
        ),
        keys = keys,
    )

    private fun stubPreference(key: String, value: String) {
        `when`(preferences.getString(eq(key), anyString())).thenReturn(value)
    }

    private fun stubAllDefaults() {
        `when`(preferences.getString(anyString(), anyString())).thenAnswer { invocation ->
            invocation.getArgument(1)
        }
    }

    // ── initial state ──

    @Test
    fun `initial state reads persisted preference values`() {
        stubPreference(keys.themeKey, KeyUtil.THEME_DARK)
        stubPreference(keys.languageKey, "fr")
        stubPreference(keys.listViewStyleKey, "1")

        val state = createViewModel().state.value

        assertEquals(KeyUtil.THEME_DARK, state.theme)
        assertEquals("fr", state.language)
        assertEquals("1", state.listViewStyle)
    }

    @Test
    fun `initial state falls back to legacy defaults when preferences are empty`() {
        stubAllDefaults()

        val state = createViewModel().state.value

        assertEquals(KeyUtil.THEME_LIGHT, state.theme)
        assertEquals(CustomizeSettingsViewModel.DEFAULT_LANGUAGE, state.language)
        assertEquals(CustomizeSettingsViewModel.DEFAULT_LIST_VIEW_STYLE, state.listViewStyle)
    }

    // ── state reduction and persistence ──

    @Test
    fun `setTheme persists the theme value and reduces state`() {
        stubAllDefaults()
        val viewModel = createViewModel()

        viewModel.setTheme(KeyUtil.THEME_BLACK)

        verify(editor).putString(keys.themeKey, KeyUtil.THEME_BLACK)
        assertEquals(KeyUtil.THEME_BLACK, viewModel.state.value.theme)
    }

    @Test
    fun `setLanguage persists the language value and reduces state`() {
        stubAllDefaults()
        val viewModel = createViewModel()

        viewModel.setLanguage("sv")

        verify(editor).putString(keys.languageKey, "sv")
        assertEquals("sv", viewModel.state.value.language)
    }

    @Test
    fun `setListViewStyle persists the list view style value and reduces state`() {
        stubAllDefaults()
        val viewModel = createViewModel()

        viewModel.setListViewStyle("2")

        verify(editor).putString(keys.listViewStyleKey, "2")
        assertEquals("2", viewModel.state.value.listViewStyle)
    }

    @Test
    fun `setters reduce state independently per row`() {
        stubAllDefaults()
        val viewModel = createViewModel()

        viewModel.setTheme(KeyUtil.THEME_DARK)
        viewModel.setLanguage("de")
        viewModel.setListViewStyle("1")

        val state = viewModel.state.value
        assertEquals(KeyUtil.THEME_DARK, state.theme)
        assertEquals("de", state.language)
        assertEquals("1", state.listViewStyle)
    }

    @Test
    fun `writes only touch the changed preference key`() {
        stubAllDefaults()
        val viewModel = createViewModel()

        viewModel.setLanguage("es")

        verify(editor).putString(eq(keys.languageKey), eq("es"))
        verify(editor, never()).putString(eq(keys.themeKey), anyString())
        verify(editor, never()).putString(eq(keys.listViewStyleKey), anyString())
        verify(editor).apply()
        verifyNoMoreInteractions(editor)
    }

    @Test
    fun `new ViewModel observes values persisted by a previous instance`() {
        stubAllDefaults()
        val first = createViewModel()
        first.setTheme(KeyUtil.THEME_BLACK)

        // Simulate the store returning what the first instance committed.
        stubPreference(keys.themeKey, KeyUtil.THEME_BLACK)
        val second = createViewModel()

        assertEquals(KeyUtil.THEME_BLACK, second.state.value.theme)
    }
}
