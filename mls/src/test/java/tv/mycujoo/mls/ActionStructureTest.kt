package tv.mycujoo.mls

import com.google.gson.Gson
import org.junit.Before
import org.junit.Test
import org.mockito.MockitoAnnotations
import tv.mycujoo.mls.entity.actions.OldActionRootSourceData
import tv.mycujoo.mls.entity.actions.ActionSourceData
import kotlin.test.assertEquals
import kotlin.test.assertNotNull


class ActionStructureTest {

    private lateinit var gson: Gson


    @Before
    fun setUp() {
        MockitoAnnotations.initMocks(this)
        gson = Gson()
    }


    @Test
    fun verifyRootId() {
        val actionRootSourceData = OldActionRootSourceData()
        actionRootSourceData.id = "id_1000"

        val json = gson.toJson(actionRootSourceData)
        val rootSourceData = gson.fromJson(json, OldActionRootSourceData::class.java)

        assertEquals("id_1000", rootSourceData.id)
    }

    @Test
    fun verifyActionId() {
        val actionRootSourceData = OldActionRootSourceData()
        actionRootSourceData.id = "id_1000"

        val actionIdentifier_0 = ActionSourceData("0")
        val actionIdentifier_1 = ActionSourceData("1")
        actionRootSourceData.actionSourceData = listOf(actionIdentifier_0, actionIdentifier_1)


        val json = gson.toJson(actionRootSourceData)
        val rootSourceData = gson.fromJson(json, OldActionRootSourceData::class.java)

        assertEquals(actionIdentifier_0, rootSourceData.actionSourceData?.first())
        assertEquals(actionIdentifier_1, rootSourceData.actionSourceData?.get(1))
    }


    @Test
    fun verifyBuildingAction() {
        val actionRootSourceData = OldActionRootSourceData()
        actionRootSourceData.id = "id_1000"

        val actionIdentifier = ActionSourceData("0")
        actionRootSourceData.actionSourceData = listOf(actionIdentifier)

        val json = gson.toJson(actionRootSourceData)
        val rootSourceData = gson.fromJson(json, OldActionRootSourceData::class.java)

        rootSourceData.build()

        assertNotNull(rootSourceData.actionsList.first())
        assertEquals(1, rootSourceData.actionsList.size)
    }
}
