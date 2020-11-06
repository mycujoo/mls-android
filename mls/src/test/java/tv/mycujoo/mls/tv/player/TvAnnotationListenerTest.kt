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
import tv.mycujoo.mls.TestData.Companion.sampleEntityWithIntroAnimation
import tv.mycujoo.mls.TestData.Companion.sampleEntityWithOutroAnimation
import tv.mycujoo.mls.TestData.Companion.sampleHideOverlayEntity
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
            .addView(context, tvOverlayContainer, overlayEntity)
    }


    @Test
    fun `removeOverlay`() {
        val overlayEntity = sampleEntityWithOutroAnimation(AnimationType.NONE)


        tvAnnotationListener.removeOverlay(overlayEntity)


        Mockito.verify(overlayViewHelper).removeView(tvOverlayContainer, overlayEntity)
    }


    @Test
    fun `removeOverlay from Hide action`() {
        val hideOverlayEntity = sampleHideOverlayEntity(AnimationType.NONE)


        tvAnnotationListener.removeOverlay(hideOverlayEntity)


        Mockito.verify(overlayViewHelper).removeView(tvOverlayContainer, hideOverlayEntity)
    }


    @Test
    fun `add or update lingering intro Overlay`() {
        val overlayEntity = sampleEntityWithIntroAnimation(AnimationType.SLIDE_FROM_LEFT)

        Mockito.`when`(downloaderClient.download(any(), any()))
            .then { i -> ((i.getArgument(1)) as (OverlayEntity) -> Unit).invoke(i.getArgument(0)) }


        tvAnnotationListener.addOrUpdateLingeringIntroOverlay(overlayEntity, 500L, true)


        Mockito.verify(overlayViewHelper)
            .addOrUpdateLingeringIntroOverlay(tvOverlayContainer, overlayEntity, 500L, true)
    }



    @Test
    fun `add or update lingering outro Overlay`() {
        val overlayEntity = sampleEntityWithIntroAnimation(AnimationType.SLIDE_FROM_LEFT)

        Mockito.`when`(downloaderClient.download(any(), any()))
            .then { i -> ((i.getArgument(1)) as (OverlayEntity) -> Unit).invoke(i.getArgument(0)) }


        tvAnnotationListener.addOrUpdateLingeringOutroOverlay(overlayEntity, 500L, true)


        Mockito.verify(overlayViewHelper)
            .addOrUpdateLingeringOutroOverlay(tvOverlayContainer, overlayEntity, 500L, true)
    }




}