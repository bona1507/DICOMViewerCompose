package com.example.dicomviewer

import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.appcompat.app.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.example.dicomviewer.screen.DicomViewerMainScreen
import com.example.dicomviewer.tools.DicomData
import com.example.dicomviewer.tools.Helper
import java.io.IOException

class MainActivity : ComponentActivity() {

    private var dicomFrames by mutableStateOf<List<Bitmap>>(emptyList()) // Ensure it's a List of Bitmap
    private var frameIndex by mutableIntStateOf(0)
    private var isAutoRotateEnabled by mutableStateOf(false)
    private var autoRotateSpeed by mutableIntStateOf(1)
    private var patientName by mutableStateOf("Unknown") // Store patient name

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        System.loadLibrary("dicomhero6")

        loadDICOMFilesFromAssets()

        setContent {
            MaterialTheme {
                DicomViewerMainScreen(
                    dicomFrames = dicomFrames,
                    frameIndex = frameIndex,
                    onFrameChange = { frameIndex = it },
                    totalFrames = dicomFrames.size,
                    isAutoRotateEnabled = isAutoRotateEnabled,
                    onToggleAutoRotate = { isAutoRotateEnabled = !isAutoRotateEnabled },
                    autoRotateSpeed = autoRotateSpeed,
                    onSpeedChange = { autoRotateSpeed = it },
                    patientName = patientName
                )
            }
        }
    }

    private fun loadDICOMFilesFromAssets() {
        try {
            val fileNames = assets.list("test") ?: return
            val dicomDataList = fileNames.mapNotNull { fileName ->
                assets.open("test/$fileName").use { inputStream ->
                    Helper.processDicomFile(inputStream)
                }
            }

            if (dicomDataList.isEmpty()) {
                showErrorDialog()
            } else {
                dicomFrames = dicomDataList.mapNotNull { it.bitmap } // Extract only bitmaps
                patientName = dicomDataList.firstOrNull()?.patientName ?: "Unknown" // Prevent null access
                frameIndex = 0
            }

        } catch (e: IOException) {
            Log.e("DICOM", "Error loading DICOM files: ${e.message}")
            showErrorDialog()
        }
    }

    private fun showErrorDialog() {
        runOnUiThread {
            AlertDialog.Builder(this).apply {
                setMessage("Failed to load DICOM File")
                setTitle("Error")
                setPositiveButton("OK") { dialog, _ -> dialog.dismiss() }
                setCancelable(true)
            }.show()
        }
    }
}
