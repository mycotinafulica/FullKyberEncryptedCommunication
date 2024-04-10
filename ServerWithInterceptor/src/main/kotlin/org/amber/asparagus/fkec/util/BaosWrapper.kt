package org.amber.asparagus.fkec.util

import jakarta.servlet.ServletOutputStream
import jakarta.servlet.WriteListener
import java.io.ByteArrayOutputStream
import java.io.PrintWriter

class BaosWrapper(private var baos: ByteArrayOutputStream) {
    var printWriter = PrintWriter(baos)
        private set
    var baosServlet = ByteArrayServletStream(baos)
        private set

    fun setBufferSize(size: Int) {
        baos        = ByteArrayOutputStream(size)
        printWriter = PrintWriter(baos)
        baosServlet = ByteArrayServletStream(baos)
    }

    fun reset() {
        baosServlet.reset()
    }

    fun toByteArray(): ByteArray {
        return baos.toByteArray()
    }
}

class ByteArrayServletStream(private var baos: ByteArrayOutputStream): ServletOutputStream() {
    private var byteWritten = false

    fun setNewOutputStream(baos: ByteArrayOutputStream) {
        if(byteWritten) {
            throw IllegalStateException("Cannot set output stream after bytes has been written")
        }
        this.baos = baos
    }

    override fun write(b: Int) {
        byteWritten = true
        baos.write(b)
    }

    override fun isReady(): Boolean {
        return false
    }

    override fun setWriteListener(listener: WriteListener?) {
    }

    fun reset() {
        byteWritten = false
        baos.reset()
    }
}