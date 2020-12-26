package io.github.wangeason.collages.model.addon

import android.graphics.Bitmap
import android.graphics.Path
import io.github.wangeason.collages.model.BaseItem
import io.github.wangeason.collages.polygon.Point
import io.github.wangeason.collages.polygon.Polygon
import io.github.wangeason.collages.polygon.PolygonBuilder
import java.util.*


open class AddOnItem : BaseItem() {
    var rotate = 0f
    var isSelected = false
    lateinit var bitmap: Bitmap
    lateinit var rotateButton: RotateButton
    lateinit var delButton: DelButton
    lateinit var center: Point

    fun generateNewDrawingZone() {
        val oriWidth = bitmap.width.toFloat()
        val oriHeight = bitmap.height.toFloat()
        val topLeft: Point = Point(0f, 0f).postMatrix(matrix)
        val topRight: Point = Point(oriWidth, 0f).postMatrix(matrix)
        val bottomRight: Point = Point(oriWidth, oriHeight).postMatrix(matrix)
        val bottomLeft: Point = Point(0f, oriHeight).postMatrix(matrix)
        val vertexes: ArrayList<Point> = ArrayList<Point>()
        vertexes.add(topLeft)
        vertexes.add(topRight)
        vertexes.add(bottomRight)
        vertexes.add(bottomLeft)
        drawingVertexes = vertexes
        val builder: PolygonBuilder = PolygonBuilder()
        for (point in vertexes) {
            builder.addVertex(point)
        }
        drawingPolygon = builder.build()
        delButton = DelButton(vertexes[0])
        rotateButton = RotateButton(vertexes[2])
        val path = Path()
        path.moveTo(vertexes[0].x, vertexes[0].y)
        for (j in 1 until vertexes.size) {
            path.lineTo(vertexes[j].x, vertexes[j].y)
        }
        path.close()
        super.path = path
        center = Point((vertexes[0].x + vertexes[2].x) / 2, (vertexes[0].y + vertexes[2].y) / 2)
    }
}