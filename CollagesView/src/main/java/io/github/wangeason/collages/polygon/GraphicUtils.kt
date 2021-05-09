package io.github.wangeason.collages.polygon

import android.graphics.Bitmap
import android.graphics.Matrix
import android.util.Log
import io.github.wangeason.CollagesView
import io.github.wangeason.collages.model.collage.PathArcPoints
import java.util.*
import kotlin.math.abs

object GraphicUtils {
    const val FLOAT_ACCURACY = 0.5f

    /**
     * 放大缩小倍数限制
     */
    private val MAX_SCALE: Float = CollagesView.MAX_SCALE
    private val MIN_SCALE: Float = CollagesView.MIN_SCALE

    /**
     * 日志状态 DEBUG
     */
    private val D: Boolean = CollagesView.DEBUG
    private const val TAG = "GraphicUtils"

    /**
     * CenterCrop
     * @param boundingBox
     * @param bitmap
     * @param pathWidth
     * @return
     */
    fun getCenterCropMatrix(
        boundingBox: BoundingBox,
        bitmap: Bitmap,
        pathWidth: Float
    ): Matrix {
        val matrix = Matrix()
        var centerCropScale = 1f // 缩放量
        val scaleX = (boundingBox.xMax - boundingBox.xMin - pathWidth) / bitmap.width
        val scaleY = (boundingBox.yMax - boundingBox.yMin - pathWidth) / bitmap.height
        centerCropScale = if (scaleX > scaleY) scaleX else scaleY
        centerCropScale = if (centerCropScale > MAX_SCALE) MAX_SCALE else centerCropScale
        centerCropScale = if (centerCropScale < MIN_SCALE) MIN_SCALE else centerCropScale
        matrix.setScale(centerCropScale, centerCropScale)
        matrix.postTranslate(
            (boundingBox.xMax + boundingBox.xMin) / 2 - centerCropScale * bitmap.width / 2,
            (boundingBox.yMax + boundingBox.yMin) / 2 - centerCropScale * bitmap.height / 2
        )
        return matrix
    }

    /**
     * FitCenter
     * @param boundingBox
     * @param bitmap
     * @return
     */
    fun getFitCenterMatrix(
        boundingBox: BoundingBox,
        bitmap: Bitmap,
        pathWidth: Float
    ): Matrix {
        val matrix = Matrix()
        var fitCenterScale = 1f // 缩放量
        val scaleX = (boundingBox.xMax - boundingBox.xMin - pathWidth) / bitmap.width
        val scaleY = (boundingBox.yMax - boundingBox.yMin - pathWidth) / bitmap.height
        fitCenterScale = if (scaleX < scaleY) scaleX else scaleY
        fitCenterScale = if (fitCenterScale > MAX_SCALE) MAX_SCALE else fitCenterScale
        fitCenterScale = if (fitCenterScale < MIN_SCALE) MIN_SCALE else fitCenterScale
        matrix.setScale(fitCenterScale, fitCenterScale)
        matrix.postTranslate(
            (boundingBox.xMax + boundingBox.xMin) / 2 - fitCenterScale * bitmap.width / 2,
            (boundingBox.yMax + boundingBox.yMin) / 2 - fitCenterScale * bitmap.height / 2
        )
        return matrix
    }

    /**
     * CenterInside
     * @param boundingBox
     * @param bitmap
     * @return
     */
    fun getCenterInsideMatrix(
        boundingBox: BoundingBox,
        bitmap: Bitmap,
        pathWidth: Float
    ): Matrix {
        val matrix = Matrix()
        var centerInside = 1f // 缩放量
        val scaleX = (boundingBox.xMax - boundingBox.xMin - pathWidth) / bitmap.width
        val scaleY = (boundingBox.yMax - boundingBox.yMin - pathWidth) / bitmap.height
        if (scaleX < 1 || scaleY < 1) {
            centerInside = if (scaleX < scaleY) scaleX else scaleY
        }
        centerInside = if (centerInside > MAX_SCALE) MAX_SCALE else centerInside
        centerInside = if (centerInside < MIN_SCALE) MIN_SCALE else centerInside
        matrix.setScale(centerInside, centerInside)
        matrix.postTranslate(
            (boundingBox.xMax + boundingBox.xMin) / 2 - centerInside * bitmap.width / 2,
            (boundingBox.yMax + boundingBox.yMin) / 2 - centerInside * bitmap.height / 2
        )
        return matrix
    }

