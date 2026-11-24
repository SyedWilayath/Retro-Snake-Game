package com.nokiasnake.game

import android.media.AudioAttributes
import android.media.SoundPool
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.nokiasnake.game.data.AppDatabase
import com.nokiasnake.game.data.ScoreEntity
import com.nokiasnake.game.sound.SoundManager
import com.nokiasnake.game.theme.RetroDarkGreen
import com.nokiasnake.game.theme.RetroGreen
import com.nokiasnake.game.theme.RetroLightGreen
import com.nokiasnake.game.theme.RetroMediumGray
import com.nokiasnake.game.theme.RetroSnakeTheme
import com.nokiasnake.game.ui.GameScreen
import com.nokiasnake.game.ui.HighScoresScreen
import com.nokiasnake.game.ui.SplashScreen
import com.nokiasnake.game.ui.StartScreen
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private val viewModel: SnakeViewModel by viewModels()
    private lateinit var database: AppDatabase
    private var soundManager: SoundManager? = null
    private var soundPool: SoundPool? = null
    private var moveSoundId = 0
    private var eatSoundId = 0
    private var gameOverSoundId = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        database = AppDatabase.getDatabase(this)
        initializeSounds()
        observeGameEvents()

        WindowCompat.setDecorFitsSystemWindows(window, false)
        val windowInsetsController = WindowCompat.getInsetsController(window, window.decorView)
        windowInsetsController.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        windowInsetsController.hide(WindowInsetsCompat.Type.systemBars())

        setContent {
            RetroSnakeTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    NavHost(navController, startDestination = "splash") {
                        composable("splash") {
                            SplashScreen(
                                onTimeout = {
                                    navController.navigate("start") {
                                        popUpTo("splash") { inclusive = true }
                                    }
                                }
                            )
                        }
                        composable("start") {
                            StartScreen(
                                onPlayClick = { navController.navigate("game") },
                                onHighScoresClick = { navController.navigate("highscores") }
                            )
                        }
                        composable("game") {
                            GameScreen(
                                viewModel = viewModel,
                                onGameOver = { score ->
                                    saveScore(score)
                                    navController.navigate("gameover/$score")
                                }
                            )
                        }
                        composable(
                            route = "gameover/{score}",
                            arguments = listOf(navArgument("score") { type = NavType.IntType })
                        ) { backStackEntry ->
                            val score = backStackEntry.arguments?.getInt("score") ?: 0
                            GameOverDialogScreen(
                                score = score,
                                onPlayAgain = {
                                    viewModel.startNewGame()
                                    navController.navigate("game") {
                                        popUpTo("start") { inclusive = false }
                                        launchSingleTop = true
                                    }
                                },
                                onQuit = {
                                    navController.navigate("start") {
                                        popUpTo("start") { inclusive = false }
                                    }
                                }
                            )
                        }
                        composable("highscores") {
                            HighScoresScreen(
                                database = database,
                                onBackClick = { navController.navigate("start") { popUpTo("start") { inclusive = false } } }
                            )
                        }
                    }
                }
            }
        }
    }

    private fun observeGameEvents() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.gameEvents.collect { event ->
                    when (event) {
                        is GameEvent.PlaySoundMove -> playMoveSound()
                        is GameEvent.PlaySoundEat -> playEatSound()
                        is GameEvent.PlaySoundGameOver -> playGameOverSound()
                        else -> {}
                    }
                }
            }
        }
    }

    private fun initializeSounds() {
        // Initialize ExoPlayer-based sound manager
        soundManager = SoundManager(this)

        // Initialize SoundPool for low-latency sounds
        val audioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_GAME)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()
        soundPool = SoundPool.Builder()
            .setMaxStreams(3)
            .setAudioAttributes(audioAttributes)
            .build()
    }

    private fun saveScore(score: Int) {
        lifecycleScope.launch {
            val entity = ScoreEntity(score = score)
            database.scoreDao().insertScore(entity)
        }
    }

    fun playMoveSound() {
        soundManager?.playMoveSound()
        soundPool?.play(moveSoundId, 1f, 1f, 1, 0, 1f)
    }

    fun playEatSound() {
        soundManager?.playEatSound()
        soundPool?.play(eatSoundId, 1f, 1f, 1, 0, 1f)
    }

    fun playGameOverSound() {
        soundManager?.playGameOverSound()
        soundPool?.play(gameOverSoundId, 1f, 1f, 1, 0, 1f)
    }

    override fun onDestroy() {
        super.onDestroy()
        soundManager?.release()
        soundPool?.release()
        soundPool = null
    }
}

@Composable
fun GameOverDialogScreen(
    score: Int,
    onPlayAgain: () -> Unit,
    onQuit: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "GAME OVER",
                fontSize = 48.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                color = Color.Red
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Final Score: $score",
                fontSize = 28.sp,
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
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp)
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
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp)
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
