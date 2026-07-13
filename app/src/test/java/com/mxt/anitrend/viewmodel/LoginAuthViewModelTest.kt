package com.mxt.anitrend.viewmodel

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Observer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestWatcher
import org.junit.runner.Description
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

@OptIn(ExperimentalCoroutinesApi::class)
class LoginAuthViewModelTest {
    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun authenticate_emitsLoadingThenUriFailure_whenAuthProviderReturnsErrorParams() {
        val error = "access_denied"
        val errorDescription = "User denied access"
        val recordedStates = collectStates(
            LoginAuthViewModel(
                authUriParser = {
                    LoginAuthViewModel.AuthCallbackResult(
                        authorizationCode = null,
                        error = error,
                        errorDescription = errorDescription,
                    )
                },
            ),
        ) { viewModel ->
            viewModel.authenticate("ignored")
        }

        assertThat(recordedStates[0], `is`(LoginAuthState.Loading))
        assertThat(
            recordedStates[1],
            `is`(
                LoginAuthState.Failure(
                    error = error,
                    errorDescription = errorDescription,
                ),
            )
        )
    }

    @Test
    fun authenticate_emitsLoadingThenEmptyFailure_whenAuthorizationCodeIsBlank() {
        val recordedStates = collectStates(
            LoginAuthViewModel(
                authUriParser = {
                    LoginAuthViewModel.AuthCallbackResult(
                        authorizationCode = "",
                        error = null,
                        errorDescription = null,
                    )
                },
            ),
        ) { viewModel ->
            viewModel.authenticate("ignored")
        }

        assertThat(recordedStates[0], `is`(LoginAuthState.Loading))
        assertThat(
            recordedStates[1],
            `is`(
                LoginAuthState.Failure(
                    error = null,
                    errorDescription = null,
                ),
            ),
        )
    }

    @Test
    fun authenticate_emitsLoadingThenSuccess_whenTokenRequestSucceeds() {
        val recordedStates = collectStates(
            LoginAuthViewModel(
                authUriParser = {
                    LoginAuthViewModel.AuthCallbackResult(
                        authorizationCode = "auth-code",
                        error = null,
                        errorDescription = null,
                    )
                },
                tokenRequester = { true },
            ),
        ) { viewModel ->
            viewModel.authenticate("ignored")
        }

        assertThat(recordedStates[0], `is`(LoginAuthState.Loading))
        assertThat(recordedStates[1], `is`(LoginAuthState.Success))
    }

    @Test
    fun authenticate_emitsLoadingThenFailure_whenTokenRequestThrows() {
        val recordedStates = collectStates(
            LoginAuthViewModel(
                authUriParser = {
                    LoginAuthViewModel.AuthCallbackResult(
                        authorizationCode = "auth-code",
                        error = null,
                        errorDescription = null,
                    )
                },
                tokenRequester = { throw IllegalStateException("forced failure") },
            ),
        ) { viewModel ->
            viewModel.authenticate("ignored")
        }

        assertThat(recordedStates[0], `is`(LoginAuthState.Loading))
        assertThat(
            recordedStates[1],
            `is`(
                LoginAuthState.Failure(
                    error = null,
                    errorDescription = "forced failure",
                ),
            ),
        )
    }

    private fun collectStates(
        viewModel: LoginAuthViewModel,
        expectedCount: Int = 2,
        action: (LoginAuthViewModel) -> Unit,
    ): List<LoginAuthState> {
        val states = CopyOnWriteArrayList<LoginAuthState>()
        val latch = CountDownLatch(expectedCount)
        val observer =
            Observer<LoginAuthState> { state ->
                if (state != null && states.size < expectedCount) {
                    states += state
                    latch.countDown()
                }
            }

        viewModel.authState.observeForever(observer)
        try {
            action(viewModel)
            assertTrue(latch.await(3, TimeUnit.SECONDS))
        } finally {
            viewModel.authState.removeObserver(observer)
        }
        return states
    }

    class MainDispatcherRule(
        private val dispatcher: TestDispatcher = UnconfinedTestDispatcher(),
    ) : TestWatcher() {
        override fun starting(description: Description) {
            Dispatchers.setMain(dispatcher)
        }

        override fun finished(description: Description) {
            Dispatchers.resetMain()
        }
    }
}
