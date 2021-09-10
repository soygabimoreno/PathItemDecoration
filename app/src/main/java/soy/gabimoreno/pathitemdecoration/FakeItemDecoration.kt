package soy.gabimoreno.pathitemdecoration

import android.content.Context
import android.content.res.Resources
import android.graphics.Canvas
import android.graphics.CornerPathEffect
import android.graphics.Paint
import android.graphics.Path
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView

private const val STROKE_WIDTH = 3f
private const val PATH_CORNER_RADIUS_IN_DP = 16
private const val CHILD_HEADER_OR_FOOTER_HEIGHT_IN_DP = 64
private const val CURSOR_RADIUS_IN_DP = 5
private const val SELECTED_CURSOR_RADIUS_IN_DP = 24

// TODO: Calculate pixels once instead of doing it each time. It is a waste of time and it affects the performance

/**
 * Based on https://github.com/GioraGit/Path-along-RecyclerView by @GioraGit.
 */
internal class FakeItemDecoration(
    context: Context,
    fakeItems: List<FakeItem>,
    private val selectedPosition: Int
) : RecyclerView.ItemDecoration() {

    data class Point(
        val x: Float,
        val y: Float
    )

    private val drawPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        strokeWidth = STROKE_WIDTH
        style = Paint.Style.STROKE
        strokeJoin = Paint.Join.ROUND
        strokeCap = Paint.Cap.ROUND
        color = ContextCompat.getColor(context, R.color.black)
        pathEffect = CornerPathEffect(
            PATH_CORNER_RADIUS_IN_DP.toPx()
                .toFloat()
        )
    }

    private val cursorPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = ContextCompat.getColor(context, R.color.black)
    }

    private val selectedCursorPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = ContextCompat.getColor(context, R.color.teal_200)
    }

    private val selectedCursorToBottomLinePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        strokeWidth = STROKE_WIDTH
        style = Paint.Style.STROKE
        color = ContextCompat.getColor(context, R.color.purple_200)
    }

    private val normalizedValues = normalizeValues(fakeItems)

    private val points = MutableList(fakeItems.size) { Point(0f, 0f) }

    override fun onDraw(
        canvas: Canvas,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        super.onDraw(canvas, parent, state)

        val path = Path()
        var newPath = true

        for (childIndex in 0 until parent.childCount) {
            val childView = parent.getChildAt(childIndex)
            val dataIndex = parent.getChildAdapterPosition(childView)
            val childViewHeight = childView.height
            val halfChildViewWidth = (childView.right.toFloat() - childView.left.toFloat()) / 2

            if (newPath) {
                val previousDataIndex = if (dataIndex > 0) (dataIndex - 1) else 0
                val moveToYPosition = calculateYValue(previousDataIndex, childViewHeight)
                path.moveTo(childView.left.toFloat() - halfChildViewWidth, moveToYPosition)
                newPath = false
            }

            val x = childView.right.toFloat() - halfChildViewWidth
            val y = calculateYValue(dataIndex, childViewHeight)
            points.set(childIndex, Point(x, y))
            Log.d("FakeItemDecoration", "onDraw [$childIndex] x: $x, y: $y")
            path.lineTo(x, y)
            if (dataIndex == selectedPosition) {
                canvas.drawCircle(
                    x, y, SELECTED_CURSOR_RADIUS_IN_DP.toPx()
                        .toFloat(), selectedCursorPaint
                )
            }
            canvas.drawLine(x, y, x, childViewHeight.toFloat() - CHILD_HEADER_OR_FOOTER_HEIGHT_IN_DP * 4, selectedCursorToBottomLinePaint)
            canvas.drawCircle(
                x, y, CURSOR_RADIUS_IN_DP.toPx()
                    .toFloat(), cursorPaint
            )

            if (childIndex == parent.childCount - 1) {
                drawPathForNextChildView(
                    dataIndex + 1,
                    childView.right.toFloat(),
                    path,
                    halfChildViewWidth,
                    childViewHeight
                )
            }
        }
        canvas.drawPath(path, drawPaint)
    }

    private fun getDataIndexOfNextChild(currentChildDataIndex: Int): Int {
        val nextChildDataIndex = currentChildDataIndex + 1
        return if (nextChildDataIndex >= normalizedValues.size) {
            currentChildDataIndex
        } else {
            nextChildDataIndex
        }
    }

    private fun calculateYValue(
        dataIndex: Int,
        childViewHeight: Int
    ): Float {
        val graphHeight = childViewHeight - (CHILD_HEADER_OR_FOOTER_HEIGHT_IN_DP * 2).toPx()
        val graphStartHeightDelta = CHILD_HEADER_OR_FOOTER_HEIGHT_IN_DP.toPx()
        return ((1 - normalizedValues[dataIndex]) * graphHeight + graphStartHeightDelta)
    }

    private fun normalizeValues(fakeItems: List<FakeItem>): List<Float> {
        val minValue = fakeItems.minByOrNull { it.value }
        val maxValue = fakeItems.maxByOrNull { it.value }

        if (minValue == null || maxValue == null) {
            return emptyList()
        }

        if (minValue.value >= maxValue.value) {
            return fakeItems.map { 0.5f }
        }

        val range = maxValue.value - minValue.value
        return fakeItems.map {
            val relativeValue = it.value - minValue.value
            return@map (relativeValue / range)
        }
    }

    private fun drawPathForNextChildView(
        nextChildViewDataIndex: Int,
        nextChildViewMiddleXValue: Float,
        path: Path,
        halfChildViewWidth: Float,
        childViewHeight: Int
    ) {
        if (nextChildViewDataIndex >= normalizedValues.size) {
            handleNextAfterLastChildView(nextChildViewMiddleXValue, path, childViewHeight)
        } else {
            val nextChildViewEndXValue = nextChildViewMiddleXValue + halfChildViewWidth
            path.lineTo(nextChildViewEndXValue, calculateYValue(nextChildViewDataIndex, childViewHeight))
        }
    }

    private fun handleNextAfterLastChildView(
        lastXValue: Float,
        path: Path,
        childViewHeight: Int
    ) {
        val y = calculateYValue(normalizedValues.size - 1, childViewHeight)
        path.lineTo(lastXValue, y)
        val childIndex = points.size - 1
        points.set(childIndex, Point(lastXValue, y))
        Log.d("FakeItemDecoration", "handleNextAfterLastChildView  [$childIndex] x: $lastXValue, y: $y")
    }

    private fun Int.toPx(): Int = (this * Resources.getSystem().displayMetrics.density).toInt()
}
