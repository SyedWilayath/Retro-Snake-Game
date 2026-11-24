package com.nokiasnake.game.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nokiasnake.game.theme.RetroBlack
import com.nokiasnake.game.theme.RetroGreen
import com.nokiasnake.game.theme.RetroLightGreen
import com.nokiasnake.game.theme.RetroMediumGray
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    onTimeout: () -> Unit
) {
    LaunchedEffect(Unit) {
        delay(1800L)
        onTimeout()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(RetroBlack),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "🐍",
                fontSize = 80.sp
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "RETRO",
                fontSize = 40.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                color = RetroGreen,
                style = androidx.compose.ui.text.TextStyle(letterSpacing = 6.sp)
            )

            Text(
                text = "SNAKE GAME",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                color = RetroLightGreen,
                style = androidx.compose.ui.text.TextStyle(letterSpacing = 4.sp)
            )

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "LOADING...",
                fontSize = 16.sp,
                fontFamily = FontFamily.Monospace,
                color = RetroMediumGray
            )
        }
    }
}
