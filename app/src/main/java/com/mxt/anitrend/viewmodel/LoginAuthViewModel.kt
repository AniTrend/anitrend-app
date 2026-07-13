package com.mxt.anitrend.viewmodel

import android.net.Uri
import androidx.annotation.VisibleForTesting
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mxt.anitrend.BuildConfig
import com.mxt.anitrend.base.custom.async.WebTokenRequest
import com.mxt.anitrend.util.KeyUtil
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.util.concurrent.ExecutionException

class LoginAuthViewModel(
    private val authUriParser: (String) -> AuthCallbackResult = ::parseAuthCallbackResult,
    private val tokenRequester: (String) -> Boolean = WebTokenRequest::getToken,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
) : ViewModel() {
    private val _authState = MutableLiveData<LoginAuthState>()
    val authState: LiveData<LoginAuthState>
        get() = _authState

    fun authenticate(authUri: String) {
        _authState.value = LoginAuthState.Loading
        viewModelScope.launch {
            _authState.value =
                withContext(ioDispatcher) {
                    runCatching {
                        val authCallbackResult = authUriParser(authUri)
                        val authorizationCode = authCallbackResult.authorizationCode

                        if (!authorizationCode.isNullOrBlank()) {
                            val isSuccess = tokenRequester(authorizationCode)
                            if (isSuccess) {
                                LoginAuthState.Success
                            } else {
                                LoginAuthState.Failure(
                                    error = null,
                                    errorDescription = null,
                                )
                            }
                        } else {
                            LoginAuthState.Failure(
                                error = authCallbackResult.error,
                                errorDescription = authCallbackResult.errorDescription,
                            )
                        }
                    }.getOrElse { throwable ->
                        when (throwable) {
                            is ExecutionException,
                            is InterruptedException,
                            -> Timber.e(throwable)
                            else -> Timber.e(throwable)
                        }
                        LoginAuthState.Failure(
                            error = null,
                            errorDescription = throwable.message,
                        )
                    }
                }
        }
    }

    @VisibleForTesting
    data class AuthCallbackResult(
        val authorizationCode: String?,
        val error: String?,
        val errorDescription: String?,
    )

    companion object {
        private fun parseAuthCallbackResult(authUri: String): AuthCallbackResult {
            val authenticatorUri = Uri.parse(authUri)
            return AuthCallbackResult(
                authorizationCode = authenticatorUri.getQueryParameter(BuildConfig.RESPONSE_TYPE),
                error = authenticatorUri.getQueryParameter(KeyUtil.arg_uri_error),
                errorDescription = authenticatorUri.getQueryParameter(KeyUtil.arg_uri_error_description),
            )
        }
    }
}

sealed interface LoginAuthState {
    data object Loading : LoginAuthState

    data object Success : LoginAuthState

    data class Failure(
        val error: String?,
        val errorDescription: String?,
    ) : LoginAuthState
}
