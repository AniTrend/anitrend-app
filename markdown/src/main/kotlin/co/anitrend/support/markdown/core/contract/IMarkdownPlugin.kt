package co.anitrend.support.markdown.core.contract

/**
 * Plugin contract
 *
 * @since 0.1.0
 */
interface IMarkdownPlugin {
    /**
     * Regular expression that should be used for the implementing classing
     */
    val regex: Regex


    companion object {
        const val NO_THUMBNAIL = "http://placehold.it/1280x720?text=No+Preview+Available"
    }
}