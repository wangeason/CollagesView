package io.github.wangeason.collages.model.collage

import android.graphics.Bitmap
import io.github.wangeason.collages.model.BaseItem
import io.github.wangeason.collages.polygon.BoundingBox
import io.github.wangeason.collages.polygon.Polygon
import java.util.*
import kotlin.collections.ArrayList

class BackBitmapItem(var bitmap: Bitmap) : BaseItem() {
    private var polygon: Polygon
    var isCenterCrop: Boolean
    var isCenterInside: Boolean
    private var dragButtons: ArrayList<DragButton>? = ArrayList<DragButton>()
    private var mPolygonChangeListener: PolygonChangeListener? = null
    fun getDragButtons(): ArrayList<DragButton>? {
        return dragButtons
    }

    fun setDragButtons(draggableSidesIndexes: ArrayList<DragButton>?) {
        dragButtons = draggableSidesIndexes
    }

    fun addDragButton(dragButton: DragButton) {
        if (dragButtons == null) {
            dragButtons = ArrayList<DragButton>()
        }
        dragButtons!!.add(dragButton)
    }

    fun clearDragButton() {
        dragButtons = ArrayList()
    }

    fun getPolygon(): Polygon {
        return polygon
    }

    fun setPolygon(polygon: Polygon) {
        if (this.polygon != null && mPolygonChangeListener != null &&
                !(this.polygon!!.boundingBox?.equals(polygon.boundingBox))!!) {
            mPolygonChangeListener!!.onBoundingChanged(this, polygon.boundingBox)
        }
        this.polygon = polygon
    }

    interface PolygonChangeListener {
        fun onBoundingChanged(src: BackBitmapItem?, newBoundingbox: BoundingBox?)
    }

    fun setPolygonChangeListener(listener: PolygonChangeListener?) {
        mPolygonChangeListener = listener
    }

    init {
        polygon = Polygon()
        isCenterCrop = false
        isCenterInside = false
    }
}
