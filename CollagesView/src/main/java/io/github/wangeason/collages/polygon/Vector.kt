package io.github.wangeason.collages.polygon

class Vector (var x: Float, var y: Float, var z:Float) {
    constructor(x: Float, y: Float,): this(x, y, 0f)



    fun scalar(v: Vector): Float {
        return x * v.x + y * v.y + z * v.z
    }

    fun cross(v: Vector): Vector {
        return Vector(y * v.z - z * v.y, z * v.x - x * v.z, x * v.y - y * v.x)
    }

    fun length(): Float {
        return if (isZero()) {
            0.0f
        } else Math.abs(Math.sqrt((x * x + y * y + z * z).toDouble()).toFloat())
    }

    fun isZero(): Boolean {
        return x == 0.0f && y == 0.0f && z == 0.0f
    }

    /**
     * if length is negative, reverse the direction
     * @param length
     * @return
     */
    fun changeLengthTo(length: Float): Vector? {
        return if (length == 0.0f || isZero()) {
            Vector(0f, 0f, 0f)
        } else {
            val scale = length / length()
            Vector(x * scale, y * scale, z * scale)
        }
    }

    fun getRotateAngle(): Double {
        return if (isZero()) {
            Double.NaN
        } else {
            var atan = Math.atan2(y.toDouble(), x.toDouble())
            if (atan < 0.0) {
                atan += Math.PI * 2
            }
            atan
        }
    }

}