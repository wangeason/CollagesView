package io.github.wangeason.collages.polygon

import android.graphics.Matrix
import kotlin.math.abs
import kotlin.math.sqrt


class Point(var x: Float,
            var y: Float,) {


    override fun equals(other: Any?): Boolean {
        if (null == other || other !is Point) return false
        if (this === other) return true
        val pt: Point = other
        return abs(x - pt.x) < GraphicUtils.FLOAT_ACCURACY && abs(y - pt.y) < GraphicUtils.FLOAT_ACCURACY
    }

    override fun hashCode(): Int {
        var result = if (x != +0.0f) java.lang.Float.floatToIntBits(x) else 0
        result = 31 * result + if (y != +0.0f) java.lang.Float.floatToIntBits(y) else 0
        return result
    }

    fun copy(): Point {
        val x = x
        val y = y
        return Point(x, y)
    }

    override fun toString(): String {
        return String.format("(%.4f,%.4f)", x, y)
    }


    fun getDst(v: Vector): Point {
        return Point(x + v.x, y + v.y)
    }

    fun getSrc(v: Vector): Point {
        return Point(x - v.x, y - v.y)
    }

    fun postMatrix(matrix: Matrix): Point {
        val newX: Float =
            GraphicUtils.getXScale(matrix) * x + GraphicUtils.getXSkew(matrix) * y + GraphicUtils.getXTran(
                matrix
            )
        val newY: Float =
            GraphicUtils.getYScale(matrix) * y + GraphicUtils.getYSkew(matrix) * x + GraphicUtils.getYTran(
                matrix
            )
        return Point(newX, newY)
    }

    fun disToPt(dest: Point): Float {
        return sqrt(((this.x - dest.x) * (this.x - dest.x) + (this.y - dest.y) * (this.y - dest.y)).toDouble())
            .toFloat()
    }
}