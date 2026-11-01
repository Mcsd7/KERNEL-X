package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ui.theme.DarkSurfaceBorder
import com.example.ui.theme.ElectricRed
import com.example.ui.theme.ElectricRedGlow

@Composable
fun WaveformChart(
  history: List<Float>,
  modifier: Modifier = Modifier,
  height: Dp = 60.dp,
  lineColor: Color = ElectricRed
) {
  Canvas(
    modifier = modifier
      .fillMaxWidth()
      .height(height)
  ) {
    val w = size.width
    val h = size.height

    if (history.isEmpty()) {
      drawLine(
        color = DarkSurfaceBorder,
        start = Offset(0f, h / 2),
        end = Offset(w, h / 2),
        strokeWidth = 2f
      )
      return@Canvas
    }

    // Grid baseline
    drawLine(
      color = DarkSurfaceBorder.copy(alpha = 0.5f),
      start = Offset(0f, h * 0.25f),
      end = Offset(w, h * 0.25f),
      strokeWidth = 1f
    )
    drawLine(
      color = DarkSurfaceBorder.copy(alpha = 0.5f),
      start = Offset(0f, h * 0.75f),
      end = Offset(w, h * 0.75f),
      strokeWidth = 1f
    )

    val stepX = w / (history.size.coerceAtLeast(2) - 1).toFloat()
    val points = history.mapIndexed { idx, frac ->
      val x = idx * stepX
      val y = (1f - frac.coerceIn(0f, 1f)) * (h - 8f) + 4f
      Offset(x, y)
    }

    // Draw area gradient / fill
    val fillPath = Path().apply {
      moveTo(points.first().x, h)
      points.forEach { lineTo(it.x, it.y) }
      lineTo(points.last().x, h)
      close()
    }

    drawPath(
      path = fillPath,
      color = lineColor.copy(alpha = 0.15f)
    )

    // Draw main stroke
    val strokePath = Path().apply {
      moveTo(points.first().x, points.first().y)
      for (i in 1 until points.size) {
        lineTo(points[i].x, points[i].y)
      }
    }

    drawPath(
      path = strokePath,
      color = lineColor,
      style = Stroke(width = 3.dp.toPx())
    )

    // Pulse dot on current point
    val lastPoint = points.last()
    drawCircle(
      color = lineColor.copy(alpha = 0.4f),
      radius = 6.dp.toPx(),
      center = lastPoint
    )
    drawCircle(
      color = Color.White,
      radius = 2.5.dp.toPx(),
      center = lastPoint
    )
  }
}
