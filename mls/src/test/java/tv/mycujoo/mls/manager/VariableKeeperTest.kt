package tv.mycujoo.mls.manager

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestCoroutineScope
import org.junit.Before
import org.junit.Test
import tv.mycujoo.domain.entity.Variable
import tv.mycujoo.domain.entity.SetVariableEntity
import tv.mycujoo.mls.model.MutablePair
import tv.mycujoo.mls.model.ScreenTimerDirection
import tv.mycujoo.mls.model.ScreenTimerFormat
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@ExperimentalCoroutinesApi
class VariableKeeperTest {
    private lateinit var variableKeeper: IVariableKeeper

    private lateinit var testCoroutineScope: TestCoroutineScope

    @Before
    fun setUp() {
        testCoroutineScope = TestCoroutineScope()
        variableKeeper = VariableKeeper(testCoroutineScope)
    }

    /**region Timer related functions test*/
    @Test
    fun `notifyTimers() should not update timers if they do NOT exist`() {
        // not creating (and starting) the timer
        // nor creating variable


        val sampleTimerVariable = getSampleTimerVariable("timer_0", 0L)
        val hashMap = HashMap<String, TimerVariable>()
        hashMap[sampleTimerVariable.name] = sampleTimerVariable
        variableKeeper.notifyTimers(hashMap)


        assertEquals(
            "",
            variableKeeper.getValue(sampleTimerVariable.name)
        )
    }

    @Test
    fun `notifyTimers() should update timers if they exist`() {
        val sampleTimerVariable = getSampleTimerVariable("timer_0", 0L)
        val hashMap = HashMap<String, TimerVariable>()
        hashMap[sampleTimerVariable.name] = sampleTimerVariable
        variableKeeper.createTimerPublisher(sampleTimerVariable.name)


        variableKeeper.notifyTimers(hashMap)


        assertEquals(
            sampleTimerVariable.getTime(),
            variableKeeper.getValue(sampleTimerVariable.name)
        )
    }


    @Test
    fun `notifyTimers() should update timers if they exist, with multiple calls`() {
        val sampleTimerVariable = getSampleTimerVariable("timer_0", 0L)
        variableKeeper.createTimerPublisher(sampleTimerVariable.name)
        val hashMap = HashMap<String, TimerVariable>()
        hashMap[sampleTimerVariable.name] = sampleTimerVariable


        variableKeeper.notifyTimers(hashMap)


        assertEquals(
            sampleTimerVariable.getTime(),
            variableKeeper.getValue(sampleTimerVariable.name)
        )
    }

    @Test
    fun `getTimerNames() should return list of created timers`() {
        val sampleTimerVariable0 = getSampleTimerVariable("timer_0", 0L)
        val sampleTimerVariable1 = getSampleTimerVariable("timer_1", 0L)
        val sampleTimerVariable2 = getSampleTimerVariable("timer_2", 0L)
        variableKeeper.createTimerPublisher(sampleTimerVariable0.name)
        variableKeeper.createTimerPublisher(sampleTimerVariable1.name)
        variableKeeper.createTimerPublisher(sampleTimerVariable2.name)

        val hashMap = HashMap<String, TimerVariable>()
        hashMap[sampleTimerVariable0.name] = sampleTimerVariable0
        hashMap[sampleTimerVariable1.name] = sampleTimerVariable1
        hashMap[sampleTimerVariable2.name] = sampleTimerVariable2


        variableKeeper.notifyTimers(hashMap)


        assertEquals(hashMap.size, variableKeeper.getTimerNames().size)
        variableKeeper.getTimerNames().forEach {
            assertTrue { hashMap.containsKey(it) }
        }
    }

