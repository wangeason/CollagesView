package io.github.wangeason.collages.polygon

import java.lang.RuntimeException
import kotlin.math.abs
import kotlin.math.sqrt

class Segment(var start: Point, var end: Point,){

    private var a = Float.NaN
    private var b = Float.NaN
    private var vertical = false
    
    init {
        if (end.x != start.x) {
            a = (end.y - start.y) / (end.x - start.x)
            b = start.y - a * start.x
        } else {
            vertical = true
        }
    }


    /**
     * Indicate whereas the line is vertical. <br></br>
     * For example, line like x=1 is vertical, in other words parallel to axis Y. <br></br>
     * In this case the A is (+/-)infinite.
     *
     * @return `True` if the line is vertical, otherwise return `False`
     */
    fun isVertical(): Boolean {
        return vertical
    }

    /**
     * y = **A**x + B
     *
     * @return The **A**
     */
    fun getA(): Float {
        return a
    }

    /**
     * y = Ax + **B**
     *
     * @return The **B**
     */
    fun getB(): Float {
        return b
    }

    /**
     * 判断两条线段相等
     * @param other
     * @return
     */
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Segment) return false
        val segment: Segment = other
        return when (start) {
            segment.end -> {
                end == segment.start
            }
            segment.start -> {
                end == segment.end
            }
            else -> {
                false
            }
        }
    }

    override fun hashCode(): Int {
        var result = start.hashCode()
        result = 31 * result + end.hashCode()
        result = 31 * result + if (a != +0.0f) java.lang.Float.floatToIntBits(a) else 0
        result = 31 * result + if (b != +0.0f) java.lang.Float.floatToIntBits(b) else 0
        result = 31 * result + if (vertical) 1 else 0
        return result
    }

    fun boundingBox(): BoundingBox {
        val boundingBox = BoundingBox()
        boundingBox.xMax = if (end.x > start.x) end.x else start.x
        boundingBox.yMax = if (end.y > start.y) end.y else start.y
        boundingBox.xMin = if (end.x < start.x) end.x else start.x
        boundingBox.yMin = if (end.y < start.y) end.y else start.y
        return boundingBox
    }


    fun copy(): Segment {
        return Segment(start.copy(), end.copy())
    }


    override fun toString(): String {
        return java.lang.String.format("%s-%s", start.toString(), end.toString())
    }

    fun length(): Float {
        return sqrt(((start.x - end.x) * (start.x - end.x) + (start.y - end.y) * (start.y - end.y)).toDouble())
            .toFloat()
    }

    fun getVector(): Vector {
        return Vector(end.x - start.x, end.y - start.y)
    }

    /**
     *
     * @param seg
     * @return the intersection of the two lines, it may not on the both segments
     * added by wangyansong
     */
    fun lineIntersect(seg: Segment): Point {
        val intersectPoint: Point

        // if both vectors aren't from the kind of x=1 lines then go into
        if (!this.isVertical() && !seg.isVertical()) {
            // check if both vectors are parallel. If they are parallel then no intersection point will exist
            if (this.getA() == seg.getA()) {
                throw RuntimeException("Two parallel segments have no intersect!!")
            }
            val x = (seg.getB() - this.getB()) / (this.getA() - seg.getA()) // x = (b2-b1)/(a1-a2)
            val y = seg.getA() * x + seg.getB() // y = a2*x+b2
            intersectPoint = Point(x, y)
        } else if (this.isVertical() && !seg.isVertical()) {
            val x: Float = this.start.x
            val y = seg.getA() * x + seg.getB()
            intersectPoint = Point(x, y)
        } else if (!this.isVertical() && seg.isVertical()) {
            val x: Float = seg.start.x
            val y = this.getA() * x + this.getB()
            intersectPoint = Point(x, y)
        } else {
            throw RuntimeException("Two vertical segments have no intersect!!")
        }
        return intersectPoint
    }

    fun isParallel(seg: Segment): Boolean {
        return (this.isVertical() && seg.isVertical()) || (this.getA() == seg.getA())
    }

    /**
     *
     * @param segment
     * @return `True` if both lines intersect, otherwise return `False`
     */
    fun intersect(segment: Segment): Boolean {
//        return GraphicUtils.isTwoPointsOnSameSide(this, segment.end, segment.start) != 1 &&
//                GraphicUtils.isTwoPointsOnSameSide(segment, this.end, this.start) != 1
        if (isParallel(segment)) return false

        val intersectPoint: Point = this.lineIntersect(segment)
        return this.boundingBox().contains(intersectPoint) &&
                segment.boundingBox().contains(intersectPoint)
    }

    /**
     * 判断两条线段是不是在同一条直线上
     * @param des
     * @return
     */
    fun isOnSameLineOf(des: Segment): Boolean {
        return if (this.isVertical() && des.isVertical()) {
            abs(this.start.x - des.start.x) < GraphicUtils.FLOAT_ACCURACY
        } else if (!this.isVertical() && !des.isVertical()) {
            (abs(this.getA() - des.getA()) < GraphicUtils.FLOAT_ACCURACY) && (abs(this.getB() - des.getB()) < GraphicUtils.FLOAT_ACCURACY)
        } else {
            false
        }
    }

    /**
     * 判断本线段是不是被des包括
     * @param des
     * @return
     */
    fun isContainedBy(des: Segment): Boolean {
        if (this.isOnSameLineOf(des)) {
            val maxX: Float =
                if (des.start.x > des.end.x) des.start.x else des.end.x
            val minX: Float =
                if (des.start.x < des.end.x) des.start.x else des.end.x
            val maxY: Float =
                if (des.start.y > des.end.y) des.start.y else des.end.y
            val minY: Float =
                if (des.start.y < des.end.y) des.start.y else des.end.y
            return ((this.start.x in (minX - GraphicUtils.FLOAT_ACCURACY)..(maxX + GraphicUtils.FLOAT_ACCURACY)) && (this.start.y in (minY - GraphicUtils.FLOAT_ACCURACY)..(maxY + GraphicUtils.FLOAT_ACCURACY))) &&
                    ((this.end.x in (minX - GraphicUtils.FLOAT_ACCURACY)..(maxX + GraphicUtils.FLOAT_ACCURACY)) && (this.end.y in (minY - GraphicUtils.FLOAT_ACCURACY)..(maxY + GraphicUtils.FLOAT_ACCURACY)))
        }
        return false
    }
}