package tv.mycujoo.domain.entity

import tv.mycujoo.mcls.enum.C

class ActionActor {
    /**
     * Actions should be provided sorted based on their offset
     */
    fun act(
        now: Long,
        showOverlayActions: ArrayList<Action.ShowOverlayAction>,
        hideOverlayActions: ArrayList<Action.HideOverlayAction>
    ): List<Pair<ActionAct, Action>> {
        val showMap = mutableMapOf<String, Action.ShowOverlayAction>()
        val hideMap = mutableMapOf<String, Action.HideOverlayAction>()

        val filteredShowOverlayActions: ArrayList<Action.ShowOverlayAction> = arrayListOf()
        val afterwardsShowOverlayActions: ArrayList<Action.ShowOverlayAction> = arrayListOf()

        showOverlayActions.forEach {
            if (it.isTillNowOrInRange(now)) {
                filteredShowOverlayActions.add(it)
            } else {
                afterwardsShowOverlayActions.add(it)
            }
        }

        filteredShowOverlayActions.sortBy { it.offset }

        filteredShowOverlayActions.forEach { action ->
            val relatedHideOverlayAction =
                hideOverlayActions.find { it.customId == action.customId }
            if (relatedHideOverlayAction != null) {
                hideOverlayActions.remove(relatedHideOverlayAction)
                if (relatedHideOverlayAction.offset >= action.offset) {
                    showMap.remove(relatedHideOverlayAction.customId)
                    hideMap[relatedHideOverlayAction.customId] = relatedHideOverlayAction
                } else {
                    hideMap.remove(action.customId)
                    showMap[action.customId] = action
                }
            } else {
                hideMap.remove(action.customId)
                showMap[action.customId] = action
            }
        }
        hideOverlayActions.forEach { action ->
            hideMap[action.customId] = action
        }

        val returnList = arrayListOf<Pair<ActionAct, Action>>()
        showMap.forEach {
            val act = getCurrentAct(now, it.value)
            returnList.add(Pair(act, it.value))
        }

        hideMap.forEach {
            val act = getCurrentAct(now, it.value)
            returnList.add(Pair(act, it.value))
        }

        afterwardsShowOverlayActions.forEach { afterwardsShowOverlayAction ->
            if (returnList.none { isPresentInReturningPairList(it, afterwardsShowOverlayAction) }
            ) {
                returnList.add(Pair(ActionAct.REMOVE, afterwardsShowOverlayAction))
            }
        }
        return returnList
    }

    private fun isPresentInReturningPairList(
        it: Pair<ActionAct, Action>,
        afterwardsShowOverlayAction: Action.ShowOverlayAction
    ) =
        it.second is Action.ShowOverlayAction && (it.second as Action.ShowOverlayAction).customId == afterwardsShowOverlayAction.customId ||
                it.second is Action.HideOverlayAction && (it.second as Action.HideOverlayAction).customId == afterwardsShowOverlayAction.customId

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

            return (currentTime > leftBound) && (currentTime + C.ONE_SECOND_IN_MS < rightBound)
        }

        fun isLingeringIntro(
            currentTime: Long,
            action: Action.ShowOverlayAction
        ): Boolean {

            if (action.introTransitionSpec == null || action.introTransitionSpec.animationDuration <= 0L) {
                return false
            }

            val leftBound = action.offset
            val rightBound =
                action.offset + action.introTransitionSpec.animationDuration

            return (leftBound <= currentTime) && (currentTime < rightBound)
        }

        fun isLingeringOutro(
            currentTime: Long,
            action: Action.ShowOverlayAction
        ): Boolean {

            if (action.outroTransitionSpec == null || action.outroTransitionSpec.animationDuration == -1L) {
                return false
            }

            val leftBound = action.outroTransitionSpec.offset
            val rightBound =
                action.outroTransitionSpec.offset + action.outroTransitionSpec.animationDuration

            return (leftBound <= currentTime) && (currentTime < rightBound)
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
        if (isLingeringIntro(currentTime, action)) {
            return ActionAct.LINGERING_INTRO
        }
        if (isAforetime(currentTime, action)) {
            return ActionAct.REMOVE
        }

        return ActionAct.DO_NOTHING

    }

    private fun getCurrentAct(
        currentTime: Long,
        action: Action.HideOverlayAction
    ): ActionAct {

        fun outroIsInCurrentTimeRange(
            currentTime: Long,
            hideOverlayAction: Action.HideOverlayAction
        ): Boolean {
            val outroOffset =
                hideOverlayAction.offset

            return (outroOffset >= currentTime) && (outroOffset < currentTime + C.ONE_SECOND_IN_MS)
        }


        if (outroIsInCurrentTimeRange(currentTime, action)) {
            return ActionAct.OUTRO
        }

        return ActionAct.REMOVE
    }


    enum class ActionAct {

        INTRO,
        MIDWAY,
        OUTRO,
        LINGERING_INTRO,
        REMOVE,
        DO_NOTHING;
    }
}


