package tv.mycujoo.mls.entity.actions

class ShowTimeLineMarkerAction : AbstractAction() {
    override var description= "Shows a little line on the seekbar of the video player"

    lateinit var tag : String
    lateinit var color : String

}