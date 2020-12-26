package io.github.wangeason.collages.model

import android.graphics.Matrix
import android.graphics.Path
import io.github.wangeason.collages.polygon.Point
import io.github.wangeason.collages.polygon.Polygon
import java.util.*

open class BaseItem {
    var oriMatrix: Matrix
        get() {
            return this.oriMatrix
        }
        set(value) {
            Matrix(value)
        }

    var postMatrix: Matrix
        get() {
            return this.postMatrix
        }
        set(value) {
            Matrix(value)
        }
    var alpha = 100
    lateinit var path: Path
    lateinit var drawingPolygon: Polygon
    lateinit var drawingVertexes: ArrayList<Point>

    val matrix: Matrix
    get() {
        val matrix = Matrix(oriMatrix)
        matrix.postConcat(postMatrix)
        return matrix
    }

    init {
        oriMatrix = Matrix()
        postMatrix = Matrix()
    }
}