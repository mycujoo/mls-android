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
import tv.mycujoo.mcls.network.socket.ConcurrencyCallback
import tv.mycujoo.mcls.network.socket.IConcurrencySocket
import tv.mycujoo.mcls.network.socket.IReactorSocket
import tv.mycujoo.mcls.network.socket.ReactorCallback

abstract class AbstractPlayerMediator(
    private val reactorSocket: IReactorSocket,
    concurrencySocket: IConcurrencySocket,
    private val coroutineScope: CoroutineScope,
    private val logger: Logger
) {
    /**region Abstracts*/
    abstract fun playVideo(event: EventEntity)
    abstract fun playVideo(eventId: String)

    abstract fun onReactorEventUpdate(eventId: String, updateId: String)
    abstract fun onReactorCounterUpdate(counts: String)
    abstract fun onReactorTimelineUpdate(timelineId: String, updateId: String)

    abstract fun onConcurrencyLimitExceeded()
    abstract fun onConcurrencyNoEntitlement()
    abstract fun onConcurrencySocketError(message: String)
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

        concurrencySocket.addListener(object : ConcurrencyCallback {
            override fun onLimitExceeded() {
                onConcurrencyLimitExceeded()
            }

            // No Action Required, the situation is OK
            override fun onOK() {}

            // TODO: Request Further Information
            override fun onForbidden() {
                onConcurrencyNoEntitlement()
            }

            override fun onNoEntitlement() {
                onConcurrencyNoEntitlement()
            }

            // Should Retry with backoff strategy
            override fun onInternalError() {
                onConcurrencySocketError("Internal Error")
            }

            // Similar to Internal Error
            override fun onUnknownError() {
                onConcurrencySocketError("Unknown Error")
            }

            // Bad Request Error
            override fun onInvalidCommand() {
                onConcurrencySocketError("Invalid Command")
            }

            // Bad Request Error
            override fun onMissingIdentifier() {
                onConcurrencySocketError("Missing Identifier")
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
