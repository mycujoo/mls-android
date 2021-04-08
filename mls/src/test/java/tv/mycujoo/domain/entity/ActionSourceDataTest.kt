package tv.mycujoo.domain.entity

import org.junit.Test
import tv.mycujoo.domain.entity.models.ActionType
import tv.mycujoo.mcls.TestData.Companion.samplePosition
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@ExperimentalStdlibApi
class ActionSourceDataTest {
    /**region 'Show' Overlay related*/
    @Test
    fun `mapping to ShowOverlayAction`() {

        val id = "id"
        val offset = 1000L
        val absoluteTime = -1L
        val data = buildMap<String, Any> {
            put("svg_url", "sample_url")
            put("duration", 50000L)
            put("position", samplePosition())
            put("animatein_type", "fade_in")
            put("animatein_duration", 3000L)
            put("variables", listOf("v1", "v2"))
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
            showOverlayAction.introTransitionSpec?.animationType?.type
        )
        assertEquals(
            data["animatein_duration"],
            showOverlayAction.introTransitionSpec?.animationDuration
        )
        assertEquals(data["variables"], showOverlayAction.placeHolders)
    }

    @Test
    fun `mapping to ShowOverlayAction with no svg_url`() {

        val id = "id"
        val offset = 1000L
        val absoluteTime = -1L
        val data = buildMap<String, Any> {
            // svg_url is NOT provided
            put("duration", 50000L)
            put("animatein_type", "fade_in")
            put("animatein_duration", 3000L)
            put("variables", listOf("v1", "v2"))
        }
        val actionSourceData =
            ActionSourceData(id, ActionType.SHOW_OVERLAY.type, offset, absoluteTime, data)


        val action = actionSourceData.toAction()


        assertTrue { action is Action.InvalidAction }
    }

    @Test
    fun `mapping to ShowOverlayAction with no position`() {

        val id = "id"
        val offset = 1000L
        val absoluteTime = -1L
        val data = buildMap<String, Any> {
            put("svg_url", "sample_url")
            put("duration", 50000L)
            // position is NOT provided
            put("animatein_type", "fade_in")
            put("animatein_duration", 3000L)
            put("variables", listOf("v1", "v2"))
        }
        val actionSourceData =
            ActionSourceData(id, ActionType.SHOW_OVERLAY.type, offset, absoluteTime, data)


        val action = actionSourceData.toAction()


        assertTrue { action is Action.InvalidAction }
    }
    /**endregion */

