package com.nokiasnake.game

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestWatcher
import org.junit.runner.Description

@OptIn(ExperimentalCoroutinesApi::class)
class SnakeViewModelTest {
    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    private val testDispatcher: TestDispatcher = StandardTestDispatcher()

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule(testDispatcher)

    private lateinit var viewModel: SnakeViewModel

    @Before
    fun setup() {
        viewModel = SnakeViewModel()
    }

    @Test
    fun `initial state - snake has correct length and position`() = runTest {
        val snakeBody = viewModel.snakeBody.value
        assertEquals(3, snakeBody.size)
        assertEquals(10, snakeBody[0].x) // Head at center
        assertEquals(10, snakeBody[0].y)
        assertEquals(9, snakeBody[1].x)
        assertEquals(8, snakeBody[2].x)
    }

    @Test
    fun `initial state - direction is RIGHT`() = runTest {
        assertEquals(Direction.RIGHT, viewModel.currentDirection.value)
        assertEquals(Direction.RIGHT, viewModel.nextDirection.value)
    }

    @Test
    fun `initial state - score is zero`() = runTest {
        assertEquals(0, viewModel.score.value)
    }

    @Test
    fun `initial state - game is not over`() = runTest {
        assertFalse(viewModel.isGameOver.value)
    }

    @Test
    fun `initial state - food is spawned`() = runTest {
        assertNotNull(viewModel.foodPosition.value)
        val food = viewModel.foodPosition.value!!
        assertTrue(food.x in 0..19)
        assertTrue(food.y in 0..19)
    }

    @Test
    fun `direction change - valid direction change works`() = runTest {
        viewModel.changeDirection(Direction.UP)
        assertEquals(Direction.UP, viewModel.nextDirection.value)
    }

    @Test
    fun `direction change - opposite direction is ignored`() = runTest {
        viewModel.changeDirection(Direction.LEFT) // Opposite of initial RIGHT
        assertEquals(Direction.RIGHT, viewModel.nextDirection.value) // Should remain RIGHT
    }

    @Test
    fun `direction change - all valid directions work`() = runTest {
        viewModel.changeDirection(Direction.UP)
        assertEquals(Direction.UP, viewModel.nextDirection.value)
        advanceGameByOneStep()

        viewModel.changeDirection(Direction.LEFT)
        assertEquals(Direction.LEFT, viewModel.nextDirection.value)
        advanceGameByOneStep()

        viewModel.changeDirection(Direction.DOWN)
        assertEquals(Direction.DOWN, viewModel.nextDirection.value)
        advanceGameByOneStep()

        viewModel.changeDirection(Direction.RIGHT)
        assertEquals(Direction.RIGHT, viewModel.nextDirection.value)
    }

    @Test
    fun `direction change - cannot reverse directly`() = runTest {
        viewModel.changeDirection(Direction.UP)
        advanceGameByOneStep() // Current direction is now UP

        viewModel.changeDirection(Direction.DOWN) // Opposite to UP
        assertEquals(Direction.UP, viewModel.nextDirection.value)

        viewModel.changeDirection(Direction.LEFT)
        advanceGameByOneStep() // Current direction is now LEFT

        viewModel.changeDirection(Direction.RIGHT) // Opposite to LEFT
        assertEquals(Direction.LEFT, viewModel.nextDirection.value)
    }

    @Test
    fun `movement - snake moves in current direction`() = runTest {
        val initialHead = viewModel.snakeBody.value.first()
        advanceGameByOneStep()
        val newHead = viewModel.snakeBody.value.first()

        assertEquals(initialHead.x + 1, newHead.x) // Moving RIGHT
        assertEquals(initialHead.y, newHead.y)
    }

    @Test
    fun `movement - snake body follows head`() = runTest {
        val initialBody = viewModel.snakeBody.value.toList()
        advanceGameByOneStep()
        val newBody = viewModel.snakeBody.value

        // New head should be at old head + direction
        assertEquals(initialBody[0].x + 1, newBody[0].x)
        // Second segment should be at old head position
        assertEquals(initialBody[0], newBody[1])
        // Third segment should be at old second position
        assertEquals(initialBody[1], newBody[2])
        // Body length should remain same (no food eaten)
        assertEquals(initialBody.size, newBody.size)
    }

    @Test
    fun `food collision - snake grows and score increases`() = runTest {
        // Manually place food in front of snake
        val head = viewModel.snakeBody.value.first()
        val foodPos = Point(head.x + 1, head.y) // Right in front

        // Use reflection to set food position for testing
        setFoodPosition(foodPos)

        val initialScore = viewModel.score.value
        val initialLength = viewModel.snakeBody.value.size

        advanceGameByOneStep()

        assertEquals(initialScore + 10, viewModel.score.value)
        assertEquals(initialLength + 1, viewModel.snakeBody.value.size)
        // Food should be respawned
        assertNotNull(viewModel.foodPosition.value)
    }

    @Test
    fun `wall collision - game over when hitting left wall`() = runTest {
        // Move snake to left wall
        setSnakeBody(listOf(Point(0, 10), Point(1, 10), Point(2, 10)))
        viewModel.changeDirection(Direction.LEFT)
        advanceGameByOneStep()

        assertTrue(viewModel.isGameOver.value)
    }

