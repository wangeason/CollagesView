package io.github.wangeason.collages.utils

import android.util.Log
import android.view.MotionEvent
import io.github.wangeason.CollagesView
import io.github.wangeason.collages.model.collage.BackBitmapItem
import io.github.wangeason.collages.model.collage.DragButton
import io.github.wangeason.collages.polygon.GraphicUtils
import io.github.wangeason.collages.polygon.Point
import io.github.wangeason.collages.polygon.Polygon
import io.github.wangeason.collages.polygon.Segment
import java.util.*
import kotlin.collections.ArrayList

object DragPolygonHelper {
    /**
     * 原polygons
     */
    private var oriPolygons: ArrayList<Polygon> = ArrayList<Polygon>()

    /**
     * 初始按下点
     */
    private var startPoint: Point? = null

    /**
     * 被移动的最短边
     */
    private var moveSegment: Segment? = null

    /**
     * 被影响的所有polygon
     */
    private val movePolygons: ArrayList<Polygon?> = ArrayList<Polygon?>()

    /**
     * 日志状态 DEBUG
     */
    private val D: Boolean = CollagesView.DEBUG
    private const val TAG = "DragPolygonHelper"

    /**
     * 在拖拉之前找出MovingLine，和各个Polygon受影响的边的index
     * @param backBitmapItems
     * @param dragButton
     * @param start
     */
    fun init(backBitmapItems: ArrayList<BackBitmapItem?>?, dragButton: DragButton, start: Point) {
        oriPolygons = ArrayList<Polygon>()
        for (backBitmapItem in backBitmapItems!!) {
            oriPolygons.add(backBitmapItem!!.getPolygon().copy())
        }
        //被操作的边
        val opSegment: Segment = dragButton.movingSegment
        startPoint = start.copy()
        /**
         * 所有的边组成的最长线段
         */
        val allComSegmentsMap: HashMap<Segment, ArrayList<Segment?>> = getAllCombinedSegments()
        val allComSegments: ArrayList<Segment?> = ArrayList<Segment?>(allComSegmentsMap.keys)
        if (D) {
            val builder = StringBuilder()
            for (seg in allComSegments) {
                builder.append(" seg: " + seg.toString())
            }
            Log.i(TAG, "allComSegments: $builder")
        }
        /**
         * 所有的顶点
         */
        val allVertexes: HashMap<Point, ArrayList<Polygon?>> = getAllVertexes(
            oriPolygons
        )
        if (D) {
            val builder = StringBuilder()
            for (vertex in allVertexes.keys) {
                builder.append(" vertex: $vertex")
            }
            Log.i(TAG, "allVertexes: $builder")
        }

        //找到移动边所在的最长线段
        var moveLine: Segment? = null
        for (seg in allComSegments) {
            if (opSegment.isContainedBy(seg!!)) {
                moveLine = seg
            }
        }
        requireNotNull(moveLine) { "cant find move segment, all segments counts:" + allComSegments.size }
        if (D) {
            Log.i(TAG, "moveLine: $moveLine")
        }

        //所有与移动边所在的最长线段相交的线段，（可以作为滑动轨道的线段）
        val tracks: ArrayList<Segment> = ArrayList<Segment>()
        for (seg in allComSegments) {
            if (GraphicUtils.isSegmentOnTrack(moveLine, seg!!)) {
                tracks.add(seg)
            }
        }
        if (D) {
            val builder = StringBuilder()
            for (seg in tracks) {
                builder.append(" seg: $seg")
            }
            Log.i(TAG, "tracks: $builder")
        }

        //所有可能的滑动交叉点
        val movePoints: ArrayList<Point> = ArrayList<Point>()
        for (point in allVertexes.keys) {
            if (GraphicUtils.isPointOnLine(point, moveLine)) {
                for (track in tracks) {
                    if (GraphicUtils.isPointOnLine(point, track)) {
                        movePoints.add(point)
                    }
                }
            }
        }
        if (D) {
            val builder = StringBuilder()
            for (pt in movePoints) {
                builder.append(" point: " + pt.toString())
            }
            Log.i(TAG, "jointers: $builder")
        }

        //所有可能的滑动交叉点，组成的能包含opSegment的最短线段
        var dis = Float.MAX_VALUE
        for (i in 0 until movePoints.size - 1) {
            for (j in 1 until movePoints.size) {
                val possible = Segment(movePoints[i], movePoints[j])
                if (opSegment.isContainedBy(possible)
                    && possible.end.disToPt(possible.start) < dis
                ) {
                    moveSegment = possible
                    dis = possible.end.disToPt(possible.start)
                }
            }
        }
        if (D) {
            Log.i(TAG, "shortest moveSegment: " + moveSegment.toString())
        }

        //受影响的Polygon，
        val tempPolygons: ArrayList<Polygon?> = ArrayList<Polygon?>()
        for (pt in allVertexes.keys) {
            if (GraphicUtils.isPointOnSegment(pt, moveSegment!!)) {
                tempPolygons.addAll(allVertexes[pt]!!)
            }
        }
        //这个表中出现超过一次的polygon才是受影响的polygon
        for (i in 0 until tempPolygons.size - 1) {
            for (j in tempPolygons.size - 1 downTo i + 1) {
                if (tempPolygons[j] === tempPolygons[i]) {
                    movePolygons.add(tempPolygons[j])
                }
            }
        }
        //去掉重复元素
        val hashSet: HashSet<Polygon> = HashSet<Polygon>(movePolygons)
        movePolygons.clear()
        movePolygons.addAll(hashSet)
    }

