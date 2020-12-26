package io.github.wangeason.collages.polygon

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
     * Indicate whereas the point lays on the rectangle of line.
     *
     * @param point
     * - The point to check
     * @return `True` if the point lays on the line, otherwise return `False`
     */
    fun isInside(point: Point): Boolean {
        val maxX = if (start.x > end.x) start.x else end.x
        val minX = if (start.x < end.x) start.x else end.x
        val maxY = if (start.y > end.y) start.y else end.y
        val minY = if (start.y < end.y) start.y else end.y
        return point.x in minX..maxX && point.y in minY..maxY
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
     * @param o
     * @return
     */
    override fun equals(o: Any?): Boolean {
        if (this === o) return true
        if (o !is Segment) return false
        val segment: Segment = o
        return if (start == segment.end) {
            end == segment.start
        } else if (start == segment.start) {
            end == segment.end
        } else {
            false
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

    fun boundingBox(): BoundingBox? {
        val _boundingBox = BoundingBox()
        _boundingBox.xMax = if (end.x > start.x) end.x else start.x
        _boundingBox.yMax = if (end.y > start.y) end.y else start.y
        _boundingBox.xMin = if (end.x < start.x) end.x else start.x
        _boundingBox.yMin = if (end.y < start.y) end.y else start.y
        return _boundingBox
    }


    fun copy(): Segment {
        return Segment(start.copy(), end.copy())
    }


    override fun toString(): String {
        return java.lang.String.format("%s-%s", start.toString(), end.toString())
    }

    fun length(): Float {
        return return Math.sqrt(((start.x - end.x) * (start.x - end.x) + (start.y - end.y) * (start.y - end.y)).toDouble())
            .toFloat()
    }

    fun getVector(): Vector {
        return Vector(end.x - start.x, end.y - start.y)
    }

    /**
     * By given this and one side of the polygon, check if both lines intersect.
     *
     * @param this
     * @param segment
     * @return `True` if both lines intersect, otherwise return `False`
     */
    fun intersect(segment: Segment): Boolean {
        var intersectPoint: Point? = null

        // if both vectors aren't from the kind of x=1 lines then go into
        intersectPoint = if (!this.isVertical() && !segment.isVertical()) {
            // check if both vectors are parallel. If they are parallel then no intersection point will exist
            if (this.getA() == segment.getA()) {
                return false
            }
            val x = (segment.getB() - this.getB()) / (this.getA() - segment.getA()) // x = (b2-b1)/(a1-a2)
            val y = segment.getA() * x + segment.getB() // y = a2*x+b2
            Point(x, y)
        } else if (this.isVertical() && !segment.isVertical()) {
            val x = this.start.x
            val y = segment.getA() * x + segment.getB()
            Point(x, y)
        } else if (!this.isVertical() && segment.isVertical()) {
            val x = segment.start.x
            val y = this.getA() * x + this.getB()
            Point(x, y)
        } else {
            return false
        }

        // System.out.println("this: " + this.toString() + " ,Side: " + side);
        // System.out.println("Intersect point: " + intersectPoint.toString());
        return segment.isInside(intersectPoint) && this.isInside(intersectPoint)
    }
}