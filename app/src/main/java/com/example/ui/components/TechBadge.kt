package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.DarkSurfaceBorder
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.ElectricRed
import com.example.ui.theme.TerminalCyan
import com.example.ui.theme.TerminalError
import com.example.ui.theme.TerminalGreen
import com.example.ui.theme.TextSecondary

@Composable
fun TechBadge(
  text: String,
  modifier: Modifier = Modifier,
  color: Color = ElectricRed,
  backgroundColor: Color = color.copy(alpha = 0.12f),
  borderColor: Color = color.copy(alpha = 0.35f)
) {
  Box(
    modifier = modifier
      .background(backgroundColor, RoundedCornerShape(4.dp))
      .border(1.dp, borderColor, RoundedCornerShape(4.dp))
      .padding(horizontal = 8.dp, vertical = 3.dp)
  ) {
    Text(
      text = text.uppercase(),
      fontFamily = FontFamily.Monospace,
      fontWeight = FontWeight.Bold,
      fontSize = 10.sp,
      letterSpacing = 0.5.sp,
      color = color
    )
  }
}
