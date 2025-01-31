package com.example.dicomviewer.screen

import android.graphics.Bitmap
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.example.dicomviewer.tools.DicomActionButton
import com.example.dicomviewer.tools.DicomSlider
import com.example.dicomviewer.tools.DicomTopAppBar
import kotlinx.coroutines.delay

@Composable
fun DicomViewerMainScreen(
    dicomFrames: List<Bitmap>,
    frameIndex: Int,
    onFrameChange: (Int) -> Unit,
    totalFrames: Int,
    isAutoRotateEnabled: Boolean,
    onToggleAutoRotate: () -> Unit,
    autoRotateSpeed: Int,
    onSpeedChange: (Int) -> Unit,
    patientName: String
) {
    Scaffold(
        topBar = {
            DicomTopAppBar()
        },
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize().padding(top = innerPadding.calculateTopPadding())) {
            val screenHeight = LocalConfiguration.current.screenHeightDp.dp
            val imageHeight = screenHeight * 0.5f
            val sliderAndButtonHeight = screenHeight * 0.4f

            // State variables to manage the scale and offset
            var scale by remember { mutableFloatStateOf(1f) } // Zoom scale
            var offset by remember { mutableStateOf(Offset(0f, 0f)) } // Pan offset

            // Image customize
            var contrast by remember { mutableFloatStateOf(1f) } // Default contrast is 1f
            var brightness by remember { mutableFloatStateOf(0f) } // Default brightness is 0f
            val colorMatrix = floatArrayOf(
                contrast, 0f, 0f, 0f, brightness,
                0f, contrast, 0f, 0f, brightness,
                0f, 0f, contrast, 0f, brightness,
                0f, 0f, 0f, 1f, 0f
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = "Nama/ID Pasien: $patientName",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.Black,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                // DICOM Image Section (with pinch-to-zoom functionality)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(imageHeight)
                        .padding(bottom = 16.dp)
                        .background(Color.Black) // Border/Background for the image holder
                        .clip(androidx.compose.ui.graphics.RectangleShape) // Clip the image to the container's border
                        .pointerInput(Unit) {
                            detectTransformGestures { _, pan, zoom, _ ->
                                // Limit zoom range
                                scale = (scale * zoom).coerceIn(1f, 3f)
                                // Adjust offset with pan, but keep it constrained within bounds
                                offset = Offset(
                                    (offset.x + pan.x).coerceIn(-scale * 100, scale * 100), // Example boundary constraints
                                    (offset.y + pan.y).coerceIn(-scale * 100, scale * 100)
                                )
                            }
                        }
                ) {
                    if (dicomFrames.isNotEmpty()) {
                        dicomFrames.forEachIndexed { index, bitmap ->
                            androidx.compose.foundation.Image(
                                painter = rememberAsyncImagePainter(bitmap),
                                contentDescription = null,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .graphicsLayer(
                                        scaleX = scale,
                                        scaleY = scale,
                                        translationX = offset.x,
                                        translationY = offset.y,
                                        alpha = if (index == frameIndex) 1f else 0f // Show only the selected frame
                                    ),
                                colorFilter = ColorFilter.colorMatrix(ColorMatrix(colorMatrix))
                            )
                        }
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.Gray)
                        )
                    }
                }

                // Auto-rotate Speed Control Row
                if (isAutoRotateEnabled) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        DicomActionButton(
                            text = "0.5x",
                            onClick = { onSpeedChange(0) },
                            backgroundColor = Color(0xFF0C965A),
                            modifier = Modifier.weight(1f)
                        )

                        Spacer(modifier = Modifier.width(8.dp))
                        DicomActionButton(
                            text = "1x",
                            onClick = { onSpeedChange(1) },
                            backgroundColor = Color(0xFF0C965A),
                            modifier = Modifier.weight(1f)
                        )

                        Spacer(modifier = Modifier.width(8.dp))
                        DicomActionButton(
                            text = "2x",
                            onClick = { onSpeedChange(2) },
                            backgroundColor = Color(0xFF0C965A),
                            modifier = Modifier.weight(1f)
                        )

                        Spacer(modifier = Modifier.width(8.dp))
                        DicomActionButton(
                            text = "4x",
                            onClick = { onSpeedChange(3) },
                            backgroundColor = Color(0xFF0C965A),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    horizontalArrangement = Arrangement.Center
                ) {
                    // Toggle Auto-rotate Button
                    DicomActionButton(
                        text = if (isAutoRotateEnabled) "Putar Manual" else "Putar Otomatis",
                        onClick = onToggleAutoRotate,
                        backgroundColor = Color(0xFF0C965A),
                        modifier = Modifier.weight(1f)
                    )

                    if (!isAutoRotateEnabled) {
                        Spacer(modifier = Modifier.width(8.dp))
                        // Toggle Auto-rotate Button
                        DicomActionButton(
                            text = "Reset Filter",
                            onClick = {
                                contrast = 1f
                                brightness = 0f
                            },
                            backgroundColor = Color(0xFF09603C),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .height(sliderAndButtonHeight)
                        .padding(16.dp)
                ) {
                    // Show the slider only if auto-rotation is not enabled
                    if (!isAutoRotateEnabled && totalFrames > 1) {
                        var frameValue by remember { mutableFloatStateOf(frameIndex.toFloat()) }

                        Text("Posisi Klip", style = MaterialTheme.typography.bodyLarge)
                        DicomSlider(
                            value = frameValue,
                            onValueChange = { value ->
                                frameValue = value
                                onFrameChange(value.toInt().coerceIn(0, totalFrames - 1))
                            },
                            valueRange = 0f..(totalFrames - 1).toFloat(),
                            sliderColor = Color(0xFF10CC7B)
                        )

                        Text("Kontras", style = MaterialTheme.typography.bodyLarge)
                        DicomSlider(
                            value = contrast,
                            onValueChange = { contrast = it },
                            valueRange = 0f..3f,
                            sliderColor = Color(0xFF0C965A)
                        )

                        Text("Kecerahan", style = MaterialTheme.typography.bodyLarge)
                        DicomSlider(
                            value = brightness,
                            onValueChange = { brightness = it },
                            valueRange = -255f..255f,
                            sliderColor = Color(0xFF09603C)
                        )
                    }
                }
            }
        }
    }

    // Handle Auto-rotate with speed
    LaunchedEffect(isAutoRotateEnabled, autoRotateSpeed) {
        if (isAutoRotateEnabled) {
            var currentIndex = frameIndex
            val delayTime = when (autoRotateSpeed) {
                0 -> 400 // 0.5x speed
                1 -> 200 // 1x speed
                2 -> 100  // 2x speed
                3 -> 25  // 3x speed
                else -> 200 // Default to 1x speed
            }
            while (isAutoRotateEnabled) {
                delay(delayTime.toLong())
                currentIndex = (currentIndex + 1) % totalFrames
                onFrameChange(currentIndex)
            }
        }
    }
}