/*******************************************************************************
 * Copyright (c) 2025 Miguel Catalan Banuls
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/

package com.mxt.anitrend.base.custom.view.search

import android.content.Context
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.os.Parcel
import android.os.Parcelable
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.AdapterView
import android.widget.Filter
import android.widget.Filterable
import android.widget.FrameLayout
import android.widget.ListAdapter
import androidx.core.content.withStyledAttributes
import androidx.core.text.trimmedLength
import androidx.core.view.isGone
import androidx.core.view.isVisible
import com.mxt.anitrend.R
import com.mxt.anitrend.databinding.SearchViewBinding
import timber.log.Timber

/**
 * @author Miguel Catalan Banuls
 */
class MaterialSearchView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : FrameLayout(context, attrs),
    Filter.FilterListener {

    private val binding: SearchViewBinding =
        SearchViewBinding.inflate(LayoutInflater.from(context), this, true)

    private var menuItem: MenuItem? = null
    var isSearchOpen = false
        private set
    private var animationDuration: Int = 0
    private var clearingFocus = false
    private var showSuggestionsFlag = false
    private var hasFocusWhenOpened = true
    private var filterSuggestionsWhenSearchEmpty = true
    private var enableTintViewFlag = false

    private var oldQueryText: CharSequence? = null
    private var userQuery: CharSequence? = null

    private var onQueryChangeListener: OnQueryTextListener? = null
    private var searchViewListeners: MutableList<SearchViewListener>? = null
    private var onClickBackListeners: MutableList<OnClickBackListener>? = null
    private var onSuggestionClickListeners: MutableList<OnSuggestionClickListener>? = null
    private var searchViewIsClosedListener: OnSearchViewIsClosedListener? = null
    private var onEditTextFocusChangedListener: OnEditTextFocusChangedListener? = null
    private var onSearchClearedListener: OnSearchClearedListener? = null

    private var adapter: ListAdapter? = null
    private var savedState: SavedState? = null
    private var submitOnClick = false
    private var ellipsize = false
    private var suggestionIcon: Drawable? = null

    private val clickListener = OnClickListener { v ->
        if (v == binding.actionUpBtn) {
            onClickBackListeners?.forEach { it.onClickBack() }
            closeSearch()
            return@OnClickListener
        }
        if (isSearchOpen) {
            when (v) {
                binding.searchTextView -> showSuggestions()
                binding.transparentView -> {
                    dismissSuggestions()
                    hideKeyboard(this@MaterialSearchView)
                }
            }
        }
    }

    init {
        initStyle(attrs, defStyleAttr)
        initSearchView()
        binding.suggestionList.isGone = true
        animationDuration = AnimationUtil.ANIMATION_DURATION_MEDIUM
    }

    private fun initStyle(attrs: AttributeSet?, defStyleAttr: Int) {
        context.withStyledAttributes(
            set = attrs,
            attrs = R.styleable.MaterialSearchView,
            defStyleAttr = defStyleAttr,
            defStyleRes = 0,
        ) {
            runCatching {
                if (hasValue(R.styleable.MaterialSearchView_searchBackground)) {
                    background = getDrawable(R.styleable.MaterialSearchView_searchBackground)
                }
            }.onFailure { Timber.w(it) }

            runCatching {
                if (hasValue(R.styleable.MaterialSearchView_android_textColor)) {
                    setTextColor(getColor(R.styleable.MaterialSearchView_android_textColor, 0))
                }
            }.onFailure { Timber.w(it) }

            runCatching {
                if (hasValue(R.styleable.MaterialSearchView_android_textColorHint)) {
                    setHintTextColor(getColor(R.styleable.MaterialSearchView_android_textColorHint, 0))
                }
            }.onFailure { Timber.w(it) }

            runCatching {
                if (hasValue(R.styleable.MaterialSearchView_android_hint)) {
                    setHint(getString(R.styleable.MaterialSearchView_android_hint))
                }
            }.onFailure { Timber.w(it) }

            runCatching {
                if (hasValue(R.styleable.MaterialSearchView_searchBackIcon)) {
                    setBackIcon(getDrawable(R.styleable.MaterialSearchView_searchBackIcon))
                }
            }.onFailure { Timber.w(it) }

            runCatching {
                if (hasValue(R.styleable.MaterialSearchView_searchSuggestionBackground)) {
                    setSuggestionBackground(getDrawable(R.styleable.MaterialSearchView_searchSuggestionBackground))
                }
            }.onFailure { Timber.w(it) }

            runCatching {
                if (hasValue(R.styleable.MaterialSearchView_searchSuggestionIcon)) {
                    setSuggestionIcon(getDrawable(R.styleable.MaterialSearchView_searchSuggestionIcon))
                }
            }.onFailure { Timber.w(it) }

            runCatching {
                if (hasValue(R.styleable.MaterialSearchView_android_inputType)) {
                    setInputType(
                        getInt(
                            R.styleable.MaterialSearchView_android_inputType,
                            EditorInfo.TYPE_NULL,
                        ),
                    )
                }
            }.onFailure { Timber.w(it) }
        }
    }

    private fun initSearchView() {
        binding.searchTextView.setOnEditorActionListener { _, _, _ ->
            onSubmitQuery()
            true
        }

        binding.searchTextView.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                userQuery = s
                startFilter(s)
                this@MaterialSearchView.onTextChanged(s)
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        binding.searchTextView.setOnFocusChangeListener { _, hasFocus ->
            onEditTextFocusChangedListener?.onFocusChanged(hasFocus)
            if (hasFocus) {
                showKeyboard(binding.searchTextView)
                showSuggestions()
            }
        }

        binding.actionUpBtn.setOnClickListener(clickListener)
        binding.searchTextView.setOnClickListener(clickListener)
        binding.transparentView.setOnClickListener(clickListener)
    }

    private fun startFilter(s: CharSequence?) {
        val filterable = adapter as? Filterable
        filterable?.filter?.filter(s, this)
    }

    private fun onTextChanged(newText: CharSequence?) {
        val text = binding.searchTextView.text
        userQuery = text

        if (onQueryChangeListener != null && !TextUtils.equals(newText, oldQueryText)) {
            onQueryChangeListener?.onQueryTextChange(newText?.toString().orEmpty())
        }
        oldQueryText = newText?.toString()
    }

    private fun onSubmitQuery() {
        val query = binding.searchTextView.text
        if (query != null && query.trimmedLength() > 0) {
            if (onQueryChangeListener == null || !onQueryChangeListener!!.onQueryTextSubmit(query.toString())) {
                closeSearch()
                binding.searchTextView.setText(null)
            }
        }
    }

    fun hideKeyboard(view: View) {
        val imm = view.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }

    fun showKeyboard(view: View) {
        val imm = view.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(view, 0)
    }

    // Public Attributes

    override fun setBackground(background: Drawable?) {
        binding.searchTopBar.background = background
    }

    override fun setBackgroundColor(color: Int) {
        binding.searchTopBar.setBackgroundColor(color)
    }

    fun setTextColor(color: Int) {
        binding.searchTextView.setTextColor(color)
    }

    fun setHintTextColor(color: Int) {
        binding.searchTextView.setHintTextColor(color)
    }

    fun setHint(hint: CharSequence?) {
        binding.searchTextView.hint = hint
    }

    fun setBackIcon(drawable: Drawable?) {
        binding.actionUpBtn.setImageDrawable(drawable)
    }

    fun setSuggestionIcon(drawable: Drawable?) {
        suggestionIcon = drawable
    }

    fun setInputType(inputType: Int) {
        binding.searchTextView.inputType = inputType
    }

    fun setSuggestionBackground(background: Drawable?) {
        binding.suggestionList.background = background
    }

    /**
     * Call this method to show suggestions list. This shows up when adapter is set.
     * Call [setAdapter] before calling this.
     */
    private fun showSuggestions() {
        if (adapter != null && adapter!!.count > 0 && binding.suggestionList.isGone) {
            binding.suggestionList.isVisible = true
            if (enableTintViewFlag) {
                binding.transparentView.isVisible = true
            }
        }
    }

    // Public Methods

    /**
     * Submit the query as soon as the user clicks the item.
     */
    fun setSubmitOnClick(submit: Boolean) {
        submitOnClick = submit
    }

    /**
     * Set Suggest List OnItemClickListener
     */
    fun setOnItemClickListener(listener: AdapterView.OnItemClickListener?) {
        binding.suggestionList.onItemClickListener = listener
    }

    /**
     * Set Adapter for suggestions list. Should implement Filterable.
     */
    fun setAdapter(adapter: ListAdapter?) {
        this.adapter = adapter
        binding.suggestionList.adapter = adapter
        startFilter(binding.searchTextView.text)
    }

    /**
     * Set Adapter for suggestions list with the given suggestion array
     */
    fun setSuggestions(suggestions: Array<String>?) {
        if (!suggestions.isNullOrEmpty()) {
            val searchAdapter = SearchAdapter(
                context,
                suggestions,
                suggestionIcon,
                ellipsize,
                filterSuggestionsWhenSearchEmpty,
            )
            setAdapter(searchAdapter)

            setOnItemClickListener { _, _, position, _ ->
                setQuery(searchAdapter.getItem(position) as String, submitOnClick)
                showSuggestionsFlag = false
            }
        }
    }

    /**
     * Dismiss the suggestions list.
     */
    fun dismissSuggestions() {
        if (binding.suggestionList.isVisible) {
            binding.suggestionList.isGone = true
            if (enableTintViewFlag) {
                binding.transparentView.isGone = true
            }
        }
    }

    /**
     * Calling this will set the query to search text box. if submit is true, it'll submit the query.
     */
    fun setQuery(query: CharSequence?, submit: Boolean) {
        binding.searchTextView.setText(query)
        if (query != null) {
            binding.searchTextView.setSelection(binding.searchTextView.length())
            userQuery = query
        }
        if (submit && !TextUtils.isEmpty(query)) {
            onSubmitQuery()
            onSuggestionClickListeners?.forEach { it.onSuggestionClick() }
        }
    }

    /**
     * Call this method and pass the menu item so this class can handle click events for the Menu Item.
     */
    fun setMenuItem(menuItem: MenuItem?) {
        this.menuItem = menuItem
        menuItem?.setOnMenuItemClickListener {
            showSearch()
            true
        }
    }

    /**
     * Sets animation duration. ONLY FOR PRE-LOLLIPOP!!
     */
    fun setAnimationDuration(duration: Int) {
        animationDuration = duration
    }

    /**
     * Open Search View. This will animate the showing of the view.
     */
    fun showSearch() {
        showSearch(true)
    }

    /**
     * Open Search View. If animate is true, Animate the showing of the view.
     */
    fun showSearch(animate: Boolean) {
        if (isSearchOpen) {
            return
        }

        isSearchOpen = true

        if (hasFocusWhenOpened) {
            binding.searchTextView.setText(null)
            binding.searchTextView.requestFocus()
        }

        if (animate) {
            setVisibleWithAnimation()
        } else {
            binding.root.isVisible = true
            searchViewListeners?.forEach { it.onSearchViewShown() }
        }
    }

    private fun setVisibleWithAnimation() {
        val animationListener = object : AnimationUtil.AnimationListener {
            override fun onAnimationStart(view: View): Boolean = false

            override fun onAnimationEnd(view: View): Boolean {
                searchViewListeners?.forEach { it.onSearchViewShown() }
                return false
            }

            override fun onAnimationCancel(view: View): Boolean = false
        }

        binding.root.isVisible = true
        AnimationUtil.reveal(binding.searchTopBar, animationListener)
    }

    /**
     * Close search view.
     */
    fun closeSearch() {
        if (!isSearchOpen) {
            return
        }

        binding.searchTextView.setText(null)
        dismissSuggestions()
        clearFocus()
        adapter = null
        binding.suggestionList.adapter = null

        binding.root.isGone = true
        searchViewListeners?.forEach { it.onSearchViewClosed() }
        isSearchOpen = false
        searchViewIsClosedListener?.searchViewIsClosed()
    }

    /**
     * Set this listener to listen to Query Change events.
     */
    fun setOnQueryTextListener(listener: OnQueryTextListener?) {
        onQueryChangeListener = listener
    }

    /**
     * Set this listener to listen to Search View open and close events.
     * Existing listeners are removed.
     */
    fun setOnSearchViewListener(listener: SearchViewListener?) {
        searchViewListeners = if (listener != null) {
            mutableListOf(listener)
        } else {
            null
        }
    }

    /**
     * Add this listener to listen to Search View open and close events
     */
    fun addOnSearchViewListener(listener: SearchViewListener) {
        if (searchViewListeners == null) {
            searchViewListeners = mutableListOf()
        }
        searchViewListeners?.add(listener)
    }

    /**
     * Ellipsize suggestions longer than one line.
     */
    fun setEllipsize(ellipsize: Boolean) {
        this.ellipsize = ellipsize
    }

    fun showSuggestions(showSuggestions: Boolean) {
        showSuggestionsFlag = showSuggestions
    }

    fun setOnFilterClickListener(onClickListener: OnClickListener?) {
        binding.filterBtn.setOnClickListener(onClickListener)
    }

    fun showFilterIcon(visible: Boolean) {
        binding.filterBtn.isVisible = visible
    }

    fun setFilterActive(isFilterActive: Boolean) {
        binding.filterBtn.drawable?.level = if (isFilterActive) 1 else 0
    }

    fun hasFocusWhenOpened(hasFocus: Boolean) {
        hasFocusWhenOpened = hasFocus
    }

    fun filterSuggestionsWhenSearchEmpty(filterSuggestionsWhenSearchEmpty: Boolean) {
        this.filterSuggestionsWhenSearchEmpty = filterSuggestionsWhenSearchEmpty
    }

    fun enableTintView(enableTintView: Boolean) {
        enableTintViewFlag = enableTintView
    }

    fun setOnClickBackListener(onClickBackListener: OnClickBackListener?) {
        onClickBackListeners = if (onClickBackListener != null) {
            mutableListOf(onClickBackListener)
        } else {
            null
        }
    }

    fun addOnClickBackListener(onClickBackListener: OnClickBackListener) {
        if (onClickBackListeners == null) {
            onClickBackListeners = mutableListOf()
        }
        onClickBackListeners?.add(onClickBackListener)
    }

    fun setOnSuggestionClickListener(onSuggestionClickListener: OnSuggestionClickListener?) {
        onSuggestionClickListeners = if (onSuggestionClickListener != null) {
            mutableListOf(onSuggestionClickListener)
        } else {
            null
        }
    }

    fun addOnSuggestionClickListener(onSuggestionClickListener: OnSuggestionClickListener) {
        if (onSuggestionClickListeners == null) {
            onSuggestionClickListeners = mutableListOf()
        }
        onSuggestionClickListeners?.add(onSuggestionClickListener)
    }

    fun setOnSearchViewIsClosedListener(onSearchViewIsClosedListener: OnSearchViewIsClosedListener?) {
        this.searchViewIsClosedListener = onSearchViewIsClosedListener
    }

    fun setOnEditTextFocusChangedListener(onEditTextFocusChangedListener: OnEditTextFocusChangedListener?) {
        this.onEditTextFocusChangedListener = onEditTextFocusChangedListener
    }

    fun setOnSearchClearedListener(onSearchClearedListener: OnSearchClearedListener?) {
        this.onSearchClearedListener = onSearchClearedListener
    }

    fun getText(): Editable = binding.searchTextView.text

    override fun onFilterComplete(count: Int) {
        if (count > 0 && showSuggestionsFlag) {
            showSuggestions()
        } else {
            dismissSuggestions()
        }
    }

    override fun requestFocus(direction: Int, previouslyFocusedRect: Rect?): Boolean {
        if (clearingFocus) return false
        if (!isFocusable) return false
        return binding.searchTextView.requestFocus(direction, previouslyFocusedRect)
    }

    override fun clearFocus() {
        clearingFocus = true
        hideKeyboard(this)
        super.clearFocus()
        binding.searchTextView.clearFocus()
        clearingFocus = false
    }

    fun getUserQuery(): CharSequence? = userQuery

    override fun onSaveInstanceState(): Parcelable? {
        val superState = super.onSaveInstanceState()
        val state = SavedState(superState)
        state.query = userQuery?.toString()
        state.isSearchOpen = this.isSearchOpen
        return state
    }

    override fun onRestoreInstanceState(state: Parcelable?) {
        if (state !is SavedState) {
            super.onRestoreInstanceState(state)
            return
        }

        this.savedState = state
        if (state.isSearchOpen) {
            showSearch(false)
            setQuery(state.query, false)
        }
        super.onRestoreInstanceState(state.superState)
    }

    internal class SavedState : BaseSavedState {
        var query: String? = null
        var isSearchOpen: Boolean = false

        constructor(superState: Parcelable?) : super(superState)

        private constructor(parcel: Parcel) : super(parcel) {
            query = parcel.readString()
            isSearchOpen = parcel.readInt() == 1
        }

        override fun writeToParcel(out: Parcel, flags: Int) {
            super.writeToParcel(out, flags)
            out.writeString(query)
            out.writeInt(if (isSearchOpen) 1 else 0)
        }

        companion object CREATOR : Parcelable.Creator<SavedState> {
            override fun createFromParcel(parcel: Parcel): SavedState = SavedState(parcel)
            override fun newArray(size: Int): Array<SavedState?> = arrayOfNulls(size)
        }
    }

    interface OnQueryTextListener {
        /**
         * Called when the user submits the query. This could be due to a key press on the
         * keyboard or due to pressing a submit button.
         * The listener can override the standard behavior by returning true
         * to indicate that it has handled the submit request. Otherwise return false to
         * let the SearchView handle the submission by launching any associated intent.
         *
         * @param query the query text that is to be submitted
         * @return true if the query has been handled by the listener, false to let the
         * SearchView perform the default action.
         */
        fun onQueryTextSubmit(query: String?): Boolean

        /**
         * Called when the query text is changed by the user.
         *
         * @param newText the new content of the query text field.
         * @return false if the SearchView should perform the default action of showing any
         * suggestions if available, true if the action was handled by the listener.
         */
        fun onQueryTextChange(newText: String?): Boolean
    }

    interface SearchViewListener {
        fun onSearchViewShown()
        fun onSearchViewClosed()
    }

    interface OnClickBackListener {
        fun onClickBack()
    }

    interface OnSuggestionClickListener {
        fun onSuggestionClick()
    }

    interface OnSearchViewIsClosedListener {
        fun searchViewIsClosed()
    }

    interface OnEditTextFocusChangedListener {
        fun onFocusChanged(hasFocus: Boolean)
    }

    interface OnSearchClearedListener {
        fun onSearchCleared()
    }
}
