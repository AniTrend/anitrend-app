package com.mxt.anitrend.view.fragment.detail

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.mxt.anitrend.R
import com.mxt.anitrend.base.custom.view.image.AspectImageView
import com.mxt.anitrend.binding.htmlText
import com.mxt.anitrend.databinding.FragmentStaffOverviewBinding
import com.mxt.anitrend.extension.getCompatDrawable
import com.mxt.anitrend.model.api.retro.WebFactory
import com.mxt.anitrend.model.api.retro.anilist.StaffModel
import com.mxt.anitrend.model.entity.base.StaffBase
import com.mxt.anitrend.util.CompatUtil
import com.mxt.anitrend.util.KeyUtil
import com.mxt.anitrend.viewmodel.StaffOverviewViewModel
import kotlinx.coroutines.launch

class StaffOverviewFragment : Fragment() {

    private var _binding: FragmentStaffOverviewBinding? = null
    private val binding get() = _binding!!

    private var staffId: Long = 0
    private var model: StaffBase? = null

    private lateinit var staffOverviewViewModel: StaffOverviewViewModel

    companion object {
        @JvmStatic
        fun newInstance(args: Bundle): StaffOverviewFragment = StaffOverviewFragment().apply {
            arguments = args
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let { args ->
            staffId = args.getLong(KeyUtil.arg_id)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentStaffOverviewBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        staffOverviewViewModel = ViewModelProvider(
            this,
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T =
                    StaffOverviewViewModel(
                        staffService = WebFactory.createService(
                            StaffModel::class.java,
                            requireContext().applicationContext,
                        ),
                    ) as T
            },
        )[StaffOverviewViewModel::class.java]

        binding.stateLayout.showLoading()
        binding.staffImg.setOnClickListener { onImageClick() }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                staffOverviewViewModel.state.collect { state ->
                    when (state) {
                        is StaffOverviewViewModel.UiState.Loading -> {
                            binding.stateLayout.showLoading()
                        }
                        is StaffOverviewViewModel.UiState.Success -> {
                            model = state.staff
                            bindStaff(state.staff)
                        }
                        is StaffOverviewViewModel.UiState.Error -> {
                            binding.stateLayout.showError(
                                requireContext().getCompatDrawable(R.drawable.ic_emoji_sweat),
                                state.message,
                                getString(R.string.try_again),
                            ) { loadStaff() }
                        }
                    }
                }
            }
        }

        loadStaff()
    }

    private fun loadStaff() {
        staffOverviewViewModel.load(staffId)
    }

    private fun bindStaff(staff: StaffBase) {
        AspectImageView.setImage(binding.staffImg, staff.image)
        binding.staffNameText.text = staff.name?.fullName
        binding.staffLanguageText.text = CompatUtil.capitalizeWords(staff.language)
        binding.staffSummaryText.htmlText(staff.description)
        binding.stateLayout.showContent()
    }

    private fun onImageClick() {
        CompatUtil.imagePreview(
            requireView(),
            model?.image?.large,
            R.string.image_preview_error_staff_image,
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
