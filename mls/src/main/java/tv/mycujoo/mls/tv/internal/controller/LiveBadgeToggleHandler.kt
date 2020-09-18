package tv.mycujoo.mls.tv.internal.controller

class LiveBadgeToggleHandler {

    private lateinit var listenerBadge: ILiveBadgeToggleListener

    fun addListener(listenerBadge: ILiveBadgeToggleListener) {
        this.listenerBadge = listenerBadge
    }

    fun toggle(state: Boolean) {
        if (this::listenerBadge.isInitialized.not()) {
            return
        }

        listenerBadge.onToggled(state)
    }


}