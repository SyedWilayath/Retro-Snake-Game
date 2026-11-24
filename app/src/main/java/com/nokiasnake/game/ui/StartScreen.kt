package com.nokiasnake.game.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nokiasnake.game.theme.RetroBlack
import com.nokiasnake.game.theme.RetroDarkGreen
import com.nokiasnake.game.theme.RetroGreen
import com.nokiasnake.game.theme.RetroLightGreen
import com.nokiasnake.game.theme.RetroMediumGray

@Composable
fun StartScreen(
    onPlayClick: () -> Unit,
    onHighScoresClick: () -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Title with gradient effect
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "RETRO",
                fontSize = 64.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                color = RetroGreen,
                style = androidx.compose.ui.text.TextStyle(
                    letterSpacing = 6.sp
                )
            )

            Text(
                text = "SNAKE GAME",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                color = RetroLightGreen,
                style = androidx.compose.ui.text.TextStyle(
                    letterSpacing = 4.sp
                )
            )
        }

        Spacer(modifier = Modifier.height(48.dp))

        // Animated snake preview
        SnakeAnimation()

        Spacer(modifier = Modifier.height(48.dp))

        // Buttons with better styling
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Button(
                onClick = onPlayClick,
                colors = ButtonDefaults.buttonColors(
                    containerColor = RetroDarkGreen,
                    contentColor = RetroGreen,
                    disabledContainerColor = RetroMediumGray,
                    disabledContentColor = RetroMediumGray.copy(alpha = 0.5f)
                ),
                modifier = Modifier
                    .fillMaxWidth(0.6f)
                    .padding(vertical = 18.dp),
                shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
                elevation = ButtonDefaults.elevatedButtonElevation(
                    defaultElevation = 8.dp,
                    pressedElevation = 4.dp,
                    focusedElevation = 8.dp,
                    disabledElevation = 0.dp,
                    hoveredElevation = 8.dp
                )
            ) {
                Text(
                    text = "PLAY",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 4.sp
                )
            }

            Button(
                onClick = onHighScoresClick,
                colors = ButtonDefaults.buttonColors(
                    containerColor = RetroMediumGray,
                    contentColor = RetroLightGreen,
                    disabledContainerColor = RetroMediumGray.copy(alpha = 0.5f),
                    disabledContentColor = RetroMediumGray.copy(alpha = 0.3f)
                ),
                modifier = Modifier
                    .fillMaxWidth(0.6f)
                    .padding(vertical = 18.dp),
                shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
                elevation = ButtonDefaults.elevatedButtonElevation(
                    defaultElevation = 8.dp,
                    pressedElevation = 4.dp,
                    focusedElevation = 8.dp,
                    disabledElevation = 0.dp,
                    hoveredElevation = 8.dp
                )
            ) {
                Text(
                    text = "HIGH SCORES",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 2.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Instructions card
        Card(
            colors = CardDefaults.cardColors(
                containerColor = RetroBlack.copy(alpha = 0.85f),
                contentColor = RetroGreen
            ),
            shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            modifier = Modifier
                .fillMaxWidth(0.8f)
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "HOW TO PLAY",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )

                Spacer(modifier = Modifier.height(12.dp))

                Column(
                    horizontalAlignment = Alignment.Start,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    InstructionRow(icon = "👆", text = "Swipe anywhere to control the snake")
                    InstructionRow(icon = "🎮", text = "Or use the D-pad buttons at bottom")
                    InstructionRow(icon = "🍎", text = "Eat apples to grow and score points")
                    InstructionRow(icon = "💀", text = "Avoid walls and your own tail")
                }
            }
        }
    }
}

@Composable
fun InstructionRow(icon: String, text: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = icon,
            fontSize = 20.sp
        )
        Text(
            text = text,
            fontSize = 14.sp,
            fontFamily = FontFamily.Monospace,
            color = RetroLightGreen,
            lineHeight = 22.sp
        )
    }
}

