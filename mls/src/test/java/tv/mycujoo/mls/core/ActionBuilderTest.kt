package tv.mycujoo.mls.core

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.never
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.MockitoAnnotations
import tv.mycujoo.domain.entity.*
import tv.mycujoo.mls.helper.DownloaderClient
import tv.mycujoo.mls.manager.ViewHandler

class ActionBuilderTest {

    private lateinit var actionBuilder: ActionBuilder


    @Mock
    lateinit var annotationListener: IAnnotationListener

    @Mock
    lateinit var downloaderClient: DownloaderClient

    @Mock
    lateinit var viewHandler: ViewHandler

    @Before
    fun setUp() {
        MockitoAnnotations.initMocks(this)

        actionBuilder = ActionBuilder(annotationListener, downloaderClient, viewHandler)
    }


    @Test
    fun `regular play mode, add overlay with animation`() {
        actionBuilder.setCurrentTime(900L, true)
        actionBuilder.buildCurrentTimeRange()
    }


    @Test
    fun `given buildCurrentTimeRange(), should build add overlays if offset is in time range`() {
        Mockito.`when`(downloaderClient.download(any(), any()))
            .then { i -> ((i.getArgument(1)) as (OverlayEntity) -> Unit).invoke(i.getArgument(0)) }
        val overlayEntity = getSampleOverlayEntity(ONE_SECONDS, FIFTEEN_SECONDS)
        actionBuilder.addOverlayEntities(listOf(overlayEntity))
        actionBuilder.setCurrentTime(1L, true)



        actionBuilder.buildCurrentTimeRange()


        Mockito.verify(annotationListener).addOverlay(overlayEntity)
    }

    @Test
    fun `given buildCurrentTimeRange(), should not build add overlays if offset has passed`() {
        Mockito.`when`(downloaderClient.download(any(), any()))
            .then { i -> ((i.getArgument(1)) as (OverlayEntity) -> Unit).invoke(i.getArgument(0)) }
        val overlayEntity = getSampleOverlayEntity(ONE_SECONDS, FIFTEEN_SECONDS)
        actionBuilder.addOverlayEntities(listOf(overlayEntity))
        actionBuilder.setCurrentTime(1001L, true)



        actionBuilder.buildCurrentTimeRange()


        Mockito.verify(annotationListener, never()).addOverlay(overlayEntity)
    }

    @Test
    fun `given buildCurrentTimeRange(), should build remove overlays if offset is in time range & is on screen`() {
        Mockito.`when`(downloaderClient.download(any(), any()))
            .then { i -> ((i.getArgument(1)) as (OverlayEntity) -> Unit).invoke(i.getArgument(0)) }
        val overlayEntity = getSampleOverlayEntity(ONE_SECONDS, FIFTEEN_SECONDS)
        overlayEntity.isOnScreen = true
        actionBuilder.addOverlayEntities(listOf(overlayEntity))
        actionBuilder.setCurrentTime(14001L, true)



        actionBuilder.buildCurrentTimeRange()


        Mockito.verify(annotationListener).removeOverlay(overlayEntity)
    }

    @Test
    fun `given buildCurrentTimeRange(), should not build remove overlays if offset is not in time range`() {
        Mockito.`when`(downloaderClient.download(any(), any()))
            .then { i -> ((i.getArgument(1)) as (OverlayEntity) -> Unit).invoke(i.getArgument(0)) }
        val overlayEntity = getSampleOverlayEntity(ONE_SECONDS, FIFTEEN_SECONDS)
        overlayEntity.isOnScreen = true
        actionBuilder.addOverlayEntities(listOf(overlayEntity))
        actionBuilder.setCurrentTime(15001L, true)



        actionBuilder.buildCurrentTimeRange()


        Mockito.verify(annotationListener, never()).removeOverlay(overlayEntity)
    }


    companion object {
        private const val INVALID = -1L
        private const val ONE_SECONDS = 1000L
        private const val TWO_SECONDS = 2000L
        private const val FIVE_SECONDS = 5000L
        private const val FIFTEEN_SECONDS = 15000L
        private const val TWENTY_FIVE_SECONDS = 25000L

        private fun getSampleOverlayEntity(introOffset: Long, outroOffset: Long): OverlayEntity {
            val viewSpec = ViewSpec(null, null)
            val svgData = SvgData(
                "https://storage.googleapis.com/mycujoo-player-app.appspot.com/scoreboard_and_timer.svg",
                null,
                null
            )


            val introTransitionSpec = TransitionSpec(introOffset, AnimationType.NONE, 0L)
            val outroTransitionSpec = TransitionSpec(outroOffset, AnimationType.NONE, 0L)

            return OverlayEntity(
                "id_1001",
                svgData,
                viewSpec,
                introTransitionSpec,
                outroTransitionSpec,
                emptyList()
            )
        }
    }
}