package tv.mycujoo.domain.usecase

import com.google.gson.Gson
import org.junit.Before
import org.junit.Test
import kotlin.test.assertNotNull

class GetActionsFromJSONUseCaseTest {

    private lateinit var gson: Gson

    @Before
    fun setUp() {
        gson = Gson()
    }


    @Test
    fun `given valid response, should convert to source data`() {
        val mappedSetVariables = GetActionsFromJSONUseCase.mappedSetVariables()
        assertNotNull(mappedSetVariables)
    }





}