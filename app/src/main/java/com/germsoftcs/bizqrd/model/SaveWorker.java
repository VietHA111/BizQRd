//package com.germsoftcs.bizqrd.model;
//
//import android.app.Activity;
//import android.content.ContentResolver;
//import android.content.ContentValues;
//import android.content.Context;
//import android.graphics.Bitmap;
//import android.net.Uri;
//import android.os.Build;
//import android.os.Environment;
//import android.provider.MediaStore;
//import android.widget.Toast;
//
//import androidx.annotation.NonNull;
//import androidx.work.Worker;
//import androidx.work.WorkerParameters;
//
//import com.germsoftcs.bizqrd.activities.MainActivity;
//
//import java.io.File;
//import java.io.FileNotFoundException;
//import java.io.FileOutputStream;
//import java.io.IOException;
//import java.io.OutputStream;
//import java.util.Objects;
// TODO: Implement SaveWorker
//public class SaveWorker extends Worker {
//
//    public SaveWorker(Context mContext, WorkerParameters params) {
//        super(mContext, params);
//    }
//
//    @NonNull
//    @Override
//    public Result doWork() {
//        long time = System.currentTimeMillis();
//        MainActivity mContext = MainActivity.getMContext();
//
//        Bitmap bm = MainActivity.getBm();
//
//        if (bm == null) {
//            Toast.makeText(mContext, "Failed to save to gallery", Toast.LENGTH_LONG).show();
//            return Result.failure();
//        }
//
//        String filename = time + ".png";
//        OutputStream imageOutStream = null;
//
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
//            ContentResolver resolver = mContext.getContentResolver();
//            ContentValues contentValues = new ContentValues();
//            contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, filename + ".jpg");
//            contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "image/jpg");
//            contentValues.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/BizQRd");
//            Uri imageUri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues);
//            try {
//                imageOutStream = resolver.openOutputStream(Objects.requireNonNull(imageUri));
//            } catch (FileNotFoundException e) {
//                e.printStackTrace();
//                return Result.failure();
//            }
//        } else {
//            String imagesDir = mContext.getExternalFilesDir(Environment.DIRECTORY_PICTURES).toString();
//            File image = new File(imagesDir, filename + ".jpg");
//            try {
//                imageOutStream = new FileOutputStream(image);
//            } catch (FileNotFoundException e) {
//                e.printStackTrace();
//                return Result.failure();
//            }
//        }
//
//        bm.compress(Bitmap.CompressFormat.JPEG, 100, imageOutStream);
//        if (imageOutStream != null) {
//            try {
//                imageOutStream.close();
//            } catch (IOException e) {
//                e.printStackTrace();
//                return Result.failure();
//            }
//        }
////        Toast.makeText(mContext, "Photo saved successfully", Toast.LENGTH_LONG).show();
//
//        return Result.success();
//    }
//}
