package tv.mycujoo.mls.model

enum class ScreenTimerDirection(var type: String) {
    UP("up"),
    DOWN("down");

    companion object {
        fun fromValue(value: String) =
            values().first { it.type == value }
    }
}
