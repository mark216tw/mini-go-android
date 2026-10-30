package com.vibecode.minigo.ui.game

import android.graphics.Paint
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vibecode.minigo.R
import com.vibecode.minigo.engine.Board
import com.vibecode.minigo.engine.Point
import com.vibecode.minigo.engine.Stone
import com.vibecode.minigo.ui.theme.BoardLine
import com.vibecode.minigo.ui.theme.BoardWood
import com.vibecode.minigo.ui.theme.BoardWoodDark
import com.vibecode.minigo.ui.theme.DeadMarker
import com.vibecode.minigo.ui.theme.HintMarker
import com.vibecode.minigo.ui.theme.LastMoveMarker
import com.vibecode.minigo.ui.theme.MiniGoTheme
import com.vibecode.minigo.ui.theme.StoneBlack
import com.vibecode.minigo.ui.theme.StoneBlackHighlight
import com.vibecode.minigo.ui.theme.StoneWhite
import com.vibecode.minigo.ui.theme.StoneWhiteBorder
import com.vibecode.minigo.ui.theme.StoneWhiteShade
import kotlin.math.abs
import kotlin.math.roundToInt

private const val COORDINATE_LETTERS = "ABCDEFGHJKLMNOPQRST"

@Composable
fun GoBoard(
    board: Board,
    lastMove: Point?,
    hint: Point?,
    deadStones: Set<Point>,
    showCoordinates: Boolean,
    enabled: Boolean,
    onTap: (Point) -> Unit,
    modifier: Modifier = Modifier,
    marks: Set<Point> = emptySet(),
) {
    val density = LocalDensity.current
    val padding = with(density) { 34.dp.toPx() }
    val coordinatePaint = remember(density) {
        Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = BoardLine.toArgb()
            textAlign = Paint.Align.CENTER
            textSize = with(density) { 9.sp.toPx() }
        }
    }
    val description = stringResource(R.string.board_desc, board.size)

    Canvas(
        modifier = modifier
            .aspectRatio(1f)
            .semantics { contentDescription = description }
            .pointerInput(board.size, enabled) {
                if (!enabled) return@pointerInput
                detectTapGestures { offset ->
                    val n = board.size
                    val step = (size.width - padding * 2f) / (n - 1)
                    if (step <= 0f) return@detectTapGestures
                    val dx = (offset.x - padding) / step
                    val dy = (offset.y - padding) / step
                    val x = dx.roundToInt()
                    val y = dy.roundToInt()
                    if (x !in 0 until n || y !in 0 until n) return@detectTapGestures
                    if (abs(dx - x) > 0.45f || abs(dy - y) > 0.45f) return@detectTapGestures
                    onTap(Point(x, y))
                }
            },
    ) {
        val n = board.size
        val width = size.width
        val step = (width - padding * 2f) / (n - 1)
        val radius = step * 0.46f

        drawRoundRect(
            brush = Brush.linearGradient(listOf(BoardWood, BoardWoodDark)),
            cornerRadius = CornerRadius(12.dp.toPx()),
        )

        for (i in 0 until n) {
            val position = padding + i * step
            drawLine(
                color = BoardLine,
                start = Offset(padding, position),
                end = Offset(padding + (n - 1) * step, position),
                strokeWidth = 1.dp.toPx(),
            )
            drawLine(
                color = BoardLine,
                start = Offset(position, padding),
                end = Offset(position, padding + (n - 1) * step),
                strokeWidth = 1.dp.toPx(),
            )
        }

        drawRoundRect(
            color = BoardLine,
            topLeft = Offset(padding, padding),
            size = androidx.compose.ui.geometry.Size((n - 1) * step, (n - 1) * step),
            cornerRadius = CornerRadius(0f),
            style = Stroke(width = 1.6.dp.toPx()),
        )

        for (point in starPoints(n)) {
            drawCircle(
                color = BoardLine,
                radius = 2.6.dp.toPx(),
                center = Offset(padding + point.x * step, padding + point.y * step),
            )
        }

        for (y in 0 until n) {
            for (x in 0 until n) {
                val stone = board.get(x, y)
                if (stone == Stone.EMPTY) continue
                val center = Offset(padding + x * step, padding + y * step)
                drawStone(stone, center, radius, step)
                if (Point(x, y) in deadStones) {
                    drawCircle(color = Color.Black.copy(alpha = 0.45f), radius = radius, center = center)
                    val cross = radius * 0.55f
                    drawLine(
                        color = DeadMarker,
                        start = Offset(center.x - cross, center.y - cross),
                        end = Offset(center.x + cross, center.y + cross),
                        strokeWidth = 2.4.dp.toPx(),
                    )
                    drawLine(
                        color = DeadMarker,
                        start = Offset(center.x + cross, center.y - cross),
                        end = Offset(center.x - cross, center.y + cross),
                        strokeWidth = 2.4.dp.toPx(),
                    )
                }
            }
        }

        lastMove?.let {
            if (it.x in 0 until n && it.y in 0 until n) {
                val center = Offset(padding + it.x * step, padding + it.y * step)
                drawCircle(
                    color = LastMoveMarker,
                    radius = radius * 0.34f,
                    center = center,
                    style = Stroke(width = 1.8.dp.toPx()),
                )
            }
        }

        for (point in marks) {
            if (point.x !in 0 until n || point.y !in 0 until n || !board.isEmpty(point)) continue
            val center = Offset(padding + point.x * step, padding + point.y * step)
            drawCircle(
                color = HintMarker,
                radius = radius * 0.8f,
                center = center,
                style = Stroke(width = 2.dp.toPx()),
            )
            drawCircle(color = HintMarker, radius = radius * 0.18f, center = center)
        }

        hint?.let {
            if (it.x in 0 until n && it.y in 0 until n && board.isEmpty(it)) {
                val center = Offset(padding + it.x * step, padding + it.y * step)
                drawCircle(
                    color = HintMarker,
                    radius = radius,
                    center = center,
                    style = Stroke(width = 2.dp.toPx()),
                )
                drawCircle(color = HintMarker, radius = radius * 0.22f, center = center)
            }
        }

        if (showCoordinates) {
            val nativeCanvas = drawContext.canvas.nativeCanvas
            val baselineOffset = coordinatePaint.textSize / 3f
            for (i in 0 until n) {
                val position = padding + i * step
                nativeCanvas.drawText(
                    COORDINATE_LETTERS[i].toString(),
                    position,
                    padding * 0.42f,
                    coordinatePaint,
                )
                nativeCanvas.drawText(
                    (n - i).toString(),
                    padding * 0.38f,
                    position + baselineOffset,
                    coordinatePaint,
                )
            }
        }
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawStone(
    stone: Stone,
    center: Offset,
    radius: Float,
    step: Float,
) {
    drawCircle(
        color = Color.Black.copy(alpha = 0.22f),
        radius = radius,
        center = Offset(center.x, center.y + step * 0.05f),
    )
    when (stone) {
        Stone.BLACK -> drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(StoneBlackHighlight, StoneBlack),
                center = Offset(center.x - radius * 0.35f, center.y - radius * 0.35f),
                radius = radius * 1.4f,
            ),
            radius = radius,
            center = center,
        )

        Stone.WHITE -> {
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(StoneWhite, StoneWhiteShade),
                    center = Offset(center.x - radius * 0.35f, center.y - radius * 0.35f),
                    radius = radius * 1.4f,
                ),
                radius = radius,
                center = center,
            )
            drawCircle(
                color = StoneWhiteBorder,
                radius = radius,
                center = center,
                style = Stroke(width = 1.dp.toPx()),
            )
        }

        Stone.EMPTY -> Unit
    }
}

private fun starPoints(size: Int): List<Point> = when (size) {
    9 -> listOf(Point(2, 2), Point(6, 2), Point(4, 4), Point(2, 6), Point(6, 6))
    13 -> listOf(Point(3, 3), Point(9, 3), Point(6, 6), Point(3, 9), Point(9, 9))
    19 -> listOf(
        Point(3, 3), Point(9, 3), Point(15, 3),
        Point(3, 9), Point(9, 9), Point(15, 9),
        Point(3, 15), Point(9, 15), Point(15, 15),
    )

    else -> emptyList()
}

@Preview(showBackground = true, widthDp = 360)
@Composable
private fun GoBoardPreview() {
    MiniGoTheme(dynamicColor = false) {
        GoBoard(
            board = Board.fromAscii(
                listOf(
                    ".........",
                    "..X...O..",
                    "...X.O...",
                    "....X....",
                    "...OXO...",
                    "..O.X.O..",
                    ".........",
                    ".........",
                    ".........",
                ),
            ),
            lastMove = Point(4, 4),
            hint = Point(6, 2),
            deadStones = emptySet(),
            showCoordinates = true,
            enabled = true,
            onTap = {},
            modifier = Modifier.fillMaxWidth().padding(16.dp),
        )
    }
}
