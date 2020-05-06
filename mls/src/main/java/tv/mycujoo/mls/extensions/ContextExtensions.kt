package tv.mycujoo.mls.extensions

import android.content.Context
import android.graphics.Point
import android.util.Size
import android.view.WindowManager

fun Context.getDisplaySize(): Size {
    val point = Point()
    val manager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
    manager.defaultDisplay.getSize(point)
    return Size(point.x, point.y)
}