    @Test
    fun `observeOnTimer() should be called when new update is available`() {
        val sampleTimerVariable = getSampleTimerVariable("timer_0", 0L)
        variableKeeper.createTimerPublisher(sampleTimerVariable.name)

        val actualResult = MutablePair("", "")
        variableKeeper.observeOnTimer(sampleTimerVariable.name) {
            actualResult.first = it.first
            actualResult.second = it.second
        }

        val hashMap = HashMap<String, TimerVariable>()
        hashMap[sampleTimerVariable.name] = sampleTimerVariable
        variableKeeper.notifyTimers(hashMap)


        assertEquals(sampleTimerVariable.name, actualResult.first)
        assertEquals(sampleTimerVariable.getTime(), actualResult.second)
    }

    /**endregion */

    /**region Variable related functions test*/
    @Test
    fun `notifyVariable() should not update variable if they do NOT exist`() {
        // not creating (set-Variable()) a Variable


        val sampleSetVariableEntity = getSampleSetVariableEntity("name_0")
        val hashMap = HashMap<String, SetVariableEntity>()
        hashMap[sampleSetVariableEntity.id] = sampleSetVariableEntity
        variableKeeper.notifyVariables(hashMap)


        assertEquals(
            "",
            variableKeeper.getValue(sampleSetVariableEntity.id)
        )
    }

    @Test
    fun `notifyVariable() should update variable if they do exist`() {
        val sampleSetVariableEntity = getSampleSetVariableEntity("name_0")
        variableKeeper.createVariablePublisher(sampleSetVariableEntity.id)
        val hashMap = HashMap<String, SetVariableEntity>()
        hashMap[sampleSetVariableEntity.id] = sampleSetVariableEntity

        variableKeeper.notifyVariables(hashMap)


        assertEquals(
            sampleSetVariableEntity.variable.printValue(),
            variableKeeper.getValue(sampleSetVariableEntity.id)
        )
    }


    @Test
    fun `getVariableNames() should return list of SetVariables name`() {
        val sampleSetVariableEntity0 = getSampleSetVariableEntity("name_0")
        val sampleSetVariableEntity1 = getSampleSetVariableEntity("name_1")
        val sampleSetVariableEntity2 = getSampleSetVariableEntity("name_2")
        variableKeeper.createVariablePublisher(sampleSetVariableEntity0.id)
        variableKeeper.createVariablePublisher(sampleSetVariableEntity1.id)
        variableKeeper.createVariablePublisher(sampleSetVariableEntity2.id)
        val hashMap = HashMap<String, SetVariableEntity>()
        hashMap[sampleSetVariableEntity0.id] = sampleSetVariableEntity0
        hashMap[sampleSetVariableEntity1.id] = sampleSetVariableEntity1
        hashMap[sampleSetVariableEntity2.id] = sampleSetVariableEntity2

        variableKeeper.notifyVariables(hashMap)


        assertEquals(hashMap.size, variableKeeper.getVariableNames().size)
        variableKeeper.getVariableNames().forEach {
            assertTrue { hashMap.containsKey(it) }
        }
    }

    @Test
    fun `observeOnVariable() should be called when new update is available`() {
        val sampleSetVariableEntity = getSampleSetVariableEntity("name_0")
        variableKeeper.createVariablePublisher(sampleSetVariableEntity.id)

        val actualResult = MutablePair("", "")
        variableKeeper.observeOnVariable(sampleSetVariableEntity.id) {
            actualResult.first = it.first
            actualResult.second = it.second
        }

        val hashMap = HashMap<String, SetVariableEntity>()
        hashMap[sampleSetVariableEntity.id] = sampleSetVariableEntity
        variableKeeper.notifyVariables(hashMap)


        assertEquals(sampleSetVariableEntity.id, actualResult.first)
        assertEquals(sampleSetVariableEntity.variable.printValue(), actualResult.second)
    }

    /**region */


    private fun getSampleTimerVariable(name: String, startValue: Long): TimerVariable {
        return TimerVariable(
            name,
            ScreenTimerFormat.SECONDS,
            ScreenTimerDirection.UP,
            startValue,
            -1L
        )
    }

    private fun getSampleSetVariableEntity(id: String): SetVariableEntity {
        val variable = Variable.LongVariable(id, 0L)
        return SetVariableEntity(
            id,
            0L,
            variable
        )
    }

}

