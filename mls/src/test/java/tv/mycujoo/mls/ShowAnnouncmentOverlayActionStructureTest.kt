package tv.mycujoo.mls

import com.google.gson.Gson
import org.junit.Before
import org.junit.Test
import tv.mycujoo.mls.entity.actions.*
import kotlin.test.assertEquals
import kotlin.test.assertTrue


class ShowAnnouncmentOverlayActionStructureTest {

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
            ABSTRACT_ACTION_SHOW_ANNOUNCEMENT_OVERLAY_ID,
            emptyList()
        )
        actionRootSourceData.actionSourceData = listOf(actionIdentifier)


        val json = gson.toJson(actionRootSourceData)
        val rootSourceData = gson.fromJson(json, ActionRootSourceData::class.java)

        rootSourceData.build()

        assertTrue(rootSourceData.actionsList.first() is ShowAnnouncementOverlayAction)
    }


    @Test
    fun verifyShowTimeLineMarkerActionContent() {
        val actionRootSourceData = ActionRootSourceData()
        actionRootSourceData.id = "id_1000"


        val metaDataColor = MetaData()
        metaDataColor.key = "color"
        metaDataColor.value = "#ffffff"

        val metaDataLine1 = MetaData()
        metaDataLine1.key = "line1"
        metaDataLine1.value = "Welcome!"

        val metaDataLine2 = MetaData()
        metaDataLine2.key = "line2"
        metaDataLine2.value = "Welcome to MLS"

        val metaDataImageUrl = MetaData()
        metaDataImageUrl.key = "imageUrl"
        metaDataImageUrl.value = "url_to_image"

        val metaDataDismissible = MetaData()
        metaDataDismissible.key = "dismissible"
        metaDataDismissible.value = "true"

        val metaDataDismissIn = MetaData()
        metaDataDismissIn.key = "dismissIn"
        metaDataDismissIn.value = "6000"

        val actionIdentifier = ActionSourceData(
            ABSTRACT_ACTION_SHOW_ANNOUNCEMENT_OVERLAY_ID,
            listOf(metaDataColor, metaDataLine1, metaDataLine2, metaDataImageUrl, metaDataDismissible, metaDataDismissIn)
        )
        actionRootSourceData.actionSourceData = listOf(actionIdentifier)

        val json = gson.toJson(actionRootSourceData)
        val rootSourceData = gson.fromJson(json, ActionRootSourceData::class.java)

        rootSourceData.build()


        assertEquals(
            metaDataColor.value,
            (rootSourceData.actionsList.first() as ShowAnnouncementOverlayAction).color
        )
        assertEquals(
            metaDataLine1.value,
            (rootSourceData.actionsList.first() as ShowAnnouncementOverlayAction).line1
        )
        assertEquals(
            metaDataLine2.value,
            (rootSourceData.actionsList.first() as ShowAnnouncementOverlayAction).line2
        )
        assertEquals(
            metaDataImageUrl.value,
            (rootSourceData.actionsList.first() as ShowAnnouncementOverlayAction).imageUrl
        )
        assertEquals(
            metaDataDismissible.value?.toBoolean(),
            (rootSourceData.actionsList.first() as ShowAnnouncementOverlayAction).dismissible
        )
        assertEquals(
            metaDataDismissIn.value?.toLong(),
            (rootSourceData.actionsList.first() as ShowAnnouncementOverlayAction).dismissIn
        )
    }
}