    fun getXScale(matrix: Matrix): Float {
        val f = FloatArray(9)
        matrix.getValues(f)
        return f[Matrix.MSCALE_X]
    }

    fun getXSkew(matrix: Matrix): Float {
        val f = FloatArray(9)
        matrix.getValues(f)
        return f[Matrix.MSKEW_X]
    }

    fun getXTran(matrix: Matrix): Float {
        val f = FloatArray(9)
        matrix.getValues(f)
        return f[Matrix.MTRANS_X]
    }

    fun getYSkew(matrix: Matrix): Float {
        val f = FloatArray(9)
        matrix.getValues(f)
        return f[Matrix.MSKEW_Y]
    }

    fun getYScale(matrix: Matrix): Float {
        val f = FloatArray(9)
        matrix.getValues(f)
        return f[Matrix.MSCALE_Y]
    }

    fun getYTran(matrix: Matrix): Float {
        val f = FloatArray(9)
        matrix.getValues(f)
        return f[Matrix.MTRANS_Y]
    }

    fun getPers0(matrix: Matrix): Float {
        val f = FloatArray(9)
        matrix.getValues(f)
        return f[Matrix.MPERSP_0]
    }

    fun getPers1(matrix: Matrix): Float {
        val f = FloatArray(9)
        matrix.getValues(f)
        return f[Matrix.MPERSP_1]
    }

    fun getPers2(matrix: Matrix): Float {
        val f = FloatArray(9)
        matrix.getValues(f)
        return f[Matrix.MPERSP_2]
    }

    /**
     * 点到直线距离
     * @param segment
     * @param point
     * @return
     */
    fun getDisFromPtToLine(segment: Segment, point: Point): Float {
        if (segment.isVertical()) {
            return abs(point.x - segment.start.x)
        } else if (segment.getA() == 0f) {
            return abs(point.y - segment.start.y)
        } else {
            val a = segment.getA()
            val b = segment.getB()
            val sy = a * point.x + b
            val sx = (point.y - b) / a
            return abs(
                (point.y - sy) * (point.x - sx) / Point(point.x, sy).disToPt(Point(sx, point.y))
            )
        }
    }

    /**
     * 获取一定距离远的平行线
     *
     */
    fun getParallelSegments(src: Segment, dis: Float, results: ArrayList<Segment>?) {
        var results = results
        val srcStart: Point = src.start
        val srcEnd: Point = src.end
        if (results == null) {
            results = ArrayList()
        }
        if (src.isVertical()) {
            results.add(
                Segment(
                    Point(srcStart.x - dis, srcStart.y),
                    Point(srcEnd.x - dis, srcEnd.y)
                )
            )
            results.add(
                Segment(
                    Point(srcStart.x + dis, srcStart.y),
                    Point(srcEnd.x + dis, srcEnd.y)
                )
            )
        } else {
            val a = src.getA()
            val dx = -dis * a / Math.sqrt((1 + a * a).toDouble()).toFloat()
            val dy = dis / Math.sqrt((1 + a * a).toDouble()).toFloat()
            results.add(
                Segment(
                    Point(srcStart.x + dx, srcStart.y + dy),
                    Point(srcEnd.x + dx, srcEnd.y + dy)
                )
            )
            results.add(
                Segment(
                    Point(srcStart.x - dx, srcStart.y - dy),
                    Point(srcEnd.x - dx, srcEnd.y - dy)
                )
            )
        }
    }

