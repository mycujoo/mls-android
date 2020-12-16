package tv.mycujoo.domain.entity

import org.junit.Test
import tv.mycujoo.domain.entity.models.ActionType
import kotlin.test.assertTrue

class ActionSourceDataTest {
    @Test
    fun `mapping to ShowOverlayAction`() {
        val actionSourceData =
            ActionSourceData("id", ActionType.SHOW_OVERLAY.type, 1000L, -1L, null)

        assertTrue { actionSourceData.toAction() is Action.ShowOverlayAction }
    }

    @Test
    fun `mapping to HideOverlayAction`() {
        val actionSourceData =
            ActionSourceData("id", ActionType.HIDE_OVERLAY.type, 1000L, -1L, null)

        assertTrue { actionSourceData.toAction() is Action.HideOverlayAction }
    }


    @Test
    fun `mapping to CreateTimerAction`() {
        val actionSourceData =
            ActionSourceData("id", ActionType.CREATE_TIMER.type, 1000L, -1L, null)

        assertTrue { actionSourceData.toAction() is Action.CreateTimerAction }
    }

    @Test
    fun `mapping to StartTimerAction`() {
        val actionSourceData = ActionSourceData("id", ActionType.START_TIMER.type, 1000L, -1L, null)

        assertTrue { actionSourceData.toAction() is Action.StartTimerAction }
    }

    @Test
    fun `mapping to PauseTimerAction`() {
        val actionSourceData = ActionSourceData("id", ActionType.PAUSE_TIMER.type, 1000L, -1L, null)

        assertTrue { actionSourceData.toAction() is Action.PauseTimerAction }
    }

    @Test
    fun `mapping to SkipTimerAction`() {
        val actionSourceData = ActionSourceData("id", ActionType.SKIP_TIMER.type, 1000L, -1L, null)

        assertTrue { actionSourceData.toAction() is Action.SkipTimerAction }
    }

    @Test
    fun `mapping to AdjustTimerAction`() {
        val actionSourceData =
            ActionSourceData("id", ActionType.ADJUST_TIMER.type, 1000L, -1L, null)

        assertTrue { actionSourceData.toAction() is Action.AdjustTimerAction }
    }

    @Test
    fun `mapping to CreateVariableAction`() {
        val actionSourceData =
            ActionSourceData("id", ActionType.SET_VARIABLE.type, 1000L, -1L, null)

        assertTrue { actionSourceData.toAction() is Action.CreateVariableAction }
    }

    @Test
    fun `mapping to IncrementVariableAction`() {
        val actionSourceData =
            ActionSourceData("id", ActionType.INCREMENT_VARIABLE.type, 1000L, -1L, null)

        assertTrue { actionSourceData.toAction() is Action.IncrementVariableAction }
    }

    @Test
    fun `mapping to MarkTimelineAction`() {
        val actionSourceData =
            ActionSourceData("id", ActionType.SHOW_TIMELINE_MARKER.type, 1000L, -1L, null)

        assertTrue { actionSourceData.toAction() is Action.MarkTimelineAction }
    }

    @Test
    fun `mapping to DeleteAction`() {
        val actionSourceData =
            ActionSourceData("id", ActionType.DELETE_ACTION.type, 1000L, -1L, null)

        assertTrue { actionSourceData.toAction() is Action.DeleteAction }
    }

}