package com.cleancut.bgremover.feature.magicbrush.ui.components

import android.graphics.Bitmap
import android.graphics.Canvas as NativeCanvas
import android.graphics.Color as NativeColor
import android.graphics.Paint as NativePaint
import android.graphics.Path as NativePath
import android.graphics.Rect as NativeRect
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.IntSize
import com.cleancut.bgremover.core.designsystem.ElectricViolet
import com.cleancut.bgremover.feature.magicbrush.domain.MaskRegion
import kotlin.math.ceil
import kotlin.math.floor

private const val MIN_SCALE = 1f
private const val MAX_SCALE = 6f

internal data class BrushStrokePoint(val position: Offset, val isStrokeStart: Boolean)

/**
 * Holds live pinch/pan state and accumulated (uncommitted) brush strokes in source-pixel
 * coordinates. Strokes are cleared on a successful erase (see BrushCanvas's revision effect)
 * but the zoom/pan [transform] persists across erases - only [BrushCanvas]'s caller decides
 * when to throw the whole state away (e.g. picking a new photo).
 */
@Stable
class BrushCanvasState {
    var transform: BrushTransform by mutableStateOf(BrushTransform())
        internal set

    private val _points: SnapshotStateList<BrushStrokePoint> = mutableStateListOf()
    internal val points: List<BrushStrokePoint> get() = _points

    private var activeStrokeStartIndex = -1

    val hasPaintedContent: Boolean get() = _points.isNotEmpty()

    internal fun beginStroke(sourcePoint: Offset) {
        activeStrokeStartIndex = _points.size
        _points.add(BrushStrokePoint(sourcePoint, isStrokeStart = true))
    }

    internal fun continueStroke(sourcePoint: Offset) {
        if (activeStrokeStartIndex < 0) return
        _points.add(BrushStrokePoint(sourcePoint, isStrokeStart = false))
    }

    /** Discards the in-progress stroke - used when a second finger joins mid-draw. */
    internal fun cancelActiveStroke() {
        if (activeStrokeStartIndex in _points.indices) {
            while (_points.size > activeStrokeStartIndex) {
                _points.removeAt(_points.size - 1)
            }
        }
        activeStrokeStartIndex = -1
    }

    internal fun endStroke() {
        activeStrokeStartIndex = -1
    }

    /** Clears painted strokes only - [transform] (zoom/pan) is left untouched. */
    fun clear() {
        _points.clear()
        activeStrokeStartIndex = -1
    }

    /**
     * Rasterizes accumulated strokes into a tight ALPHA_8 mask in domain polarity (255 =
     * erase, 0 = keep), or null if nothing has been painted. [strokeWidthPx] is the brush
     * diameter in source pixels.
     */
    fun toMaskRegion(strokeWidthPx: Float): MaskRegion? {
        if (_points.isEmpty()) return null

        var minX = Float.MAX_VALUE
        var minY = Float.MAX_VALUE
        var maxX = -Float.MAX_VALUE
        var maxY = -Float.MAX_VALUE
        for (point in _points) {
            if (point.position.x < minX) minX = point.position.x
            if (point.position.y < minY) minY = point.position.y
            if (point.position.x > maxX) maxX = point.position.x
            if (point.position.y > maxY) maxY = point.position.y
        }

        val halfStroke = strokeWidthPx / 2f
        val boundingBox = NativeRect(
            floor(minX - halfStroke).toInt(),
            floor(minY - halfStroke).toInt(),
            ceil(maxX + halfStroke).toInt(),
            ceil(maxY + halfStroke).toInt(),
        )
        if (boundingBox.width() <= 0 || boundingBox.height() <= 0) return null

        val maskBitmap = Bitmap.createBitmap(boundingBox.width(), boundingBox.height(), Bitmap.Config.ALPHA_8)
        val canvas = NativeCanvas(maskBitmap)
        val paint = NativePaint(NativePaint.ANTI_ALIAS_FLAG).apply {
            color = NativeColor.WHITE
            strokeWidth = strokeWidthPx
            strokeCap = NativePaint.Cap.ROUND
            strokeJoin = NativePaint.Join.ROUND
        }

        var path: NativePath? = null
        for (point in _points) {
            val x = point.position.x - boundingBox.left
            val y = point.position.y - boundingBox.top
            if (point.isStrokeStart) {
                path?.let {
                    paint.style = NativePaint.Style.STROKE
                    canvas.drawPath(it, paint)
                }
                path = NativePath().apply { moveTo(x, y) }
                paint.style = NativePaint.Style.FILL
                canvas.drawCircle(x, y, halfStroke, paint)
            } else {
                path?.lineTo(x, y)
            }
        }
        path?.let {
            paint.style = NativePaint.Style.STROKE
            canvas.drawPath(it, paint)
        }

        return MaskRegion(maskBitmap, boundingBox)
    }
}

