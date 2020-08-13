package tv.mycujoo.mls.data

import tv.mycujoo.domain.entity.EventEntity

interface IDataHolder {
    var currentEvent: EventEntity?
}