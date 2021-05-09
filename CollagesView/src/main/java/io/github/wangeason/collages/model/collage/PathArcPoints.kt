package io.github.wangeason.collages.model.collage

import android.graphics.RectF
import io.github.wangeason.collages.polygon.GraphicUtils
import io.github.wangeason.collages.polygon.Point

class PathArcPoints {
    private lateinit var center: Point
    private lateinit var startPoint: Point
    lateinit var endPoint //clockwise
            : Point
    var startAngle = 0f
    var sweepAngle = 0f

    fun getCenter(): Point {
        return center
    }

    fun setCenter(center: Point) {
        this.center = center
    }

    fun getStartPoint(): Point {
        return startPoint
    }

    fun setStartPoint(startPoint: Point) {
        this.startPoint = startPoint
    }

    val rectF: RectF
        get() {
            val r: Float = center.disToPt(startPoint)
            return RectF(center.x - r, center.y - r, center.x + r, center.y + r)
        }
}