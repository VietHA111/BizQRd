package com.example.bizqd.model;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;

import ezvcard.VCard;

public class QRCodeGenerator {
    VCardGenerator vCardGenerator;

    public QRCodeGenerator(Uri uriContact, Context mContext, boolean[] settings) {
        this.vCardGenerator = new VCardGenerator(uriContact, mContext, settings);
    }

    public Bitmap generateQRCode() throws Exception {
        String vCard = vCardGenerator.generateVCard();

        MultiFormatWriter multiFormatWriter = new MultiFormatWriter();
        BitMatrix bitMatrix;
        try {
            bitMatrix = multiFormatWriter.encode(vCard, BarcodeFormat.QR_CODE, 800, 800);
        } catch (WriterException e) {
            e.printStackTrace();
            throw new Exception("Failed bitMatrix generation");
        }
        int width = bitMatrix.getWidth();
        int height = bitMatrix.getHeight();
        Bitmap bmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                bmp.setPixel(x, y, bitMatrix.get(x, y) ? Color.BLACK : Color.WHITE);
            }
        }
        return bmp;
    }

    public String getFirstLastName() {
        return vCardGenerator.getFirstLastName();
    }
}
