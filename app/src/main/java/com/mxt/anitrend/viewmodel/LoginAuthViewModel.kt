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

/**
 * Owns the login authentication callback flow and exposes a coroutine-backed
 * authentication state for the login screen.
 */
class LoginAuthViewModel(
    private val authUriParser: (String) -> AuthCallbackResult = ::parseAuthCallbackResult,
    private val tokenRequester: (String) -> Boolean = WebTokenRequest::getToken,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
) : ViewModel() {
    private val _authState = MutableLiveData<LoginAuthState>()
    val authState: LiveData<LoginAuthState>
        get() = _authState

    /**
     * Parses the OAuth callback URI and updates [authState] with the resulting
     * authentication outcome.
     */
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
    /**
     * Parsed authentication callback payload used by the ViewModel.
     */
    data class AuthCallbackResult(val authorizationCode: String?, val error: String?, val errorDescription: String?)

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

/**
 * UI state emitted while the login authentication callback is being resolved.
 */
sealed interface LoginAuthState {
    /** Authentication request is currently in progress. */
    data object Loading : LoginAuthState

    /** Authentication request completed successfully. */
    data object Success : LoginAuthState

    /** Authentication request failed or returned an OAuth error payload. */
    data class Failure(
        val error: String?,
        val errorDescription: String?,
    ) : LoginAuthState
}
