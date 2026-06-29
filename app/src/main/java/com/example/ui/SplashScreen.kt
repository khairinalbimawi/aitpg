package com.example.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

val LightGreen = Color(0xFFA5D6A7)

@Composable
fun SplashScreen(onTimeout: () -> Unit) {
    val alpha = remember { Animatable(0f) }
    val scale = remember { Animatable(0.5f) }

    LaunchedEffect(Unit) {
        alpha.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 1000)
        )
        scale.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 1000)
        )
        delay(1500)
        onTimeout()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkGreen),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .alpha(alpha.value)
                .scale(scale.value)
        ) {
            Text(
                text = "KOMBAT",
                color = LightGreen,
                fontSize = 48.sp,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.displayLarge
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Kompeten Mandiri Berkarakter",
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                style = MaterialTheme.typography.titleMedium
            )
        }
    }
}
