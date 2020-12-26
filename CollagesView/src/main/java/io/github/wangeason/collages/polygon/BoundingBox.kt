package io.github.wangeason.collages.polygon

class BoundingBox {

    var xMax = Float.NEGATIVE_INFINITY
    var xMin = Float.NEGATIVE_INFINITY
    var yMax = Float.NEGATIVE_INFINITY
    var yMin = Float.NEGATIVE_INFINITY

    constructor() {}

    constructor(src: BoundingBox) {
        xMin = src.xMin
        xMax = src.xMax
        yMin = src.yMin
        yMax = src.yMax
    }

    operator fun contains(pt: Point): Boolean {
        return pt.x <= xMax + GraphicUtils.float_COM && pt.x >= xMin - GraphicUtils.float_COM && pt.y <= yMax + GraphicUtils.float_COM && pt.y >= yMin - GraphicUtils.float_COM
    }


    override fun equals(o: Any?): Boolean {
        if (this === o) return true
        if (o !is BoundingBox) return false
        val boundingBox = o
        return !(xMax != boundingBox.xMax || xMin != boundingBox.xMin || yMax != boundingBox.yMax || yMin != boundingBox.yMin)
    }

    override fun hashCode(): Int {
        var result = if (xMax != +0.0f) java.lang.Float.floatToIntBits(xMax) else 0
        result = 31 * result + if (xMin != +0.0f) java.lang.Float.floatToIntBits(xMin) else 0
        result = 31 * result + if (yMax != +0.0f) java.lang.Float.floatToIntBits(yMax) else 0
        result = 31 * result + if (yMin != +0.0f) java.lang.Float.floatToIntBits(yMin) else 0
        return result
    }

    override fun toString(): String {
        return "xMin=$xMin xMax=$xMax yMin=$yMin yMax=$yMax"
    }
}