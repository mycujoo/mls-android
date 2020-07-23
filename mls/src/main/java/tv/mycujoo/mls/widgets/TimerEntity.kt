package tv.mycujoo.mls.widgets

class TimerEntity(
    val name: String,
    val createCommand: CreateTimerEntity,
    val startCommand: ArrayList<StartTimerEntity> = ArrayList(),
    val pauseCommand: ArrayList<PauseTimerEntity> = ArrayList(),
    val adjustCommand: ArrayList<AdjustTimerEntity> = ArrayList()
)