package tv.mycujoo.mcls.api

interface PlayerStatus {

    fun getCurrentPosition(): Long
    fun getDuration(): Long
}