package com.mxt.anitrend.view.sheet

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.mxt.anitrend.R
import com.mxt.anitrend.adapter.recycler.index.UserAdapter
import com.mxt.anitrend.base.custom.sheet.BottomSheetBase
import com.mxt.anitrend.base.custom.sheet.BottomSheetList
import com.mxt.anitrend.databinding.BottomSheetListBinding
import com.mxt.anitrend.extension.parcelableArrayList
import com.mxt.anitrend.model.entity.base.UserBase
import com.mxt.anitrend.presenter.base.BasePresenter
import com.mxt.anitrend.util.KeyUtil
import com.mxt.anitrend.view.activity.detail.ProfileActivity

class BottomSheetUsers : BottomSheetList<UserBase>() {
    private var binding: BottomSheetListBinding? = null

    companion object {
        @JvmStatic
        fun newInstance(bundle: Bundle): BottomSheetUsers = BottomSheetUsers().apply {
            arguments = bundle
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val ctx = requireContext()
        presenter = BasePresenter(ctx)
        mColumnSize = resources.getInteger(R.integer.single_list_x1)
        mAdapter = UserAdapter(ctx)
        val baseList = arguments?.parcelableArrayList<UserBase>(KeyUtil.arg_list_model)
        if (!baseList.isNullOrEmpty()) {
            mAdapter.onItemsInserted(baseList)
        }
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
        toolbarTitle?.text = getString(mTitle, mAdapter.itemCount)
        toolbarSearch?.visibility = View.VISIBLE
        injectAdapter()
    }

    override fun makeRequest() = Unit

    override fun onStart() {
        super.onStart()
        val editText = searchBar?.findViewById<android.widget.EditText>(
            resources.getIdentifier("search_bar_text_input", "id", "com.google.android.material"),
        )
        editText?.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (!TextUtils.isEmpty(s) && mAdapter.filter != null) {
                    mAdapter.filter.filter(s)
                }
            }
            override fun afterTextChanged(s: android.text.Editable?) {}
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }

    override fun onItemClick(
        target: View,
        data: IndexedValue<UserBase>,
    ) {
        when (target.id) {
            R.id.container -> {
                val host = activity ?: return
                val intent =
                    Intent(host, ProfileActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                        putExtra(KeyUtil.arg_id, data.value.id)
                    }
                host.startActivity(intent)
            }
        }
    }

    override fun onItemLongClick(
        target: View,
        data: IndexedValue<UserBase>,
    ) = Unit

    class Builder : BottomSheetBuilder() {
        override fun build(): BottomSheetBase<*> = newInstance(bundle)

        fun setModel(model: List<UserBase>): Builder {
            bundle.putParcelableArrayList(KeyUtil.arg_list_model, ArrayList(model))
            return this
        }
    }
}
