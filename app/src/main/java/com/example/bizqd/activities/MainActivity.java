package com.example.bizqd.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.WallpaperManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.preference.PreferenceManager;

import com.example.bizqd.R;
import com.example.bizqd.model.QRCodeGenerator;
import com.example.bizqd.model.VCardGenerator;

import java.io.IOException;

import static androidx.preference.PreferenceManager.setDefaultValues;
import static java.lang.Boolean.FALSE;


public class MainActivity extends AppCompatActivity {

    int CODE_GALLERY_REQUEST = 1;
    int CODE_PICK_CONTACT = 2;
    int CODE_REQUEST_READ_CONTACT = 3;

    ImageButton help, settings, contacts, image, save;
    ImageView background, qrImage;
    TextView name;
    Uri imageUri;
    boolean[] settingsArray;

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
                createDialog();
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

        name = (TextView) findViewById(R.id.name);

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

    private void createDialog() {
        AlertDialog.Builder alertDlg = new AlertDialog.Builder(this);
        alertDlg.setMessage("Would you like to set this image as your background?");
        alertDlg.setCancelable(false);

        alertDlg.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                saveImage();
            }
        });

        alertDlg.setNegativeButton("No", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        alertDlg.create().show();
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
        CharSequence prevName = name.getText();
        name.setText("");
        Bitmap bgImage = Bitmap.createBitmap(backgroundLayout.getWidth(),backgroundLayout.getHeight(),Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bgImage);
        Drawable bgDrawable = backgroundLayout.getBackground();
        if (bgDrawable != null){
            bgDrawable.draw(canvas);
        }else {
            canvas.drawColor(getResources().getColor(android.R.color.white));
        }
        backgroundLayout.draw(canvas);
        name.setText(prevName);
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
                Uri uriContact = data.getData();
                try {
                    QRCodeGenerator qrCodeGenerator = new QRCodeGenerator(uriContact, mContext, settingsArray);
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