    /**region 'Hide' Overlay related*/
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
            hideOverlayAction.outroTransitionSpec?.animationType?.type
        )
        assertEquals(
            data["animateout_duration"],
            hideOverlayAction.outroTransitionSpec?.animationDuration
        )
        assertEquals(data["custom_id"], hideOverlayAction.customId)
    }

    @Test
    fun `mapping to HideOverlayAction with no custom_id`() {
        val id = "id"
        val offset = 1000L
        val absoluteTime = -1L
        val data = buildMap<String, Any> {
            put("animateout_type", "fade_out")
            put("animateout_duration", 3000L)
            // custom_id is not provided
        }

        val actionSourceData =
            ActionSourceData(id, ActionType.HIDE_OVERLAY.type, offset, absoluteTime, data)


        val action = actionSourceData.toAction()


        assertTrue { action is Action.InvalidAction }
    }

    /**endregion */

    /**region Reshow-Overlay related*/
    @Test
    fun `mapping to ReshowOverlayAction`() {
        val id = "id_1"
        val offset = 1000L
        val absoluteTime = -1L
        val data = buildMap<String, Any> {
            put("custom_id", "id_0")
        }
        val actionSourceData =
            ActionSourceData(id, ActionType.RESHOW_OVERLAY.type, offset, absoluteTime, data)

        val action = actionSourceData.toAction()


        assertTrue { action is Action.ReshowOverlayAction }
    }

    @Test
    fun `mapping to ReshowOverlayAction without customId`() {
        val id = "id_1"
        val offset = 1000L
        val absoluteTime = -1L
        val data = buildMap<String, Any> {
            // custom_id is not provided
        }
        val actionSourceData =
            ActionSourceData(id, ActionType.RESHOW_OVERLAY.type, offset, absoluteTime, data)

        val action = actionSourceData.toAction()


        assertTrue { action is Action.InvalidAction }
    }
    /**endregion */

    /**region Timer related*/
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
    /**endregion */

    /**region Variable related*/
    @Test
    fun `mapping to CreateVariableAction`() {
        val id = "id"
        val offset = 1000L
        val absoluteTime = -1L
        val data = buildMap<String, Any> {
            put("name", "var1")
            put("value", 0L)
            put("type", "long")
        }
        val actionSourceData =
            ActionSourceData(id, ActionType.SET_VARIABLE.type, offset, absoluteTime, data)

        val action = actionSourceData.toAction()
        assertTrue { action is Action.CreateVariableAction }
        val createVariableAction = action as Action.CreateVariableAction
        assertEquals(id, action.id)
        assertEquals(offset, action.offset)
        assertEquals(absoluteTime, action.absoluteTime)
        assertEquals(data["name"], createVariableAction.variable.name)
        assertEquals(data["value"].toString(), createVariableAction.variable.printValue())
    }

    @Test
    fun `mapping to IncrementVariableAction`() {
        val id = "id"
        val offset = 1000L
        val absoluteTime = -1L
        val data = buildMap<String, Any> {
            put("name", "var1")
            put("amount", 5000.toDouble())
        }
        val actionSourceData =
            ActionSourceData(id, ActionType.INCREMENT_VARIABLE.type, offset, absoluteTime, data)


        val action = actionSourceData.toAction()


        assertTrue { action is Action.IncrementVariableAction }
        val incrementVariableAction = action as Action.IncrementVariableAction
        assertEquals(id, action.id)
        assertEquals(offset, action.offset)
        assertEquals(data["name"], incrementVariableAction.name)
        assertEquals(data["amount"], incrementVariableAction.amount)
    }
    /**endregion */

    /**region MarkTimeline related*/
    @Test
    fun `mapping to MarkTimelineAction`() {
        val id = "id"
        val offset = 1000L
        val absoluteTime = -1L
        val data = buildMap<String, Any> {
            put("seek_offset", 1000.toLong())
            put("label", "Goal")
            put("color", "#ffffff")
        }
        val actionSourceData =
            ActionSourceData(id, ActionType.SHOW_TIMELINE_MARKER.type, offset, absoluteTime, data)


        val action = actionSourceData.toAction()


        assertTrue { action is Action.MarkTimelineAction }
        val markTimelineAction = action as Action.MarkTimelineAction
        assertEquals(data["seek_offset"], markTimelineAction.seekOffset)
        assertEquals(data["label"], markTimelineAction.label)
        assertEquals(data["color"], markTimelineAction.color)
    }

    @Test
    fun `mapping to MarkTimelineAction with no label`() {
        val id = "id"
        val offset = 1000L
        val absoluteTime = -1L
        val data = buildMap<String, Any> {
            put("seek_offset", 1000.toLong())
            // label is not provided
            put("color", "#ffffff")
        }
        val actionSourceData =
            ActionSourceData(id, ActionType.SHOW_TIMELINE_MARKER.type, offset, absoluteTime, data)


        val action = actionSourceData.toAction()


        assertTrue { action is Action.InvalidAction }
    }


    @Test
    fun `mapping to MarkTimelineAction with no color`() {
        val id = "id"
        val offset = 1000L
        val absoluteTime = -1L
        val data = buildMap<String, Any> {
            put("seek_offset", 1000.toLong())
            put("label", "Goal")
            // color is not provided
        }
        val actionSourceData =
            ActionSourceData(id, ActionType.SHOW_TIMELINE_MARKER.type, offset, absoluteTime, data)


        val action = actionSourceData.toAction()


        assertTrue { action is Action.InvalidAction }
    }
    /**endregion */

    /**region DeleteAction related*/
    @Test
    fun `mapping to DeleteAction`() {
        val id = "id"
        val offset = 1000L
        val absoluteTime = -1L
        val data = buildMap<String, Any> {
            put("action_id", "STR")
        }
        val actionSourceData =
            ActionSourceData(id, ActionType.DELETE_ACTION.type, offset, absoluteTime, data)


        val action = actionSourceData.toAction()
        assertTrue { action is Action.DeleteAction }
        val deleteAction = action as Action.DeleteAction
        assertEquals(data["action_id"], deleteAction.targetActionId)
    }

    @Test
    fun `mapping to DeleteAction with invalid data`() {
        val id = "id"
        val offset = 1000L
        val absoluteTime = -1L
        val data = buildMap<String, Any> {
            // action_id is not provided
        }
        val actionSourceData =
            ActionSourceData(id, ActionType.DELETE_ACTION.type, offset, absoluteTime, data)


        val action = actionSourceData.toAction()
        assertTrue { action is Action.InvalidAction }
    }
    /**endregion */

}