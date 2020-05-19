package tv.mycujoo.mls.data

import tv.mycujoo.mls.model.Event

class DataHolder : DataHolderContract {
    override fun getEvent(): Event? {
        return eventLiveData
    }

    var eventLiveData : Event? = null
}

interface DataHolderContract {
    fun getEvent(): Event?
}
