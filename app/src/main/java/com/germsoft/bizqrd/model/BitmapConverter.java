package com.germsoft.bizqrd.model;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.widget.RelativeLayout;

public class BitmapConverter extends Thread {
    final Bitmap bm;
    final RelativeLayout rl;
    final int color;

    public BitmapConverter(Bitmap bm, RelativeLayout rl, int color) {
        this.bm = bm;
        this.rl = rl;
        this.color = color;
    }

    //MODIFIES: this
    //EFFECTS: draw RelativeLayout rl onto Bitmap bm
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
