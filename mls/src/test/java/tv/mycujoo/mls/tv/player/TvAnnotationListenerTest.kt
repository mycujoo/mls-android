package tv.mycujoo.mls.tv.player

import android.content.Context
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.whenever
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.MockitoAnnotations
import tv.mycujoo.domain.entity.AnimationType
import tv.mycujoo.domain.entity.OverlayEntity
import tv.mycujoo.domain.entity.TransitionSpec
import tv.mycujoo.domain.entity.ViewSpec
import tv.mycujoo.mls.helper.DownloaderClient
import tv.mycujoo.mls.helper.OverlayViewHelper

class TvAnnotationListenerTest {

    private lateinit var tvAnnotationListener: TvAnnotationListener

    @Mock
    lateinit var tvOverlayContainer: TvOverlayContainer

    @Mock
    lateinit var context: Context

    @Mock
    lateinit var overlayViewHelper: OverlayViewHelper

    @Mock
    lateinit var downloaderClient: DownloaderClient


    @Before
    fun setUp() {
        MockitoAnnotations.initMocks(this)
        tvAnnotationListener = TvAnnotationListener(
            tvOverlayContainer,
            overlayViewHelper,
            downloaderClient
        )

        whenever(tvOverlayContainer.context).thenReturn(context)
    }

    @Test
    fun `addOverlay`() {
        val overlayEntity = sampleEntityWithIntroAnimation(AnimationType.NONE)

        Mockito.`when`(downloaderClient.download(any(), any()))
            .then { i -> ((i.getArgument(1)) as (OverlayEntity) -> Unit).invoke(i.getArgument(0)) }


        tvAnnotationListener.addOverlay(overlayEntity)


        Mockito.verify(overlayViewHelper)
            .addViewWithNoAnimation(context, tvOverlayContainer, overlayEntity)
    }


    @Test
    fun `removeOverlay`() {
        val overlayEntity = sampleEntityWithOutroAnimation(AnimationType.NONE)


        tvAnnotationListener.removeOverlay(overlayEntity)


        Mockito.verify(overlayViewHelper).removeView(tvOverlayContainer, overlayEntity)
    }

    @Test
    fun `addOverlay with No animation`() {
        val overlayEntity = sampleEntityWithIntroAnimation(AnimationType.NONE)

        Mockito.`when`(downloaderClient.download(any(), any()))
            .then { i -> ((i.getArgument(1)) as (OverlayEntity) -> Unit).invoke(i.getArgument(0)) }


        tvAnnotationListener.addOverlay(overlayEntity)


        Mockito.verify(overlayViewHelper)
            .addViewWithNoAnimation(context, tvOverlayContainer, overlayEntity)
    }

    @Test
    fun `addOverlay with Unspecified animation`() {
        val overlayEntity = sampleEntityWithIntroAnimation(AnimationType.UNSPECIFIED)

        Mockito.`when`(downloaderClient.download(any(), any()))
            .then { i -> ((i.getArgument(1)) as (OverlayEntity) -> Unit).invoke(i.getArgument(0)) }


        tvAnnotationListener.addOverlay(overlayEntity)


        Mockito.verify(overlayViewHelper)
            .addViewWithNoAnimation(context, tvOverlayContainer, overlayEntity)
    }


    @Test
    fun `addOverlay with animation`() {
        val overlayEntity = sampleEntityWithIntroAnimation(AnimationType.SLIDE_FROM_LEFT)
        Mockito.`when`(downloaderClient.download(any(), any()))
            .then { i -> ((i.getArgument(1)) as (OverlayEntity) -> Unit).invoke(i.getArgument(0)) }


        tvAnnotationListener.addOverlay(overlayEntity)


        Mockito.verify(overlayViewHelper)
            .addViewWithAnimation(context, tvOverlayContainer, overlayEntity)
    }


    @Test
    fun `removeOverlay with No animation`() {
        val overlayEntity = sampleEntityWithOutroAnimation(AnimationType.NONE)


        tvAnnotationListener.removeOverlay(overlayEntity)


        Mockito.verify(overlayViewHelper).removeView(tvOverlayContainer, overlayEntity)
    }

    @Test
    fun `removeOverlay with Unspecified animation`() {
        val overlayEntity = sampleEntityWithOutroAnimation(AnimationType.UNSPECIFIED)


        tvAnnotationListener.removeOverlay(overlayEntity)


        Mockito.verify(overlayViewHelper).removeView(tvOverlayContainer, overlayEntity)
    }

    @Test
    fun `removeOverlay with animation`() {
        val overlayEntity = sampleEntityWithOutroAnimation(AnimationType.SLIDE_TO_LEFT)


        tvAnnotationListener.removeOverlay(overlayEntity)


        Mockito.verify(overlayViewHelper).removeView(tvOverlayContainer, overlayEntity)
    }


    private fun sampleEntityWithIntroAnimation(animationType: AnimationType): OverlayEntity {
        return OverlayEntity(
            "id_0",
            null,
            ViewSpec(null, null),
            TransitionSpec(0L, animationType, 0L),
            TransitionSpec(-1L, AnimationType.NONE, -1L),
            emptyList()
        )
    }

    private fun sampleEntityWithOutroAnimation(animationType: AnimationType): OverlayEntity {
        return OverlayEntity(
            "id_0",
            null,
            ViewSpec(null, null),
            TransitionSpec(0L, AnimationType.NONE, 0L),
            TransitionSpec(-1L, animationType, -1L),
            emptyList()
        )
    }
}