package tv.mycujoo.domain.entity

import org.junit.Test
import tv.mycujoo.domain.entity.models.ActionType
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@ExperimentalStdlibApi
class ActionSourceDataTest {
    @Test
    fun `mapping to ShowOverlayAction`() {

        val id = "id"
        val offset = 1000L
        val absoluteTime = -1L
        val data = buildMap<String, Any> {
            put("svg_url", "sample_url")
            put("duration", 50000L)
            put("animatein_type", "fade_in")
            put("animatein_duration", 3000L)
            put("variable_positions", listOf("v1", "v2"))
        }
        val actionSourceData =
            ActionSourceData(id, ActionType.SHOW_OVERLAY.type, offset, absoluteTime, data)


        val action = actionSourceData.toAction()


        assertTrue { action is Action.ShowOverlayAction }
        val showOverlayAction = action as Action.ShowOverlayAction
        assertEquals(id, action.id)
        assertEquals(offset, action.offset)
        assertEquals(absoluteTime, action.absoluteTime)
        assertEquals(data["svg_url"], showOverlayAction.svgData?.svgUrl)
        assertEquals(data["duration"], showOverlayAction.duration)
        assertEquals(
            data["animatein_type"],
            showOverlayAction.introAnimationSpec?.animationType?.type
        )
        assertEquals(
            data["animatein_duration"],
            showOverlayAction.introAnimationSpec?.animationDuration
        )
        assertEquals(data["variable_positions"], showOverlayAction.placeHolders)
    }

    @Test
    fun `mapping to HideOverlayAction`() {
        val id = "id"
        val offset = 1000L
        val absoluteTime = -1L
        val data = buildMap<String, Any> {
            put("animateout_type", "fade_out")
            put("animateout_duration", 3000L)
            put("custom_id", "scoreboard1")
        }

        val actionSourceData =
            ActionSourceData(id, ActionType.HIDE_OVERLAY.type, offset, absoluteTime, data)


        val action = actionSourceData.toAction()


        assertTrue { action is Action.HideOverlayAction }
        val hideOverlayAction = action as Action.HideOverlayAction
        assertEquals(id, action.id)
        assertEquals(offset, action.offset)
        assertEquals(absoluteTime, action.absoluteTime)
        assertEquals(
            data["animateout_type"],
            hideOverlayAction.outroAnimationSpec?.animationType?.type
        )
        assertEquals(
            data["animateout_duration"],
            hideOverlayAction.outroAnimationSpec?.animationDuration
        )
        assertEquals(data["custom_id"], hideOverlayAction.customId)
    }


    @Test
    fun `mapping to CreateTimerAction`() {
        val id = "id"
        val offset = 1000L
        val absoluteTime = -1L
        val data = buildMap<String, Any> {
            put("name", "timer1")
            put("format", "ms")
            put("direction", "up")
            put("start_value", 0L)
            put("cap_value", -1L)
        }
        val actionSourceData =
            ActionSourceData(id, ActionType.CREATE_TIMER.type, offset, absoluteTime, data)


        val action = actionSourceData.toAction()


        assertTrue { action is Action.CreateTimerAction }
        val createTimerAction = action as Action.CreateTimerAction
        assertEquals(id, action.id)
        assertEquals(offset, action.offset)
        assertEquals(absoluteTime, action.absoluteTime)
        assertEquals(data["name"], createTimerAction.name)
        assertEquals(data["format"], createTimerAction.format.type)
        assertEquals(data["direction"], createTimerAction.direction.type)
        assertEquals(data["start_value"], createTimerAction.startValue)
        assertEquals(data["cap_value"], createTimerAction.capValue)
    }

    @Test
    fun `mapping to StartTimerAction`() {
        val id = "id"
        val offset = 1000L
        val absoluteTime = -1L
        val data = buildMap<String, Any> {
            put("name", "timer")
        }
        val actionSourceData =
            ActionSourceData(id, ActionType.START_TIMER.type, offset, absoluteTime, data)


        val action = actionSourceData.toAction()


        assertTrue { action is Action.StartTimerAction }
        val startTimerAction = action as Action.StartTimerAction
        assertEquals(id, action.id)
        assertEquals(offset, action.offset)
        assertEquals(absoluteTime, action.absoluteTime)
        assertEquals(data["name"], startTimerAction.name)
    }


    @Test
    fun `mapping to PauseTimerAction`() {
        val id = "id"
        val offset = 1000L
        val absoluteTime = -1L
        val data = buildMap<String, Any> {
            put("name", "timer")
        }
        val actionSourceData =
            ActionSourceData(id, ActionType.PAUSE_TIMER.type, offset, absoluteTime, data)

        val action = actionSourceData.toAction()


        assertTrue { action is Action.PauseTimerAction }
        val pauseTimerAction = action as Action.PauseTimerAction
        assertEquals(id, action.id)
        assertEquals(offset, action.offset)
        assertEquals(absoluteTime, action.absoluteTime)
        assertEquals(data["name"], pauseTimerAction.name)
    }

    @Test
    fun `mapping to AdjustTimerAction`() {
        val id = "id"
        val offset = 1000L
        val absoluteTime = -1L
        val data = buildMap<String, Any> {
            put("name", "timer")
            put("value", 3000L)
        }
        val actionSourceData =
            ActionSourceData("id", ActionType.ADJUST_TIMER.type, offset, absoluteTime, data)

        val action = actionSourceData.toAction()
        assertTrue { action is Action.AdjustTimerAction }
        val adjustTimerAction = action as Action.AdjustTimerAction
        assertEquals(id, action.id)
        assertEquals(offset, action.offset)
        assertEquals(absoluteTime, action.absoluteTime)
        assertEquals(data["name"], adjustTimerAction.name)
        assertEquals(data["value"], adjustTimerAction.value)
    }

    @Test
    fun `mapping to SkipTimerAction`() {
        val id = "id"
        val offset = 1000L
        val absoluteTime = -1L
        val data = buildMap<String, Any> {
            put("name", "timer")
            put("value", 3000L)
        }
        val actionSourceData =
            ActionSourceData(id, ActionType.SKIP_TIMER.type, offset, absoluteTime, data)

        val action = actionSourceData.toAction()
        assertTrue { action is Action.SkipTimerAction }
        val skipTimerAction = action as Action.SkipTimerAction
        assertEquals(id, action.id)
        assertEquals(offset, action.offset)
        assertEquals(absoluteTime, action.absoluteTime)
        assertEquals(data["name"], skipTimerAction.name)
        assertEquals(data["value"], skipTimerAction.value)


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