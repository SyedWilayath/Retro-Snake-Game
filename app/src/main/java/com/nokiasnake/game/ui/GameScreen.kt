package com.nokiasnake.game.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nokiasnake.game.Direction
import com.nokiasnake.game.FoodType
import com.nokiasnake.game.Point
import com.nokiasnake.game.SnakeViewModel
import com.nokiasnake.game.theme.RetroBlack
import com.nokiasnake.game.theme.RetroDarkGreen
import com.nokiasnake.game.theme.RetroGreen
import com.nokiasnake.game.theme.RetroLightGreen
import com.nokiasnake.game.theme.RetroMediumGray
import kotlin.math.abs
import kotlin.math.hypot
import kotlin.math.min

@Composable
fun GameScreen(
    viewModel: SnakeViewModel = androidx.lifecycle.viewmodel.compose.viewModel(),
    onGameOver: (Int) -> Unit
) {
    val snakeBody by viewModel.snakeBody.collectAsStateWithLifecycle()
    val foodPosition by viewModel.foodPosition.collectAsStateWithLifecycle()
    val foodType by viewModel.foodType.collectAsStateWithLifecycle()
    val score by viewModel.score.collectAsStateWithLifecycle()
    val isGameOver by viewModel.isGameOver.collectAsStateWithLifecycle()
    val isPaused by viewModel.isPaused.collectAsStateWithLifecycle()
    val showControls by viewModel.showControls.collectAsStateWithLifecycle()
    val currentDirection by viewModel.currentDirection.collectAsStateWithLifecycle()
    val gridWidth = viewModel.getGridWidth()
    val gridHeight = viewModel.getGridHeight()

    LaunchedEffect(isGameOver) {
        if (isGameOver) {
            onGameOver(score)
        }
    }

    var dragStartX by remember { mutableFloatStateOf(0f) }
    var dragStartY by remember { mutableFloatStateOf(0f) }
    var lastDragX by remember { mutableFloatStateOf(0f) }
    var lastDragY by remember { mutableFloatStateOf(0f) }
    val swipeThreshold = 30f

    Box(
        modifier = Modifier
            .fillMaxSize()
            .onGloballyPositioned { coordinates ->
                viewModel.updateScreenSize(coordinates.size.width.toFloat(), coordinates.size.height.toFloat())
            }
            .pointerInput(Unit) {
                val screenWidth = size.width.toFloat()
                val screenHeight = size.height.toFloat()
                val centerX = screenWidth / 2f
                val centerY = screenHeight / 2f

                detectDragGestures(
                    onDragStart = { offset ->
                        dragStartX = offset.x
                        dragStartY = offset.y
                        lastDragX = offset.x
                        lastDragY = offset.y
                    },
                    onDrag = { change, _ ->
                        lastDragX = change.position.x
                        lastDragY = change.position.y
                    },
                    onDragEnd = {
                        val deltaX = lastDragX - dragStartX
                        val deltaY = lastDragY - dragStartY
                        val distance = hypot(deltaX.toDouble(), deltaY.toDouble())

                        if (distance < swipeThreshold) {
                            val dx = dragStartX - centerX
                            val dy = dragStartY - centerY
                            if (abs(dx) > abs(dy)) {
                                if (dx > 0) viewModel.changeDirection(Direction.RIGHT)
                                else viewModel.changeDirection(Direction.LEFT)
                            } else {
                                if (dy > 0) viewModel.changeDirection(Direction.DOWN)
                                else viewModel.changeDirection(Direction.UP)
                            }
                        } else {
                            if (abs(deltaX) > abs(deltaY)) {
                                if (deltaX > swipeThreshold) viewModel.changeDirection(Direction.RIGHT)
                                else if (deltaX < -swipeThreshold) viewModel.changeDirection(Direction.LEFT)
                            } else {
                                if (deltaY > swipeThreshold) viewModel.changeDirection(Direction.DOWN)
                                else if (deltaY < -swipeThreshold) viewModel.changeDirection(Direction.UP)
                            }
                        }
                    }
                )
            }
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(RetroBlack)
                .border(3.dp, RetroDarkGreen)
                .clipToBounds()
        ) {
            GameCanvas(
                snakeBody = snakeBody,
                foodPosition = foodPosition,
                foodType = foodType,
                gridWidth = gridWidth,
                gridHeight = gridHeight,
                currentDirection = currentDirection,
                modifier = Modifier.fillMaxSize()
            )

            ScoreOverlay(
                score = score,
                isPaused = isPaused,
                onPauseClick = { viewModel.togglePause() }
            )

            if (isPaused) {
                PauseOverlay(
                    showControls = showControls,
                    onResume = { viewModel.resumeGame() },
                    onRestart = { viewModel.startNewGame() },
                    onToggleControls = { viewModel.toggleControls() },
                    onQuit = { onGameOver(score) }
                )
            }

            if (isGameOver) {
                GameOverOverlay(
                    score = score,
                    onPlayAgain = { viewModel.startNewGame() },
                    onQuit = { onGameOver(score) }
                )
            }
        }

        if (showControls) {
            ModernDpadControls(
                currentDirection = currentDirection,
                onDirectionChange = { direction ->
                    viewModel.changeDirection(direction)
                },
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            )
        }
    }
}

