package tv.mycujoo.mls.core

import android.content.Context
import androidx.constraintlayout.widget.ConstraintLayout
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.MockitoAnnotations
import tv.mycujoo.domain.entity.AnimationType
import tv.mycujoo.domain.entity.OverlayEntity
import tv.mycujoo.domain.entity.OverlayEntityTest
import tv.mycujoo.domain.entity.TransitionSpec
import tv.mycujoo.mls.helper.IDownloaderClient
import tv.mycujoo.mls.helper.OverlayViewHelper
import tv.mycujoo.mls.widgets.MLSPlayerView

class AnnotationListenerTest {

    /**region Subject under test*/
    private lateinit var annotationListener: AnnotationListener
    /**endregion */

    /**region Fields*/
    @Mock
    lateinit var playerView: MLSPlayerView

    @Mock
    lateinit var overlayContainer: ConstraintLayout

    @Mock
    lateinit var overlayViewHelper: OverlayViewHelper

    @Mock
    lateinit var context: Context

    @Mock
    lateinit var downloaderClient: IDownloaderClient

    /**endregion */

    @Before
    fun setUp() {
        MockitoAnnotations.initMocks(this)
        annotationListener = AnnotationListener(playerView, overlayViewHelper, downloaderClient)
        whenever(playerView.overlayHost).thenReturn(overlayContainer)
        whenever(playerView.context).thenReturn(context)
        Mockito.`when`(downloaderClient.download(any(), any()))
            .then { i -> ((i.getArgument(1)) as (OverlayEntity) -> Unit).invoke(i.getArgument(0)) }
    }

    /**region addOverlay() tests*/
    @Test
    fun `given overlay with intro animation to add, should add it with animation`() {
        val introTransitionSpec = TransitionSpec(15000L, AnimationType.FADE_IN, 2000L)
        val overlayEntity = OverlayEntityTest.getSampleOverlayEntity(introTransitionSpec, 25000L)

        annotationListener.addOverlay(overlayEntity)


        verify(overlayViewHelper).addView(context, overlayContainer, overlayEntity)
    }

    @Test
    fun `given overlay without intro animation to add, should add it without animation`() {
        val introTransitionSpec = TransitionSpec(15000L, AnimationType.NONE, 2000L)
        val overlayEntity = OverlayEntityTest.getSampleOverlayEntity(introTransitionSpec, 25000L)

        annotationListener.addOverlay(overlayEntity)


        verify(overlayViewHelper).addView(context, overlayContainer, overlayEntity)
    }
    /**endregion */

    /**endregion removeOverlay() tests*/
    @Test
    fun `given overlay with outro animation to remove, should remove it with animation`() {
        val introTransitionSpec = TransitionSpec(15000L, AnimationType.FADE_IN, 2000L)
        val outroTransitionSpec = TransitionSpec(25000L, AnimationType.FADE_OUT, 2000L)
        val overlayEntity =
            OverlayEntityTest.getSampleOverlayEntity(introTransitionSpec, outroTransitionSpec)

        annotationListener.removeOverlay(overlayEntity)


        verify(overlayViewHelper).removeView(overlayContainer, overlayEntity)
    }

    @Test
    fun `given overlay without outro animation to remove, should remove it with animation`() {
        val introTransitionSpec = TransitionSpec(15000L, AnimationType.FADE_IN, 2000L)
        val outroTransitionSpec = TransitionSpec(25000L, AnimationType.NONE, 2000L)
        val overlayEntity =
            OverlayEntityTest.getSampleOverlayEntity(introTransitionSpec, outroTransitionSpec)

        annotationListener.removeOverlay(overlayEntity)


        verify(overlayViewHelper).removeView(overlayContainer, overlayEntity)
    }
    /**endregion */

    /**region addOrUpdateLingeringIntroOverlay() tests*/
    @Test
    fun `given lingering-intro overlay which is attached, should update it`() {
        val introTransitionSpec = TransitionSpec(15000L, AnimationType.NONE, 2000L)
        val overlayEntity = OverlayEntityTest.getSampleOverlayEntity(introTransitionSpec, 25000L)


        annotationListener.addOrUpdateLingeringIntroOverlay(overlayEntity, 123L, true)


        verify(overlayViewHelper).addOrUpdateLingeringIntroOverlay(
            overlayContainer,
            overlayEntity,
            123L,
            true
        )
    }

