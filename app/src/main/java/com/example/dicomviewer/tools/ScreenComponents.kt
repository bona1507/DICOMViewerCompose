package com.example.dicomviewer.tools

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DicomTopAppBar() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(4.dp)
    ) {
        TopAppBar(
            title = {
                Text(
                    text = "DICOM Viewer",
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier.padding(start = 16.dp),
                    color = Color.Black
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White),
            colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
        )
        // Bottom border
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomStart)
                .height(1.dp)
                .background(Color.Black)
        )
    }
}


@Composable
fun DicomActionButton(
    text: String,
    onClick: () -> Unit,
    backgroundColor: Color = Color(0xFF0C965A),
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        modifier = Modifier.height(56.dp),
        colors = ButtonDefaults.buttonColors(containerColor = backgroundColor)
    ) {
        Text(text = text)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DicomSlider(value: Float, onValueChange: (Float) -> Unit, valueRange: ClosedFloatingPointRange<Float>, sliderColor: Color) {
    Slider(
        value = value,
        onValueChange = onValueChange,
        valueRange = valueRange,
        modifier = Modifier.padding(16.dp).height(4.dp),
        steps = 0,
        colors = SliderDefaults.colors(
            thumbColor = sliderColor,
            activeTrackColor = sliderColor,
            inactiveTrackColor = Color.Gray
        ),
        thumb = {
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .background(sliderColor, shape = CircleShape)
            )
        }
    )
}
