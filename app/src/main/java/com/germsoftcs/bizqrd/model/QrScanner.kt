package com.germsoftcs.bizqrd.model

import android.graphics.Bitmap
import android.util.Log
import com.germsoftcs.bizqrd.model.QRCodeGenerator.QR_CODE_HEIGHT
import com.germsoftcs.bizqrd.model.QRCodeGenerator.QR_CODE_WIDTH
import com.germsoftcs.bizqrd.model.QRCodeGenerator.bitMatrixToBitmap
import com.germsoftcs.bizqrd.model.QRCodeGenerator.generateQRCode
import com.google.zxing.Binarizer
import com.google.zxing.BinaryBitmap
import com.google.zxing.RGBLuminanceSource
import com.google.zxing.common.BitMatrix
import com.google.zxing.common.GlobalHistogramBinarizer
import com.google.zxing.common.HybridBinarizer
import com.google.zxing.common.detector.WhiteRectangleDetector
import com.google.zxing.qrcode.QRCodeReader
import com.google.zxing.qrcode.detector.Detector
import java.lang.String.valueOf


class QrScanner {
    companion object {
        fun scan(bitmap: Bitmap) : Bitmap {
            val width = bitmap.width
            val height = bitmap.height
            val pixels = IntArray(width * height)
            bitmap.getPixels(pixels, 0, width, 0, 0, width, height)
            val detector = Detector(BinaryBitmap(
                HybridBinarizer(RGBLuminanceSource(width, height, pixels))
            ).blackMatrix)
            val result = detector.detect()
//            Log.i("QrScanner", valueOf(resultPoints[0].x) + " " + valueOf(resultPoints[0].y))
            val bitMatrix: BitMatrix = result.bits
            val returnVal = Bitmap.createScaledBitmap(bitMatrixToBitmap(bitMatrix), QR_CODE_WIDTH, QR_CODE_HEIGHT, false)
            Log.i("QrScanner", valueOf(returnVal.width) + valueOf(returnVal.height))
            return returnVal
        }
    }
}