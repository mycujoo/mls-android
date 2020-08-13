package tv.mycujoo.mls.data

import tv.mycujoo.mls.model.Event

interface IDataHolder {
    fun getCurrentEvent(): Event?
}