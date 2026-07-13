package com.mxt.anitrend.base.custom.viewmodel

import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.mxt.anitrend.base.interfaces.event.ResponseCallback

@Suppress("UNCHECKED_CAST")
internal fun <T> Fragment.acquireTypedViewModelBase(
    observer: Observer<T?>,
    stateSupported: Boolean,
    state: ResponseCallback?,
): ViewModelBase<T> {
    val viewModel = ViewModelProvider(this).get(ViewModelBase::class.java) as ViewModelBase<T>
    viewModel.setContext(requireContext())
    if (viewModel.model.hasActiveObservers() == false) {
        viewModel.model.observe(this, observer)
    }
    if (stateSupported) {
        viewModel.state = state
    }
    return viewModel
}
