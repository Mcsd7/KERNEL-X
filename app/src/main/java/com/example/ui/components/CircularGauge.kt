package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.DarkSurfaceBorder
import com.example.ui.theme.ElectricRed
import com.example.ui.theme.ElectricRedGlow
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary

@Composable
fun CircularGauge(
  percentage: Float, // 0.0 to 1.0
  title: String,
  subtitle: String? = null,
  modifier: Modifier = Modifier,
  size: Dp = 110.dp,
  strokeWidth: Dp = 9.dp,
  gaugeColor: Color = ElectricRed
) {
  val clamped = percentage.coerceIn(0f, 1f)
  val sweepAngle = clamped * 260f
  val startAngle = 140f

  Column(
    modifier = modifier,
    horizontalAlignment = Alignment.CenterHorizontally
  ) {
    Box(
      modifier = Modifier.size(size),
      contentAlignment = Alignment.Center
    ) {
      Canvas(modifier = Modifier.size(size)) {
        val stroke = strokeWidth.toPx()
        val diameter = this.size.minDimension - stroke
        val topLeft = Offset(stroke / 2, stroke / 2)
        val arcSize = Size(diameter, diameter)

        // Background track arc
        drawArc(
          color = DarkSurfaceBorder,
          startAngle = startAngle,
          sweepAngle = 260f,
          useCenter = false,
          topLeft = topLeft,
          size = arcSize,
          style = Stroke(width = stroke, cap = StrokeCap.Round)
        )

        // Outer glow
        drawArc(
          color = gaugeColor.copy(alpha = 0.25f),
          startAngle = startAngle,
          sweepAngle = sweepAngle,
          useCenter = false,
          topLeft = topLeft,
          size = arcSize,
          style = Stroke(width = stroke * 1.6f, cap = StrokeCap.Round)
        )

        // Foreground filled progress arc
        drawArc(
          color = gaugeColor,
          startAngle = startAngle,
          sweepAngle = sweepAngle,
          useCenter = false,
          topLeft = topLeft,
          size = arcSize,
          style = Stroke(width = stroke, cap = StrokeCap.Round)
        )
      }

      Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
          text = "${(clamped * 100).toInt()}%",
          fontFamily = FontFamily.Monospace,
          fontWeight = FontWeight.Bold,
          fontSize = 18.sp,
          color = TextPrimary
        )
        if (subtitle != null) {
          Text(
            text = subtitle,
            fontFamily = FontFamily.Monospace,
            fontSize = 9.sp,
            color = TextMuted
          )
        }
      }
    }

    Text(
      text = title.uppercase(),
      fontFamily = FontFamily.Monospace,
      fontWeight = FontWeight.SemiBold,
      fontSize = 11.sp,
      letterSpacing = 0.8.sp,
      color = TextMuted
    )
  }
}
