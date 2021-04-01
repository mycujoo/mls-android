package tv.mycujoo.mcls.core

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import tv.mycujoo.domain.entity.EventEntity
import tv.mycujoo.mcls.enum.C
import tv.mycujoo.mcls.enum.MessageLevel
import tv.mycujoo.mcls.enum.StreamStatus
import tv.mycujoo.mcls.manager.Logger
import tv.mycujoo.mcls.network.socket.IReactorSocket
import tv.mycujoo.mcls.network.socket.ReactorCallback

abstract class AbstractPlayerMediator(
    private val reactorSocket: IReactorSocket,
    private val coroutineScope: CoroutineScope,
    protected val logger: Logger
) {
    /**region Abstracts*/
    abstract fun playVideo(event: EventEntity)
    abstract fun playVideo(eventId: String)

    abstract fun onReactorEventUpdate(eventId: String, updateId: String)
    abstract fun onReactorCounterUpdate(counts: String)
    abstract fun onReactorTimelineUpdate(timelineId: String, updateId: String)
    /**endregion */

    /**region Initializing*/
    init {
        reactorSocket.addListener(reactorCallback = object : ReactorCallback {
            override fun onEventUpdate(eventId: String, updateId: String) {
                onReactorEventUpdate(eventId, updateId)
                logger.log(MessageLevel.INFO, C.EVENT_UPDATE_MESSAGE)
            }

            override fun onCounterUpdate(counts: String) {
                onReactorCounterUpdate(counts)
                logger.log(MessageLevel.INFO, C.VIEWERS_COUNT_UPDATE_MESSAGE)

            }

            override fun onTimelineUpdate(timelineId: String, updateId: String) {
                onReactorTimelineUpdate(timelineId, updateId)
                logger.log(MessageLevel.INFO, C.TIMELINE_UPDATE_MESSAGE)
            }
        })
    }
    /**endregion */

    /**region fields*/
    protected var streaming = false
    protected var streamStatus = StreamStatus.NO_STREAM_URL
    protected var isLive: Boolean = false

    private lateinit var streamUrlPullJob: Job
    /**endregion */

    /**region Stream status*/
    protected fun updateStreamStatus(event: EventEntity) {
        streamStatus = event.streamStatus()
    }
    /**endregion */

    /**region Reactor functions*/
    protected fun joinEvent(event: EventEntity) {
        reactorSocket.joinEvent(event.id)
    }
    /**endregion */

    /**region Stream Url-polling job*/
    protected fun startStreamUrlPullingIfNeeded(event: EventEntity) {
        cancelStreamUrlPulling()
        if (streamStatus == StreamStatus.PLAYABLE) {
            return
        }
        streamUrlPullJob = coroutineScope.launch {
            delay(30000L)
            playVideo(event.id)
        }
    }

    protected fun cancelStreamUrlPulling() {
        if (this::streamUrlPullJob.isInitialized) {
            streamUrlPullJob.cancel()
        }
    }
    /**endregion */

}
