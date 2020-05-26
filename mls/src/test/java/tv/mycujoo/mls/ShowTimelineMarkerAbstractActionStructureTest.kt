package tv.mycujoo.mls

import com.google.gson.Gson
import org.junit.Before
import org.junit.Test
import tv.mycujoo.mls.entity.actions.*
import kotlin.test.assertEquals
import kotlin.test.assertTrue


class ShowTimelineMarkerAbstractActionStructureTest {

    private lateinit var gson: Gson


    @Before
    fun setUp() {
        gson = Gson()
    }


    @Test
    fun verifyActionType() {
        val actionRootSourceData = ActionRootSourceData()
        actionRootSourceData.id = "id_1000"


        val actionIdentifier = ActionIdentifier(
            ABSTRACT_ACTION_SHOW_TIME_LINE_MARKER_ID,
            emptyList()
        )
        actionRootSourceData.actionIdentifiers = listOf(actionIdentifier)


        val json = gson.toJson(actionRootSourceData)
        val rootSourceData = gson.fromJson(json, ActionRootSourceData::class.java)

        rootSourceData.build()

        assertTrue(rootSourceData.actionsList.first() is ShowTimeLineMarkerAction)
    }


    @Test
    fun verifyShowTimeLineMarkerActionContent() {
        val actionRootSourceData = ActionRootSourceData()
        actionRootSourceData.id = "id_1000"


        val metaDataTag = MetaData()
        metaDataTag.key = "tag"
        metaDataTag.value = "Goal!"

        val metaDataColor = MetaData()
        metaDataColor.key = "color"
        metaDataColor.value = "#ffffff"

        val actionIdentifier = ActionIdentifier(
            ABSTRACT_ACTION_SHOW_TIME_LINE_MARKER_ID,
            listOf(metaDataTag, metaDataColor)
        )
        actionRootSourceData.actionIdentifiers = listOf(actionIdentifier)

        val json = gson.toJson(actionRootSourceData)
        val rootSourceData = gson.fromJson(json, ActionRootSourceData::class.java)

        rootSourceData.build()


        assertEquals(
            metaDataTag.value,
            (rootSourceData.actionsList.first() as ShowTimeLineMarkerAction).tag
        )
        assertEquals(
            metaDataColor.value,
            (rootSourceData.actionsList.first() as ShowTimeLineMarkerAction).color
        )
    }
}