/**
 * The brush editing surface: displays [bitmap] (keyed on [revision], since it's mutated in
 * place across erase steps - see MagicBrushUiState.Editing), supports single-finger stroke
 * drawing and two-finger pinch/pan, and renders a translucent live preview of [state]'s
 * uncommitted strokes. There's no built-in Compose gesture that composes "1 finger draws, 2+
 * fingers transform", so pointer events are branched manually by active pointer count.
 */
@Composable
fun BrushCanvas(
    bitmap: Bitmap,
    revision: Int,
    brushSizePx: Float,
    state: BrushCanvasState,
    modifier: Modifier = Modifier,
    strokeColor: Color = ElectricViolet.copy(alpha = 0.55f),
) {
    LaunchedEffect(revision) {
        state.clear()
    }

    val imageBitmap = remember(bitmap, revision) { bitmap.asImageBitmap() }
    val bitmapSizePx = remember(bitmap) { IntSize(bitmap.width, bitmap.height) }

    BoxWithConstraints(modifier = modifier) {
        val canvasSizePx = IntSize(constraints.maxWidth, constraints.maxHeight)

        Box(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(bitmap, canvasSizePx) {
                    awaitEachGesture {
                        val down = awaitFirstDown(requireUnconsumed = false)
                        var previousCentroid = down.position
                        var previousDistance = 0f
                        var isTransforming = false
                        var strokeActive = true
                        state.beginStroke(
                            BrushCoordinateMapper.screenToSource(down.position, canvasSizePx, bitmapSizePx, state.transform),
                        )

                        while (true) {
                            val event = awaitPointerEvent()
                            val activeChanges = event.changes.filter { it.pressed }

                            if (activeChanges.size >= 2) {
                                if (strokeActive) {
                                    state.cancelActiveStroke()
                                    strokeActive = false
                                }
                                val centroid = activeChanges
                                    .map { it.position }
                                    .reduce { a, b -> a + b } / activeChanges.size.toFloat()
                                val distance = (activeChanges[0].position - activeChanges[1].position).getDistance()

                                if (!isTransforming) {
                                    isTransforming = true
                                } else {
                                    val scaleDelta = if (previousDistance > 1f) distance / previousDistance else 1f
                                    val newScale = (state.transform.scale * scaleDelta).coerceIn(MIN_SCALE, MAX_SCALE)
                                    val panDelta = centroid - previousCentroid
                                    state.transform = if (newScale <= MIN_SCALE) {
                                        BrushTransform(scale = MIN_SCALE, offsetPx = Offset.Zero)
                                    } else {
                                        state.transform.copy(
                                            scale = newScale,
                                            offsetPx = state.transform.offsetPx + panDelta,
                                        )
                                    }
                                }
                                previousCentroid = centroid
                                previousDistance = distance
                                activeChanges.forEach { it.consume() }
                            } else if (activeChanges.size == 1 && strokeActive) {
                                val change = activeChanges.first()
                                state.continueStroke(
                                    BrushCoordinateMapper.screenToSource(
                                        change.position,
                                        canvasSizePx,
                                        bitmapSizePx,
                                        state.transform,
                                    ),
                                )
                                change.consume()
                            }

                            if (activeChanges.isEmpty()) break
                        }
                        if (strokeActive) state.endStroke()
                    }
                }
                .graphicsLayer(
                    scaleX = state.transform.scale,
                    scaleY = state.transform.scale,
                    translationX = state.transform.offsetPx.x,
                    translationY = state.transform.offsetPx.y,
                ),
        ) {
            Image(
                bitmap = imageBitmap,
                contentDescription = "Photo being edited",
                contentScale = ContentScale.Fit,
                modifier = Modifier.fillMaxSize(),
            )

            Canvas(modifier = Modifier.fillMaxSize()) {
                val strokeWidthScreenPx = brushSizePx * BrushCoordinateMapper.fitScale(canvasSizePx, bitmapSizePx)
                val strokeStyle = Stroke(width = strokeWidthScreenPx, cap = StrokeCap.Round, join = StrokeJoin.Round)
                var path: Path? = null
                for (point in state.points) {
                    val screenPoint = BrushCoordinateMapper.sourceToScreen(point.position, canvasSizePx, bitmapSizePx)
                    if (point.isStrokeStart || path == null) {
                        path?.let { drawPath(it, color = strokeColor, style = strokeStyle) }
                        path = Path().apply { moveTo(screenPoint.x, screenPoint.y) }
                    } else {
                        path.lineTo(screenPoint.x, screenPoint.y)
                    }
                }
                path?.let { drawPath(it, color = strokeColor, style = strokeStyle) }
            }
        }
    }
}
