package com.mxt.anitrend.view.fragment.detail

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.mxt.anitrend.R
import com.mxt.anitrend.base.custom.fragment.FragmentBase
import com.mxt.anitrend.base.custom.view.image.AspectImageView
import com.mxt.anitrend.binding.htmlText
import com.mxt.anitrend.databinding.FragmentCharacterOverviewBinding
import com.mxt.anitrend.extension.getCompatDrawable
import com.mxt.anitrend.model.entity.anilist.MediaCharacter
import com.mxt.anitrend.presenter.base.BasePresenter
import com.mxt.anitrend.util.CompatUtil
import com.mxt.anitrend.util.KeyUtil

/**
 * Created by max on 2018/01/30.
 * CharacterOverviewFragment
 */
class CharacterOverviewFragment : FragmentBase<MediaCharacter, BasePresenter, MediaCharacter>() {
    private var model: MediaCharacter? = null
    private var binding: FragmentCharacterOverviewBinding? = null
    private var id: Long = 0

    companion object {
        @JvmStatic
        fun newInstance(args: Bundle): CharacterOverviewFragment = CharacterOverviewFragment().apply {
            arguments = args
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
        savedInstanceState: Bundle?,
    ): View? {
        binding = FragmentCharacterOverviewBinding.inflate(inflater, container, false)
        binding?.stateLayout?.showLoading()
        binding?.characterImg?.setOnClickListener(this)
        return binding?.root
    }

    override fun updateUI() {
        val binding = binding ?: return
        val model = model
        if (model != null) {
            AspectImageView.setImage(binding.characterImg, model.image)
            binding.characterNameText.text = model.name?.fullName
            binding.characterNativeText.text = model.name?.original
            binding.characterAlternativeText.htmlText(model.name?.alternativeFormatted)
            binding.characterSummaryText.htmlText(model.description)
            binding.stateLayout.showContent()
        } else {
            binding.stateLayout.showError(
                context?.getCompatDrawable(R.drawable.ic_warning_white_18dp, R.color.colorStateBlue),
                getString(R.string.layout_empty_response),
                getString(R.string.try_again),
            ) { makeRequest() }
        }
    }

    override fun onStart() {
        super.onStart()
        if (model != null) {
            updateUI()
        } else {
            makeRequest()
        }
    }

    override fun makeRequest() {
        val ctx = context ?: return
        viewModel?.params?.apply {
            putLong(KeyUtil.arg_id, id)
            putBoolean(KeyUtil.arg_asHtml, false)
        }
        viewModel?.requestData(KeyUtil.CHARACTER_OVERVIEW_REQ, ctx)
    }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.character_img -> {
                CompatUtil.imagePreview(
                    view,
                    model?.image?.large,
                    R.string.image_preview_error_character_image,
                )
            }
            else -> super.onClick(view)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }

    override fun onChanged(model: MediaCharacter?) {
        if (model != null) {
            this.model = model
        }
        updateUI()
    }
}