    @Test
    fun `wall collision - game over when hitting right wall`() = runTest {
        setSnakeBody(listOf(Point(19, 10), Point(18, 10), Point(17, 10)))
        viewModel.changeDirection(Direction.RIGHT)
        advanceGameByOneStep()

        assertTrue(viewModel.isGameOver.value)
    }

    @Test
    fun `wall collision - game over when hitting top wall`() = runTest {
        setSnakeBody(listOf(Point(10, 0), Point(10, 1), Point(10, 2)))
        viewModel.changeDirection(Direction.UP)
        advanceGameByOneStep()

        assertTrue(viewModel.isGameOver.value)
    }

    @Test
    fun `wall collision - game over when hitting bottom wall`() = runTest {
        setSnakeBody(listOf(Point(10, 19), Point(10, 18), Point(10, 17)))
        viewModel.changeDirection(Direction.DOWN)
        advanceGameByOneStep()

        assertTrue(viewModel.isGameOver.value)
    }

    @Test
    fun `self collision - game over when hitting own body`() = runTest {
        // Create a snake that can collide with itself
        // Shape: head at (5,5), body goes right, down, left, up
        setSnakeBody(listOf(
            Point(5, 5), Point(5, 6), Point(5, 7),
            Point(6, 7), Point(7, 7),
            Point(7, 6), Point(7, 5),
            Point(6, 5)
        ))
        viewModel.changeDirection(Direction.RIGHT) // Head at (5,5) moving right into body at (6,5)
        advanceGameByOneStep()

        assertTrue(viewModel.isGameOver.value)
    }

    @Test
    fun `startNewGame - resets all state`() = runTest {
        // Play a bit first
        advanceGameByOneStep()
        advanceGameByOneStep()
        viewModel.changeDirection(Direction.UP)
        advanceGameByOneStep()

        // Start new game
        viewModel.startNewGame()

        // Check all state is reset
        assertEquals(3, viewModel.snakeBody.value.size)
        assertEquals(Direction.RIGHT, viewModel.currentDirection.value)
        assertEquals(Direction.RIGHT, viewModel.nextDirection.value)
        assertEquals(0, viewModel.score.value)
        assertFalse(viewModel.isGameOver.value)
        assertNotNull(viewModel.foodPosition.value)
    }

    @Test
    fun `grid size is 20x20`() = runTest {
        assertEquals(20, viewModel.getGridSize())
    }

    @Test
    fun `direction opposite detection`() = runTest {
        assertTrue(Direction.UP.isOpposite(Direction.DOWN))
        assertTrue(Direction.DOWN.isOpposite(Direction.UP))
        assertTrue(Direction.LEFT.isOpposite(Direction.RIGHT))
        assertTrue(Direction.RIGHT.isOpposite(Direction.LEFT))

        assertFalse(Direction.UP.isOpposite(Direction.LEFT))
        assertFalse(Direction.UP.isOpposite(Direction.RIGHT))
        assertFalse(Direction.UP.isOpposite(Direction.UP))
    }

    @Test
    fun `point addition works correctly`() = runTest {
        val p1 = Point(5, 10)
        val p2 = Point(3, -2)
        val result = p1 + p2
        assertEquals(Point(8, 8), result)
    }

    @Test
    fun `food spawns on empty cell only`() = runTest {
        // Fill 399 cells, leaving only (0, 19) empty
        val snakeBody = mutableListOf<Point>()
        for (x in 0..19) {
            for (y in 0..19) {
                if (x != 0 || y != 19) {
                    snakeBody.add(Point(x, y))
                }
            }
        }
        setSnakeBody(snakeBody)

        // Call private spawnFood() via reflection
        val method = SnakeViewModel::class.java.getDeclaredMethod("spawnFood")
        method.isAccessible = true
        method.invoke(viewModel)

        // Food must spawn at the only empty cell (0, 19)
        assertEquals(Point(0, 19), viewModel.foodPosition.value)
    }

    private fun advanceGameByOneStep() {
        val method = SnakeViewModel::class.java.getDeclaredMethod("moveSnake")
        method.isAccessible = true
        method.invoke(viewModel)
    }

    private fun setSnakeBody(body: List<Point>) {
        val field = SnakeViewModel::class.java.getDeclaredField("_snakeBody")
        field.isAccessible = true
        field.set(viewModel, kotlinx.coroutines.flow.MutableStateFlow(body))
    }

    private fun setFoodPosition(pos: Point) {
        val field = SnakeViewModel::class.java.getDeclaredField("_foodPosition")
        field.isAccessible = true
        field.set(viewModel, kotlinx.coroutines.flow.MutableStateFlow(pos))
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
class MainDispatcherRule(
    val testDispatcher: TestDispatcher = StandardTestDispatcher()
) : TestWatcher() {
    override fun starting(description: Description) {
        Dispatchers.setMain(testDispatcher)
    }

    override fun finished(description: Description) {
        Dispatchers.resetMain()
    }
}
