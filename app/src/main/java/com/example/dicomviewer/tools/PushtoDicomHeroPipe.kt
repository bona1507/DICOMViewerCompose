package com.example.dicomviewer.tools

import com.dicomhero.api.MutableMemory
import com.dicomhero.api.PipeStream
import com.dicomhero.api.StreamWriter
import java.io.InputStream

class PushToDicomheroPipe(
    private val imebraPipe: PipeStream,
    private val inputStream: InputStream?
) : Runnable {

    override fun run() {
        val pipeWriter = StreamWriter(imebraPipe.streamOutput)
        try {
            val buffer = ByteArray(128000)
            val memory = MutableMemory()

            inputStream?.use { stream ->
                while (true) {
                    val readBytes = stream.read(buffer)
                    if (readBytes < 0) break
                    if (readBytes > 0) {
                        memory.assign(buffer)
                        memory.resize(readBytes.toLong())
                        pipeWriter.write(memory)
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            pipeWriter.delete()
            imebraPipe.close(50000)
        }
    }
}