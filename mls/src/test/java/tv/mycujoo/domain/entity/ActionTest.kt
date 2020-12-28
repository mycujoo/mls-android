package tv.mycujoo.domain.entity

import org.junit.Assert.assertEquals
import org.junit.Test

class ActionTest {

    @Test
    fun `ShowOverlayAction updateOffset test`() {
        val introTransitionSpec = TransitionSpec(0L, AnimationType.FADE_IN, 1000L)
        val outroTransitionSpec = TransitionSpec(0L, AnimationType.FADE_OUT, 1000L)
        val showOverlayAction =
            Action.ShowOverlayAction(
                "id_00",
                0L,
                55550000L,
                outroTransitionSpec = outroTransitionSpec,
                introTransitionSpec = introTransitionSpec
            )

        val newOffset = 2000L
        val showOverlayActionWithUpdatedOffset =
            showOverlayAction.updateOffset(newOffset)

        assertEquals(newOffset, showOverlayActionWithUpdatedOffset.offset)
        assertEquals(newOffset, showOverlayActionWithUpdatedOffset.introTransitionSpec!!.offset)
        assertEquals(newOffset, showOverlayActionWithUpdatedOffset.outroTransitionSpec!!.offset)
    }

    @Test
    fun `HideOverlayAction updateOffset test`() {
        val outroTransitionSpec = TransitionSpec(0L, AnimationType.FADE_OUT, 1000L)
        val hideOverlayAction =
            Action.HideOverlayAction("id_00", 0L, 55550000L, outroTransitionSpec, "cid_00")

        val newOffset = 2000L
        val hideOverlayActionWithUpdatedOffset =
            hideOverlayAction.updateOffset(newOffset)

        assertEquals(newOffset, hideOverlayActionWithUpdatedOffset.offset)
        assertEquals(newOffset, hideOverlayActionWithUpdatedOffset.outroTransitionSpec!!.offset)
    }

    @Test
    fun `ReshowOverlayAction updateOffset test`() {
        val reshowOverlayAction =
            Action.ReshowOverlayAction("id_00", 0L, 55550000L, "cid_00")

        val newOffset = 2000L
        val reshowOverlayActionWithUpdatedOffset =
            reshowOverlayAction.updateOffset(newOffset)

        assertEquals(newOffset, reshowOverlayActionWithUpdatedOffset.offset)
    }

    @Test
    fun `CreateTimerAction updateOffset test`() {
        val createTimerAction =
            Action.CreateTimerAction("id_00", 0L, 55550000L, "name", capValue = -1L)

        val newOffset = 2000L
        val createTimerActionWithUpdatedOffset =
            createTimerAction.updateOffset(newOffset)

        assertEquals(newOffset, createTimerActionWithUpdatedOffset.offset)
    }

    @Test
    fun `StartTimerAction updateOffset test`() {
        val startTimerAction =
            Action.StartTimerAction("id_00", 0L, 55550000L, "name")

        val newOffset = 2000L
        val startTimerActionWithUpdatedOffset =
            startTimerAction.updateOffset(newOffset)

        assertEquals(newOffset, startTimerActionWithUpdatedOffset.offset)
    }


    @Test
    fun `PauseTimerAction updateOffset test`() {
        val pauseTimerAction =
            Action.PauseTimerAction("id_00", 0L, 55550000L, "name")

        val newOffset = 2000L
        val pauseTimerActionWithUpdatedOffset =
            pauseTimerAction.updateOffset(newOffset)

        assertEquals(newOffset, pauseTimerActionWithUpdatedOffset.offset)
    }

    @Test
    fun `AdjustTimerAction updateOffset test`() {
        val adjustTimerAction =
            Action.AdjustTimerAction("id_00", 0L, 55550000L, "name", 0L)

        val newOffset = 2000L
        val adjustTimerActionWithUpdatedOffset =
            adjustTimerAction.updateOffset(newOffset)

        assertEquals(newOffset, adjustTimerActionWithUpdatedOffset.offset)
    }

    @Test
    fun `SkipTimerAction updateOffset test`() {
        val skipTimerAction =
            Action.SkipTimerAction("id_00", 0L, 55550000L, "name", 0L)

        val newOffset = 2000L
        val skipTimerActionWithUpdatedOffset =
            skipTimerAction.updateOffset(newOffset)

        assertEquals(newOffset, skipTimerActionWithUpdatedOffset.offset)
    }

    @Test
    fun `CreateVariableAction updateOffset test`() {
        val createVariableAction =
            Action.CreateVariableAction("id_00", 0L, 55550000L, Variable.LongVariable("name", 0L))

        val newOffset = 2000L
        val createVariableActionWithUpdatedOffset =
            createVariableAction.updateOffset(newOffset)

        assertEquals(newOffset, createVariableActionWithUpdatedOffset.offset)
    }

    @Test
    fun `IncrementVariableAction updateOffset test`() {
        val incrementVariableAction =
            Action.IncrementVariableAction("id_00", 0L, 55550000L, "name", 1.toDouble())

        val newOffset = 2000L
        val incrementVariableActionWithUpdatedOffset =
            incrementVariableAction.updateOffset(newOffset)

        assertEquals(newOffset, incrementVariableActionWithUpdatedOffset.offset)
    }

    @Test
    fun `MarkTimelineAction updateOffset test`() {
        val markTimelineAction =
            Action.MarkTimelineAction("id_00", 0L, 55550000L, 1000L, "Goal!", "#ffffff")

        val newOffset = 2000L
        val markTimelineActionWithUpdatedOffset = markTimelineAction.updateOffset(newOffset)

        assertEquals(newOffset, markTimelineActionWithUpdatedOffset.offset)
    }

    @Test
    fun `DeleteAction updateOffset test`() {
        val deleteAction = Action.DeleteAction("id_00", 0L, 55550000L, "targetId_00")

        val newOffset = 2000L
        val deleteActionWithUpdatedOffset = deleteAction.updateOffset(newOffset)

        assertEquals(newOffset, deleteActionWithUpdatedOffset.offset)
    }

    @Test
    fun `InvalidAction updateOffset test`() {
        val invalidAction = Action.InvalidAction("id_00", 0L, 55550000L)

        val newOffset = 2000L
        val invalidActionWithUpdatedTime = invalidAction.updateOffset(newOffset)

        assertEquals(newOffset, invalidActionWithUpdatedTime.offset)
    }
}