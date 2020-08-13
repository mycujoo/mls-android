package tv.mycujoo.mls.data

import tv.mycujoo.mls.model.Event

class DataHolder : IDataHolder {
    override fun getCurrentEvent(): Event? {
        return eventLiveData
    }

    var eventLiveData: Event? = null
}

