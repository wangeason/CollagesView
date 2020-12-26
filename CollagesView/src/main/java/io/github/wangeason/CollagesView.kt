package io.github.wangeason

import android.content.Context
import android.graphics.*
import android.text.TextPaint
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import io.github.wangeason.collages.model.BaseItem
import io.github.wangeason.collages.model.addon.AddOnItem
import io.github.wangeason.collages.model.addon.AddOnTextItem
import io.github.wangeason.collages.model.addon.DelButton
import io.github.wangeason.collages.model.addon.RotateButton
import io.github.wangeason.collages.model.collage.BackBitmapItem
import io.github.wangeason.collages.model.collage.DragButton
import io.github.wangeason.collages.model.collage.PathArcPoints
import io.github.wangeason.collages.polygon.*
import io.github.wangeason.collages.polygon.Point
import io.github.wangeason.collages.utils.DensityUtils
import io.github.wangeason.collages.utils.DragPolygonHelper

/**
 * TEST
 *
 * @constructor
 * TEST
 *
 * @param context
 */
class CollagesView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = R.style.DefaultCollagesView
) :
    View(context, attrs, defStyleAttr) {
    /**
     * 图片间默认间隔
     */
    private var pathWidth: Float

    /**
     * 图片选中标识默认宽度
     */
    private val selectorWidth: Float

    /**
     * DragButton半径
     */
    private val dragBtnRadius: Float

    /**
     * 圆角起止点距离顶点的距离占最大可能值得百分比0-100
     */
    private var sideCutRatio = 0f

    /**
     * 长宽比aspect with/length;
     */
    private var aspectRatio = 1.0f
    private val backBitmapItems: ArrayList<BackBitmapItem?>? = ArrayList<BackBitmapItem?>()
    private val addOnItems: ArrayList<AddOnItem> = ArrayList<AddOnItem>()

    /**
     * 长按响应任务
     */
    private val mStartSwapRunnable: Runnable = Runnable {
        if (isMode(Mode.BI_START)) {
            setMode(Mode.BI_SWAP_START)
        } else {
        }
    }

    /**
     * 取消选定响应任务
     */
    private val mCancelSelectRunnable: Runnable = Runnable {
        if (isCancelable) {
            Log.i(TAG, "Cancel Select")
            setSelectMode(SelectMode.NONE, null)
            isCancelable = false
        }
    }

    /**
     * 状态改变接口
     */
    private var mModeChangeListener: ModeChangeListener = io.github.wangeason.collages.listener.ModeChangeListener()
    private var touchedItem: BaseItem? = null

    /**
     * 是否显示选中和编辑标记
     */
    private var isSelectorEnable = true

    /**
     * 是否是系统触发的onLayout
     */
    private var isAuto = false

    /**
     * view的宽高
     */
    var viewWidth = 0
    var viewHeight = 0

    /**
     * 判断两次按下的是不是同一个BackBitmapItem
     */
    private var isCancelable = false

    /**
     * 两个手指做旋转用
     */
    private var startVector: Vector? = null
    fun setIsSelectorEnable(selectorEnable: Boolean) {
        isSelectorEnable = selectorEnable
        invalidate()
    }

    fun isSelectorEnable(): Boolean {
        return isSelectorEnable
    }

    private var onBtnClickListener: OnBtnClickListener = io.github.wangeason.collages.listener.OnBtnClickListener()

    interface OnBtnClickListener {
        fun onDelBtnClicked(
            context: Context?,
            collagesView: CollagesView?,
            addOnItem: AddOnItem?
        )

        fun onRotateButtonClicked(
            context: Context?,
            collagesView: CollagesView?,
            addOnItem: AddOnItem?
        )
    }

    fun setOnBtnClickListener(listener: OnBtnClickListener) {
        onBtnClickListener = listener
    }

    interface ModeChangeListener {
        fun onInit(event: Point?, currentBackBitmapItem: BackBitmapItem?)
        fun onBitmapItemStart(point: Point?, currentBackBitmapItem: BackBitmapItem?)
        fun onBitmapItemDragging(point: Point?, currentBackBitmapItem: BackBitmapItem?)
        fun onBitmapItemDragEnd(point: Point?, currentBackBitmapItem: BackBitmapItem?)
        fun onBitmapItemZoomStart(point: Point?, currentBackBitmapItem: BackBitmapItem?)
        fun onBitmapItemZoomingImg(point: Point?, currentBackBitmapItem: BackBitmapItem?)
        fun onBitmapItemZoomEnd(point: Point?, currentBackBitmapItem: BackBitmapItem?)
        fun onBitmapItemDragPolygonStart(point: Point?, currentBackBitmapItem: BackBitmapItem?)
        fun onBitmapItemDraggingPolygon(point: Point?, currentBackBitmapItem: BackBitmapItem?)
        fun onBitmapItemDragPolygonEnd(point: Point?, currentBackBitmapItem: BackBitmapItem?)
        fun onBitmapItemSwapStart(point: Point?, currentBackBitmapItem: BackBitmapItem?)
        fun onBitmapItemSwapDragging(point: Point?, currentBackBitmapItem: BackBitmapItem?)
        fun onBitmapItemSwapDropped(point: Point?, currentBackBitmapItem: BackBitmapItem?)
        fun onAddOnItemStart(point: Point?, currentBackBitmapItem: AddOnItem?)
        fun onAddOnItemDragging(point: Point?, currentBackBitmapItem: AddOnItem?)
        fun onAddOnItemDragEnd(point: Point?, currentBackBitmapItem: AddOnItem?)
        fun onAddOnItemZoomStart(point: Point?, currentBackBitmapItem: AddOnItem?)
        fun onAddOnItemZoomingImg(point: Point?, currentBackBitmapItem: AddOnItem?)
        fun onAddOnItemZoomEnd(point: Point?, currentBackBitmapItem: AddOnItem?)
    }

    fun setOnModeChangeListener(listener: ModeChangeListener) {
        mModeChangeListener = listener
    }

    /** 操作状态
     * INIT|BI_DRAGGING -> BI_DRAGGING -> BI_DRAG_END -> INIT
     * INIT|BI_DRAGGING|BI_ZOOMING -> BI_ZOOMING -> BI_ZOOM_END -> INIT
     * INIT -> BI_DRAG_POLYGON_START/BI_DRAGGING_POLYGON -> BI_DRAGGING_POLYGON -> BI_DRAG_POLYGON_END -> INIT
     * BI stands for "BackBitmapItem";
     */
    private enum class Mode {
        INIT,

        /** 无操作状态, 无手指按上屏幕  */
        BI_START,  // one finger down on something other than path
        BI_DRAGGING, BI_DRAG_END, BI_ZOOM_START, BI_ZOOMING, BI_ZOOM_END, BI_DRAG_POLYGON_START, BI_DRAGGING_POLYGON, BI_DRAG_POLYGON_END, BI_SWAP_START, BI_SWAP_DRAGGING, BI_SWAP_DROPPED, AO_START, AO_DRAGGING, AO_DRAG_END, AO_ZOOM_START, AO_ZOOMING, AO_ZOOM_END
    }

    /** 记录是拖拉照片模式还是放大缩小照片模式  */
    private var mode = Mode.INIT // 初始状态

    /** 记录是否选定  */
    enum class SelectMode {
        NONE, BACK_BITMAP, ADDON_BITMAP, ADDON_TEXT
    }

    private var selectMode: SelectMode? = null
    private var currentItem: BaseItem? = null

    /** 用于记录开始时候的坐标位置  */
    private val startPoint = PointF()

    /** 用于记录任何时候的坐标位置  */
    private var touchPoint: Point? = null

    /** 两个手指的开始距离  */
    private var startDis = 0f

    /** 两个手指的中间点  */
    private var midPoint: PointF? = null

    /** 图片初始位置脚本  */
    private lateinit var script: Array<IntArray>

    /**
     * padding 四周都使用paddingLeft,其他三个方向屏蔽
     */
    private var padding = 0

    /**
     * layout
     */
    private lateinit var layout: IntArray

    /**
     * onDraw 时避免new Object
     * @param context
     */
    private var paint: Paint? = null
    private val pfd = PaintFlagsDrawFilter(0, Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)
    private var dashLinePaint: Paint? = null
    private var linePaint: Paint? = null
    private var dragButtonPaint: Paint? = null
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val heightSpecMode = MeasureSpec.getMode(heightMeasureSpec)
        viewWidth = getDefaultSize(suggestedMinimumWidth, widthMeasureSpec)
        viewHeight = getDefaultSize(suggestedMinimumHeight, heightMeasureSpec)

        //默认设置为比例要求高度
        if (viewHeight == 0 && heightSpecMode == MeasureSpec.UNSPECIFIED) {
            viewHeight = (viewWidth / aspectRatio).toInt()
        }
        if (aspectRatio != viewWidth.toFloat() / viewHeight.toFloat()) {
            if (viewWidth / viewHeight.toFloat() > aspectRatio) {
                viewHeight = (viewHeight * aspectRatio).toInt()
            } else {
                viewHeight = (viewWidth / aspectRatio).toInt()
            }
        }
        setMeasuredDimension(viewWidth, viewHeight)
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
//        super.onLayout(changed, left, top, right, bottom);
        layout = intArrayOf(left, top, right, bottom)
        Log.i(
            TAG,
            "onLayoutpr LEFT=$left TOP=$top right=$right bottom=$bottom"
        )
        if (getBackBitmapCount() < MIN_PICS && getAddOnCount() == 0) {
            return
        }


        //padding
        padding = paddingLeft

//        state = START;
        setLayerType(LAYER_TYPE_SOFTWARE, null) // 软件加速
        prepareForOnDraw()

        //若有拼接用的Bitmap
        if (getBackBitmapCount() >= MIN_PICS) {
            initBackBitmapPolygons()

            //被系统自动layout，要初始化bitmapItem的oriMatrix
            initBackBitmapMatrix(isAuto) // 缩小图片
            if (!isAuto) {
                isAuto = true
            }
            initBackBitmapPath()
            initBackBitmapDragButton()
        }
    }

    /**
     * 贴纸的数量
     * @return
     */
    private fun getAddOnCount(): Int {
        return addOnItems.size
    }

    private fun prepareForOnDraw() {
        if (paint == null) {
            paint = Paint()
        }
        if (linePaint == null) {
            linePaint = Paint()
            linePaint!!.style = Paint.Style.STROKE
            linePaint!!.color = Color.BLUE
            linePaint!!.strokeWidth = selectorWidth * 2
        }
        if (dashLinePaint == null) {
            dashLinePaint = Paint()
            dashLinePaint!!.style = Paint.Style.STROKE
            dashLinePaint!!.color = Color.BLUE
            dashLinePaint!!.strokeWidth = selectorWidth * 2
            dashLinePaint!!.pathEffect = DashPathEffect(floatArrayOf(5f, 5f, 5f, 5f), 0f)
        }
        if (dragButtonPaint == null) {
            dragButtonPaint = Paint()
            dragButtonPaint!!.color = Color.BLUE
        }
    }

    private fun initBackBitmapDragButton() {
        val border1 = Segment(Point(0f + padding, 0f + padding), Point(width.toFloat() - padding, 0f + padding))
        val border2 =
            Segment(Point(width.toFloat() - padding, 0f + padding), Point(width.toFloat() - padding, height.toFloat() - padding))
        val border3 =
            Segment(Point(width.toFloat() - padding, height.toFloat() - padding), Point(0f + padding, height.toFloat() - padding))
        val border4 = Segment(Point(0f + padding, height.toFloat() - padding), Point(0f + padding, 0f + padding))

        //CalcDraggableSides
        for (backBitmapItem: BackBitmapItem? in backBitmapItems!!) {
            for (index in 0 until backBitmapItem!!.getPolygon().sides.size) {
                if (!GraphicUtils.isSegmentsOnSameLine(
                        backBitmapItem.getPolygon().sides.get(index), border1
                    )
                    && !GraphicUtils.isSegmentsOnSameLine(
                        backBitmapItem.getPolygon().sides.get(index), border2
                    )
                    && !GraphicUtils.isSegmentsOnSameLine(
                        backBitmapItem.getPolygon().sides.get(index), border3
                    )
                    && !GraphicUtils.isSegmentsOnSameLine(
                        backBitmapItem.getPolygon().sides.get(index), border4
                    )
                ) {
                    val dragButton = DragButton()
                    dragButton.index = index
                    dragButton.rotateAngle = (
                        if (backBitmapItem.getPolygon().sides.get(index)
                                .isVertical()
                        ) 90f
                        else Math.atan(
                            backBitmapItem.getPolygon().sides.get(index).getA().toDouble()
                        )
                            .toFloat()
                    )
                    val segment: Segment = backBitmapItem.drawingPolygon.sides.get(index)
                    val start = Point(segment.start.x, segment.start.y)
                    val end = Point(segment.end.x, segment.end.y)
                    dragButton.center = Point((start.x + end.x) / 2, (start.y + end.y) / 2)
                    dragButton.movingSegment = (backBitmapItem.getPolygon().sides.get(index))
                    backBitmapItem.addDragButton(dragButton)
                }
            }
        }
    }

    /**
     * 初始化多边形
     */
    private fun initBackBitmapPolygons() {
        var polygons: ArrayList<Polygon> = ArrayList<Polygon>()
        if (script == null) {
            polygons = getBackBitmapPolygons(
                DEFAULT_POSITION[getBackBitmapCount()],
                width, height, padding
            )
        } else {
            polygons = getBackBitmapPolygons(script!!, width, height, padding)
        }

        //只有重新加载脚本以后才需要找活动边界，拖拉活动边界的时候，只需要重新调用setPolygons,再refresh
        setBackBitmapPolygons(polygons)
    }

    /**
     * 从setPolygonsWithCalcDraggableSides分离出来，接口手动设置位置模板
     * @param polygons
     */
    private fun setBackBitmapPolygons(polygons: ArrayList<Polygon>) {
        if (polygons.size != getBackBitmapCount()) {
            throw IllegalArgumentException(
                "setting position scripts size is different from bitmaps size, "
                        + "scripts: " + polygons.size + " bitmaps: " + backBitmapItems!!.size
            )
        }
        for (i in 0 until getBackBitmapCount()) {
            backBitmapItems!![i]?.setPolygon(polygons[i])
            backBitmapItems[i]?.setPolygonChangeListener(polygonChangeListener)
            backBitmapItems[i]?.getDragButtons()?.clear()
        }
    }

    /**
     * 从脚本中获取位置信息
     * @param script
     * @param viewWidth CollegeImageView.getWidth()减去CollagesView的padding值
     * @param viewHeight 同上
     * @param padding
     * @return
     */
    private fun getBackBitmapPolygons(
        script: Array<IntArray>,
        viewWidth: Int,
        viewHeight: Int,
        padding: Int
    ): ArrayList<Polygon> {
        val paintWidth = viewWidth - padding * 2
        val paintHeight = viewHeight - padding * 2
        val results: ArrayList<Polygon> = ArrayList<Polygon>()
        for (polygons in script.indices) {
            val vertexes: ArrayList<Point> = ArrayList<Point>()
            for (points in 0 until script[polygons].size / 2) {
                val vertex = Point(
                    script[polygons][points * 2] * paintWidth / 1000f + padding,
                    script[polygons][points * 2 + 1] * paintHeight / 1000f + padding
                )
                vertexes.add(vertex)
            }
            val builder: PolygonBuilder = PolygonBuilder()
            for (point: Point in vertexes) {
                builder.addVertex(point)
            }
            results.add(builder.build())
        }
        return results
    }

    // 初始化矩阵并缩放图片匹配polygon boundingbox
    private fun initBackBitmapMatrix(isPostMatrixReset: Boolean) {
        for (backBitmapItem: BackBitmapItem? in backBitmapItems!!) {
            //默认CenterCrop
            if (backBitmapItem!!.isCenterInside) {
                backBitmapItem.oriMatrix = (
                    GraphicUtils.getCenterInsideMatrix(
                        backBitmapItem.getPolygon().boundingBox,
                        backBitmapItem.bitmap,
                        pathWidth
                    )
                )
                backBitmapItem.isCenterCrop = false
                backBitmapItem.isCenterInside = true
            } else {
                backBitmapItem.oriMatrix = (
                    GraphicUtils.getCenterCropMatrix(
                        backBitmapItem.getPolygon().boundingBox,
                        backBitmapItem.bitmap,
                        pathWidth
                    )
                )
                backBitmapItem.isCenterCrop = true
                backBitmapItem.isCenterInside = false
            }
            if (isPostMatrixReset) {
                backBitmapItem.postMatrix.reset()
            }
        }
    }

    // 画好矩阵模块
    private fun initBackBitmapPath() {
        for (backBitmapItem: BackBitmapItem? in backBitmapItems!!) {
            val path = Path()
            backBitmapItem!!.drawingPolygon = (
                GraphicUtils.getShrinkPolygon(
                    backBitmapItem.getPolygon(),
                    pathWidth / 2
                )
            )
            val vertexes: ArrayList<Point> = backBitmapItem.drawingPolygon.vertexes
            backBitmapItem.drawingVertexes = (vertexes)
            if (getSideCutRatio() == 0f) {
                path.moveTo(vertexes[0].x, vertexes[0].y)
                for (j in 1 until vertexes.size) {
                    path.lineTo(vertexes[j].x, vertexes[j].y)
                }
                path.close()
            } else {
                val centers: ArrayList<PathArcPoints> = ArrayList<PathArcPoints>()
                GraphicUtils.getPathArcCenter(
                    backBitmapItem.drawingPolygon,
                    getSideCutLength(),
                    centers
                )
                val size = centers.size
                path.moveTo(centers[0].getStartPoint().x, centers[0].getStartPoint().y)
                for (i in 0 until size) {
                    val thisArc: PathArcPoints = centers[i]
                    val nextArc: PathArcPoints = centers[(i + 1 + size) % size]
                    path.arcTo(
                        thisArc.rectF,
                            (thisArc.startAngle * 180 / Math.PI).toFloat(),
                            (thisArc.sweepAngle * 180 / Math.PI).toFloat()
                    )
                    path.lineTo(nextArc.getStartPoint().x, nextArc.getStartPoint().y)
                }
                path.close()
                //                path.moveTo(centers.get(0).getStartPoint().x, centers.get(0).getStartPoint().y);
//                path.addArc(centers.get(0).getRectF(), centers.get(0).getStartAngle(), 0 - thisArc.getSweepAngle());
//                for(int i = 0; i < size; i++){
//                    PathArcPoints thisArc = centers.get((i-1+size)%size);
//                    PathArcPoints nextArc = centers.get(i);
////                    path.lineTo(thisArc.getEndPoint().x, thisArc.getEndPoint().y);
//                    path.addArc(thisArc.getRectF(),thisArc.getStartAngle(),0-thisArc.getSweepAngle());
//                    path.lineTo(nextArc.getStartPoint().x, nextArc.getStartPoint().y);
//                }
//                path.lineTo(centers.get(size-1).getEndPoint().x, centers.get(size-1).getEndPoint().y);
//                path.close();
            }
            backBitmapItem.path = path
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (getBackBitmapCount() >= MIN_PICS) {
            // 设置抗锯齿
            canvas.drawFilter = pfd
            for (backBitmapItem: BackBitmapItem? in backBitmapItems!!) {
                canvas.save()
                canvas.clipPath(backBitmapItem!!.path)
                canvas.drawBitmap(backBitmapItem.bitmap, backBitmapItem.matrix, paint)
                canvas.restore()
            }
        }
        if (getAddOnCount() > 0) {
            for (item: AddOnItem in addOnItems) {
                if (item is AddOnTextItem) {
                    val addOnTextItem: AddOnTextItem = (item as AddOnTextItem)
                    val bounds: Rect = addOnTextItem.bounds!!
                    val oriPoint = Point(
                        bounds.width() / 2 + dragBtnRadius * 3 / 2,
                        bounds.height() - bounds.bottom + dragBtnRadius * 3 / 2
                    )
                    canvas.save()
                    canvas.concat(addOnTextItem.matrix)
                    canvas.drawText(
                        addOnTextItem.text,
                        oriPoint.x,
                        oriPoint.y,
                        addOnTextItem.strokePaint
                    )
                    canvas.drawText(
                        addOnTextItem.text,
                        oriPoint.x,
                        oriPoint.y,
                        addOnTextItem.textPaint
                    )
                    //                    canvas.drawText(addOnTextItem.text, bounds.width() / 2, bounds.height() - bounds.bottom, addOnTextItem.strokePaint);
//                    canvas.drawText(addOnTextItem.text, bounds.width() / 2, bounds.height() - bounds.bottom, addOnTextItem.textPaint);
                    canvas.restore()
                } else {
                    canvas.drawBitmap(item.bitmap, item.matrix, paint)
                }
            }
        }
        if (isSelectorEnable() && currentItem != null) {
            if (currentItem is BackBitmapItem) {
                val backBitmapItem: BackBitmapItem = (currentItem as BackBitmapItem)
                //拖拽时画两个框，不画拖拽按钮
                if (isMode(Mode.BI_SWAP_START) || isMode(Mode.BI_SWAP_DRAGGING)) {
                    canvas.save()
                    canvas.clipPath(backBitmapItem.path)
                    canvas.drawPath(backBitmapItem.path, (linePaint)!!)
                    canvas.restore()
                    if (currentItem !== touchedItem && touchedItem != null) {
                        canvas.save()
                        canvas.clipPath((touchedItem as BackBitmapItem).path)
                        canvas.drawPath((touchedItem as BackBitmapItem).path, (linePaint)!!)
                        canvas.restore()
                    }
                } else if (isSelectMode(SelectMode.BACK_BITMAP)) { //画选择的边框
                    canvas.save()
                    canvas.clipPath(backBitmapItem.path)
                    canvas.drawPath(backBitmapItem.path, (linePaint)!!)
                    canvas.restore()
                    //画拖拉圆点
                    for (dragButton: DragButton in (currentItem as BackBitmapItem?)!!.getDragButtons()!!) {
                        canvas.drawCircle(
                            dragButton.center.x, dragButton.center.y, dragBtnRadius,
                            (dragButtonPaint)!!
                        )
                    }
                }
            } else if (currentItem is AddOnItem) {
                val addOnItem: AddOnItem = currentItem as AddOnItem
                canvas.save()
                canvas.clipPath(addOnItem.path, Region.Op.DIFFERENCE)
                canvas.drawPath(addOnItem.path, (linePaint)!!)
                canvas.restore()
                if (!(isMode(Mode.AO_DRAGGING) || isMode(Mode.AO_ZOOM_START) || isMode(Mode.AO_ZOOMING))) {
                    canvas.drawCircle(
                        addOnItem.delButton.center.x,
                        addOnItem.delButton.center.y,
                        dragBtnRadius * 2,
                        (dragButtonPaint)!!
                    )
                    canvas.drawCircle(
                        addOnItem.rotateButton.center.x,
                        addOnItem.rotateButton.center.y,
                        dragBtnRadius * 2,
                        (dragButtonPaint)!!
                    )
                }
            }
        } else {
            //Do Nothing
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        synchronized(this) {
            touchPoint = Point(event.getX(), event.getY())
            touchedItem = getEventItem(event)
            when (event.getAction() and MotionEvent.ACTION_MASK) {
                MotionEvent.ACTION_DOWN -> {
                    isCancelable = false
                    // 当第一个手指按下时
                    startPoint.set(event.getX(), event.getY())
                    postDelayed(
                        mStartSwapRunnable,
                        LONG_PRESS_THRESHOLD.toLong()
                    )

                    //优先处理DragButton事件 和 AddOn的菜单事件
                    if (isSelectMode(SelectMode.BACK_BITMAP)) {
                        //优先检查拖拉按钮
                        val dragButton: DragButton? = onWhichDragButton(
                                (currentItem as BackBitmapItem?)!!.getDragButtons()!!,
                            startPoint
                        )
                        //点击在dragButton上
                        if (dragButton != null) {
                            setMode(Mode.BI_DRAG_POLYGON_START)
                            DragPolygonHelper.init(
                                getBackBitmapItems(),
                                dragButton,
                                dragButton.center
                            )

                            invalidate() // 重绘
                            return true
                        }
                    } else if (isSelectMode(SelectMode.ADDON_BITMAP) || isSelectMode(
                            SelectMode.ADDON_TEXT
                        )
                    ) {
                        val delBtn: DelButton? =
                            onWhichDelButton((currentItem as AddOnItem?)!!.delButton, startPoint)
                        val rotateBtn: RotateButton? = onWhichRotateButton(
                            (currentItem as AddOnItem?)!!.rotateButton,
                            startPoint
                        )
                        if (delBtn != null) {
                            onBtnClickListener.onDelBtnClicked(
                                getContext(),
                                this,
                                currentItem as AddOnItem?
                            )

                            invalidate() // 重绘
                            return true
                        }
                        if (rotateBtn != null) {
                            setMode(Mode.AO_ZOOM_START)
                            startVector = Vector(
                                (currentItem as AddOnItem?)!!.center.x - event.getX(),
                                (currentItem as AddOnItem?)!!.center.y - event.getY()
                            )

                            invalidate() // 重绘
                            return true
                        }
                    }

                    //贴纸先于背景，后添加的贴纸先于先添加的
                    var foundAddOn: Boolean = false
                    var i: Int = addOnItems.size - 1
                    while (i >= 0) {
                        val addOnItem: AddOnItem = addOnItems.get(i)
                        if (addOnItem.drawingPolygon
                                .contains(Point(event.getX(), event.getY()))
                        ) {
                            foundAddOn = true
                            if (currentItem !== addOnItem) {
                                Log.i(TAG, "Click new Item")
                            } else {
                                Log.i(
                                    TAG,
                                    "Click same Item"
                                )
                                //双击同一个
                                isCancelable = true
                            }
                            if (addOnItem is AddOnTextItem) {
                                setSelectMode(SelectMode.ADDON_TEXT, addOnItem)
                            } else {
                                setSelectMode(SelectMode.ADDON_BITMAP, addOnItem)
                            }
                            setMode(Mode.AO_START)
                            setAddOnTop(addOnItem)
                            break
                        }
                        i--
                    }
                    if (foundAddOn) {

                        invalidate() // 重绘
                        return true
                    }

                    //如果点击在bitmap上
                    for (backBitmapItem: BackBitmapItem? in backBitmapItems!!) {
                        if (backBitmapItem!!.drawingPolygon
                                .contains(Point(event.getX(), event.getY()))
                        ) {
                            if (currentItem !== backBitmapItem) {
                                Log.i(TAG, "Click new Item")
                            } else {
                                Log.i(
                                    TAG,
                                    "Click same Item"
                                )
                                //双击同一个
                                if (System.currentTimeMillis() - lastClickTime < DOUBLE_CLICK_THRESHOLD) {
                                    removeCallbacks(mCancelSelectRunnable)
                                    Log.i(
                                        TAG,
                                        "float click: " + (System.currentTimeMillis() - lastClickTime)
                                    )
                                    if (backBitmapItem.isCenterCrop) {
                                        backBitmapItem.oriMatrix = (
                                            GraphicUtils.getCenterInsideMatrix(
                                                backBitmapItem.getPolygon().boundingBox,
                                                backBitmapItem.bitmap,
                                                pathWidth
                                            )
                                        )
                                        backBitmapItem.postMatrix = Matrix()
                                        backBitmapItem.isCenterInside = true
                                        backBitmapItem.isCenterCrop = false
                                    } else {
                                        backBitmapItem.oriMatrix = (
                                            GraphicUtils.getCenterCropMatrix(
                                                backBitmapItem.getPolygon().boundingBox,
                                                backBitmapItem.bitmap,
                                                pathWidth
                                            )
                                        )
                                        backBitmapItem.postMatrix = (Matrix())
                                        backBitmapItem.isCenterInside = (false)
                                        backBitmapItem.isCenterCrop = (true)
                                    }
                                } else {
                                    isCancelable = true
                                }
                            }
                            setSelectMode(SelectMode.BACK_BITMAP, backBitmapItem)
                            setMode(Mode.BI_START)
                            break
                        }
                    }
                    lastClickTime = System.currentTimeMillis()
                }
                MotionEvent.ACTION_MOVE -> {
                    val dx: Float = event.getX() - startPoint.x // 减去第一次的移动距离
                    val dy: Float = event.getY() - startPoint.y

                    //开始拖动以后，不考虑门限值
                    val isMoving: Boolean =
                        Math.abs(dx) > TOUCH_SLOP || Math.abs(
                            dy
                        ) > TOUCH_SLOP
                    if (isMode(Mode.BI_START) && !isMoving) {
                        Log.i(TAG, "not moving")

                        invalidate() // 重绘
                        return true
                    }
                    Log.i(
                        TAG,
                        "move point: " + Point(event.getX(), event.getY()).toString()
                    )
                    if (currentItem != null) {
                        if (isMode(Mode.BI_DRAG_POLYGON_START) || isMode(
                                Mode.BI_DRAGGING_POLYGON
                            )
                        ) {
                            setMode(Mode.BI_DRAGGING_POLYGON)
                            val newPolygons: ArrayList<Polygon>? =
                                DragPolygonHelper.getNewPolygons(event)
                            if (isPolygonsAvailable(newPolygons)) {
                                setBackBitmapPolygons(newPolygons!!)
                                initBackBitmapPath()
                                initBackBitmapDragButton()
                            } else {
                                Log.i(TAG, "drag failed")
                                setMode(Mode.BI_DRAG_POLYGON_END)
                            }
                        } else if (isMode(Mode.BI_DRAGGING) || isMode(
                                Mode.BI_START
                            )
                        ) { // 拖拉图片
                            setMode(Mode.BI_DRAGGING)
                            startPoint.x = event.getX()
                            startPoint.y = event.getY()
                            // 在没有移动之前的位置上进行移动
                            currentItem!!.postMatrix.postTranslate(dx, dy)
                        } else if (isMode(Mode.BI_ZOOM_START) || isMode(
                                Mode.BI_ZOOMING
                            )
                        ) { // 放大缩小图片
                            val endDis: Float = distance(event)
                            setMode(Mode.BI_ZOOMING)
                            // 结束距离
                            if (endDis > 10f) {
                                // 两个手指并拢在一起的时候素大于10
                                var scale: Float = endDis / startDis
                                startDis = endDis
                                val oriScale: Float =
                                    GraphicUtils.getXScale(currentItem!!.matrix)
                                scale = checkScale(oriScale, scale)
                                currentItem!!.postMatrix
                                    .postScale(scale, scale, midPoint!!.x, midPoint!!.y)
                            }
                        } else if (isMode(Mode.BI_SWAP_START) || isMode(
                                Mode.BI_SWAP_DRAGGING
                            )
                        ) {
                            setMode(Mode.BI_SWAP_DRAGGING)
                        } else if (isMode(Mode.AO_DRAGGING) || isMode(
                                Mode.AO_START
                            )
                        ) { // 拖拉图片
                            setMode(Mode.AO_DRAGGING)
                            startPoint.x = event.getX()
                            startPoint.y = event.getY()
                            // 在没有移动之前的位置上进行移动
                            currentItem!!.postMatrix.postTranslate(dx, dy)
                            (currentItem as AddOnItem).generateNewDrawingZone()
                        } else if (isMode(Mode.AO_ZOOM_START) || isMode(
                                Mode.AO_ZOOMING
                            )
                        ) { // 放大缩小图片
                            val endVector: Vector
                            if (event.getPointerCount() == 1) {
                                endVector = Vector(
                                    (currentItem as AddOnItem).center.x - event.getX(),
                                    (currentItem as AddOnItem).center.y - event.getY()
                                )
                            } else {
                                endVector = Vector(
                                    event.getX(0) - event.getX(1),
                                    event.getY(0) - event.getY(1)
                                )
                            }
                            setMode(Mode.AO_ZOOMING)
                            // 结束距离
                            if (endVector.length() > 10f) {
                                // 两个手指并拢在一起的时候素大于10
                                var scale: Float = endVector.length() / startVector!!.length()
                                val acos: Double =
                                    endVector.scalar(startVector!!) / endVector.length() / startVector!!.length().toDouble()
                                if (acos > 1 || acos < -1) {

                                    invalidate() // 重绘
                                    return true
                                }
                                val rotateAngle: Double = Math.acos(acos)
                                val direction: Int =
                                    if (startVector!!.cross(endVector).z > 0) 1 else -1
                                startVector = endVector
                                val oriXScale: Float =
                                    Math.abs(GraphicUtils.getXScale(currentItem!!.matrix))
                                val oriXSkew: Float =
                                    Math.abs(GraphicUtils.getXSkew(currentItem!!.matrix))
                                scale = checkScale(
                                    Math.sqrt((oriXScale * oriXScale + oriXSkew * oriXSkew).toDouble())
                                        .toFloat(), scale
                                )
                                Log.i(
                                    TAG,
                                    "oriXScale = " + oriXScale + " oriXSkew = " + oriXSkew + " scale = " + scale + " rotateAngle = " + rotateAngle + " direction=" + direction
                                )
                                currentItem!!.postMatrix.postRotate(
                                    (rotateAngle * 180 / Math.PI * direction).toFloat(),
                                    (currentItem as AddOnItem?)!!.center.x,
                                    (currentItem as AddOnItem?)!!.center.y
                                )
                                currentItem!!.postMatrix.postScale(
                                    scale,
                                    scale,
                                    (currentItem as AddOnItem?)!!.center.x,
                                    (currentItem as AddOnItem?)!!.center.y
                                )
                                (currentItem as AddOnItem?)!!.generateNewDrawingZone()
                            }
                        }
                    }
                }
                MotionEvent.ACTION_UP -> {
                    removeCallbacks(mStartSwapRunnable)
                    if (isMode(Mode.BI_DRAG_POLYGON_START) || isMode(
                            Mode.BI_DRAGGING_POLYGON
                        )
                    ) {
                        setMode(Mode.BI_DRAG_POLYGON_END)
                    } else if (isMode(Mode.BI_DRAGGING)) {
                        setMode(Mode.BI_DRAG_END)
                    } else if (isMode(Mode.BI_SWAP_DRAGGING) || isMode(
                            Mode.BI_SWAP_START
                        )
                    ) {
                        setMode(Mode.BI_SWAP_DROPPED)
                        val backBitmapItem: BackBitmapItem? = getEventBitmapItem(event)
                        if ((backBitmapItem != null) && (currentItem != null) && (backBitmapItem !== currentItem)) {
                            swapBitmap(backBitmapItem, (currentItem as BackBitmapItem?)!!)
                        }
                    } else if (isMode(Mode.BI_START)) {
                        if (!isSelectMode(SelectMode.NONE) && isCancelable) {
                            setSelectMode(SelectMode.NONE)
                            //解决双击的可能
                            postDelayed(
                                mCancelSelectRunnable,
                                DOUBLE_CLICK_THRESHOLD
                            )
                        }
                    }
                    setMode(Mode.INIT)
                }
                MotionEvent.ACTION_POINTER_UP ->                 // 当触点离开屏幕，但是屏幕上还有触点(手指)
                    if (isMode(Mode.BI_ZOOM_START) || isMode(Mode.BI_ZOOMING)) {
                        setMode(Mode.BI_ZOOM_END)
                    } else if (isMode(Mode.AO_ZOOM_START) || isMode(
                            Mode.AO_ZOOMING
                        )
                    ) {
                        setMode(Mode.AO_ZOOM_END)
                    }
                MotionEvent.ACTION_POINTER_DOWN -> if (currentItem != null && currentItem is BackBitmapItem) {
                    // 当屏幕上已经有触点(手指)，再有一个触点压下屏幕
                    if (!isMode(Mode.BI_DRAGGING_POLYGON) && !isMode(
                            Mode.BI_DRAG_POLYGON_START
                        )
                    ) {
                        if (isMode(Mode.BI_DRAGGING)) {
                            setMode(Mode.BI_DRAG_END)
                        }
                        setMode(Mode.BI_ZOOM_START)
                        startDis = distance(event)
                        if (startDis > 10f) { // 两个手指并拢在一起的时候像素大于10
                            midPoint = mid(event)
                            // 记录当前ImageView的缩放倍数
                        }
                    }
                } else if (currentItem != null && currentItem is AddOnItem) {
                    if (isMode(Mode.AO_DRAGGING)) {
                        setMode(Mode.AO_DRAG_END)
                    }
                    setMode(Mode.AO_ZOOM_START)
                    startVector =
                        Vector(event.getX(0) - event.getX(1), event.getY(0) - event.getY(1))
                }
            }
            invalidate() // 重绘
            return true
        }
    }

    private fun setAddOnTop(addOnItem: AddOnItem) {
        addOnItems.remove(addOnItem)
        addOnItems.add(addOnItem)
    }

    private fun setAddOnBottom(addOnItem: AddOnItem) {
        addOnItems.remove(addOnItem)
        addOnItems.add(0, addOnItem)
    }

    private fun onWhichRotateButton(rotateButton: RotateButton, startPoint: PointF): RotateButton? {
        return if (GraphicUtils.getDisPtToPt(
                rotateButton.center,
                Point(startPoint.x, startPoint.y)
            ) < dragBtnRadius * 3
        ) {
            rotateButton
        } else null
    }

    private fun onWhichDelButton(delButton: DelButton, startPoint: PointF): DelButton? {
        return if (GraphicUtils.getDisPtToPt(
                delButton.center,
                Point(startPoint.x, startPoint.y)
            ) < dragBtnRadius * 3
        ) {
            delButton
        } else null
    }

    private fun getEventItem(event: MotionEvent): BaseItem? {
        val addOnItem: AddOnItem? = getEventAddOnItem(event)
        return if (addOnItem != null) {
            addOnItem
        } else {
            getEventBitmapItem(event)
        }
    }

    private fun getEventBitmapItem(event: MotionEvent): BackBitmapItem? {
        for (backBitmapItem: BackBitmapItem? in backBitmapItems!!) {
            if (backBitmapItem!!.drawingPolygon.contains(Point(event.x, event.y))) {
                return backBitmapItem
            }
        }
        return null
    }

    private fun getEventAddOnItem(event: MotionEvent): AddOnItem? {
        val size = addOnItems.size
        for (i in size - 1 downTo 0) {
            if (addOnItems[i].drawingPolygon.contains(Point(event.x, event.y))) {
                return addOnItems[i]
            }
        }
        return null
    }

    /**
     * 判断点击位置在当前显示的哪个拖拉按钮上，响应空间为圆点为中心，半径两倍的方形区域,
     * 若不在返回
     * @param dragButtons
     * @param startPoint 点击位置
     * @return
     */
    private fun onWhichDragButton(
        dragButtons: ArrayList<DragButton>,
        startPoint: PointF
    ): DragButton? {
        for (dragButton: DragButton in dragButtons) {
            if ((startPoint.x <= dragButton.center.x + dragBtnRadius * 2) && (
                        startPoint.x >= dragButton.center.x - dragBtnRadius * 2) && (
                        startPoint.y <= dragButton.center.y + dragBtnRadius * 2) && (
                        startPoint.y >= dragButton.center.y - dragBtnRadius * 2)
            ) {
                return dragButton
            }
        }
        return null
    }

    /** 计算两个手指间的距离  */
    private fun distance(event: MotionEvent): Float {
        val dx = event.getX(1) - event.getX(0)
        val dy = event.getY(1) - event.getY(0)
        /** 使用勾股定理返回两点之间的距离  */
        return Math.sqrt((dx * dx + dy * dy).toDouble()).toFloat()
    }

    /** 计算两个手指间的中间点  */
    private fun mid(event: MotionEvent): PointF {
        val midX = (event.getX(1) + event.getX(0)) / 2
        val midY = (event.getY(1) + event.getY(0)) / 2
        return PointF(midX, midY)
    }

    /**
     * 返回图片数量
     * @return
     */
    fun getBackBitmapCount(): Int {
        if (backBitmapItems == null) {
            return 0
        } else return if (backBitmapItems.size >= MAX_PICS) {
            MAX_PICS
        } else {
            backBitmapItems.size
        }
    }

    /**
     * 设置当前模式
     * @param mode
     */
    private fun setMode(mode: Mode) {
        this.mode = mode
        when (mode) {
            Mode.INIT -> mModeChangeListener.onInit(touchPoint, null)
            Mode.BI_START -> mModeChangeListener.onBitmapItemStart(
                touchPoint,
                currentItem as BackBitmapItem?
            )
            Mode.BI_DRAGGING -> mModeChangeListener.onBitmapItemDragging(
                touchPoint,
                currentItem as BackBitmapItem?
            )
            Mode.BI_DRAG_END -> mModeChangeListener.onBitmapItemDragEnd(
                touchPoint,
                currentItem as BackBitmapItem?
            )
            Mode.BI_ZOOM_START -> mModeChangeListener.onBitmapItemZoomStart(
                touchPoint,
                currentItem as BackBitmapItem?
            )
            Mode.BI_ZOOMING -> mModeChangeListener.onBitmapItemZoomingImg(
                touchPoint,
                currentItem as BackBitmapItem?
            )
            Mode.BI_ZOOM_END -> mModeChangeListener.onBitmapItemZoomEnd(
                touchPoint,
                currentItem as BackBitmapItem?
            )
            Mode.BI_DRAG_POLYGON_START -> mModeChangeListener.onBitmapItemDragPolygonStart(
                touchPoint,
                currentItem as BackBitmapItem?
            )
            Mode.BI_DRAGGING_POLYGON -> mModeChangeListener.onBitmapItemDraggingPolygon(
                touchPoint,
                currentItem as BackBitmapItem?
            )
            Mode.BI_DRAG_POLYGON_END -> mModeChangeListener.onBitmapItemDragPolygonEnd(
                touchPoint,
                currentItem as BackBitmapItem?
            )
            Mode.BI_SWAP_START -> mModeChangeListener.onBitmapItemSwapStart(
                touchPoint,
                currentItem as BackBitmapItem?
            )
            Mode.BI_SWAP_DRAGGING -> mModeChangeListener.onBitmapItemSwapDragging(
                touchPoint,
                currentItem as BackBitmapItem?
            )
            Mode.BI_SWAP_DROPPED -> mModeChangeListener.onBitmapItemSwapDropped(
                touchPoint,
                currentItem as BackBitmapItem?
            )
            Mode.AO_START -> mModeChangeListener.onAddOnItemStart(
                touchPoint,
                currentItem as AddOnItem?
            )
            Mode.AO_DRAGGING -> mModeChangeListener.onAddOnItemDragging(
                touchPoint,
                currentItem as AddOnItem?
            )
            Mode.AO_DRAG_END -> mModeChangeListener.onAddOnItemDragEnd(
                touchPoint,
                currentItem as AddOnItem?
            )
            Mode.AO_ZOOM_START -> mModeChangeListener.onAddOnItemZoomStart(
                touchPoint,
                currentItem as AddOnItem?
            )
            Mode.AO_ZOOMING -> mModeChangeListener.onAddOnItemZoomingImg(
                touchPoint,
                currentItem as AddOnItem?
            )
            Mode.AO_ZOOM_END -> mModeChangeListener.onAddOnItemZoomEnd(
                touchPoint,
                currentItem as AddOnItem?
            )
        }
    }

    private fun isMode(i: Mode): Boolean {
        return mode == i
    }

    /**
     * 检查有效性
     * @param result
     * @return
     */
    private fun isPolygonsAvailable(result: ArrayList<Polygon>?): Boolean {
        if (result == null || result.size != getBackBitmapCount()) {
            return false
        }
        for (polygon: Polygon in result) {
            for (seg: Segment in polygon.sides) {
                if (seg.length() < dragBtnRadius * MIN_TIMES_OF_DRAG_BUTTON_RADIUS + pathWidth) {
                    return false
                }
            }
        }
        return true
    }

    fun isSelectMode(selectMode: SelectMode): Boolean {
        //冗余的检查 currentItem!=null
        return this.selectMode == selectMode
    }

    fun setSelectMode(mode: SelectMode?) {
        selectMode = mode
    }

    fun setSelectMode(mode: SelectMode?, baseItem: BaseItem?) {
        selectMode = mode
        currentItem = baseItem
    }

    /**
     * 设置图片队列
     * @param bitmaps
     */
    fun setImageBitmaps(bitmaps: ArrayList<Bitmap?>) {
        if (bitmaps.size < 1) {
            throw IllegalArgumentException(
                ("bitmaps size can not be less than "
                        + 1)
            )
        }
        backBitmapItems!!.clear()
        for (bitmap: Bitmap? in bitmaps) {
            backBitmapItems.add(BackBitmapItem(bitmap!!))
        }

//        state = INIT;
        invalidate()
    }

    fun getBackBitmapItems(): ArrayList<BackBitmapItem?>? {
        return backBitmapItems
    }

    /**
     * 设置图片位置
     * @param script
     */
    fun setPositionScript(script: Array<IntArray>?) {
        this.script = script!!
    }

    fun reset() {
//        state = INIT;
        initBackBitmapPolygons()
        initBackBitmapMatrix(true) // 缩小图片
        initBackBitmapPath()
        initBackBitmapDragButton()
        invalidate()
    }

    private val polygonChangeListener: BackBitmapItem.PolygonChangeListener =
        object : BackBitmapItem.PolygonChangeListener {
            override fun onBoundingChanged(src: BackBitmapItem?, newBoundingbox: BoundingBox?) {

                val oldBound: BoundingBox = src!!.getPolygon().boundingBox
                var xScale: Float = (newBoundingbox!!.xMax - newBoundingbox!!.xMin) / (oldBound.xMax - oldBound.xMin)
                var yScale: Float = (newBoundingbox!!.yMax - newBoundingbox!!.yMin) / (oldBound.yMax - oldBound.yMin)
                val yTrans: Float =
                        (newBoundingbox.yMax + newBoundingbox.yMin) / 2 - (oldBound.yMin + oldBound.yMax) / 2
                val xTrans: Float =
                        (newBoundingbox.xMax + newBoundingbox.xMin) / 2 - (oldBound.xMin + oldBound.xMax) / 2
                xScale = checkScale(GraphicUtils.getXScale(src.matrix), xScale)
                yScale = checkScale(GraphicUtils.getYScale(src.matrix), yScale)
                val x = if (xScale > 1 / xScale) xScale else 1 / xScale
                val y = if (yScale > 1 / yScale) yScale else 1 / yScale
                val scale = if (x > y) xScale else yScale
                src.postMatrix.postScale(
                        scale,
                        scale,
                        (oldBound.xMin + oldBound.xMax) / 2,
                        (oldBound.yMin + oldBound.yMax) / 2
                )
                src.postMatrix.postTranslate(xTrans, yTrans)
            }
        }

    private fun checkScale(ori: Float, postScale: Float): Float {
        if (ori * postScale > MAX_SCALE) {
            return MAX_SCALE / ori
        } else return if (ori * postScale < MIN_SCALE) {
            MIN_SCALE / ori
        } else {
            postScale
        }
    }

    fun getSideCutRatio(): Float {
        return sideCutRatio
    }

    /**
     * @return 最短边的长度的一半乘以sideCutRatio
     */
    fun getSideCutLength(): Float {
        var minSideLength = Float.MAX_VALUE
        for (item: BackBitmapItem? in backBitmapItems!!) {
            for (side: Segment in item!!.getPolygon().sides) {
                if (side.length() < minSideLength) {
                    minSideLength = side.length()
                }
            }
        }
        return (minSideLength * sideCutRatio) / 100 / 2
    }

    fun setSideCutRatio(sideCutRatio: Float) {
        var sideCutRatio = sideCutRatio
        if (sideCutRatio < 0) {
            sideCutRatio = 0f
        } else if (sideCutRatio > 100) {
            sideCutRatio = 100f
        }
        //        state = INIT;
        this.sideCutRatio = sideCutRatio
        initBackBitmapPath()
        invalidate()
    }

    fun getPadding(): Int {
        return padding
    }

    fun setPadding(padding: Int) {
//        state = INIT;
        this.padding = padding
        isAuto = false
        setPadding(padding, padding, padding, padding)
    }

    //屏蔽
    override fun getPaddingTop(): Int {
        return super.getPaddingLeft()
    }

    override fun getPaddingRight(): Int {
        return super.getPaddingLeft()
    }

    override fun getPaddingBottom(): Int {
        return super.getPaddingLeft()
    }

    fun getAspectRatio(): Float {
        return aspectRatio
    }

    fun setAspectRatio(aspectRatio: Float) {
        this.aspectRatio = aspectRatio
        //        state = INIT;
        isAuto = false
        requestLayout()
    }

    fun getPathWidth(): Float {
        return pathWidth
    }

    fun setPathWidth(pathWidth: Float) {
        this.pathWidth = pathWidth
        //        state = INIT;
        initBackBitmapPolygons()
        initBackBitmapMatrix(false) // 缩小图片
        initBackBitmapPath()
        initBackBitmapDragButton()
        invalidate()
    }

    fun getLayout(): IntArray {
        return layout
    }

    /**
     * 交换图片
     * @param i
     * @param j
     */
    fun swapBitmap(i: Int, j: Int) {
        if (i > getBackBitmapCount() - 1 || j > getBackBitmapCount() - 1) {
            return
        }
        if (backBitmapItems!![i] == null || backBitmapItems[j] == null) {
            return
        }
        swapBitmap(backBitmapItems[i]!!, backBitmapItems[j]!!)
    }

    private fun swapBitmap(backBitmapItemI: BackBitmapItem, backBitmapItemJ: BackBitmapItem) {
        val temp: Bitmap = backBitmapItemI.bitmap
        backBitmapItemI.bitmap = (backBitmapItemJ.bitmap)
        backBitmapItemJ.bitmap = (temp)
        val isICenterCrop: Boolean = backBitmapItemI.isCenterCrop
        val isICenterInside: Boolean = backBitmapItemI.isCenterInside
        val isJCenterCrop: Boolean = backBitmapItemJ.isCenterCrop
        val isJCenterInside: Boolean = backBitmapItemJ.isCenterInside
        backBitmapItemI.isCenterInside = (isJCenterInside)
        backBitmapItemI.isCenterCrop = (isJCenterCrop)
        backBitmapItemJ.isCenterCrop = (isICenterCrop)
        backBitmapItemJ.isCenterInside = (isICenterInside)
        initBackBitmapMatrix(true)
        invalidate()
    }

    fun addOnText(text: String) {
        val addOnTextItem = AddOnTextItem(text)
        val bounds = Rect()
        val paint: TextPaint = addOnTextItem.textPaint
        paint.getTextBounds(text, 0, text.length, bounds)
        val bitmap = Bitmap.createBitmap(
            bounds.width() + dragBtnRadius.toInt() * 3,
            bounds.height() + dragBtnRadius.toInt() * 3,
            Bitmap.Config.ARGB_8888
        )
        addOnTextItem.bitmap = (bitmap)
        addOnTextItem.bounds = (bounds)
        addOnTextItem.postMatrix.postTranslate(
            (viewWidth - addOnTextItem.bitmap.getWidth().toFloat()) / 2,
            (viewHeight - addOnTextItem.bitmap.getHeight().toFloat()) / 2
        )

//        Log.i(TAG, "width=" + bounds.width() + " height=" + bounds.height());
//        Log.i(TAG, "padding=" + dragBtnRadius * 3);
//        Paint.FontMetrics m = paint.getFontMetrics();
//        Log.i(TAG, "top:"+ m.top + " ascent:" + m.ascent + " leading:" + m.leading + " descent:" + m.descent + " bottom:" + m.bottom);
        addOnTextItem.generateNewDrawingZone()
        addOnItems.add(addOnTextItem)
        invalidate()
    }

    fun addOnBitmap(bitmap: Bitmap) {
        val scaleHeight = (viewHeight / bitmap.height).toFloat()
        val scaleWidth = (viewWidth / bitmap.width).toFloat()
        val scale = Math.min(1.0f, Math.min(scaleHeight, scaleWidth))
        val newAddon = AddOnItem()
        newAddon.bitmap = (
            Bitmap.createScaledBitmap(
                bitmap,
                (bitmap.width * scale).toInt(), (bitmap.height * scale).toInt(), true
            )
        )
        newAddon.postMatrix.postTranslate(
            (viewWidth - newAddon.bitmap.getWidth().toFloat()) / 2,
            (viewHeight - newAddon.bitmap.getHeight().toFloat()) / 2
        )
        newAddon.generateNewDrawingZone()
        addOnItems.add(newAddon)
        invalidate()
    }

    fun removeAddOn(addOnItem: AddOnItem) {
        addOnItems.remove(addOnItem)
        setSelectMode(SelectMode.NONE, null)
        invalidate()
    }

    /**
     * 获得输出BITMAP
     * @return
     */
    fun getBitmap(): Bitmap {
        isDrawingCacheEnabled = false
        isDrawingCacheEnabled = true
        return drawingCache
    }

    companion object {
        /** 默认位置 直线 */
        val DEFAULT_POSITION = arrayOf(
            arrayOf(),
            arrayOf(intArrayOf(0, 0, 1000, 0, 1000, 1000, 0, 1000)),
            arrayOf(
                intArrayOf(0, 0, 500, 0, 500, 1000, 0, 1000),
                intArrayOf(500, 0, 1000, 0, 1000, 1000, 500, 1000)
            ),
            arrayOf(
                intArrayOf(0, 0, 1000, 0, 1000, 400, 0, 400),
                intArrayOf(0, 400, 500, 400, 500, 1000, 0, 1000),
                intArrayOf(500, 400, 1000, 400, 1000, 1000, 500, 1000)
            ),
            arrayOf(
                intArrayOf(0, 0, 500, 0, 500, 500, 0, 500),
                intArrayOf(500, 0, 1000, 0, 1000, 500, 500, 500),
                intArrayOf(0, 500, 500, 500, 500, 1000, 0, 1000),
                intArrayOf(500, 500, 1000, 500, 1000, 1000, 500, 1000)
            )
        ) //4张图

        /**
         * 放大缩小倍数限制
         */
        val MAX_SCALE = 4.0f
        val MIN_SCALE = 0.25f
        private val DEFAULT_PATH_WIDTH_DP = 6f
        private val DEFAULT_SELECTOR_WIDTH_DP = 2f
        private val DEFAULT_DRAG_BUTTON_RADIUS_DP = 6f

        /**
         * 最短边长相对拖动按钮半径的最小倍数
         */
        private val MIN_TIMES_OF_DRAG_BUTTON_RADIUS = 6
        /**
         * 用于判断只在第一次绘制才初始化一些资源数据
         */
        //    private final int INIT = -1;
        //    private final int START = 1;
        //    private int state = INIT;
        /**
         * 拼接图片数量范围
         */
        private val MIN_PICS = 1
        private val MAX_PICS = 4

        /**
         * bitmap双击相关
         */
        private val DOUBLE_CLICK_THRESHOLD: Long = 300
        private var lastClickTime: Long = 0

        /**
         * bitmap长按阈值
         */
        private val LONG_PRESS_THRESHOLD = 800

        /**
         * 日志TAG
         */
        val DEBUG = true
        private val TAG = "CollagesView"

        /**
         * 移动的阈值
         */
        private val TOUCH_SLOP = 20
    }

    init {
        val a = context.obtainStyledAttributes(
            attrs, R.styleable.CollagesView,
            R.attr.collagesViewStyle, defStyleAttr
        )
        pathWidth = a.getDimension(
            R.styleable.CollagesView_path_width,
                DensityUtils.dip2px(context, DEFAULT_PATH_WIDTH_DP).toFloat()
        )
        selectorWidth = a.getDimension(
            R.styleable.CollagesView_selector_line_width,
            DensityUtils.dip2px(context, DEFAULT_SELECTOR_WIDTH_DP).toFloat()
        )
        dragBtnRadius = a.getDimension(
            R.styleable.CollagesView_dragbutton_radius,
            DensityUtils.dip2px(context, DEFAULT_DRAG_BUTTON_RADIUS_DP).toFloat()
        )
        a.recycle()
    }
}
