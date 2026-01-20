package com.ufony.trackersdk

import android.view.View
import android.content.Context
 import android.util.AttributeSet
 import android.graphics.*


class FocusView : View {
    private val mTransparentPaint = Paint().apply {
        color = Color.TRANSPARENT
        strokeWidth = 10F
    }

    private val mSemiBlackPaint = Paint().apply {
        color = Color.TRANSPARENT
        strokeWidth = 10F
    }

    private val mPath = Path()
    var radiusGap: Int = 0

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    override
    fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        mPath.reset()

        radiusGap = (width / 10) + 30


        val xPoint = width / 2.0f
        val yPoint = height / 2.0f
        val radius = (width / 2.0f) - radiusGap

        mPath.addCircle(xPoint, yPoint, radius, Path.Direction.CW)
        mPath.fillType = Path.FillType.INVERSE_EVEN_ODD

        canvas.drawCircle(xPoint, yPoint, radius, mTransparentPaint)

        canvas.drawPath(mPath, mSemiBlackPaint)

        canvas.clipPath(mPath)

        canvas.drawColor(Color.parseColor("#C61e90FF"))
    }
}