package com.mxt.anitrend.view.fragment.detail

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.mxt.anitrend.R
import com.mxt.anitrend.base.custom.fragment.FragmentBase
import com.mxt.anitrend.base.custom.view.image.AspectImageView
import com.mxt.anitrend.binding.htmlText
import com.mxt.anitrend.databinding.FragmentStaffOverviewBinding
import com.mxt.anitrend.extension.getCompatDrawable
import com.mxt.anitrend.model.entity.base.StaffBase
import com.mxt.anitrend.presenter.base.BasePresenter
import com.mxt.anitrend.util.CompatUtil
import com.mxt.anitrend.util.KeyUtil
import com.mxt.anitrend.util.graphql.GraphUtil

/**
 * Created by max on 2018/01/30.
 * StaffOverviewFragment
 */
class StaffOverviewFragment : FragmentBase<StaffBase, BasePresenter, StaffBase>() {

    private var model: StaffBase? = null
    private var binding: FragmentStaffOverviewBinding? = null
    private var id: Long = 0

    companion object {
        @JvmStatic
        fun newInstance(args: Bundle): StaffOverviewFragment {
            return StaffOverviewFragment().apply {
                arguments = args
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let { args ->
            id = args.getLong(KeyUtil.arg_id)
        }
        setViewModel(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentStaffOverviewBinding.inflate(inflater, container, false)
        binding?.stateLayout?.showLoading()
        binding?.staffImg?.setOnClickListener(this)
        return binding?.root
    }

    override fun updateUI() {
        val binding = binding ?: return
        val model = model
        if (model != null) {
            AspectImageView.setImage(binding.staffImg, model.image)
            binding.staffNameText.text = model.name?.fullName
            binding.staffLanguageText.text = CompatUtil.capitalizeWords(model.language)
            binding.staffSummaryText.htmlText(model.description)
            binding.stateLayout.showContent()
        } else {
            binding.stateLayout.showError(
                context?.getCompatDrawable(R.drawable.ic_emoji_sweat),
                getString(R.string.layout_empty_response),
                getString(R.string.try_again)
            ) { makeRequest() }
        }
    }

    override fun onStart() {
        super.onStart()
        if (model != null)
            updateUI()
        else
            makeRequest()
    }

    override fun makeRequest() {
        val ctx = context ?: return
        val queryContainer = GraphUtil.getDefaultQuery(false)
            .putVariable(KeyUtil.arg_id, id)
        viewModel?.params?.putParcelable(KeyUtil.arg_graph_params, queryContainer)
        viewModel?.requestData(KeyUtil.STAFF_OVERVIEW_REQ, ctx)
    }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.staff_img -> {
                CompatUtil.imagePreview(
                    view,
                    model?.image?.large,
                    R.string.image_preview_error_staff_image
                )
            }
            else -> super.onClick(view)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }

    override fun onChanged(model: StaffBase?) {
        if (model != null)
            this.model = model
        updateUI()
    }
}
