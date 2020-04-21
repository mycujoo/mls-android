package tv.mycujoo.mls.api

interface PlayerStatus {

    fun getCurrentPosition(): Long

    fun getDuration(): Long
}