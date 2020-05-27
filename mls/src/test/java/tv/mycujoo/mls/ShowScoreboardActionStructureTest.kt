package tv.mycujoo.mls

import com.google.gson.Gson
import org.junit.Before
import org.junit.Test
import tv.mycujoo.mls.entity.actions.*
import kotlin.test.assertEquals
import kotlin.test.assertTrue


class ShowScoreboardActionStructureTest {

    private lateinit var gson: Gson

    @Before
    fun setUp() {
        gson = Gson()
    }

    @Test
    fun verifyActionType() {
        val actionRootSourceData = ActionRootSourceData()
        actionRootSourceData.id = "id_1000"

        val actionIdentifier = ActionSourceData(
            ABSTRACT_ACTION_SHOW_SCOREBOARD_OVERLAY_ID,
            emptyList()
        )
        actionRootSourceData.actionSourceData = listOf(actionIdentifier)

        val json = gson.toJson(actionRootSourceData)
        val rootSourceData = gson.fromJson(json, ActionRootSourceData::class.java)

        rootSourceData.build()

        assertTrue(rootSourceData.actionsList.first() is ShowScoreboardOverlayAction)
    }


    @Test
    fun verifyShowScoreboardOverlayActionContent() {
        val actionRootSourceData = ActionRootSourceData()
        actionRootSourceData.id = "id_1000"


        val metaDataColorLeft = MetaData()
        metaDataColorLeft.key = "colorLeft"
        metaDataColorLeft.value = "#cccccc"

        val metaDataColorRight = MetaData()
        metaDataColorRight.key = "colorRight"
        metaDataColorRight.value = "#ffffff"

        val metaDataAbbrLeft = MetaData()
        metaDataAbbrLeft.key = "abbrLeft"
        metaDataAbbrLeft.value = "FCB"

        val metaDataAbbrRight = MetaData()
        metaDataAbbrRight.key = "abbrRight"
        metaDataAbbrRight.value = "CFC"

        val metaDataScoreLeft = MetaData()
        metaDataScoreLeft.key = "scoreLeft"
        metaDataScoreLeft.value = "0"

        val metaDataScoreRight = MetaData()
        metaDataScoreRight.key = "scoreRight"
        metaDataScoreRight.value = "0"

        val metaDataViewId = MetaData()
        metaDataViewId.key = "viewId"
        metaDataViewId.value = "sho_timeline_marker_id_00"

        val metaDataDismissible = MetaData()
        metaDataDismissible.key = "dismissible"
        metaDataDismissible.value = "true"

        val metaDataDismissIn = MetaData()
        metaDataDismissIn.key = "dismissIn"
        metaDataDismissIn.value = "6000"

        val actionIdentifier = ActionSourceData(
            ABSTRACT_ACTION_SHOW_SCOREBOARD_OVERLAY_ID,
            listOf(
                metaDataColorLeft,
                metaDataColorRight,
                metaDataAbbrLeft,
                metaDataAbbrRight,
                metaDataScoreLeft,
                metaDataScoreRight,
                metaDataDismissible,
                metaDataDismissIn,
                metaDataViewId
            )
        )
        actionRootSourceData.actionSourceData = listOf(actionIdentifier)

        val json = gson.toJson(actionRootSourceData)
        val rootSourceData = gson.fromJson(json, ActionRootSourceData::class.java)

        rootSourceData.build()


        assertEquals(
            metaDataColorLeft.value,
            (rootSourceData.actionsList.first() as ShowScoreboardOverlayAction).colorLeft
        )
        assertEquals(
            metaDataColorRight.value,
            (rootSourceData.actionsList.first() as ShowScoreboardOverlayAction).colorRight
        )
        assertEquals(
            metaDataAbbrLeft.value,
            (rootSourceData.actionsList.first() as ShowScoreboardOverlayAction).abbrLeft
        )
        assertEquals(
            metaDataAbbrRight.value,
            (rootSourceData.actionsList.first() as ShowScoreboardOverlayAction).abbrRight
        )
        assertEquals(
            metaDataScoreLeft.value,
            (rootSourceData.actionsList.first() as ShowScoreboardOverlayAction).scoreLeft
        )
        assertEquals(
            metaDataScoreRight.value,
            (rootSourceData.actionsList.first() as ShowScoreboardOverlayAction).scoreRight
        )
        assertEquals(
            metaDataDismissible.value?.toBoolean(),
            (rootSourceData.actionsList.first() as ShowScoreboardOverlayAction).dismissible
        )
        assertEquals(
            metaDataDismissIn.value?.toLong(),
            (rootSourceData.actionsList.first() as ShowScoreboardOverlayAction).dismissIn
        )
        assertEquals(
            metaDataViewId.value,
            (rootSourceData.actionsList.first() as ShowScoreboardOverlayAction).viewId
        )

    }
}
