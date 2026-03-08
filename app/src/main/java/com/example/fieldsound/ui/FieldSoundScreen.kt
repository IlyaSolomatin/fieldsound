package com.example.fieldsound.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.fieldsound.audio.FrequencyMapper
import com.example.fieldsound.audio.SineWaveGenerator
import com.example.fieldsound.sensor.MagnetometerReader

@Composable
fun FieldSoundScreen(
    magnetometerReader: MagnetometerReader,
    sineWaveGenerator: SineWaveGenerator
) {
    var isRunning by remember { mutableStateOf(false) }
    val magnitude by magnetometerReader.magnitude.collectAsStateWithLifecycle()
    val frequency = FrequencyMapper.map(magnitude)

    LaunchedEffect(frequency) {
        if (isRunning) {
            sineWaveGenerator.setFrequency(frequency)
        }
    }

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_PAUSE && isRunning) {
                sineWaveGenerator.stop()
                magnetometerReader.stop()
                isRunning = false
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "FieldSound",
            style = MaterialTheme.typography.headlineLarge
        )

        Spacer(modifier = Modifier.height(48.dp))

        if (!magnetometerReader.isAvailable) {
            Text(
                text = "Magnetometer not available on this device",
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyLarge
            )
        } else {
            Text(
                text = "%.1f \u00B5T".format(magnitude),
                fontSize = 48.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "%d Hz".format(frequency.toInt()),
                fontSize = 32.sp,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(24.dp))

            LinearProgressIndicator(
                progress = { (magnitude / 200f).coerceIn(0f, 1f) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(12.dp),
            )

            Spacer(modifier = Modifier.height(48.dp))

            Button(
                onClick = {
                    if (isRunning) {
                        sineWaveGenerator.stop()
                        magnetometerReader.stop()
                    } else {
                        magnetometerReader.start()
                        sineWaveGenerator.start(frequency)
                    }
                    isRunning = !isRunning
                },
                colors = if (isRunning) {
                    ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                } else {
                    ButtonDefaults.buttonColors()
                }
            ) {
                Text(
                    text = if (isRunning) "Stop" else "Start",
                    fontSize = 20.sp,
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
                )
            }
        }
    }
}
