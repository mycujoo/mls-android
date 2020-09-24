package tv.mycujoo.mls.core

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import tv.mycujoo.domain.entity.EventEntity
import tv.mycujoo.mls.enum.C
import tv.mycujoo.mls.enum.MessageLevel
import tv.mycujoo.mls.manager.Logger
import tv.mycujoo.mls.network.socket.IReactorSocket
import tv.mycujoo.mls.network.socket.ReactorCallback

abstract class AbstractPlayerMediator(
    private val reactorSocket: IReactorSocket,
    private val coroutineScope: CoroutineScope,
    protected val logger: Logger
) {

    abstract fun playVideo(event: EventEntity)
    abstract fun playVideo(eventId: String)

    abstract fun onReactorEventUpdate(eventId: String, updateId: String)
    abstract fun onReactorCounterUpdate(counts: String)
    abstract fun onReactorTimelineUpdate(timelineId: String, updateId: String)

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

    /**region fields*/
    protected var eventMayBeStreamed = false
    protected var isLive: Boolean = false

    private lateinit var streamUrlPullJob: Job

    /**endregion */


    protected fun joinToReactor(event: EventEntity) {
        reactorSocket.joinEvent(event.id)
    }


    protected fun startStreamUrlPullingIfNeeded(event: EventEntity) {
        cancelStreamUrlPulling()
        if (eventMayBeStreamed) {
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

    /**region Helper functions*/
    protected fun mayPlayVideo(event: EventEntity): Boolean {
        eventMayBeStreamed = event.streams.firstOrNull()?.fullUrl != null
        return eventMayBeStreamed
    }
    /**endregion */

}