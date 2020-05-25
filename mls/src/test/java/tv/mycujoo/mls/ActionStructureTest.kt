package tv.mycujoo.mls

import android.content.Context
import com.google.gson.Gson
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import tv.mycujoo.mls.entity.actions.ActionIdentifier
import tv.mycujoo.mls.entity.actions.ActionRootSourceData
import tv.mycujoo.mls.entity.actions.MetaData
import tv.mycujoo.mls.entity.actions.ShowScoreboardOverlayAction
import kotlin.test.assertEquals
import kotlin.test.assertNotNull


class ActionStructureTest {

    private lateinit var gson: Gson

    @Mock
    lateinit var context: Context


    @Before
    fun setUp() {
        MockitoAnnotations.initMocks(this)
        gson = Gson()
    }


    @Test
    fun verifyRootId() {
        val actionRootSourceData = ActionRootSourceData()
        actionRootSourceData.id = "id_1000"

        val json = gson.toJson(actionRootSourceData)
        val rootSourceData = gson.fromJson(json, ActionRootSourceData::class.java)

        assertEquals("id_1000", rootSourceData.id)
    }

    @Test
    fun verifyActionId() {
        val actionRootSourceData = ActionRootSourceData()
        actionRootSourceData.id = "id_1000"

        val actionIdentifier_0 = ActionIdentifier("0")
        val actionIdentifier_1 = ActionIdentifier("1")
        actionRootSourceData.actionIdentifiers = listOf(actionIdentifier_0, actionIdentifier_1)


        val json = gson.toJson(actionRootSourceData)
        val rootSourceData = gson.fromJson(json, ActionRootSourceData::class.java)

        assertEquals(actionIdentifier_0, rootSourceData.actionIdentifiers?.first())
        assertEquals(actionIdentifier_1, rootSourceData.actionIdentifiers?.get(1))
    }


    @Test
    fun verifyBuildingAction() {
        val actionRootSourceData = ActionRootSourceData()
        actionRootSourceData.id = "id_1000"

        val actionIdentifier_0 = ActionIdentifier("0")
        actionRootSourceData.actionIdentifiers = listOf(actionIdentifier_0)

        val json = gson.toJson(actionRootSourceData)
        val rootSourceData = gson.fromJson(json, ActionRootSourceData::class.java)

        rootSourceData.build()

        assertNotNull(rootSourceData.actionsList.first())
        assertEquals(1, rootSourceData.actionsList.size)
    }

    @Test
    fun verifyActionContent() {
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

        val actionIdentifier_0 = ActionIdentifier(
            "0",
            listOf(metaDataColorLeft, metaDataColorRight, metaDataAbbrLeft, metaDataAbbrRight)
        )
        actionRootSourceData.actionIdentifiers = listOf(actionIdentifier_0)

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

    }
}
