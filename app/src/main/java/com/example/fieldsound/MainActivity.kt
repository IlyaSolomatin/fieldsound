package com.example.fieldsound

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.example.fieldsound.audio.SineWaveGenerator
import com.example.fieldsound.sensor.MagnetometerReader
import com.example.fieldsound.ui.FieldSoundScreen
import com.example.fieldsound.ui.theme.FieldSoundTheme

class MainActivity : ComponentActivity() {

    private lateinit var magnetometerReader: MagnetometerReader
    private lateinit var sineWaveGenerator: SineWaveGenerator

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        magnetometerReader = MagnetometerReader(this)
        sineWaveGenerator = SineWaveGenerator()

        setContent {
            FieldSoundTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    FieldSoundScreen(magnetometerReader, sineWaveGenerator)
                }
            }
        }
    }

    override fun onPause() {
        super.onPause()
        sineWaveGenerator.stop()
        magnetometerReader.stop()
    }
}
