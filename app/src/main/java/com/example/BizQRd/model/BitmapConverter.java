package com.example.BizQRd.model;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.widget.RelativeLayout;

public class BitmapConverter extends Thread {
    Bitmap bm;
    RelativeLayout rl;
    int color;

    public BitmapConverter(Bitmap bm, RelativeLayout rl, int color) {
        this.bm = bm;
        this.rl = rl;
        this.color = color;
    }

    @Override
    public void run() {
        Canvas canvas = new Canvas(bm);
        Drawable bgDrawable = rl.getBackground();
        if (bgDrawable != null){
            bgDrawable.draw(canvas);
        }else {
            canvas.drawColor(color);
        }
        rl.draw(canvas);
    }
}