@Composable
fun GameCanvas(
    snakeBody: List<Point>,
    foodPosition: Point?,
    foodType: FoodType,
    gridWidth: Int,
    gridHeight: Int,
    currentDirection: Direction,
    modifier: Modifier = Modifier
) {
    var prevSnakeBody by remember { mutableStateOf(snakeBody) }
    val progress = remember { androidx.compose.animation.core.Animatable(1f) }

    LaunchedEffect(snakeBody) {
        if (snakeBody != prevSnakeBody) {
            prevSnakeBody = snakeBody
            progress.snapTo(0f)
            progress.animateTo(
                targetValue = 1f,
                animationSpec = androidx.compose.animation.core.tween(
                    durationMillis = 140,
                    easing = androidx.compose.animation.core.LinearEasing
                )
            )
        }
    }

    Canvas(modifier = modifier) {
        val cellWidth = size.width / gridWidth
        val cellHeight = size.height / gridHeight

        // Draw subtle grid pattern across full screen
        for (i in 0..gridWidth) {
            val posX = i.toFloat() * cellWidth
            drawLine(
                color = RetroMediumGray.copy(alpha = 0.12f),
                start = Offset(posX, 0f),
                end = Offset(posX, size.height),
                strokeWidth = 0.5f
            )
        }
        for (j in 0..gridHeight) {
            val posY = j.toFloat() * cellHeight
            drawLine(
                color = RetroMediumGray.copy(alpha = 0.12f),
                start = Offset(0f, posY),
                end = Offset(size.width, posY),
                strokeWidth = 0.5f
            )
        }

        // Draw smooth interpolated snake body path
        if (snakeBody.isNotEmpty()) {
            val currentProgress = progress.value
            val snakePath = Path().apply {
                snakeBody.forEachIndexed { index, point ->
                    val prevPoint = if (index < prevSnakeBody.size) prevSnakeBody[index] else point
                    val dx = point.x - prevPoint.x
                    val dy = point.y - prevPoint.y

                    val interpX = if (abs(dx) > 1) point.x.toFloat() else prevPoint.x.toFloat() + dx.toFloat() * currentProgress
                    val interpY = if (abs(dy) > 1) point.y.toFloat() else prevPoint.y.toFloat() + dy.toFloat() * currentProgress

                    val x = interpX * cellWidth + cellWidth / 2f
                    val y = interpY * cellHeight + cellHeight / 2f

                    if (index == 0) {
                        moveTo(x, y)
                    } else {
                        val prevIndexPoint = snakeBody[index - 1]
                        val wrapDx = abs(point.x - prevIndexPoint.x)
                        val wrapDy = abs(point.y - prevIndexPoint.y)
                        if (wrapDx > 1 || wrapDy > 1) {
                            moveTo(x, y)
                        } else {
                            lineTo(x, y)
                        }
                    }
                }
            }

            val strokeWidth = min(cellWidth, cellHeight) * 0.85f

            // Outer glow
            drawPath(
                path = snakePath,
                color = RetroGreen.copy(alpha = 0.4f),
                style = Stroke(width = strokeWidth + 4f, cap = StrokeCap.Round, join = StrokeJoin.Round)
            )

            // Main gradient snake body
            val snakeGradient = Brush.linearGradient(
                colors = listOf(RetroLightGreen, RetroGreen, RetroDarkGreen),
                start = Offset(0f, 0f),
                end = Offset(size.width, size.height),
                tileMode = TileMode.Clamp
            )
            drawPath(
                path = snakePath,
                brush = snakeGradient,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round, join = StrokeJoin.Round)
            )

            // Draw head details (eyes)
            val head = snakeBody.first()
            val prevHead = if (snakeBody.isNotEmpty() && prevSnakeBody.isNotEmpty()) prevSnakeBody.first() else head
            val hDx = head.x - prevHead.x
            val hDy = head.y - prevHead.y
            val headInterpX = if (abs(hDx) > 1) head.x.toFloat() else prevHead.x.toFloat() + hDx.toFloat() * currentProgress
            val headInterpY = if (abs(hDy) > 1) head.y.toFloat() else prevHead.y.toFloat() + hDy.toFloat() * currentProgress
            val headX = headInterpX * cellWidth + cellWidth / 2f
            val headY = headInterpY * cellHeight + cellHeight / 2f
            val eyeSize = strokeWidth / 5f

            val (eyeOffsetX1, eyeOffsetX2, eyeOffsetY1, eyeOffsetY2) = when (currentDirection) {
                Direction.UP -> arrayOf(-strokeWidth/4f, strokeWidth/4f, -strokeWidth/4f, -strokeWidth/4f)
                Direction.DOWN -> arrayOf(-strokeWidth/4f, strokeWidth/4f, strokeWidth/4f, strokeWidth/4f)
                Direction.LEFT -> arrayOf(-strokeWidth/4f, -strokeWidth/4f, -strokeWidth/4f, strokeWidth/4f)
                Direction.RIGHT -> arrayOf(strokeWidth/4f, strokeWidth/4f, -strokeWidth/4f, strokeWidth/4f)
            }

            drawCircle(color = Color.White, center = Offset(headX + eyeOffsetX1, headY + eyeOffsetY1), radius = eyeSize)
            drawCircle(color = Color.White, center = Offset(headX + eyeOffsetX2, headY + eyeOffsetY2), radius = eyeSize)
            drawCircle(color = RetroBlack, center = Offset(headX + eyeOffsetX1, headY + eyeOffsetY1), radius = eyeSize / 2f)
            drawCircle(color = RetroBlack, center = Offset(headX + eyeOffsetX2, headY + eyeOffsetY2), radius = eyeSize / 2f)
        }

        // Draw food
        foodPosition?.let { food ->
            val x = food.x.toFloat() * cellWidth
            val y = food.y.toFloat() * cellHeight
            val centerX = x + cellWidth / 2f
            val centerY = y + cellHeight / 2f
            val radius = min(cellWidth, cellHeight) * 0.4f

            val colors = when (foodType) {
                FoodType.APPLE -> listOf(Color(0xFFFF3333), Color(0xFFCC0000))
                FoodType.GOLDEN_APPLE -> listOf(Color(0xFFFFD700), Color(0xFFFFA500))
                FoodType.SPECIAL_STAR -> listOf(Color(0xFF00FFFF), Color(0xFF0088FF))
            }

            val foodGradient = Brush.radialGradient(
                colors = colors,
                center = Offset(centerX, centerY),
                radius = radius
            )

            drawCircle(
                brush = foodGradient,
                center = Offset(centerX, centerY),
                radius = radius
            )

            drawCircle(
                color = colors[0].copy(alpha = 0.5f),
                center = Offset(centerX, centerY),
                radius = radius + 3f,
                style = Stroke(2f)
            )

            if (foodType == FoodType.APPLE) {
                val stemPath = Path().apply {
                    moveTo(centerX, centerY - radius)
                    lineTo(centerX + radius * 0.1f, centerY - radius * 1.3f)
                }
                drawPath(path = stemPath, color = Color(0xFF4A2C0A), style = Stroke(3f, cap = StrokeCap.Round))
            }
        }
    }
}

