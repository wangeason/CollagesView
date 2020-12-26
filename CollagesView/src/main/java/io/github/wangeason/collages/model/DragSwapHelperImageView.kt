package io.github.wangeason.collages.model

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.Log
import android.view.View
import io.github.wangeason.R
import io.github.wangeason.collages.model.collage.BackBitmapItem

class DragSwapHelperImageView @JvmOverloads constructor(
    context: Context?,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = R.style.DefaultDragSwapHelperImageView
) :
    View(context, attrs, defStyleAttr) {
    private lateinit var layout: IntArray

    /**
     * onDraw 时避免new Object
     * @param context
     */
    private var paint: Paint? = null
    private var pfd: PaintFlagsDrawFilter? = null

    /**
     * 被拖拽的对象
     */
    private var backBitmapItem: BackBitmapItem? = null
    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        layout = intArrayOf(left, top, right, bottom)
        Log.i(
            TAG,
            "onLayout LEFT=$left TOP=$top right=$right bottom=$bottom"
        )
        if (backBitmapItem == null) {
            return
        }
        setLayerType(LAYER_TYPE_SOFTWARE, null) // 软件加速
        prepareForOnDraw()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (backBitmapItem != null) {
            // 设置抗锯齿
            canvas.drawFilter = pfd
            canvas.save()
            canvas.drawBitmap(backBitmapItem!!.bitmap, backBitmapItem!!.matrix, paint)
            canvas.restore()
        }
    }
    /**
     * 设置图片队列
     * @param bitmaps
     */
    /**
     * 设置图片和偏移
     * @param item
     * @param layoutOffset
     */
    fun setImage(item: BackBitmapItem, layoutOffset: IntArray, clippedBitmap: Bitmap) {
        val dx = (layoutOffset[0] - layout[0]).toFloat()
        val dy = (layoutOffset[1] - layout[1]).toFloat()
        backBitmapItem = BackBitmapItem(getTransparentBitmap(clippedBitmap, 50))
        val oriMatrix = Matrix(item.oriMatrix)
        oriMatrix.postTranslate(dx, dy)
        backBitmapItem!!.oriMatrix = (oriMatrix)
        //让人看出产生了新图层
        val postMatrix = Matrix()
        postMatrix.setTranslate(4f, 4f)
        backBitmapItem!!.postMatrix = (postMatrix)
        backBitmapItem!!.path = (Path(item.path))
        backBitmapItem!!.path.transform(oriMatrix)
        invalidate()
    }

    fun moveImage(dx: Float, dy: Float) {
        backBitmapItem!!.postMatrix.setTranslate(dx, dy)
        invalidate()
    }

    private fun prepareForOnDraw() {
        paint = Paint()
        paint!!.isAntiAlias = true
        paint!!.isDither = true // 防抖动
        paint!!.isFilterBitmap = true // 过滤
        pfd = PaintFlagsDrawFilter(0, Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)
    }

    fun clear() {
        backBitmapItem = null
        invalidate()
    }

    companion object {
        private const val TAG = "DragSwapHelperImageView"

        /**
         * 按百分比修改透明度 100 为原状，0为全透明
         * @param sourceImg
         * @param number >0 <100
         * @return
         */
        fun getTransparentBitmap(sourceImg: Bitmap, number: Int): Bitmap {
            var sourceImg = sourceImg
            val argb = IntArray(sourceImg.width * sourceImg.height)
            sourceImg.getPixels(
                argb, 0, sourceImg.width, 0, 0, sourceImg
                    .width, sourceImg.height
            ) // 获得图片的ARGB值
            for (i in argb.indices) {
                val alpha = (argb[i] ushr 24) * number / 100
                argb[i] = alpha shl 24 or (argb[i] and 0x00FFFFFF)
            }
            sourceImg = Bitmap.createBitmap(
                argb, sourceImg.width, sourceImg
                    .height, Bitmap.Config.ARGB_8888
            )
            return sourceImg
        }
    }
}
