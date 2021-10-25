package com.germsoftcs.bizqrd.model

import android.graphics.Bitmap
import com.germsoftcs.bizqrd.model.QRCodeGenerator.generateQRCode
import com.google.zxing.Binarizer
import com.google.zxing.BinaryBitmap
import com.google.zxing.RGBLuminanceSource
import com.google.zxing.common.GlobalHistogramBinarizer
import com.google.zxing.common.HybridBinarizer
import com.google.zxing.qrcode.QRCodeReader


class QrScanner {
    companion object {
        fun scan(bitmap: Bitmap) : Bitmap {
            val width = bitmap.width
            val height = bitmap.height
            val pixels = IntArray(width * height)
            bitmap.getPixels(pixels, 0, width, 0, 0, width, height)
            val qrCodeReader = QRCodeReader()
            val result = qrCodeReader.decode(BinaryBitmap(
                GlobalHistogramBinarizer(RGBLuminanceSource(width, height, pixels))
            ))
            return generateQRCode(result.text)
        }
    }
}