@Composable
fun ScoreOverlay(
    score: Int,
    isPaused: Boolean,
    onPauseClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Card(
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            colors = CardDefaults.cardColors(
                containerColor = RetroBlack.copy(alpha = 0.85f),
                contentColor = RetroGreen
            ),
            shape = RoundedCornerShape(16.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, RetroDarkGreen)
        ) {
            Text(
                text = "SCORE: $score",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
        }

        Button(
            onClick = onPauseClick,
            colors = ButtonDefaults.buttonColors(
                containerColor = RetroDarkGreen,
                contentColor = RetroGreen
            ),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.size(48.dp),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp)
        ) {
            Text(
                text = if (isPaused) "▶" else "❚❚",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
        }
    }
}

@Composable
fun PauseOverlay(
    showControls: Boolean,
    onResume: () -> Unit,
    onRestart: () -> Unit,
    onToggleControls: () -> Unit,
    onQuit: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.85f)),
        contentAlignment = Alignment.Center
    ) {
        Card(
            elevation = CardDefaults.cardElevation(defaultElevation = 16.dp),
            colors = CardDefaults.cardColors(
                containerColor = RetroBlack,
                contentColor = RetroGreen
            ),
            shape = RoundedCornerShape(24.dp),
            border = androidx.compose.foundation.BorderStroke(2.dp, RetroGreen)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.padding(32.dp)
            ) {
                Text(
                    text = "PAUSED",
                    fontSize = 40.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    color = RetroGreen
                )

                Spacer(modifier = Modifier.height(32.dp))

                Button(
                    onClick = onResume,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = RetroGreen,
                        contentColor = RetroBlack
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "RESUME",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = onRestart,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = RetroDarkGreen,
                        contentColor = RetroGreen
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "RESTART",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = onToggleControls,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = RetroDarkGreen,
                        contentColor = RetroGreen
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = if (showControls) "CONTROLS: ON" else "CONTROLS: OFF",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = onQuit,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = RetroMediumGray,
                        contentColor = RetroLightGreen
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "QUIT",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }
    }
}

