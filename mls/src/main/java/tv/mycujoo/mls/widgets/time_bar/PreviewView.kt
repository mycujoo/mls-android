package tv.mycujoo.mls.widgets.time_bar

interface PreviewView {
    fun getProgress(): Int

    fun getMax(): Int

    fun getThumbOffset(): Int


    fun addOnPreviewChangeListener(listener: OnPreviewChangeListener)
}