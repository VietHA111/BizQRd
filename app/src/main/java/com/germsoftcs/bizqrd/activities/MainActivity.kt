package com.germsoftcs.bizqrd.activities

import android.Manifest
import androidx.appcompat.app.AppCompatActivity
import com.germsoftcs.bizqrd.model.Contact
import android.os.Bundle
import com.germsoftcs.bizqrd.R
import com.germsoftcs.bizqrd.activities.MainActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts.RequestPermission
import androidx.activity.result.ActivityResultCallback
import android.os.Build
import androidx.core.content.ContextCompat
import android.content.pm.PackageManager
import android.content.DialogInterface
import android.graphics.Bitmap
import android.content.ContentResolver
import android.content.ContentValues
import android.provider.MediaStore
import com.germsoftcs.bizqrd.model.BitmapConverter
import android.content.Intent
import com.germsoftcs.bizqrd.activities.SettingsActivity
import com.germsoftcs.bizqrd.activities.HelpActivity
import kotlin.Throws
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import android.app.Activity
import android.app.AlertDialog
import android.net.Uri
import android.os.Environment
import android.provider.ContactsContract
import android.util.Log
import android.view.View
import android.widget.*
import androidx.preference.PreferenceManager
import kotlinx.coroutines.*
import java.io.*
import java.lang.Exception
import java.util.*

class MainActivity : AppCompatActivity() {
    private val viewModelScope: CoroutineScope = CoroutineScope(Dispatchers.IO)
    private lateinit var contact: Contact
    private lateinit var help: ImageButton
    private lateinit var settings: ImageButton
    private lateinit var contacts: ImageButton
    private lateinit var image: ImageButton
    private lateinit var save: ImageButton
    private lateinit var background: ImageView
    private lateinit var qrImage: ImageView
    private lateinit var name: TextView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        settings = findViewById(R.id.settings)
        settings.setOnClickListener { settingsActivity() }
        help = findViewById(R.id.help)
        help.setOnClickListener { helpActivity() }
        save = findViewById(R.id.save)
        save.setOnClickListener { createDialog() }
        background = findViewById(R.id.background)
        image = findViewById(R.id.image)
        image.setOnClickListener { chooseImage() }
        contacts = findViewById(R.id.contacts)
        contacts.setOnClickListener { contactPermission }
        qrImage = findViewById(R.id.qrCode)
        name = findViewById(R.id.name)
        if (contactUri != null) {
            try {
                contact = Contact(contactUri, this@MainActivity)
                createQRCode()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        if (imageUri != null) {
            background.setImageURI(imageUri)
        }
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false)
    }

    //EFFECTS: if read contacts permission is granted, read contacts,
    //         else show Toast
    private val contactRequestPermissionLauncher = registerForActivityResult(RequestPermission()) { isGranted: Boolean ->
        if (isGranted) {
            readContacts()
        } else {
            Toast.makeText(this@MainActivity, "The app was not allowed to read your contacts", Toast.LENGTH_LONG).show()
        }
    }

