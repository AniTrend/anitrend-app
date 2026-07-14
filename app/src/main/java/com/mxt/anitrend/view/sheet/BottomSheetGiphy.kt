package com.mxt.anitrend.view.sheet

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.mxt.anitrend.R
import com.mxt.anitrend.adapter.recycler.detail.GiphyAdapter
import com.mxt.anitrend.base.custom.sheet.BottomSheetBase
import com.mxt.anitrend.base.custom.sheet.BottomSheetGiphyList
import com.mxt.anitrend.databinding.BottomSheetListBinding
import com.mxt.anitrend.model.entity.giphy.Gif
import com.mxt.anitrend.model.entity.giphy.Giphy
import com.mxt.anitrend.presenter.base.BasePresenter
import com.mxt.anitrend.util.KeyUtil
import com.mxt.anitrend.util.NotifyUtil
import com.mxt.anitrend.view.activity.base.GiphyPreviewActivity

/**
 * Created by max on 2017/12/09.
 * giphy bottom sheet container
 */
class BottomSheetGiphy : BottomSheetGiphyList() {
    private var binding: BottomSheetListBinding? = null

    @KeyUtil.RequestType
    private var requestType: Int = 0

    companion object {
        @JvmStatic
        fun newInstance(bundle: Bundle): BottomSheetGiphy = BottomSheetGiphy().apply {
            arguments = bundle
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val ctx = requireContext()
        presenter = BasePresenter(ctx)
        mAdapter = GiphyAdapter(ctx)
        mColumnSize = resources.getInteger(R.integer.grid_giphy_x3)
        isPager = true
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        binding = BottomSheetListBinding.inflate(layoutInflater)
        dialog.setContentView(requireNotNull(binding).root)
        bindToolbarViews(requireNotNull(binding).root)
        bindListViews(requireNotNull(binding).root)
        createBottomSheetBehavior(requireNotNull(binding).root)
        mLayoutManager = StaggeredGridLayoutManager(mColumnSize, StaggeredGridLayoutManager.VERTICAL)
        return dialog
    }

    override fun updateUI() {
        toolbarTitle?.text = getString(mTitle)
        toolbarSearch?.visibility = View.VISIBLE
        val editText = searchBar?.findViewById<android.widget.EditText>(
            resources.getIdentifier("search_bar_text_input", "id", "com.google.android.material"),
        )
        editText?.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: android.text.Editable?) {}
        })
        editText?.setOnEditorActionListener { v: android.widget.TextView, actionId: Int, _ ->
            if (actionId == android.view.inputmethod.EditorInfo.IME_ACTION_SEARCH) {
                searchQuery = v.text.toString()
                stateLayout?.showLoading()
                onRefresh()
                true
            } else {
                false
            }
        }
        injectAdapter()
        if (presenter.settings.shouldShowTipFor(KeyUtil.KEY_GIPHY_TIP)) {
            activity?.let {
                NotifyUtil.createAlerter(
                    it,
                    R.string.title_new_feature,
                    R.string.text_giphy_feature,
                    R.drawable.ic_gif_white_24dp,
                    R.color.colorStateBlue,
                    KeyUtil.DURATION_LONG,
                )
            }
            presenter.settings.disableTipFor(KeyUtil.KEY_GIPHY_TIP)
        }
    }

    override fun makeRequest() {
        val ctx = context ?: return
        val hasQuery = !searchQuery.isNullOrEmpty()
        val bundle = viewModel?.params ?: Bundle()
        bundle.putInt(KeyUtil.arg_page_offset, presenter.currentOffset)
        requestType = if (hasQuery) KeyUtil.GIPHY_SEARCH_REQ else KeyUtil.GIPHY_TRENDING_REQ
        if (hasQuery) {
            bundle.putString(KeyUtil.arg_search, searchQuery)
        }
        viewModel?.requestData(requestType, ctx)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }

    override fun onItemClick(
        target: View,
        data: IndexedValue<Giphy>,
    ) {
        presenter.notifyAllListeners(data, false)
        closeDialog()
    }

    override fun onItemLongClick(
        target: View,
        data: IndexedValue<Giphy>,
    ) {
        activity?.let { host ->
            val index = KeyUtil.GIPHY_LARGE_DOWN_SAMPLE
            val giphySample: Gif? = data.value.images[index]
            val intent =
                Intent(host, GiphyPreviewActivity::class.java).apply {
                    putExtra(KeyUtil.arg_model, giphySample?.url)
                }
            host.startActivity(intent)
        }
    }

    class Builder : BottomSheetBuilder() {
        override fun build(): BottomSheetBase<*> = newInstance(bundle)
    }
}
