package com.example.dicomviewer.tools

import android.graphics.Bitmap
import android.util.Log
import com.dicomhero.api.CodecFactory
import com.dicomhero.api.ColorTransformsFactory
import com.dicomhero.api.DrawBitmap
import com.dicomhero.api.PipeStream
import com.dicomhero.api.StreamReader
import com.dicomhero.api.TransformsChain
import com.dicomhero.api.VOILUT
import com.dicomhero.api.drawBitmapType_t
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.nio.ByteBuffer


object Helper {

    fun processDicomFile(inputStream: InputStream): Bitmap? {
        return try {
            loadDicomImage(inputStream)
        } catch (e: IOException) {
            Log.e("DICOM", "Error processing DICOM file: ${e.message}")
            null
        }
    }

    private fun loadDicomImage(inputStream: InputStream): Bitmap? {
        return try {
            val dicomheroPipe = PipeStream(32000).apply {
                Thread(PushToDicomheroPipe(this, inputStream)).start()
            }
            val loadDataSet = CodecFactory.load(StreamReader(dicomheroPipe.streamInput))
            val dicomImage = loadDataSet.getImageApplyModalityTransform(0)
            val chain = TransformsChain().apply {
                if (ColorTransformsFactory.isMonochrome(dicomImage.colorSpace)) {
                    addTransform(VOILUT(VOILUT.getOptimalVOI(dicomImage, 0, 0, dicomImage.width, dicomImage.height)))
                }
            }
            val drawBitmap = DrawBitmap(chain)
            val memory = drawBitmap.getBitmap(dicomImage, drawBitmapType_t.drawBitmapRGBA, 4)
            val renderBitmap = Bitmap.createBitmap(dicomImage.width.toInt(), dicomImage.height.toInt(), Bitmap.Config.ARGB_8888)
            val memoryByte = ByteArray(memory.size().toInt())
            memory.data(memoryByte)
            renderBitmap.copyPixelsFromBuffer(ByteBuffer.wrap(memoryByte))
            renderBitmap
        } catch (e: IOException) {
            Log.e("DICOM", "Error loading DICOM: ${e.message}")
            null
        }
    }
}