    //EFFECTS: ask for read contacts permission,
    //         if already granted, read contacts
    private val contactPermission: Unit
        get() {
            if (Build.VERSION.SDK_INT >= 23) {
                if (ContextCompat.checkSelfPermission(this@MainActivity, Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED) {
                    readContacts()
                } else {
                    contactRequestPermissionLauncher.launch(Manifest.permission.READ_CONTACTS)
                }
            } else readContacts()
        }

    //EFFECTS: ask user if they would like to save the image,
    //         show Toast reflecting user's decision and the outcome
    private fun createDialog() {
        val alertDlg = AlertDialog.Builder(this)
        alertDlg.setMessage("Would you like to save this image?")
        alertDlg.setCancelable(false)
        alertDlg.setPositiveButton("Yes") { dialog: DialogInterface?, which: Int -> saveImageToGallery() }
        alertDlg.setNegativeButton("No") { dialog: DialogInterface?, which: Int -> Toast.makeText(this@MainActivity, "The app did not save your image", Toast.LENGTH_LONG).show() }
        alertDlg.create().show()
    }

    //EFFECTS: save image to gallery,
    //         show Toast reflecting method outcome
    private fun saveImageToGallery() {
//        RelativeLayout backgroundLayout = findViewById(R.id.backgroundLayout);
//        Bitmap bgArray[] = new Array[];
//        BitmapConverter bitmapConverter = new BitmapConverter(bg);
        val time = System.currentTimeMillis()
        val bitmap = getBackground()
        val filename = "$time.png"
        var imageOutStream: OutputStream? = null
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val resolver = contentResolver
            val contentValues = ContentValues()
            contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, "$filename.jpg")
            contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "image/jpg")
            contentValues.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/BizQRd")
            val imageUri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)!!
            try {
                imageOutStream = resolver.openOutputStream(Objects.requireNonNull(imageUri))
            } catch (e: FileNotFoundException) {
                e.printStackTrace()
            }
        } else {
            val imagesDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES).toString()
            val image = File(imagesDir, name.toString() + ".jpg")
            try {
                imageOutStream = FileOutputStream(image)
            } catch (e: FileNotFoundException) {
                e.printStackTrace()
            }
        }
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, imageOutStream)
        if (imageOutStream != null) {
            try {
                imageOutStream.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
        Toast.makeText(this@MainActivity, "Photo saved successfully", Toast.LENGTH_LONG).show()
    }

    //EFFECTS: return bitmap of background image
    private fun getBackground(): Bitmap {
        val backgroundLayout = findViewById<RelativeLayout>(R.id.backgroundLayout)
        val prevName = name.text
        name.text = ""
        val bgImage = Bitmap.createBitmap(backgroundLayout.width, backgroundLayout.height, Bitmap.Config.ARGB_8888)
        val bc = BitmapConverter(bgImage, backgroundLayout, ContextCompat.getColor(this, R.color.white))
        bc.start()
        try {
            bc.join()
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }
        name.text = prevName
        return bgImage
    }

    //EFFECTS: go to SettingsActivity
    private fun settingsActivity() {
        val settingsAct = Intent(this, SettingsActivity::class.java)
        startActivity(settingsAct)
    }

    //EFFECTS: go to HelpActivity
    private fun helpActivity() {
        val helpAct = Intent(this, HelpActivity::class.java)
        startActivity(helpAct)
    }

    //EFFECTS: create QR code
    @Throws(Exception::class)
    private fun createQRCode() : Bitmap {
        return contact.generateQRCode()
    }

    //EFFECTS: create QR code for picked contact and display
    private val readContactResultLauncher = registerForActivityResult(
            StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            val data = result.data
            if (data != null) {
                var newQrCode: Bitmap?
                viewModelScope.launch(Dispatchers.Unconfined) {
                    contactUri = data.data
                    try {
                        withContext (Dispatchers.IO) {
                            contact = Contact(contactUri, this@MainActivity)
                            newQrCode = createQRCode()
                        }
                        withContext(Dispatchers.Main) {
                            name.text = contact.firstLastName
                            qrImage.setImageBitmap(Bitmap.createScaledBitmap(requireNotNull(newQrCode), 900, 900, false))
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                        withContext(Dispatchers.Main) {
                            Toast.makeText(
                                this@MainActivity,
                                "Failed to retrieve contact info",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }
                }
            }
        }
    }

    //EFFECTS: set background as chosen image
    val chooseImageResultLauncher = registerForActivityResult(
            StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            val data = result.data
            if (data != null) {
                imageUri = data.data
                this@MainActivity.contentResolver.takePersistableUriPermission(imageUri!!, Intent.FLAG_GRANT_READ_URI_PERMISSION)
                background.setImageURI(imageUri)
            }
        }
    }

    //EFFECTS: let user pick a contact from contacts list,
    //         launch readContactResultLauncher
    private fun readContacts() {
        val intent = Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI)
        readContactResultLauncher.launch(intent)
    }

    //EFFECTS: let user pick an image from gallery,
    //         launch chooseImageResultLauncher
    private fun chooseImage() {
        //Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI);
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        intent.type = "image/*"
        chooseImageResultLauncher.launch(intent)
    }

    companion object {
        private const val TAG = "MainActivityTag"
        private var contactUri: Uri? = null
        private var imageUri: Uri? = null
    }
}