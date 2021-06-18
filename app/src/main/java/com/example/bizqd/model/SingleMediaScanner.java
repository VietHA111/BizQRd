package com.example.bizqd.model;

import android.media.MediaScannerConnection;
import android.net.Uri;
import android.content.Context;

import java.io.File;

public class SingleMediaScanner implements MediaScannerConnection.MediaScannerConnectionClient {

    private MediaScannerConnection mMs;
    private File mFile;

    public SingleMediaScanner(Context mContext, File f) {
        mFile = f;
        mMs = new MediaScannerConnection(mContext, this);
        mMs.connect();
    }

    @Override
    public void onMediaScannerConnected() {
        mMs.scanFile(mFile.getAbsolutePath(), null);
    }

    @Override
    public void onScanCompleted(String path, Uri uri) {

    }
}
