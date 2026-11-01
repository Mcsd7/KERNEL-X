package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.ElectricRed
import com.example.ui.theme.ElectricRedGlow
import com.example.ui.theme.ElectricRedHover
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary

@Composable
fun KernelLogo(
  modifier: Modifier = Modifier,
  size: Dp = 48.dp,
  showSubtext: Boolean = true
) {
  val infiniteTransition = rememberInfiniteTransition(label = "pulse")
  val pulseAlpha by infiniteTransition.animateFloat(
    initialValue = 0.4f,
    targetValue = 0.95f,
    animationSpec = infiniteRepeatable(
      animation = tween(1500, easing = FastOutSlowInEasing),
      repeatMode = RepeatMode.Reverse
    ),
    label = "alpha"
  )

  Row(
    modifier = modifier,
    verticalAlignment = Alignment.CenterVertically
  ) {
    Box(
      modifier = Modifier.size(size),
      contentAlignment = Alignment.Center
    ) {
      Canvas(modifier = Modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height

        // Outer chip boundary
        drawRoundRect(
          color = Color(0xFF1E1E26),
          size = Size(w, h),
          cornerRadius = CornerRadius(w * 0.18f, h * 0.18f)
        )

        // Subtle electric border
        drawRoundRect(
          color = ElectricRed.copy(alpha = pulseAlpha * 0.7f),
          size = Size(w, h),
          cornerRadius = CornerRadius(w * 0.18f, h * 0.18f),
          style = Stroke(width = w * 0.04f)
        )

        // Circuit connection pins on edges
        val pinLength = w * 0.08f
        val pinStroke = w * 0.035f
        for (i in 1..3) {
          val frac = i * 0.25f
          // Top pins
          drawLine(
            color = ElectricRed.copy(alpha = 0.5f),
            start = Offset(w * frac, 0f),
            end = Offset(w * frac, pinLength),
            strokeWidth = pinStroke
          )
          // Bottom pins
          drawLine(
            color = ElectricRed.copy(alpha = 0.5f),
            start = Offset(w * frac, h),
            end = Offset(w * frac, h - pinLength),
            strokeWidth = pinStroke
          )
          // Left pins
          drawLine(
            color = ElectricRed.copy(alpha = 0.5f),
            start = Offset(0f, h * frac),
            end = Offset(pinLength, h * frac),
            strokeWidth = pinStroke
          )
          // Right pins
          drawLine(
            color = ElectricRed.copy(alpha = 0.5f),
            start = Offset(w, h * frac),
            end = Offset(w - pinLength, h * frac),
            strokeWidth = pinStroke
          )
        }

        // Inner processor die
        val dieMargin = w * 0.22f
        drawRoundRect(
          color = Color(0xFF0F0F14),
          topLeft = Offset(dieMargin, dieMargin),
          size = Size(w - dieMargin * 2, h - dieMargin * 2),
          cornerRadius = CornerRadius(w * 0.08f, h * 0.08f)
        )

        // Electric 'X' circuit paths
        val p1 = Offset(w * 0.32f, h * 0.32f)
        val p2 = Offset(w * 0.68f, h * 0.68f)
        val p3 = Offset(w * 0.68f, h * 0.32f)
        val p4 = Offset(w * 0.32f, h * 0.68f)

        // Glow stroke behind X
        val glowStroke = w * 0.11f
        drawLine(
          color = ElectricRedGlow.copy(alpha = pulseAlpha),
          start = p1,
          end = p2,
          strokeWidth = glowStroke,
          cap = StrokeCap.Round
        )
        drawLine(
          color = ElectricRedGlow.copy(alpha = pulseAlpha),
          start = p3,
          end = p4,
          strokeWidth = glowStroke,
          cap = StrokeCap.Round
        )

        // Crisp X stroke
        val xStroke = w * 0.065f
        drawLine(
          color = ElectricRedHover,
          start = p1,
          end = p2,
          strokeWidth = xStroke,
          cap = StrokeCap.Round
        )
        drawLine(
          color = ElectricRedHover,
          start = p3,
          end = p4,
          strokeWidth = xStroke,
          cap = StrokeCap.Round
        )

        // Central processor node dot
        drawCircle(
          color = Color.White,
          radius = w * 0.045f,
          center = Offset(w * 0.5f, h * 0.5f)
        )
      }
    }

    if (showSubtext) {
      Spacer(modifier = Modifier.width(12.dp))
      Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Text(
            text = "KERNEL",
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Black,
            fontSize = 20.sp,
            letterSpacing = 2.sp,
            color = TextPrimary
          )
          Spacer(modifier = Modifier.width(6.dp))
          Text(
            text = "X",
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Black,
            fontSize = 22.sp,
            letterSpacing = 1.sp,
            color = ElectricRed
          )
        }
        Text(
          text = "ANDROID SYSTEM CONTROL",
          fontFamily = FontFamily.Monospace,
          fontWeight = FontWeight.Bold,
          fontSize = 9.sp,
          letterSpacing = 1.5.sp,
          color = TextMuted
        )
      }
    }
  }
}
