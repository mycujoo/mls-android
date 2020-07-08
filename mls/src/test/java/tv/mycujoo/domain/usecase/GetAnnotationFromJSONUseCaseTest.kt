package tv.mycujoo.domain.usecase

import com.google.gson.Gson
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import tv.mycujoo.domain.entity.AnnotationsSourceData
import tv.mycujoo.domain.mapper.AnnotationMapper

class GetAnnotationFromJSONUseCaseTest {

    private lateinit var gson: Gson

    @Before
    fun setUp() {
        gson = Gson()
    }


    @Test
    fun `given valid response, should convert to source data`() {
        val annotationsSourceData = Gson().fromJson<AnnotationsSourceData>(
            GetAnnotationFromJSONUseCase.sourceRawResponse,
            AnnotationsSourceData::class.java
        )


        assertNotNull(annotationsSourceData)
    }


    @Test
    fun `given valid source data, should map to entity`() {
        val annotationsSourceData = Gson().fromJson<AnnotationsSourceData>(
            GetAnnotationFromJSONUseCase.sourceRawResponse,
            AnnotationsSourceData::class.java
        )


        val list = AnnotationMapper.mapToAnnotationEntityList(annotationsSourceData)
    }
}