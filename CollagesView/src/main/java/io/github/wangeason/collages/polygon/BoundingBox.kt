package io.github.wangeason.collages.polygon

class BoundingBox {

    var xMax = Float.NEGATIVE_INFINITY
    var xMin = Float.NEGATIVE_INFINITY
    var yMax = Float.NEGATIVE_INFINITY
    var yMin = Float.NEGATIVE_INFINITY

    constructor()

    constructor(src: BoundingBox) {
        xMin = src.xMin
        xMax = src.xMax
        yMin = src.yMin
        yMax = src.yMax
    }

    operator fun contains(pt: Point): Boolean {
        return pt.x in (xMin - GraphicUtils.FLOAT_ACCURACY)..(xMax + GraphicUtils.FLOAT_ACCURACY) &&
                pt.y in (yMin - GraphicUtils.FLOAT_ACCURACY)..(yMax + GraphicUtils.FLOAT_ACCURACY)
    }


    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is BoundingBox) return false
        return !(xMax != other.xMax || xMin != other.xMin || yMax != other.yMax || yMin != other.yMin)
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