package tv.mycujoo.mls.api

import android.app.Activity
import android.content.res.AssetManager
import com.nhaarman.mockitokotlin2.whenever
import org.junit.Before
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import tv.mycujoo.domain.repository.EventsRepository
import tv.mycujoo.mls.CoroutineTestRule
import tv.mycujoo.mls.core.VideoPlayerCoordinator
import tv.mycujoo.mls.data.IInternalDataProvider
import tv.mycujoo.mls.entity.msc.VideoPlayerConfig
import tv.mycujoo.mls.manager.ViewHandler
import tv.mycujoo.mls.player.IPlayer
import tv.mycujoo.mls.player.Player
import tv.mycujoo.mls.widgets.MLSPlayerView
import tv.mycujoo.mls.widgets.mlstimebar.MLSTimeBar

class MLSTest {

    lateinit var MLS: MLS
    private lateinit var mLSTestBuilder: MLSBuilder


    @Mock
    lateinit var videoPlayerCoordinator: VideoPlayerCoordinator

    @Mock
    lateinit var playerView: MLSPlayerView


    @Mock
    lateinit var MLSBuilder: MLSBuilder


    @Mock
    lateinit var videoPlayerConfig: VideoPlayerConfig

    @Mock
    lateinit var viewHandler: ViewHandler

    @Mock
    lateinit var dispatcher: CoroutineScope

    @Mock
    lateinit var eventsRepository: EventsRepository

    @Mock
    lateinit var internalDataProvider: IInternalDataProvider

    @Mock
    lateinit var activity: AppCompatActivity

    lateinit var player: IPlayer

    @Mock
    lateinit var exoPlayer: SimpleExoPlayer

    @Mock
    lateinit var activity: Activity

    @Mock
    lateinit var assetManager: AssetManager

    @Mock
    lateinit var MLSPlayer: MLSPlayerView


    @Before
    fun setUp() {
        MockitoAnnotations.initMocks(this)
        whenever(activity.assets).thenReturn(assetManager)


        whenever(MLSBuilder.activity).thenReturn(activity)
        whenever(MLSBuilder.createExoPlayer(any())).thenReturn(exoPlayer)
        whenever(MLSBuilder.createMediaFactory(any())).thenReturn(mediaFactory)
        whenever(MLSBuilder.mlsConfiguration).thenReturn(MLSConfiguration())
        whenever(MLSBuilder.hasAnalytic).thenReturn(false)

        whenever(playerView.context).thenReturn(activity)
        whenever(playerView.getTimeBar()).thenReturn(timeBar)

        whenever(dispatcher.coroutineContext).thenReturn(coroutineTestRule.testDispatcher)

        player = Player()
        player.create(mediaFactory, exoPlayer)

        videoPlayerCoordinator.initialize(playerView, player, MLSBuilder)


        mLSTestBuilder = MLSTestBuilder()
            .publicKey("3HFCBP4EQJME2EH8H0SBH9RCST0IR269")
            .withActivity(activity)
            .setConfiguration(MLSConfiguration(seekTolerance = 1000L))
        MLS =
            mLSTestBuilder
                .build()

        MLS.onStart(playerView)

    }
}