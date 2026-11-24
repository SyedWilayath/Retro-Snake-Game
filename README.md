# NokiaSnake - Classic Snake Game for Android

A faithful recreation of the classic Nokia Snake game built with modern Android technologies: Kotlin, Jetpack Compose, Material 3, MVVM architecture, and Room database.

## Features

- **Classic Gameplay**: 20×20 grid, snake movement every 150ms, wall/self collision detection
- **Modern UI**: Jetpack Compose with Material 3, light/dark theme support
- **Sound Effects**: ExoPlayer for movement, eating, and game-over sounds
- **High Scores**: Persistent top 10 scores using Room database
- **Accessibility**: Large text support, TalkBack announcements
- **Controls**: Swipe gestures + on-screen arrow buttons
- **Architecture**: MVVM with `SnakeViewModel` managing all game state
- **Testing**: Unit tests for ViewModel logic

## Screenshots

*Add screenshots here after building the app*

## Requirements

- Android Studio Koala (2024.1.1) or later
- Android SDK 34 (compile target)
- Minimum SDK 26 (Android 8.0 Oreo)
- JDK 17 or later
- Kotlin 1.9.22+

## Project Structure

```
NokiaSnakeGame/
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/nokiasnake/game/
│   │   │   │   ├── data/           # Room database (ScoreEntity, ScoreDao, AppDatabase)
│   │   │   │   ├── sound/          # SoundManager (ExoPlayer + SoundPool)
│   │   │   │   ├── theme/          # Material 3 theme (Color, Theme, Typography)
│   │   │   │   ├── ui/             # Compose screens (GameScreen, StartScreen, HighScoresScreen)
│   │   │   │   ├── SnakeViewModel.kt  # Game logic (MVVM)
│   │   │   │   └── MainActivity.kt    # Navigation + sound integration
│   │   │   ├── res/
│   │   │   │   ├── values/         # Strings, colors, themes
│   │   │   │   └── raw/            # Sound effects (.ogg files)
│   │   │   └── AndroidManifest.xml
│   │   └── test/                   # Unit tests
│   ├── build.gradle.kts
│   └── proguard-rules.pro
├── build.gradle.kts
├── settings.gradle.kts
├── gradle/
│   └── wrapper/
└── README.md
```

## Building the Project

### From Android Studio

1. Open Android Studio
2. Select **File > Open** and choose the `NokiaSnakeGame` directory
3. Wait for Gradle sync to complete
4. Click **Run** (▶️) or press `Shift+F10`

### From Command Line

```bash
# Navigate to project directory
cd NokiaSnakeGame

# Build debug APK
./gradlew assembleDebug

# Run tests
./gradlew test

# Install on connected device/emulator
./gradlew installDebug
```

## Running the App

1. **Start Screen**: Tap **PLAY** to begin or **HIGH SCORES** to view leaderboard
2. **Game Screen**:
   - Control snake with **swipe gestures** anywhere on screen
   - Or use **on-screen arrow buttons** at bottom
   - Eat red apples to grow and score 10 points
   - Avoid walls and your own tail
3. **Game Over**: Shows final score with **PLAY AGAIN** and **QUIT** options
4. **High Scores**: Displays top 10 scores with timestamps

## Game Rules

- Grid: 20×20 cells
- Initial snake length: 3 segments
- Movement speed: 150ms per step (fixed)
- Score: +10 points per food eaten
- Game over on: wall collision or self collision
- High scores persist across app restarts

## Architecture

### SnakeViewModel

Central game state management:
- `snakeBody`: List<Point> - snake segments (head first)
- `foodPosition`: Point? - current food location
- `currentDirection` / `nextDirection`: Direction - input handling
- `score`: Int - current game score
- `isGameOver`: Boolean - game state flag
- `gameEvents`: Flow<GameEvent> - sound triggers, game over notification

### Data Layer

- **ScoreEntity**: Room entity with id, score, timestamp
- **ScoreDao**: Insert, query top 10, clear all
- **AppDatabase**: Room database singleton

### UI Layer (Compose)

- **StartScreen**: Title, animated snake, Play/High Scores buttons
- **GameScreen**: Canvas rendering, score overlay, arrow controls, game over dialog
- **HighScoresScreen**: LazyColumn with ranked scores, medals for top 3

### Sound

- **SoundManager**: ExoPlayer for high-quality playback
- **SoundPool**: Low-latency fallback for game sounds
- Three sounds: move (blip), eat (pop), game over (buzz)

## Theming

Custom Nokia-inspired color palette:
- **Primary**: `#008800` (Dark Green)
- **Accent**: `#00FF00` (Classic Nokia Green)
- **Light**: `#88FF88` (Light Green)
- **Dark backgrounds**: `#000000`, `#1A1A1A`, `#333333`

Supports system light/dark theme with proper status/navigation bar coloring.

## Accessibility

- Monospace font family for retro feel
- Large text scaling support
- Semantic content descriptions for TalkBack
- High contrast colors (WCAG AA compliant)
- Touch targets ≥ 48dp

## Testing

Run unit tests:
```bash
./gradlew test
```

Test coverage includes:
- Direction handling (valid changes, opposite prevention)
- Collision detection (walls, self)
- Scoring (food consumption, growth)
- Game state reset

## Adding Real Sound Effects

Replace placeholder files in `app/src/main/res/raw/`:
- `move_sound.ogg` - Short blip (50-100ms)
- `eat_sound.ogg` - Pop/chomp (200-300ms)
- `game_over_sound.ogg` - Buzz/tone (500-1000ms)

Recommended format: 44.1kHz, mono, 16-bit, Ogg Vorbis.

## License

MIT License - Feel free to use for learning or personal projects.

## Credits

Inspired by the classic Snake game on Nokia 3310/5110 phones.
Built with ❤️ using Android Jetpack Compose.