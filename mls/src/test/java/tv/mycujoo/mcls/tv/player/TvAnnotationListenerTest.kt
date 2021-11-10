package tv.mycujoo.mcls.tv.player

import android.content.Context
import androidx.constraintlayout.widget.ConstraintLayout
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever
import tv.mycujoo.domain.entity.AnimationType
import tv.mycujoo.domain.entity.OverlayEntity
import tv.mycujoo.mcls.TestData.Companion.getSampleHideOverlayAction
import tv.mycujoo.mcls.TestData.Companion.getSampleShowOverlayAction
import tv.mycujoo.mcls.TestData.Companion.getSampleShowOverlayActionOutro
import tv.mycujoo.mcls.helper.DownloaderClient
import tv.mycujoo.mcls.helper.OverlayViewHelper

class TvAnnotationListenerTest {

    private lateinit var tvAnnotationListener: TvAnnotationListener

    @Mock
    lateinit var tvOverlayContainer: ConstraintLayout

    @Mock
    lateinit var context: Context

    @Mock
    lateinit var overlayViewHelper: OverlayViewHelper

    @Mock
    lateinit var downloaderClient: DownloaderClient


    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        tvAnnotationListener = TvAnnotationListener(
            tvOverlayContainer,
            overlayViewHelper,
            downloaderClient
        )

        whenever(tvOverlayContainer.context).thenReturn(context)
    }

    @Test
    fun `addOverlay`() {
        val action = getSampleShowOverlayAction(AnimationType.NONE)

        Mockito.`when`(downloaderClient.download(any(), any()))
            .then { i -> ((i.getArgument(1)) as (OverlayEntity) -> Unit).invoke(i.getArgument(0)) }


        tvAnnotationListener.addOverlay(action)


        Mockito.verify(overlayViewHelper)
            .addView(context, tvOverlayContainer, action)
    }


    @Test
    fun `removeOverlay`() {
        val action = getSampleShowOverlayActionOutro(AnimationType.NONE)


        tvAnnotationListener.removeOverlay(action.id, action.outroTransitionSpec)


        Mockito.verify(overlayViewHelper)
            .removeView(tvOverlayContainer, action.id, action.outroTransitionSpec)
    }


    @Test
    fun `removeOverlay from Hide action`() {
        val hideOverlayAction = getSampleHideOverlayAction(AnimationType.NONE)


        tvAnnotationListener.removeOverlay(
            hideOverlayAction.id,
            hideOverlayAction.outroTransitionSpec
        )


        Mockito.verify(overlayViewHelper).removeView(
            tvOverlayContainer,
            hideOverlayAction.id,
            hideOverlayAction.outroTransitionSpec
        )
    }


    @Test
    fun `add or update lingering intro Overlay`() {
        val action = getSampleShowOverlayAction(AnimationType.SLIDE_FROM_LEFT)
        Mockito.`when`(downloaderClient.download(any(), any()))
            .then { i -> ((i.getArgument(1)) as (OverlayEntity) -> Unit).invoke(i.getArgument(0)) }


        tvAnnotationListener.addOrUpdateLingeringIntroOverlay(action, 500L, true)


        Mockito.verify(overlayViewHelper)
            .addOrUpdateLingeringIntroOverlay(tvOverlayContainer, action, 500L, true)
    }


    @Test
    fun `add or update lingering outro Overlay`() {
        val action = getSampleShowOverlayAction(AnimationType.SLIDE_FROM_LEFT)
        Mockito.`when`(downloaderClient.download(any(), any()))
            .then { i -> ((i.getArgument(1)) as (OverlayEntity) -> Unit).invoke(i.getArgument(0)) }


        tvAnnotationListener.addOrUpdateLingeringOutroOverlay(action, 500L, true)


        Mockito.verify(overlayViewHelper)
            .addOrUpdateLingeringOutroOverlay(tvOverlayContainer, action, 500L, true)
    }

    @Test
    fun `add or update lingering midway Overlay`() {
        val action = getSampleShowOverlayAction(AnimationType.SLIDE_FROM_LEFT)
        Mockito.`when`(downloaderClient.download(any(), any()))
            .then { i -> ((i.getArgument(1)) as (OverlayEntity) -> Unit).invoke(i.getArgument(0)) }


        tvAnnotationListener.addOrUpdateLingeringMidwayOverlay(action)


        Mockito.verify(overlayViewHelper)
            .addOrUpdateLingeringMidwayOverlay(tvOverlayContainer, action)
    }


    @Test
    fun `remove lingering Overlay`() {
        val action = getSampleShowOverlayAction(AnimationType.SLIDE_FROM_LEFT)


        tvAnnotationListener.removeLingeringOverlay(action.id, action.outroTransitionSpec)


        Mockito.verify(overlayViewHelper)
            .removeView(tvOverlayContainer, action.id, action.outroTransitionSpec)
    }
}