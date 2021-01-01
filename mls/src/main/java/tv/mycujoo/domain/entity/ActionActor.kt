package tv.mycujoo.domain.entity

import tv.mycujoo.mls.enum.C

class ActionActor() {
    /**
     * 1/ Only actions which are until now or within range should be provided to this method.
     * Actions which belong to future (+1000ms and more) should NOT be provided.
     * Use untilNowOrInRange method on Action to filter for this criteria.
     *
     * 2/ Actions should be provided sorted based on their offset
     */
    fun act(
        now: Long,
        showMap: MutableMap<String, Action.ShowOverlayAction>,
        hideMap: MutableMap<String, Action.HideOverlayAction>
    ): List<Pair<ActionAct, Action>> {
        val list = arrayListOf<Pair<ActionAct, Action>>()
        showMap.forEach { entry ->
            if (hideMap.any { it.key == entry.key && it.value.customId == entry.value.customId }) {
                val hideOverlayAction = hideMap.remove(entry.key)!!
                if (hideOverlayAction.offset >= entry.value.offset) {
                    list.add(Pair(ActionAct.REMOVE, hideOverlayAction))
                } else {
                    list.add(Pair(ActionAct.INTRO, entry.value))
                }
                return@forEach
            }
            list.add(Pair(ActionAct.INTRO, entry.value))
        }
        hideMap.forEach { entry ->
            list.add(Pair(ActionAct.REMOVE, entry.value))
        }

        val returnList = arrayListOf<Pair<ActionAct, Action>>()

        list.forEach { pair ->
            when (val action = pair.second) {
                is Action.ShowOverlayAction -> {
                    val act =
                        getCurrentAct(now, action)

                    returnList.add(Pair(act, action))
                }
                is Action.HideOverlayAction -> {
                    returnList.add(Pair(pair.first, action))
                }
                else -> {
                    // should not happen
                }
            }

        }


        return returnList
    }

    private fun getCurrentAct(
        currentTime: Long,
        action: Action.ShowOverlayAction
    ): ActionAct {

        fun isIntro(
            currentTime: Long,
            action: Action
        ): Boolean {
            return (action.offset >= currentTime) && (action.offset < currentTime + C.ONE_SECOND_IN_MS)
        }

        fun isOutro(
            currentTime: Long,
            action: Action.ShowOverlayAction
        ): Boolean {
            val bound: Long
            if (action.outroTransitionSpec != null) {
                bound =
                    action.outroTransitionSpec.offset
            } else {
                bound = action.offset + (action.duration ?: 0L)
            }
            return currentTime < bound && currentTime + C.ONE_SECOND_IN_MS > bound
        }

        // action belongs to the past
        fun isAforetime(currentTime: Long, action: Action.ShowOverlayAction): Boolean {
            if (action.outroTransitionSpec != null) {
                return currentTime > action.outroTransitionSpec.offset + action.outroTransitionSpec.animationDuration
            } else {
                return currentTime > action.offset + (action.duration ?: 0L)
            }
        }

        fun isMidway(
            currentTime: Long,
            action: Action.ShowOverlayAction
        ): Boolean {
            var leftBound = action.offset
            if (action.introTransitionSpec != null) {
                leftBound =
                    action.offset + action.introTransitionSpec.animationDuration
            }
            var rightBound = Long.MAX_VALUE
            if (action.outroTransitionSpec != null) {
                rightBound = action.outroTransitionSpec.offset
            }
            if (action.duration != null) {
                rightBound =
                    action.offset + action.duration
            }

            return (currentTime > leftBound) && (currentTime < rightBound)
        }

        if (isIntro(currentTime, action)) {
            return ActionAct.INTRO
        }
        if (isMidway(currentTime, action)) {
            return ActionAct.MIDWAY
        }
        if (isOutro(currentTime, action)) {
            return ActionAct.OUTRO
        }
        if (isAforetime(currentTime, action)) {
            return ActionAct.REMOVE
        }

        return ActionAct.DO_NOTHING

    }


    enum class ActionAct {

        INTRO,
        MIDWAY,
        OUTRO,
        REMOVE,
        DO_NOTHING;
    }
}


