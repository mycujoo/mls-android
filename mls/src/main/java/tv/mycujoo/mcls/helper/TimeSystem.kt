package tv.mycujoo.mcls.helper

/**
 * 2 Types of time system may be used in SDK.
 * Absolute means the given offset is actually value since the beginning of video.
 * Relative is calculated based on current 'dvr' window size
 */
enum class TimeSystem {
    RELATIVE,
    ABSOLUTE
}