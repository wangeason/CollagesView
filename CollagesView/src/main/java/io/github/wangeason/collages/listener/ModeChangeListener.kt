package io.github.wangeason.collages.listener

import android.util.Log
import io.github.wangeason.CollagesView
import io.github.wangeason.collages.model.addon.AddOnItem
import io.github.wangeason.collages.model.collage.BackBitmapItem
import io.github.wangeason.collages.polygon.Point

class ModeChangeListener : CollagesView.ModeChangeListener {
    override fun onInit(point: Point?, currentBackBitmapItem: BackBitmapItem?) {
        Log.i(TAG, "INIT")
    }

    override fun onBitmapItemStart(point: Point?, currentBackBitmapItem: BackBitmapItem?) {
        Log.i(TAG, "BI_START")
    }

    override fun onBitmapItemDragging(point: Point?, currentBackBitmapItem: BackBitmapItem?) {
        Log.i(TAG, "BI_DRAGGING")
    }

    override fun onBitmapItemDragEnd(point: Point?, currentBackBitmapItem: BackBitmapItem?) {
        Log.i(TAG, "BI_DRAG_END")
    }

    override fun onBitmapItemZoomStart(point: Point?, currentBackBitmapItem: BackBitmapItem?) {
        Log.i(TAG, "BI_ZOOM_START")
    }

    override fun onBitmapItemZoomingImg(point: Point?, currentBackBitmapItem: BackBitmapItem?) {
        Log.i(TAG, "BI_ZOOMING")
    }

    override fun onBitmapItemZoomEnd(point: Point?, currentBackBitmapItem: BackBitmapItem?) {
        Log.i(TAG, "BI_ZOOM_END")
    }

    override fun onBitmapItemDragPolygonStart(point: Point?, currentBackBitmapItem: BackBitmapItem?) {
        Log.i(TAG, "BI_DRAG_POLYGON_START")
    }

    override fun onBitmapItemDraggingPolygon(point: Point?, currentBackBitmapItem: BackBitmapItem?) {
        Log.i(TAG, "BI_DRAGGING_POLYGON")
    }

    override fun onBitmapItemDragPolygonEnd(point: Point?, currentBackBitmapItem: BackBitmapItem?) {
        Log.i(TAG, "BI_DRAG_POLYGON_END")
    }

    override fun onBitmapItemSwapStart(point: Point?, currentBackBitmapItem: BackBitmapItem?) {
        Log.i(TAG, "BI_SWAP_START")
    }

    override fun onBitmapItemSwapDragging(point: Point?, currentBackBitmapItem: BackBitmapItem?) {
        Log.i(TAG, "BI_SWAP_DRAGGING")
    }

    override fun onBitmapItemSwapDropped(point: Point?, oriBackBitmapItem: BackBitmapItem?, currentBackBitmapItem: BackBitmapItem?) {
        Log.i(TAG, "BI_SWAP_DROPPED")
    }

    override fun onAddOnItemStart(point: Point?, currentBackBitmapItem: AddOnItem?) {
        Log.i(TAG, "AO_START")
    }

    override fun onAddOnItemDragging(point: Point?, currentBackBitmapItem: AddOnItem?) {
        Log.i(TAG, "AO_DRAGGING")
    }

    override fun onAddOnItemDragEnd(point: Point?, currentBackBitmapItem: AddOnItem?) {
        Log.i(TAG, "AO_DRAG_END")
    }

    override fun onAddOnItemZoomStart(point: Point?, currentBackBitmapItem: AddOnItem?) {
        Log.i(TAG, "AO_ZOOM_START")
    }

    override fun onAddOnItemZoomingImg(point: Point?, currentBackBitmapItem: AddOnItem?) {
        Log.i(TAG, "AO_ZOOMING")
    }

    override fun onAddOnItemZoomEnd(point: Point?, currentBackBitmapItem: AddOnItem?) {
        Log.i(TAG, "AO_ZOOM_END")
    }

    companion object {
        private const val TAG = "CollagesView"
    }

}
