package io.github.wangeason.collages.polygon

import java.util.ArrayList

class PolygonBuilder {
    private var _vertexes: MutableList<Point> = ArrayList()
    private val _sides: MutableList<Segment> = ArrayList()
    private lateinit var _boundingBox: BoundingBox
    private var _firstPoint = true
    private var _isClosed = false

    /**
     * Add vertex points of the polygon.<br></br>
     * It is very important to add the vertexes by order, like you were drawing them one by one.
     *
     * @param point
     * The vertex point
     * @return The builder
     */
    fun addVertex(point: Point): PolygonBuilder {
        if (_isClosed) {
            // each hole we start with the new array of vertex points
            _vertexes = ArrayList()
            _isClosed = false
        }
        updateBoundingBox(point)
        _vertexes.add(point)

        // add line (edge) to the polygon
        if (_vertexes.size > 1) {
            val Segment = Segment(_vertexes[_vertexes.size - 2], point)
            _sides.add(Segment)
        }
        return this
    }

    /**
     * Close the polygon shape. This will create a new side (edge) from the **last** vertex point to the **first** vertex point.
     *
     * @return The builder
     */
    fun close(): PolygonBuilder {
        validate()

        // add last Segment
        _sides.add(Segment(_vertexes[_vertexes.size - 1], _vertexes[0]))
        _isClosed = true
        return this
    }

    /**
     * Build the instance of the polygon shape.
     *
     * @return The polygon
     */
    fun build(): Polygon {
        validate()

        // in case you forgot to close
        if (!_isClosed) {
            // add last Segment
            _sides.add(Segment(_vertexes[_vertexes.size - 1], _vertexes[0]))
        }
        return Polygon(_sides, _boundingBox)
    }

    /**
     * Update bounding box with a new point.<br></br>
     *
     * @param point
     * New point
     */
    private fun updateBoundingBox(point: Point) {
        if (_firstPoint) {
            _boundingBox = BoundingBox()
            _boundingBox!!.xMax = point.x
            _boundingBox!!.xMin = point.x
            _boundingBox!!.yMax = point.y
            _boundingBox!!.yMin = point.y
            _firstPoint = false
        } else {
            // set bounding box
            if (point.x > _boundingBox!!.xMax) {
                _boundingBox!!.xMax = point.x
            } else if (point.x < _boundingBox!!.xMin) {
                _boundingBox!!.xMin = point.x
            }
            if (point.y > _boundingBox!!.yMax) {
                _boundingBox!!.yMax = point.y
            } else if (point.y < _boundingBox!!.yMin) {
                _boundingBox!!.yMin = point.y
            }
        }
    }

    private fun validate() {
        if (_vertexes.size < 3) {
            throw RuntimeException("Polygon must have at least 3 points")
        }
    }
}