package com.germsoftcs.bizqrd.model

import kotlin.Throws
import android.graphics.Bitmap
import android.graphics.Color
import com.google.zxing.MultiFormatWriter
import com.google.zxing.common.BitMatrix
import com.google.zxing.BarcodeFormat
import com.google.zxing.WriterException
import com.germsoftcs.bizqrd.model.QRCodeGenerator
import com.google.zxing.EncodeHintType
import java.lang.Exception

object QRCodeGenerator {
    const val QR_CODE_WIDTH = 700
    const val QR_CODE_HEIGHT = 700

    //EFFECTS: create bitmap of contact's QR code
    @JvmStatic
    @Throws(Exception::class)
    fun generateQRCode(data: String?): Bitmap {
        val multiFormatWriter = MultiFormatWriter()
        val hints = HashMap<EncodeHintType, Int>()
        hints[EncodeHintType.MARGIN] = 0
        val bitMatrix: BitMatrix = try {
            multiFormatWriter.encode(data, BarcodeFormat.QR_CODE, QR_CODE_WIDTH, QR_CODE_HEIGHT, hints)
        } catch (e: WriterException) {
            e.printStackTrace()
            throw Exception("Failed bitMatrix generation")
        }
        return bitMatrixToBitmap(bitMatrix)
    }

    fun bitMatrixToBitmap(bitMatrix: BitMatrix): Bitmap {
        val width = bitMatrix.width
        val height = bitMatrix.height
        val bmp = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)
        for (x in 0 until width) {
            for (y in 0 until height) {
                bmp.setPixel(x, y, if (bitMatrix[x, y]) Color.BLACK else Color.WHITE)
            }
        }
        return bmp
    }
}