    /**
     * 仅对凸多边形有效，凹多边形，有洞的多边形不能用此方法
     * @param src
     * @param dis
     * @return
     */
    fun getShrinkPolygon(src: Polygon, dis: Float): Polygon {
        if (dis == 0f) {
            return Polygon(src)
        } else if (dis > 0) {
            //get Limit dis
            val center: Point = src.geoCenter
            val parallelSegments = ArrayList<Segment>()
            var maxDis = Float.MAX_VALUE
            for (segment: Segment in src.sides) {
                maxDis = Math.min(maxDis, getDisFromPtToLine(segment, center))
            }
            if (dis > maxDis) {
                return Polygon()
            }

            //获取所有
            for (segment: Segment in src.sides) {
                getParallelSegments(segment, dis, parallelSegments)
            }

            //获取所有缩进的Line,仅对凸多边形有效
            val insideSegments = ArrayList<Segment>()
            for (i in 0 until parallelSegments.size / 2) {
                if (getDisFromPtToLine(parallelSegments[i * 2], center) > getDisFromPtToLine(
                        parallelSegments[i * 2 + 1], center
                    )
                ) {
                    insideSegments.add(parallelSegments[i * 2 + 1])
                } else {
                    insideSegments.add(parallelSegments[i * 2])
                }
            }

            //获取缩进线段交点,
            val vertexes = ArrayList<Point>()
            val num = insideSegments.size
            //为了每条边和原多边形对应
            for (i in num until num * 2) {
                vertexes.add(insideSegments[(i - 1) % num].lineIntersect(insideSegments[i % num]))
            }

            //创建缩小的多边形
            val builder: PolygonBuilder = PolygonBuilder()
            for (point: Point in vertexes) {
                builder.addVertex(point)
            }
            return builder.build()
        } else { //是扩大了
            //TODO
            return Polygon(src)
        }
    }

    /**
     * 判断两条线段是不是在同一条直线上
     * @param src
     * @param des
     * @return
     */
    fun isSegmentsOnSameLine(src: Segment, des: Segment): Boolean {
        if (src.isVertical() && des.isVertical()) {
            return abs(src.start.x - des.start.x) < FLOAT_ACCURACY
        } else if (!src.isVertical() && !des.isVertical()) {
            return (abs(src.getA() - des.getA()) < FLOAT_ACCURACY) && (abs(src.getB() - des.getB()) < FLOAT_ACCURACY)
        } else {
            return false
        }
    }



    /**
     * position = sign( (Bx-Ax)*(Y-Ay) - (By-Ay)*(X-Ax) )
     * It is 0 on the segment, and +1 on one side, -1 on the other side.
     * @param segment
     * @param pt1
     * @param pt2
     * @return 0,有点在线上；1， 同一边； -1，不同边
     */
    fun isTwoPointsOnSameSide(segment: Segment, pt1: Point, pt2: Point): Int {
        val start: Point = segment.start
        val end: Point = segment.end
        val pt1Pos = (start.x - end.x) * (pt1.y - end.y) - (start.y - end.y) * (pt1.x - end.x)
        val pt2Pos = (start.x - end.x) * (pt2.y - end.y) - (start.y - end.y) * (pt2.x - end.x)
        return if (abs(pt1Pos) < FLOAT_ACCURACY || abs(pt2Pos) < FLOAT_ACCURACY) {
            0
        } else if (pt1Pos * pt2Pos > 0) {
            1
        } else {
            -1
        }
    }

    /**
     * 判断pt是否在直线line上
     * @param pt
     * @param segment
     * @return
     */
    fun isPointOnLine(pt: Point, segment: Segment): Boolean {
        val start: Point = segment.start
        val end: Point = segment.end
        val sidePt = (start.x - end.x) * (pt.y - end.y) - (start.y - end.y) * (pt.x - end.x)
        return abs(sidePt) < FLOAT_ACCURACY
//        return abs(sidePt) == 0f
    }

