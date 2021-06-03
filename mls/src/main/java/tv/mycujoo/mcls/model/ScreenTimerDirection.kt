package tv.mycujoo.mcls.model

/**
 * Direction of Timer, which indicates it will increase or decrees over time
 */
enum class ScreenTimerDirection(var type: String) {
    UP("up"),
    DOWN("down");

    companion object {
        fun fromValue(value: String) =
            values().first { it.type == value }
    }
}
