package tv.mycujoo.mls

import com.google.gson.Gson
import org.junit.Before
import org.junit.Test
import tv.mycujoo.mls.entity.actions.*
import kotlin.test.assertEquals
import kotlin.test.assertTrue


class CommandActionStructureTest {

    private lateinit var gson: Gson


    @Before
    fun setUp() {
        gson = Gson()
    }


    @Test
    fun verifyActionType() {
        val actionRootSourceData = OldActionRootSourceData()
        actionRootSourceData.id = "id_1000"


        val actionIdentifier = ActionSourceData(
            ABSTRACT_ACTION_COMMAND_ID,
            emptyList()
        )
        actionRootSourceData.actionSourceData = listOf(actionIdentifier)


        val json = gson.toJson(actionRootSourceData)
        val rootSourceData = gson.fromJson(json, OldActionRootSourceData::class.java)

        rootSourceData.build()

        assertTrue(rootSourceData.actionsList.first() is CommandAction)
    }


    @Test
    fun verifyCommandActionContent() {
        val actionRootSourceData = OldActionRootSourceData()
        actionRootSourceData.id = "id_1000"


        val metaDataTargetViewId = MetaData()
        metaDataTargetViewId.key = "targetViewId"
        metaDataTargetViewId.value = "scoreboard_id_00"

        val metaDataVerb = MetaData()
        metaDataVerb.key = "verb"
        metaDataVerb.value = "hide"

        val metaDataOffset = MetaData()
        metaDataOffset.key = "offset"
        metaDataOffset.value = "2000"

        val actionIdentifier = ActionSourceData(
            ABSTRACT_ACTION_COMMAND_ID,
            listOf(
                metaDataTargetViewId,
                metaDataVerb,
                metaDataOffset
            )
        )
        actionRootSourceData.actionSourceData = listOf(actionIdentifier)

        val json = gson.toJson(actionRootSourceData)
        val rootSourceData = gson.fromJson(json, OldActionRootSourceData::class.java)

        rootSourceData.build()


        assertEquals(
            metaDataTargetViewId.value,
            (rootSourceData.actionsList.first() as CommandAction).targetViewId
        )
        assertEquals(
            metaDataVerb.value,
            (rootSourceData.actionsList.first() as CommandAction).verb
        )
        assertEquals(
            metaDataOffset.value?.toLong(),
            (rootSourceData.actionsList.first() as CommandAction).offset
        )
    }
}
