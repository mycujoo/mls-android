package tv.mycujoo.mls.entity.actions
@Deprecated("Use Action instead")
class CommandAction : AbstractAction() {
    lateinit var targetViewId: String
    lateinit var verb: String
    var offset: Long = -1L
}