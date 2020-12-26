package io.github.wangeason.collages.model.collage

import io.github.wangeason.collages.polygon.Point
import io.github.wangeason.collages.polygon.Segment

class DragButton {
    var index = 0
    var rotateAngle //显示按钮时顺时针旋转这个角度
            = 0f
    lateinit var center: Point
    lateinit var movingSegment: Segment
}