    /**
     * 判断是否线段端点
     * @param pt
     * @param segment
     * @return
     */
    fun isPointVertexOfSegment(pt: Point, segment: Segment): Boolean {
        if (pt == segment.end) {
            return true
        } else return pt == segment.start
    }

    /**
     * 判断点在线段中
     * @param pt
     * @param segment
     * @return
     */
    fun isPointOnSegment(pt: Point, segment: Segment): Boolean {
        return if (isPointOnLine(pt, segment) &&
            segment.boundingBox()!!.contains(pt)
        ) {
            true
        } else false
    }

    /**
     * 把src合并到des中，如果不成功返回NULL，否则修改des
     * @param src
     * @param des
     * @return
     */
    fun combine(src: Segment, des: Segment): Segment? {
        //不在一条直线上
        if (!des.isOnSameLineOf(src)) {
            if (D) Log.i(TAG, "src:$src des:$des")
            return null
        }

        //包含
        if (des.isContainedBy(src)) {
            return src
        }
        if (src.isContainedBy(des)) {
            return des
        }

        //不重叠
        if (!(des.boundingBox().contains(src.end) || des.boundingBox()
                .contains(src.start))
        ) {
            return null
        }

        //部分重叠
        if (des.boundingBox().contains(src.end)) {
            return if (src.start.disToPt(des.start) > src.start.disToPt(des.end)) {
                Segment(des.start, src.start)
            } else {
                Segment(des.end, src.start)
            }
        } else {
            return if (src.end.disToPt(des.start) > src.end.disToPt(des.end)) {
                Segment(des.start, src.end)
            } else {
                Segment(des.end, src.end)
            }
        }
    }

    /**
     * polygon移动一条边
     * @param src 原多边形
     * @param moveSegment
     * @param dis
     * @param direction 为0，忽略；为1，缩进; 为-1，放大
     * @return
     */
    fun moveOneSide(
        src: Polygon,
        moveSegment: Segment,
        dis: Float,
        direction: Int
    ): Polygon {
        val result = src.copy()
        if (direction == 0 || dis == 0f) {
            return result
        }


        //被移动的边
        var movingSide: Segment? = null
        var index = 0
        for (i in 0 until result.sides.size) {
            val side: Segment = result.sides.get(i)
            if (side.isOnSameLineOf(moveSegment)) {
                movingSide = side
                index = i
                break
            }
        }
        if (D) {
            Log.i(
                "GraphicUtils",
                "movingSide:$moveSegment polygon sides: " + result.sides[0].toString()
                    .toString() + " " + result.sides[1].toString()
                    .toString() + " " + result.sides[2].toString()
                    .toString() + " " + result.sides[3].toString()
            )
        }


        //与被移动的边相交的边
        val indexBefore: Int = (index + result.sides.size - 1) % result.sides.size
        val indexAfter: Int = (index + result.sides.size + 1) % result.sides.size
        val trackBefore: Segment = result.sides.get(indexBefore)
        val trackAfter: Segment = result.sides.get(indexAfter)


        //移动的目标边
        val targetSide: Segment
        val parallelSide = ArrayList<Segment>()
        getParallelSegments(moveSegment, dis, parallelSide)
        if (getDisFromPtToLine(parallelSide[0], result.geoCenter) > getDisFromPtToLine(
                parallelSide[1], result.geoCenter
            )
        ) {
            if (direction == 1) {
                targetSide = parallelSide[1]
            } else {
                targetSide = parallelSide[0]
            }
        } else {
            if (direction == 1) {
                targetSide = parallelSide[0]
            } else {
                targetSide = parallelSide[1]
            }
        }

        //移动的目标边与相邻边的交点
        val crossBefore = targetSide.lineIntersect(trackBefore)
        val crossAfter = targetSide.lineIntersect(trackAfter)

        //替换Side
        result.sides.remove(trackBefore)
        result.sides.add(indexBefore, Segment(trackBefore.start, (crossBefore)))
        result.sides.remove(movingSide)
        result.sides.add(index, Segment((crossBefore), (crossAfter)))
        result.sides.remove(trackAfter)
        result.sides.add(indexAfter, Segment((crossAfter), trackAfter.end))

        //替换boundingBox
        val vertexes = ArrayList<Point>()
        for (side: Segment in result.sides) {
            vertexes.add(side.start)
        }
        result.boundingBox.yMin = Float.MAX_VALUE
        result.boundingBox.yMax = Float.MIN_VALUE
        result.boundingBox.xMin = Float.MAX_VALUE
        result.boundingBox.xMax = Float.MIN_VALUE
        for (vertex: Point in vertexes) {
            result.boundingBox.yMin =
                if (vertex.y < result.boundingBox.yMin) vertex.y else result.boundingBox.yMin
            result.boundingBox.yMax =
                if (vertex.y > result.boundingBox.yMax) vertex.y else result.boundingBox.yMax
            result.boundingBox.xMin =
                if (vertex.x < result.boundingBox.xMin) vertex.x else result.boundingBox.xMin
            result.boundingBox.xMax =
                if (vertex.x > result.boundingBox.xMax) vertex.x else result.boundingBox.xMax
        }
        return result
    }

