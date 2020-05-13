package tv.mycujoo.mls.widgets.time_bar

interface OnPreviewChangeListener {

    fun onStartPreview(previewView: PreviewView, progress: Int)
    fun onStopPreview(previewView: PreviewView, progress: Int)
    fun onPreview(
        previewView: PreviewView,
        progress: Int,
        fromUser: Boolean
    )
}