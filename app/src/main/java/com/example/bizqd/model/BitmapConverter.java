package com.example.bizqd.model;

import android.graphics.Bitmap;

public class BitmapConverter extends Thread {
    Bitmap bm;

    public BitmapConverter(Bitmap bm) {
        this.bm = bm;
    }

    public void run() {

    }
}
