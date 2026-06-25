package com.mxt.anitrend.widget.progress

enum class ProgressLayoutState {
    CONTENT,
    LOADING,
    ERROR,
    ;

    companion object {
        fun initial(): ProgressLayoutState = CONTENT

        fun transition(
            from: ProgressLayoutState,
            to: ProgressLayoutState,
        ): ProgressLayoutState = when (from) {
            CONTENT ->
                when (to) {
                    CONTENT -> CONTENT
                    LOADING -> LOADING
                    ERROR -> ERROR
                }
            LOADING ->
                when (to) {
                    CONTENT -> CONTENT
                    LOADING -> LOADING
                    ERROR -> ERROR
                }
            ERROR ->
                when (to) {
                    CONTENT -> CONTENT
                    LOADING -> LOADING
                    ERROR -> ERROR
                }
        }
    }
}
