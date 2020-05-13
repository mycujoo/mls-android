package tv.mycujoo.mls.core

import tv.mycujoo.mls.model.Placard
import tv.mycujoo.mls.network.Api

class PlacardProcessor(val api: Api) {
    fun process() {
        val placards = api.getPlacardsSpecs()
        val placard = placards[0]


    }


//    class PlacardTypeFactory {
//        fun createPlacardType(id: String): Placard {
//            when (id) {
//                "LIVE_MODE_01" -> {
//
//                }
//                else -> {
//                }
//            }
//        }
//    }

}
