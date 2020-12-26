package io.github.wangeason.collages.polygon

import java.util.*

class Polygon {
    val boundingBox: BoundingBox
    val sides: MutableList<Segment>

    constructor() {
        boundingBox = BoundingBox()
        sides = ArrayList()
    }

    //重新设置polygon,
    constructor(src: Polygon) {
        boundingBox = BoundingBox(src.boundingBox)
        sides = ArrayList()
        sides.addAll(src.sides)
    }

    //指向
    constructor(sides: MutableList<Segment>, boundingBox: BoundingBox) {
        this.sides = sides
        this.boundingBox = boundingBox
    }

    /**
     * Check if the the given point is inside of the polygon.<br></br>
     *
     * @param point
     * The point to check
     * @return `True` if the point is inside the polygon, otherwise return `False`
     */
    operator fun contains(point: Point): Boolean {
        if (inBoundingBox(point)) {
            val ray = createRay(point)
            var intersection = 0
            for (side in sides) {
                if (ray.intersect(side)) {
                    // System.out.println("intersection++");
                    intersection++
                }
            }

            /*
         * If the number of intersections is odd, then the point is inside the polygon
         */if (intersection % 2 == 1) {
                return true
            }
        }
        return false
    }

    /**
     * Create a ray. The ray will be created by given point and on point outside of the polygon.<br></br>
     * The outside point is calculated automatically.
     *
     * @param point
     * @return
     */
    private fun createRay(point: Point): Segment {
        // create outside point
        val epsilon = (boundingBox.xMax - boundingBox.xMin) / 100f
        val outsidePoint = Point(boundingBox.xMin - epsilon, boundingBox.yMin)
        return Segment(outsidePoint, point)
    }

    /**
     * Check if the given point is in bounding box
     *
     * @param point
     * @return `True` if the point in bounding box, otherwise return `False`
     */
    private fun inBoundingBox(point: Point): Boolean {
        return !(point.x < boundingBox.xMin || point.x > boundingBox.xMax || point.y < boundingBox.yMin || point.y > boundingBox.yMax)
    }

    val vertexes: ArrayList<Point>
        get() {
            val vertexes = ArrayList<Point>()
            for (segment in sides) {
                vertexes.add(segment.start)
            }
            return vertexes
        }

    /**
     * get The geometric center
     * @return
     */
    val geoCenter: Point
        get() {
            var xsum = 0f
            var ysum = 0f
            val num = sides.size.toFloat()
            for (vertex in vertexes) {
                xsum += vertex.x
                ysum += vertex.y
            }
            return Point(xsum / num, ysum / num)
        }

    fun copy(): Polygon {
        val build = PolygonBuilder()
        for (point in vertexes) {
            build.addVertex(point.copy())
        }
        return build.build()
    }
}