@Composable
fun SnakeAnimation() {
    // Animated snake with gradient segments
    Canvas(modifier = Modifier.size(280.dp, 80.dp)) {
        val segments = 10
        val segmentWidth = 26f
        val segmentHeight = 44f
        val gap = 3f
        val totalWidth = (segments * segmentWidth) + ((segments - 1) * gap)
        val startX = (size.width - totalWidth) / 2
        val centerY = size.height / 2

        // Subtle background glow
        val glowRect = Rect(
            left = startX - 10f,
            top = centerY - segmentHeight / 2 - 10f,
            right = startX + totalWidth + 10f,
            bottom = centerY + segmentHeight / 2 + 10f
        )
        val glowGradient = Brush.linearGradient(
            colors = listOf(
                RetroGreen.copy(alpha = 0.05f),
                RetroGreen.copy(alpha = 0.1f),
                RetroGreen.copy(alpha = 0.05f)
            ),
            start = Offset(glowRect.left, glowRect.top),
            end = Offset(glowRect.right, glowRect.bottom),
            tileMode = TileMode.Clamp
        )
        drawRoundRect(
            brush = glowGradient,
            topLeft = Offset(glowRect.left, glowRect.top),
            size = Size(glowRect.right - glowRect.left, glowRect.bottom - glowRect.top),
            cornerRadius = CornerRadius(20f, 20f),
            style = Fill
        )

        for (i in 0 until segments) {
            val x = startX + i.toFloat() * (segmentWidth + 3f)
            val y = centerY - segmentHeight / 2 + (if (i % 2 == 0) 8f else -8f) * (System.currentTimeMillis() % 2000 / 1000f)

            val color = if (i == 0) RetroGreen else if (i < 3) RetroLightGreen else RetroMediumGray
            val borderColor = if (i == 0) RetroDarkGreen else if (i < 3) RetroGreen else RetroMediumGray

            val segmentRect = Rect(
                left = x,
                top = y,
                right = x + segmentWidth,
                bottom = y + segmentHeight
            )

            val segmentGradient = Brush.linearGradient(
                colors = listOf(
                    color.copy(alpha = 0.9f),
                    color.copy(alpha = 0.7f),
                    borderColor.copy(alpha = 0.8f)
                ),
                start = Offset(segmentRect.left, segmentRect.top),
                end = Offset(segmentRect.right, segmentRect.bottom),
                tileMode = TileMode.Clamp
            )

            drawRoundRect(
                brush = segmentGradient,
                topLeft = Offset(x, y),
                size = Size(segmentWidth, segmentHeight),
                cornerRadius = CornerRadius(10f, 10f),
                style = Fill
            )

            // Highlight
            val highlightRect = Rect(
                left = x + 2f,
                top = y + 2f,
                right = x + segmentWidth - 2f,
                bottom = y + segmentHeight / 2.5f
            )
            val highlightGradient = Brush.linearGradient(
                colors = listOf(Color.White.copy(alpha = 0.3f), Color.White.copy(alpha = 0.0f)),
                start = Offset(highlightRect.left, highlightRect.top),
                end = Offset(highlightRect.left, highlightRect.bottom),
                tileMode = TileMode.Clamp
            )
            drawRoundRect(
                brush = highlightGradient,
                topLeft = Offset(highlightRect.left, highlightRect.top),
                size = Size(highlightRect.right - highlightRect.left, highlightRect.bottom - highlightRect.top),
                cornerRadius = CornerRadius(8f, 8f),
                style = Fill
            )

            // Border
            drawRoundRect(
                color = borderColor.copy(alpha = 0.5f),
                topLeft = Offset(x - 1f, y - 1f),
                size = Size(segmentWidth + 2f, segmentHeight + 2f),
                cornerRadius = CornerRadius(10f, 10f),
                style = Stroke(1f)
            )

            // Eyes on head
            if (i == 0) {
                val eyeSize = 7f
                // Left eye
                drawCircle(
                    color = Color.White,
                    center = Offset(x + 8f, y + 12f),
                    radius = eyeSize
                )
                drawCircle(
                    color = RetroBlack,
                    center = Offset(x + 8f, y + 12f),
                    radius = eyeSize / 2f
                )
                drawCircle(
                    color = Color.White,
                    center = Offset(x + 8f - 1.5f, y + 12f - 1.5f),
                    radius = eyeSize / 4f
                )

                // Right eye
                drawCircle(
                    color = Color.White,
                    center = Offset(x + 18f, y + 12f),
                    radius = eyeSize
                )
                drawCircle(
                    color = RetroBlack,
                    center = Offset(x + 18f, y + 12f),
                    radius = eyeSize / 2f
                )
                drawCircle(
                    color = Color.White,
                    center = Offset(x + 18f - 1.5f, y + 12f - 1.5f),
                    radius = eyeSize / 4f
                )
            }
        }
    }
}
