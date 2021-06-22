package com.example.BizQRd.activities;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.preference.PreferenceManager;

import com.example.BizQRd.R;
import com.example.BizQRd.model.BitmapConverter;
import com.example.BizQRd.model.QRCodeGenerator;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Objects;

import static android.content.pm.PackageManager.PERMISSION_GRANTED;
import static androidx.preference.PreferenceManager.setDefaultValues;
import static java.lang.Boolean.FALSE;


public class MainActivity extends AppCompatActivity {

    int CODE_GALLERY_REQUEST = 1;

    ImageButton help, settings, contacts, image, save;
    ImageView background, qrImage;
    TextView name;
    Uri imageUri;
    boolean[] settingsArray;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        settings = findViewById(R.id.settings);
        settings.setOnClickListener(v -> settingsActivity());

        help = findViewById(R.id.help);
        help.setOnClickListener(v -> helpActivity());

        save = findViewById(R.id.save);
        save.setOnClickListener(v -> getWritePermission());

        background = findViewById(R.id.background);
        image = findViewById(R.id.image);
        image.setOnClickListener(v -> getReadPermission());

        contacts = findViewById(R.id.contacts);
        contacts.setOnClickListener(v -> getContactPermission());

        qrImage = findViewById(R.id.qrCode);

        name = findViewById(R.id.name);

        setDefaultValues(this, R.xml.preferences, false);

        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        settingsArray = new boolean[11];
        settingsArray[0] = sharedPref.getBoolean(SettingsActivity.KEY_NAME_PREF, FALSE);
        settingsArray[1] = sharedPref.getBoolean(SettingsActivity.KEY_NAME_GIVEN, FALSE);
        settingsArray[2] = sharedPref.getBoolean(SettingsActivity.KEY_NAME_MIDDLE, FALSE);
        settingsArray[3] = sharedPref.getBoolean(SettingsActivity.KEY_NAME_FAMILY, FALSE);
        settingsArray[4] = sharedPref.getBoolean(SettingsActivity.KEY_NAME_SUFF, FALSE);
        settingsArray[5] = sharedPref.getBoolean(SettingsActivity.KEY_NICKNAME, FALSE);
        settingsArray[6] = sharedPref.getBoolean(SettingsActivity.KEY_ORG, FALSE);
        settingsArray[7] = sharedPref.getBoolean(SettingsActivity.KEY_DEPT, FALSE);
        settingsArray[8] = sharedPref.getBoolean(SettingsActivity.KEY_JOB, FALSE);
        settingsArray[9] = sharedPref.getBoolean(SettingsActivity.KEY_POSTAL, FALSE);
        settingsArray[10] = sharedPref.getBoolean(SettingsActivity.KEY_NOTES, FALSE);

    }

    private ActivityResultLauncher<String> contactRequestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    readContacts();
                } else {
                    Toast.makeText(MainActivity.this, "The app was not allowed to read your contacts", Toast.LENGTH_LONG).show();
                }
            });

    private void getContactPermission() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_CONTACTS) == PERMISSION_GRANTED) {
                readContacts();
            } else {
                contactRequestPermissionLauncher.launch(Manifest.permission.READ_CONTACTS);
            }
        } else readContacts();
    }

    private ActivityResultLauncher<String> readRequestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    chooseImage();
                } else {
                    Toast.makeText(MainActivity.this, "The app was not allowed to view your images", Toast.LENGTH_LONG).show();
                }
            });

    private void getReadPermission() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) == PERMISSION_GRANTED) {
                chooseImage();
            } else {
                readRequestPermissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE);
            }
        } else chooseImage();
    }

    private ActivityResultLauncher<String> writeRequestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    createDialog();
                } else {
                    Toast.makeText(MainActivity.this, "The app was not allowed to save your image", Toast.LENGTH_LONG).show();
                }
            });

    private void getWritePermission() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) == PERMISSION_GRANTED) {
                createDialog();
            } else {
                writeRequestPermissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE);
            }
        } else createDialog();
    }



    private void createDialog() {
        AlertDialog.Builder alertDlg = new AlertDialog.Builder(this);
        alertDlg.setMessage("Would you like to save this image?");
        alertDlg.setCancelable(false);

        alertDlg.setPositiveButton("Yes", (dialog, which) -> saveImageToGallery());

        alertDlg.setNegativeButton("No", (dialog, which) -> Toast.makeText(MainActivity.this, "The app did not save your image", Toast.LENGTH_LONG).show());
        alertDlg.create().show();
    }


    private void saveImageToGallery() {
//        RelativeLayout backgroundLayout = findViewById(R.id.backgroundLayout);
//        Bitmap bgArray[] = new Array[];
//        BitmapConverter bitmapConverter = new BitmapConverter(bg);
        long time = System.currentTimeMillis();
        Bitmap bitmap = getBackground();
        if (bitmap == null) {
            Toast.makeText(MainActivity.this, "Failed to save to gallery", Toast.LENGTH_LONG).show();
            return;
        }
        String filename = time + ".png";
        OutputStream imageOutStream = null;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ContentResolver resolver = getContentResolver();
            ContentValues contentValues = new ContentValues();
            contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, filename + ".jpg");
            contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "image/jpg");
            contentValues.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/BizQRd");
            Uri imageUri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues);
            try {
                imageOutStream = resolver.openOutputStream(Objects.requireNonNull(imageUri));
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        } else {
            String imagesDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES).toString();
            File image = new File(imagesDir, name + ".jpg");
            try {
                imageOutStream = new FileOutputStream(image);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }

        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, imageOutStream);
        if (imageOutStream != null) {
            try {
                imageOutStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        Toast.makeText(MainActivity.this, "Photo saved successfully", Toast.LENGTH_LONG).show();
    }

    private Bitmap getBackground() {
        RelativeLayout backgroundLayout = findViewById(R.id.backgroundLayout);
        CharSequence prevName = name.getText();
        name.setText("");
        Bitmap bgImage = Bitmap.createBitmap(backgroundLayout.getWidth(),backgroundLayout.getHeight(),Bitmap.Config.ARGB_8888);
        BitmapConverter bc = new BitmapConverter(bgImage, backgroundLayout, getResources().getColor(android.R.color.white));
        bc.start();
        try {
            bc.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        name.setText(prevName);
        return bgImage;
    }

    private void settingsActivity() {
        Intent settingsAct = new Intent(this, SettingsActivity.class);
        startActivity(settingsAct);
    }

    private void helpActivity() {
        Intent helpAct = new Intent(this, HelpActivity.class);
        startActivity(helpAct);
    }

    ActivityResultLauncher<Intent> readContactResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Intent data = result.getData();
                        if (data != null) {
                            Uri uriContact = data.getData();
                            try {
                                QRCodeGenerator qrCodeGenerator = new QRCodeGenerator(uriContact, MainActivity.this, settingsArray);
                                Bitmap qrCode = qrCodeGenerator.generateQRCode();
                                name.setText(qrCodeGenerator.getFirstLastName());
                                qrImage.setImageBitmap(Bitmap.createScaledBitmap(qrCode, 900, 900, false));
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
            }
    );

    ActivityResultLauncher<Intent> chooseImageResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Intent data = result.getData();
                        if (data != null) {
                            imageUri = data.getData();
                            background.setImageURI(imageUri);
                        }
                    }
                }
            }
    );

    private void readContacts() {
        Intent intent = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
        readContactResultLauncher.launch(intent);
    }

    private void chooseImage() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI);
        chooseImageResultLauncher.launch(intent);
    }
}
