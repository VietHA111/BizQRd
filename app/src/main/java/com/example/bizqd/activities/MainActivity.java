package com.example.bizqd.activities;

import android.Manifest;
import android.app.Activity;
import android.app.WallpaperManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.preference.PreferenceManager;

import com.example.bizqd.R;
import com.example.bizqd.model.QRCodeGenerator;

import net.glxn.qrgen.android.QRCode;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.HashMap;

import ezvcard.Ezvcard;
import ezvcard.VCard;
import ezvcard.VCardVersion;
import ezvcard.parameter.TelephoneType;

import static androidx.preference.PreferenceManager.setDefaultValues;
import static java.lang.Boolean.FALSE;


public class MainActivity extends AppCompatActivity {
    private static final String TAG = MainActivity.class.getSimpleName();

    int CODE_GALLERY_REQUEST = 1;
    int CODE_PICK_CONTACT = 2;
    int CODE_REQUEST_READ_CONTACT = 3;
    int CODE_REQUEST_SET_WALLPAPER = 4;

    ImageButton help, settings, contacts, image, save;
    ImageView background, qrImage;
    Uri imageUri;
    boolean[] settingsArray;

    private Uri uriContact;
    private Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        settings = findViewById(R.id.settings);
        settings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                settingsActivity();
            }
        });

        help = findViewById(R.id.help);
        help.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                helpActivity();
            }
        });

        save = findViewById(R.id.save);
        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveImage();
            }
        });

        background = findViewById(R.id.background);
        image = findViewById(R.id.image);
        image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                chooseImage();
            }
        });

        contacts = findViewById(R.id.contacts);
        contacts.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getContactPermission();
            }
        });

        qrImage = findViewById(R.id.qrCode);

        setDefaultValues(this, R.xml.preferences, false);

        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        settingsArray = new boolean[10];
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
    }


    private void getContactPermission() {
        mContext = MainActivity.this;
        if (Build.VERSION.SDK_INT >= 23) {
            String[] PERMISSIONS = {android.Manifest.permission.READ_CONTACTS};
            if (!hasPermissions(mContext, PERMISSIONS)) {
                ActivityCompat.requestPermissions((Activity) mContext, PERMISSIONS, CODE_REQUEST_READ_CONTACT );
            } else {
                readContacts();
            }
        } else {
            readContacts();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case 3: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    readContacts();
                } else {
                    Toast.makeText(mContext, "The app was not allowed to read your contact", Toast.LENGTH_LONG).show();
                }
            }
        }
    }

    private static boolean hasPermissions(Context context, String... permissions) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && context != null && permissions != null) {
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }

    private void saveImage() {
        Bitmap bg = getBackground();

        WallpaperManager wallpaperManager = WallpaperManager.getInstance(this);

        try {
            wallpaperManager.setBitmap(bg);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private Bitmap getBackground() {
        RelativeLayout backgroundLayout = findViewById(R.id.backgroundLayout);
        Bitmap bgImage = Bitmap.createBitmap(backgroundLayout.getWidth(),backgroundLayout.getHeight(),Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bgImage);
        Drawable bgDrawable = backgroundLayout.getBackground();
        if (bgDrawable != null){
            bgDrawable.draw(canvas);
        }else {
            canvas.drawColor(getResources().getColor(android.R.color.white));
        }
        backgroundLayout.draw(canvas);
        return bgImage;
    }

    private void readContacts() {
        startActivityForResult(new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI), CODE_PICK_CONTACT);
    }

    private void settingsActivity() {
        Intent settingsAct = new Intent(this, SettingsActivity.class);
        startActivity(settingsAct);
    }

    private void helpActivity() {
        Intent helpAct = new Intent(this, HelpActivity.class);
        startActivity(helpAct);
    }

    private void chooseImage() {
        Intent gallery = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI);
        startActivityForResult(gallery, CODE_GALLERY_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK)
            if (requestCode == CODE_GALLERY_REQUEST) {
                imageUri = data.getData();
                background.setImageURI(imageUri);
            }
        if (requestCode == CODE_PICK_CONTACT) {
            if (data != null) {
                uriContact = data.getData();
                QRCodeGenerator qrCodeGen = new QRCodeGenerator(uriContact, mContext, settingsArray);
                Bitmap qrCode = qrCodeGen.generateQRCode();
                qrImage.setImageBitmap(qrCode.createScaledBitmap(qrCode, 700, 700, false));
            }
        }
    }


}
