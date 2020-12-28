package io.github.wangeason.collages.model.addon

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.Path
import io.github.wangeason.R
import io.github.wangeason.collages.model.BaseItem
import io.github.wangeason.collages.polygon.Point
import io.github.wangeason.collages.polygon.PolygonBuilder
import java.util.*


open class AddOnItem(var index: Int) : BaseItem() {
    var rotate = 0f
    var isSelected = false
    lateinit var bitmap: Bitmap
    lateinit var rotateButton: RotateButton
    lateinit var delButton: DelButton
    lateinit var flipButton: FlipButton
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
        delButton = DelButton(vertexes[0], R.drawable.collage_icon_del)
        flipButton = FlipButton(vertexes[1], R.drawable.collage_icon_flip)
        rotateButton = RotateButton(vertexes[2], R.drawable.collage_icon_rotate)
        val path = Path()
        path.moveTo(vertexes[0].x, vertexes[0].y)
        for (j in 1 until vertexes.size) {
            path.lineTo(vertexes[j].x, vertexes[j].y)
        }
        path.close()
        super.path = path
        center = Point((vertexes[0].x + vertexes[2].x) / 2, (vertexes[0].y + vertexes[2].y) / 2)
    }

    open fun flipBitmap() {
        val dst = Bitmap.createBitmap(bitmap.width, bitmap.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(dst)
        val flipHorizontalMatrix = Matrix()
        flipHorizontalMatrix.setScale(-1f, 1f)
        flipHorizontalMatrix.postTranslate(bitmap.width.toFloat(), 0f)
        canvas.drawBitmap(bitmap, flipHorizontalMatrix, null)
        bitmap = dst
    }
}