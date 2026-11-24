package com.nokiasnake.game

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Random

data class Point(val x: Int, val y: Int) {
    operator fun plus(other: Point) = Point(x + other.x, y + other.y)
}

enum class Direction(val vector: Point) {
    UP(Point(0, -1)),
    DOWN(Point(0, 1)),
    LEFT(Point(-1, 0)),
    RIGHT(Point(1, 0));

    fun isOpposite(other: Direction): Boolean = when (this) {
        UP -> other == DOWN
        DOWN -> other == UP
        LEFT -> other == RIGHT
        RIGHT -> other == LEFT
    }
}

enum class FoodType(val points: Int, val growth: Int) {
    APPLE(10, 1),
    GOLDEN_APPLE(30, 2),
    SPECIAL_STAR(50, 3)
}

sealed interface GameEvent {
    data class Move(val direction: Direction) : GameEvent
    data class EatFood(val points: Int) : GameEvent
    object GameOver : GameEvent
    object PlaySoundMove : GameEvent
    object PlaySoundEat : GameEvent
    object PlaySoundGameOver : GameEvent
}

class SnakeViewModel : ViewModel() {
    companion object {
        private const val INITIAL_SNAKE_LENGTH = 3
        private const val GAME_SPEED_MS = 150L
    }
    private val random = Random()

    private var gridWidth = 18
    private var gridHeight = 32

    private val _snakeBody = MutableStateFlow<List<Point>>(emptyList())
    val snakeBody = _snakeBody.asStateFlow()

    private val _foodPosition = MutableStateFlow<Point?>(null)
    val foodPosition = _foodPosition.asStateFlow()

    private val _foodType = MutableStateFlow<FoodType>(FoodType.APPLE)
    val foodType = _foodType.asStateFlow()

    private val _currentDirection = MutableStateFlow<Direction>(Direction.RIGHT)
    val currentDirection = _currentDirection.asStateFlow()

    private val _nextDirection = MutableStateFlow<Direction>(Direction.RIGHT)
    val nextDirection = _nextDirection.asStateFlow()

    private val _score = MutableStateFlow<Int>(0)
    val score = _score.asStateFlow()

    private val _isGameOver = MutableStateFlow<Boolean>(false)
    val isGameOver = _isGameOver.asStateFlow()

    private val _isPaused = MutableStateFlow<Boolean>(false)
    val isPaused = _isPaused.asStateFlow()

    private val _showControls = MutableStateFlow<Boolean>(true)
    val showControls = _showControls.asStateFlow()

    private val _gameEvents = MutableStateFlow<GameEvent?>(null)
    val gameEvents = _gameEvents.asStateFlow()

    private var gameLoopJob: kotlinx.coroutines.Job? = null

    init {
        startNewGame()
    }

    fun updateScreenSize(width: Float, height: Float) {
        if (width > 0 && height > 0) {
            val newHeight = (gridWidth * (height / width)).toInt().coerceAtLeast(20)
            if (newHeight != gridHeight) {
                gridHeight = newHeight
            }
        }
    }

    fun startNewGame() {
        _snakeBody.value = (0 until INITIAL_SNAKE_LENGTH).map { Point(gridWidth / 2 - it, gridHeight / 2) }.toList()
        _currentDirection.value = Direction.RIGHT
        _nextDirection.value = Direction.RIGHT
        _score.value = 0
        _isGameOver.value = false
        _isPaused.value = false
        spawnFood()
        startGameLoop()
    }

    fun pauseGame() {
        _isPaused.value = true
    }

    fun resumeGame() {
        _isPaused.value = false
    }

    fun togglePause() {
        _isPaused.value = !_isPaused.value
    }

    fun toggleControls() {
        _showControls.value = !_showControls.value
    }

    private fun startGameLoop() {
        gameLoopJob?.cancel()
        gameLoopJob = viewModelScope.launch {
            while (!_isGameOver.value) {
                delay(GAME_SPEED_MS)
                if (!_isGameOver.value && !_isPaused.value) {
                    moveSnake()
                }
            }
        }
    }

    fun changeDirection(newDirection: Direction) {
        if (_isPaused.value) return
        val current = _currentDirection.value
        if (!newDirection.isOpposite(current)) {
            _nextDirection.value = newDirection
            _gameEvents.value = GameEvent.PlaySoundMove
        }
    }

    private fun moveSnake() {
        if (_isPaused.value) return
        _currentDirection.value = _nextDirection.value
        val head = _snakeBody.value.first()
        val rawHead = head + _currentDirection.value.vector

        // Wrap around walls (toroidal grid - continue from opposite side) based on dynamic grid dimensions
        val wrappedX = when {
            rawHead.x < 0 -> gridWidth - 1
            rawHead.x >= gridWidth -> 0
            else -> rawHead.x
        }
        val wrappedY = when {
            rawHead.y < 0 -> gridHeight - 1
            rawHead.y >= gridHeight -> 0
            else -> rawHead.y
        }
        val newHead = Point(wrappedX, wrappedY)

        // Check self collision
        if (_snakeBody.value.contains(newHead)) {
            gameOver()
            return
        }

        val newBody = _snakeBody.value.toMutableList()
        newBody.add(0, newHead)

        val foodEaten = _foodPosition.value?.let { food ->
            if (newHead == food) {
                val currentType = _foodType.value
                _score.value += currentType.points
                _gameEvents.value = GameEvent.EatFood(currentType.points)
                _gameEvents.value = GameEvent.PlaySoundEat
                // Grow extra segments if growth > 1
                repeat(currentType.growth - 1) {
                    val tail = newBody.last()
                    newBody.add(tail)
                }
                true
            } else {
                false
            }
        } ?: false

        if (!foodEaten) {
            newBody.removeAt(newBody.lastIndex)
        }

        _snakeBody.value = newBody

        if (foodEaten) {
            spawnFood()
        }
    }

    private fun spawnFood() {
        val occupied = _snakeBody.value.toSet()
        val showCtrl = _showControls.value
        val minY = 3 // Avoid top score/pause overlay
        val maxY = if (showCtrl) gridHeight - 6 else gridHeight - 2 // Avoid bottom d-pad controls if enabled

        val validYRange = if (minY < maxY) minY until maxY else 0 until gridHeight

        val emptyCells = (0 until gridWidth).flatMap { x ->
            validYRange.map { y -> Point(x, y) }
        }.filter { it !in occupied }

        val fallbackCells = if (emptyCells.isNotEmpty()) {
            emptyCells
        } else {
            (0 until gridWidth).flatMap { x ->
                (0 until gridHeight).map { y -> Point(x, y) }
            }.filter { it !in occupied }
        }

        if (fallbackCells.isNotEmpty()) {
            _foodPosition.value = fallbackCells.random()
            val rand = random.nextDouble()
            _foodType.value = when {
                rand < 0.85 -> FoodType.APPLE        // 85% Common
                rand < 0.97 -> FoodType.GOLDEN_APPLE // 12% Rare
                else -> FoodType.SPECIAL_STAR        // 3% Very Rare
            }
        }
    }

    private fun gameOver() {
        _isGameOver.value = true
        gameLoopJob?.cancel()
        _gameEvents.value = GameEvent.GameOver
        _gameEvents.value = GameEvent.PlaySoundGameOver
    }

    fun getGridWidth(): Int = gridWidth
    fun getGridHeight(): Int = gridHeight

    fun consumeEvent(): GameEvent? {
        val event = _gameEvents.value
        _gameEvents.value = null
        return event
    }

    override fun onCleared() {
        gameLoopJob?.cancel()
        super.onCleared()
    }
}
