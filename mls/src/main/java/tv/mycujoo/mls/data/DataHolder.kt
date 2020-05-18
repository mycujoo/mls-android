package tv.mycujoo.mls.data

import tv.mycujoo.mls.model.Event
import tv.mycujoo.mls.model.SingleLiveEvent

class DataHolder : DataHolderContract {
    override fun getEvent(): Event? {
        return eventLiveData.value
    }

    val eventLiveData = SingleLiveEvent<Event>()
}

interface DataHolderContract {
    fun getEvent(): Event?
}
