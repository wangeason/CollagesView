package io.github.wangeason.collages.model

import android.graphics.Matrix
import android.graphics.Path
import android.util.Log
import io.github.wangeason.collages.polygon.Point
import io.github.wangeason.collages.polygon.Polygon
import java.util.*

open class BaseItem {
    var oriMatrix: Matrix = Matrix()
        get() = field
        set(value) {
            Log.i("BaseItem", value.toString())
            field = Matrix(value)
        }

    var postMatrix: Matrix = Matrix()
        get() = field
        set(value) {
            field = Matrix(value)
        }
    var alpha = 100
    lateinit var path: Path
    lateinit var drawingPolygon: Polygon
    lateinit var drawingVertexes: ArrayList<Point>

    val matrix: Matrix
    get() {
        val matrix = Matrix(oriMatrix)
        matrix.postConcat(postMatrix)
        Log.i("BaseItem oriMatrix", oriMatrix.toString())
        Log.i("BaseItem postMatrix", postMatrix.toString())
        return matrix
    }

    init {
        oriMatrix = Matrix()
        postMatrix = Matrix()
    }
}