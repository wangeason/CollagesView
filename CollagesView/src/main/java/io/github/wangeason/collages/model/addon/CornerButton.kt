package io.github.wangeason.collages.model.addon

import android.graphics.drawable.Drawable
import io.github.wangeason.collages.polygon.Point


open class CornerButton(center: Point, var resId: Int) {
    var center: Point = center
    set(value) {
        field = value
        if (drawable != null) {
            drawable!!.setBounds(
                center.x.toInt() - radius,
                center.y.toInt() - radius,
                center.x.toInt() + radius,
                center.y.toInt() + radius
            )
        }
    }

    var drawable: Drawable? = null
    var radius = 0

    fun setDrawable(drawable: Drawable, radius: Int) {
        this.drawable = drawable
        this.radius = radius
        drawable.setBounds(
            center.x.toInt() - radius,
            center.y.toInt() - radius,
            center.x.toInt() + radius,
            center.y.toInt() + radius
        )
    }

}
