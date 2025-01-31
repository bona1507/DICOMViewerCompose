package com.example.dicomviewer.tools

import android.graphics.Bitmap
import android.util.Log
import com.dicomhero.api.CodecFactory
import com.dicomhero.api.ColorTransformsFactory
import com.dicomhero.api.DrawBitmap
import com.dicomhero.api.PersonName
import com.dicomhero.api.PipeStream
import com.dicomhero.api.StreamReader
import com.dicomhero.api.TagId
import com.dicomhero.api.TransformsChain
import com.dicomhero.api.VOILUT
import com.dicomhero.api.drawBitmapType_t
import java.io.IOException
import java.io.InputStream
import java.nio.ByteBuffer


data class DicomData(val bitmap: Bitmap?, val patientName: String)

object Helper {

    fun processDicomFile(inputStream: InputStream): DicomData {
        return try {
            loadDicomImage(inputStream)
        } catch (e: IOException) {
            Log.e("DICOM", "Error processing DICOM file: ${e.message}")
            DicomData(null, "Unknown")
        }
    }

    private fun loadDicomImage(inputStream: InputStream): DicomData {
        return try {
            val dicomheroPipe = PipeStream(32000).apply {
                Thread(PushToDicomheroPipe(this, inputStream)).start()
            }
            val loadDataSet = CodecFactory.load(StreamReader(dicomheroPipe.streamInput))
            val patientNameTag = TagId(0x00100010)
            val patientName = try {
                val nameObj = loadDataSet.getPatientName(patientNameTag, 0, PersonName("","",""))
                val firstName = nameObj.phoneticRepresentation ?: "" // Replace with actual field names if necessary
                val middleName = nameObj.alphabeticRepresentation ?: ""
                val lastName = nameObj.ideographicRepresentation ?: ""
                "$firstName $middleName $lastName".trim()
            } catch (e: Exception) {
                Log.e("DICOM", "Error getting patient name: ${e.message}")
                "Unknown"
            }
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

            DicomData(renderBitmap, patientName) // Return both Bitmap & Patient Name
        } catch (e: IOException) {
            Log.e("DICOM", "Error loading DICOM: ${e.message}")
            DicomData(null, "Unknown")
        }
    }
}