    @Test
    fun `given lingering-intro overlay which is not attached, should add it as lingering`() {
        val introTransitionSpec = TransitionSpec(15000L, AnimationType.NONE, 2000L)
        val overlayEntity = OverlayEntityTest.getSampleOverlayEntity(introTransitionSpec, 25000L)


        annotationListener.addOrUpdateLingeringIntroOverlay(overlayEntity, 123L, true)


        verify(overlayViewHelper).addOrUpdateLingeringIntroOverlay(
            overlayContainer,
            overlayEntity,
            123L,
            true
        )
    }
    /**endregion */

    /**region addOrUpdateLingeringOutroOverlay() tests*/
    @Test
    fun `given lingering-outro overlay which is attached, should update it`() {
        val introTransitionSpec = TransitionSpec(15000L, AnimationType.FADE_IN, 2000L)
        val outroTransitionSpec = TransitionSpec(25000L, AnimationType.NONE, 2000L)
        val overlayEntity =
            OverlayEntityTest.getSampleOverlayEntity(introTransitionSpec, outroTransitionSpec)


        annotationListener.addOrUpdateLingeringOutroOverlay(overlayEntity, 123L, true)


        verify(overlayViewHelper).addOrUpdateLingeringOutroOverlay(
            overlayContainer,
            overlayEntity,
            123L,
            true
        )
    }

    @Test
    fun `given lingering-outro overlay which is not attached, should add it as lingering`() {
        val introTransitionSpec = TransitionSpec(15000L, AnimationType.FADE_IN, 2000L)
        val outroTransitionSpec = TransitionSpec(25000L, AnimationType.NONE, 2000L)
        val overlayEntity =
            OverlayEntityTest.getSampleOverlayEntity(introTransitionSpec, outroTransitionSpec)

        annotationListener.addOrUpdateLingeringOutroOverlay(overlayEntity, 123L, true)


        verify(overlayViewHelper).addOrUpdateLingeringOutroOverlay(
            overlayContainer,
            overlayEntity,
            123L,
            true
        )
    }
    /**endregion */

    /**region addOrUpdateLingeringMidwayOverlay() tests*/
    @Test
    fun `given lingering-midway overlay which is attached, should update it`() {
        val introTransitionSpec = TransitionSpec(15000L, AnimationType.UNSPECIFIED, -1L)
        val outroTransitionSpec = TransitionSpec(25000L, AnimationType.UNSPECIFIED, -1L)
        val overlayEntity =
            OverlayEntityTest.getSampleOverlayEntity(introTransitionSpec, outroTransitionSpec)


        annotationListener.addOrUpdateLingeringMidwayOverlay(overlayEntity)


        verify(overlayViewHelper).updateLingeringMidwayOverlay(overlayContainer, overlayEntity)
    }

    @Test
    fun `given lingering-midway overlay which is not attached, should add it as lingering`() {
        val introTransitionSpec = TransitionSpec(15000L, AnimationType.UNSPECIFIED, -1L)
        val outroTransitionSpec = TransitionSpec(25000L, AnimationType.UNSPECIFIED, -1L)
        val overlayEntity =
            OverlayEntityTest.getSampleOverlayEntity(introTransitionSpec, outroTransitionSpec)


        annotationListener.addOrUpdateLingeringMidwayOverlay(overlayEntity)


        verify(overlayViewHelper).updateLingeringMidwayOverlay(overlayContainer, overlayEntity)
    }
    /**endregion */


    /**region removeLingeringOverlay() tests*/
    @Test
    fun `given remove lingering overlay, should mark it as its not on screen`() {
        val introTransitionSpec = TransitionSpec(15000L, AnimationType.NONE, 2000L)
        val overlayEntity = OverlayEntityTest.getSampleOverlayEntity(introTransitionSpec, 25000L)
        overlayEntity.isOnScreen = true

        annotationListener.removeLingeringOverlay(overlayEntity)


        verify(overlayViewHelper).removeView(overlayContainer, overlayEntity)
    }
    /**endregion */

    /**region clearScreen() tests*/
    @Test
    fun `given clear screen, should clear given list`() {
        val list = listOf("a0", "b1", "c2", "d4")


        annotationListener.clearScreen(list)


        verify(playerView).clearScreen(list)
    }

    /**endregion */
}