    /**
     * 获取圆弧角的圆心
     * @param polygon
     * @param pathCut
     * @param centers
     */
    fun getPathArcCenter(polygon: Polygon, pathCut: Float, centers: ArrayList<PathArcPoints>) {
        val sides: List<Segment> = polygon.sides
        val size = sides.size
        for (i in 0 until size) {
            val center = PathArcPoints()
            val thisSide = sides[(i - 1 + size) % size]
            val nextSide = sides[i]
            val vertex: Point = thisSide.end
            center.setStartPoint(vertex.getSrc((thisSide.getVector().changeLengthTo(pathCut))!!))
            center.endPoint = (vertex.getDst((nextSide.getVector().changeLengthTo(pathCut))!!))

            //通过两个向量的夹角angle(小于Math.PI)的角平分线找到圆心
            val thisAngle = thisSide.getVector().getRotateAngle()
            val nextAngle = nextSide.getVector().getRotateAngle()
            val angle =
                if (abs(thisAngle - nextAngle + Math.PI) < Math.PI) abs(thisAngle - nextAngle + Math.PI) else Math.PI * 2 - abs(
                    thisAngle - nextAngle + Math.PI
                )
            //只有方向
            val vertexToCenter = Segment(
                vertex,
                Point(
                    (center.getStartPoint().x + center.endPoint.x) / 2,
                    (center.getStartPoint().y + center.endPoint.y) / 2
                )
            ).getVector()
            center.setCenter(vertex.getDst((vertexToCenter.changeLengthTo((pathCut / Math.cos(angle / 2)).toFloat()))!!))
            val startVector = Vector(
                center.getStartPoint().x - center.getCenter().x,
                center.getStartPoint().y - center.getCenter().y
            )
            val targetVector = Vector(
                center.endPoint.x - center.getCenter().x,
                center.endPoint.y - center.getCenter().y
            )
            val startAngle = startVector.getRotateAngle()
            val targetAngle = targetVector.getRotateAngle()
            center.sweepAngle = (
                if (abs(targetAngle - startAngle) < Math.PI) (abs(
                    targetAngle - startAngle
                )).toFloat() else (Math.PI * 2 - abs(targetAngle - startAngle)).toFloat()
            )

            //这个判断与多变形的初始化向量方向有关，此处选大于0
            center.startAngle = (
                if (startVector.cross(targetVector)
                        .z > 0
                ) startAngle.toFloat() else targetAngle.toFloat()
            )
            centers.add(center)
        }
    }
}
