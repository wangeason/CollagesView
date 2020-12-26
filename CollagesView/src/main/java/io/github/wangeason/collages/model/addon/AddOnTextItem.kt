package io.github.wangeason.collages.model.addon

import android.graphics.Paint
import android.graphics.Paint.Align
import android.graphics.Rect
import android.text.TextPaint

class AddOnTextItem(var text: String) : AddOnItem() {
    var textSize = 16
    private var mOutLineRadius = 0f
    private var mShadowRadius = 0f
    private var mShadowDx = 0f
    private var mShadowDy = 0f
    private var mTextColor: Int
    private var mShadowColor: Int
    private var mOutLineColor: Int
    var textPaint: TextPaint
    var strokePaint: TextPaint
    var bounds: Rect? = null

    var align: Align

    fun getmOutLineRadius(): Float {
        return mOutLineRadius
    }

    fun setmOutLineRadius(mOutLineRadius: Float) {
        this.mOutLineRadius = mOutLineRadius
    }

    fun getmShadowRadius(): Float {
        return mShadowRadius
    }

    fun setmShadowRadius(mShadowRadius: Float) {
        this.mShadowRadius = mShadowRadius
    }

    fun getmShadowDx(): Float {
        return mShadowDx
    }

    fun setmShadowDx(mShadowDx: Float) {
        this.mShadowDx = mShadowDx
    }

    fun getmShadowDy(): Float {
        return mShadowDy
    }

    fun setmShadowDy(mShadowDy: Float) {
        this.mShadowDy = mShadowDy
    }

    fun getmTextColor(): Int {
        return mTextColor
    }

    fun setmTextColor(mTextColor: Int) {
        this.mTextColor = mTextColor
    }

    fun getmShadowColor(): Int {
        return mShadowColor
    }

    fun setmShadowColor(mShadowColor: Int) {
        this.mShadowColor = mShadowColor
    }

    fun getmOutLineColor(): Int {
        return mOutLineColor
    }

    fun setmOutLineColor(mOutLineColor: Int) {
        this.mOutLineColor = mOutLineColor
    }

    init {
        mTextColor = -0x1000000
        mShadowColor = 0xfffffff
        mOutLineColor = 0xfffffff
        align = Align.LEFT
        textPaint = TextPaint()
        textPaint.setARGB(255, 255, 255, 255)
        textPaint.textAlign = Align.CENTER
        textPaint.textSize = 64f
        strokePaint = TextPaint()
        strokePaint.setARGB(255, 0, 0, 0)
        strokePaint.textAlign = Align.CENTER
        strokePaint.textSize = 64f
        strokePaint.style = Paint.Style.STROKE
        strokePaint.strokeWidth = 10f
    }
}