@Composable
fun GameOverOverlay(
    score: Int,
    onPlayAgain: () -> Unit,
    onQuit: () -> Unit = {}
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.9f)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "GAME OVER",
                fontSize = 56.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                color = Color.Red
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Final Score",
                fontSize = 18.sp,
                fontWeight = FontWeight.Normal,
                fontFamily = FontFamily.Monospace,
                color = RetroMediumGray
            )

            Text(
                text = "$score",
                fontSize = 56.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                color = RetroGreen
            )

            Spacer(modifier = Modifier.height(32.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Button(
                    onClick = onPlayAgain,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = RetroDarkGreen,
                        contentColor = RetroGreen
                    ),
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "PLAY AGAIN",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }

                Button(
                    onClick = onQuit,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = RetroMediumGray,
                        contentColor = RetroLightGreen
                    ),
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "QUIT",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }
    }
}

@Composable
fun ModernDpadControls(
    currentDirection: Direction,
    onDirectionChange: (Direction) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.BottomCenter
    ) {
        Box(
            modifier = Modifier
                .padding(bottom = 24.dp)
                .size(190.dp)
                .background(RetroBlack.copy(alpha = 0.6f), RoundedCornerShape(95.dp))
                .border(2.dp, RetroGreen.copy(alpha = 0.5f), RoundedCornerShape(95.dp)),
            contentAlignment = Alignment.Center
        ) {
            // Up
            DpadDirectionButton(
                text = "▲",
                isActive = currentDirection == Direction.UP,
                onClick = { onDirectionChange(Direction.UP) },
                modifier = Modifier.align(Alignment.TopCenter).padding(top = 10.dp)
            )
            // Down
            DpadDirectionButton(
                text = "▼",
                isActive = currentDirection == Direction.DOWN,
                onClick = { onDirectionChange(Direction.DOWN) },
                modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 10.dp)
            )
            // Left
            DpadDirectionButton(
                text = "◄",
                isActive = currentDirection == Direction.LEFT,
                onClick = { onDirectionChange(Direction.LEFT) },
                modifier = Modifier.align(Alignment.CenterStart).padding(start = 10.dp)
            )
            // Right
            DpadDirectionButton(
                text = "►",
                isActive = currentDirection == Direction.RIGHT,
                onClick = { onDirectionChange(Direction.RIGHT) },
                modifier = Modifier.align(Alignment.CenterEnd).padding(end = 10.dp)
            )
            // Center core
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(RetroDarkGreen, RoundedCornerShape(24.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "◆", color = RetroGreen, fontSize = 16.sp)
            }
        }
    }
}

@Composable
fun DpadDirectionButton(
    text: String,
    isActive: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val bg = if (isActive) RetroLightGreen else RetroDarkGreen
    val fg = if (isActive) RetroBlack else RetroGreen

    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = bg,
            contentColor = fg
        ),
        modifier = modifier.size(48.dp),
        shape = RoundedCornerShape(12.dp),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp)
    ) {
        Text(
            text = text,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace
        )
    }
}