    /**
     * 返回计算出的新布局
     * @param event
     * @return
     */
    fun getNewPolygons(event: MotionEvent): ArrayList<Polygon>? {
        val result: ArrayList<Polygon> = ArrayList<Polygon>()
        val movePoint = Point(event.x, event.y)
        val dis: Float = Math.abs(
            GraphicUtils.getDisFromPtToLine(
                moveSegment!!,
                startPoint!!
            ) - GraphicUtils.getDisFromPtToLine(
                moveSegment!!, movePoint
            )
        )
        if (dis < GraphicUtils.FLOAT_ACCURACY) {
            return null
        }
        for (polygon in oriPolygons) {
            if (movePolygons.contains(polygon)) {
//                int direction = GraphicUtils.getDisPtToPt(polygon.geoCenter, startPoint) > GraphicUtils.getDisPtToPt(polygon.geoCenter, movePoint)?1:-1;
                val direction: Int = GraphicUtils.isTwoPointsOnSameSide(
                    moveSegment!!,
                    movePoint,
                    polygon.geoCenter
                )
                result.add(
                    GraphicUtils.moveOneSide(
                        polygon,
                        moveSegment!!,
                        dis,
                        direction
                    )
                )
            } else {
                result.add(Polygon(polygon.sides, polygon.boundingBox))
            }
        }
        return result
    }

    /**
     * 合并图片中所有的能合并的线段
     * @return
     */
    private fun getAllCombinedSegments(): HashMap<Segment, ArrayList<Segment?>> {
        val result: HashMap<Segment, ArrayList<Segment?>> = HashMap<Segment, ArrayList<Segment?>>()

        //初始化所有边
        for (polygon in oriPolygons) {
            for (side in polygon.sides) {
                if (result.size == 0) {
                    val segList: ArrayList<Segment?> = ArrayList()
                    segList.add(side)
                    result[side] = segList
                } else {
                    var isFound = false
                    val keys: Set<Segment> = result.keys
                    for (target in keys) {
                        if (GraphicUtils.combine(side, target) != null) {
                            val newKey: Segment = GraphicUtils.combine(side, target)!!
                            val newArrayList: ArrayList<Segment?> = result[target]!!
                            newArrayList.add(side)
                            result.remove(target)
                            result[newKey] = newArrayList
                            isFound = true
                            break
                        }
                    }
                    if (!isFound) {
                        val segList: ArrayList<Segment?> = ArrayList<Segment?>()
                        segList.add(side)
                        result[side] = segList
                    }
                }
            }
        }
        return result
    }

    private fun getAllVertexes(src: ArrayList<Polygon>): HashMap<Point, ArrayList<Polygon?>> {
        val result: HashMap<Point, ArrayList<Polygon?>> = HashMap<Point, ArrayList<Polygon?>>()

        //初始化所有顶点
        for (polygon in src) {
            for (point in polygon.vertexes) {
                if (result.size == 0) {
                    val polygonList: ArrayList<Polygon?> = ArrayList()
                    polygonList.add(polygon)
                    result[point] = polygonList
                } else {
                    var isFound = false
                    val keys: Set<Point> = result.keys
                    for (target in keys) {
                        if (point.equals(target)) {
                            result[target]!!.add(polygon)
                            isFound = true
                            break
                        }
                    }
                    if (!isFound) {
                        val polygonList: ArrayList<Polygon?> = ArrayList<Polygon?>()
                        polygonList.add(polygon)
                        result[point] = polygonList
                    }
                }
            }
        }
        return result
    }
}
