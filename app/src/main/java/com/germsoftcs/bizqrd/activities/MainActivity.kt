package com.germsoftcs.bizqrd.activities

import android.Manifest
import android.app.AlertDialog
import android.content.ContentValues
import android.content.DialogInterface
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.ImageDecoder.createSource
import android.graphics.ImageDecoder.decodeBitmap
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.ContactsContract
import android.provider.MediaStore
import android.provider.MediaStore.Images.Media.getBitmap
import android.util.Log
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts.RequestPermission
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.preference.PreferenceManager
import com.germsoftcs.bizqrd.R
import com.germsoftcs.bizqrd.model.*
import com.google.zxing.ChecksumException
import com.google.zxing.FormatException
import com.google.zxing.NotFoundException
import kotlinx.coroutines.*
import java.io.*
import java.util.*
import kotlin.math.roundToInt

class MainActivity : AppCompatActivity() {
    private val viewModelScope: CoroutineScope = CoroutineScope(Dispatchers.IO)
    private lateinit var pref: SharedPreferences
    private lateinit var contact: Contact
    private lateinit var help: ImageButton
    private lateinit var settings: ImageButton
    private lateinit var createButton: ImageButton
    private lateinit var image: ImageButton
    private lateinit var save: ImageButton
    private lateinit var background: ImageView
    private lateinit var qrImage: ImageView
    private lateinit var name: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        pref = PreferenceManager.getDefaultSharedPreferences(this)
        settings = findViewById(R.id.settings)
        settings.setOnClickListener { settingsActivity() }
        help = findViewById(R.id.help)
        help.setOnClickListener { helpActivity() }
        save = findViewById(R.id.save)
        save.setOnClickListener { if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            writePermission
            } else {
                createDialog()
            }
        }
        background = findViewById(R.id.background)
        image = findViewById(R.id.image)
        image.setOnClickListener { chooseImage() }
        createButton = findViewById(R.id.create_button)
        if (pref.getString(SettingsActivity.KEY_QR_TYPE, "") == "Contact") {
            createButton.setImageResource(R.drawable.icons8_person_48)
        } else if (pref.getString(SettingsActivity.KEY_QR_TYPE, "") == "Scan from image") {
            createButton.setImageResource(R.drawable.icon_qrcode)
        }
        createButton.setOnClickListener { callPermission }
        qrImage = findViewById(R.id.qrCode)
        name = findViewById(R.id.name)

        if (qrUri != null && pref.getString(SettingsActivity.KEY_QR_TYPE, "") == "Contact") {
            try {
                viewModelScope.launch(Dispatchers.Unconfined) {
                    createContactQrCode()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        if (qrUri != null && pref.getString(SettingsActivity.KEY_QR_TYPE, "") == "Scan from image") {
            scanImageQrCode()
        }
        if (imageUri != null) {
            background.setImageURI(imageUri)
        }
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false)
        Log.i(TAG, "Called ViewModelProvider.get")
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

    //EFFECTS: ask for appropriate permission,
    //         if already granted, call appropriate qr generation method
    private val callPermission: Unit
        get() {
            if (PreferenceManager.getDefaultSharedPreferences(this@MainActivity)
                    .getString(SettingsActivity.KEY_QR_TYPE, null)
                    .equals("Contact")) {
                if (ContextCompat.checkSelfPermission(
                        this@MainActivity,
                        Manifest.permission.READ_CONTACTS
                    ) == PackageManager.PERMISSION_GRANTED
                ) {
                    readContacts()
                } else {
                    contactRequestPermissionLauncher.launch(Manifest.permission.READ_CONTACTS)
                }
            } else if (PreferenceManager.getDefaultSharedPreferences(this@MainActivity)
                    .getString(SettingsActivity.KEY_QR_TYPE, null)
                    .equals("Scan from image")) {
                //NO PERMISSION NEEDED FOR GALLERY ACCESS
                scanImage()
            }
        }

    //EFFECTS: if write permission is granted, createDialog(),
    //         else show Toast
    private val writeRequestPermissionLauncher = registerForActivityResult(RequestPermission()) { isGranted: Boolean ->
        if (isGranted) {
            createDialog()
        } else {
            Toast.makeText(this@MainActivity, "The app was not allowed to save images", Toast.LENGTH_LONG).show()
        }
    }

    //EFFECTS: ask for write permission
    //         if write permission is granted, createDialog()
    private val writePermission: Unit
        get() {
            if (ContextCompat.checkSelfPermission(
                    this@MainActivity,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                createDialog()
            } else {
                writeRequestPermissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            }
        }

    //EFFECTS: ask user if they would like to save the image,
    //         show Toast reflecting user's decision and the outcome
    private fun createDialog() {

        val alertDlg = AlertDialog.Builder(this)
        alertDlg.setMessage("Would you like to save this image?")
        alertDlg.setCancelable(false)
        alertDlg.setPositiveButton("Yes") { _: DialogInterface?, _: Int -> saveImageToGallery() }
        alertDlg.setNegativeButton("No") { _: DialogInterface?, _: Int -> Toast.makeText(this@MainActivity, "The app did not save your image", Toast.LENGTH_LONG).show() }
        alertDlg.create().show()
    }

    //EFFECTS: save image to gallery,
    //         show Toast reflecting method outcome
    private fun saveImageToGallery() {
//        RelativeLayout backgroundLayout = findViewById(R.id.backgroundLayout);
//        Bitmap bgArray[] = new Array[];
//        BitmapConverter bitmapConverter = new BitmapConverter(bg);



        viewModelScope.launch(Dispatchers.Unconfined) {
            var bitmap : Bitmap
            val prevName = name.text
            name.text = ""

            withContext(Dispatchers.IO) {
                bitmap = getBackground()
            }

            withContext(Dispatchers.Main) {
                name.text = prevName
            }

            withContext(Dispatchers.IO) {
                val time = System.currentTimeMillis()
                val filename = "$time"
                var imageOutStream: OutputStream? = null

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    val resolver = contentResolver
                    val contentValues = ContentValues()
                    contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, "$filename.jpg")
                    contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "image/jpg")
                    contentValues.put(
                        MediaStore.MediaColumns.RELATIVE_PATH,
                        Environment.DIRECTORY_PICTURES + "/BizQRd"
                    )
                    val imageUri = resolver.insert(
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                        contentValues
                    ) ?: throw IOException("Failed to create new MediaStore record.")

                    try {
                        imageOutStream = resolver.openOutputStream(Objects.requireNonNull(imageUri))

                    } catch (e: FileNotFoundException) {
                        e.printStackTrace()
                    }
                } else {
                    val imagesDir = File(Environment.getExternalStoragePublicDirectory(
                        Environment.DIRECTORY_PICTURES).toString() + File.separator + "BizQRd"
                    )
                    if (!imagesDir.exists()) {
                        imagesDir.mkdir()
                    }
                    val image = File(imagesDir, "$filename.jpg")
                    try {
                        imageOutStream = FileOutputStream(image)
                    } catch (e: FileNotFoundException) {
                        e.printStackTrace()
                        Log.e(TAG, "imageOutStream FAILING")
                    }
                }


                if (imageOutStream != null) {
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, imageOutStream)
                    try {
                        imageOutStream.close()
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@MainActivity, "Photo saved successfully", Toast.LENGTH_LONG)
                            .show()
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@MainActivity, "Photo could not be saved", Toast.LENGTH_LONG)
                            .show()
                    }
                }
            }

        }

    }

    //EFFECTS: return bitmap of background image
    private fun getBackground(): Bitmap {
        val backgroundLayout = findViewById<RelativeLayout>(R.id.backgroundLayout)

        val bgImage = Bitmap.createBitmap(backgroundLayout.width, backgroundLayout.height, Bitmap.Config.ARGB_8888)
        val bc = BitmapConverter(bgImage, backgroundLayout, ContextCompat.getColor(this, R.color.white))
        bc.start()
        try {
            bc.join()
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }

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
    //         set qrImage to generated QR code
    @Throws(Exception::class)
    private fun createContactQrCode() {

        var newQrCode : Bitmap

        viewModelScope.launch(Dispatchers.Unconfined) {
            withContext(Dispatchers.IO) {
                contact = Contact(qrUri, this@MainActivity)
                newQrCode = contact.generateQRCode()
            }

            withContext(Dispatchers.Main) {
                name.text = contact.firstLastName
                qrImage.setImageBitmap(Bitmap.createScaledBitmap(newQrCode, QRCodeGenerator.QR_CODE_HEIGHT, QRCodeGenerator.QR_CODE_WIDTH, false))
                addPaddingQrImage()
            }
        }
    }

    //EFFECTS: scans image for QR code
    //         set qrImage to scanned QR code
    private fun scanImageQrCode() {
        viewModelScope.launch(Dispatchers.Unconfined) {
            try {
                var qrCode: Bitmap?
                withContext(Dispatchers.IO) {
                    val bitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                        decodeBitmap(createSource(contentResolver, qrUri!!)).copy(Bitmap.Config.RGB_565, true)
                    } else {
                        getBitmap(contentResolver, qrUri!!)
                    }
                    qrCode = QrScanner.scan(bitmap)
                }
                withContext(Dispatchers.Main) {
                    qrImage.setImageBitmap(qrCode)
                    addPaddingQrImage()
                }
            } catch (e: NotFoundException) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        this@MainActivity,
                        "Could not find a QR code",
                        Toast.LENGTH_LONG
                    ).show()
                }
                e.message?.let { Log.e(TAG, it)}
            } catch (e: FormatException) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        this@MainActivity,
                        "Could not decode the QR code",
                        Toast.LENGTH_LONG
                    ).show()
                }
                e.message?.let { Log.e(TAG, it) }
            } catch (e: ChecksumException) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        this@MainActivity,
                        "Scan failed",
                        Toast.LENGTH_LONG
                    ).show()
                }
                e.message?.let { Log.e(TAG, it) }
            }
        }
    }

    //EFFECTS: create QR code for picked contact and display
    private val readContactResultLauncher = registerForActivityResult(
            StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            val data = result.data
            if (data != null) {
                qrUri = data.data
                try {
                    createContactQrCode()
                } catch (e: Exception) {
                    e.printStackTrace()
                    Toast.makeText(
                        this@MainActivity,
                        "Failed to retrieve contact info",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }

    //EFFECTS: scan image for qr code and add to screen
    private val scanImageResultLauncher = registerForActivityResult(
        StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            val data = result.data
            if (data != null) {
                qrUri = data.data
                this@MainActivity.contentResolver.takePersistableUriPermission(qrUri!!, Intent.FLAG_GRANT_READ_URI_PERMISSION)
                scanImageQrCode()
            }
        }

    }

    //EFFECTS: set background as chosen image
    private val chooseImageResultLauncher = registerForActivityResult(
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

    private fun scanImage() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        intent.type = "image/*"
        scanImageResultLauncher.launch(intent)
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

    private fun addPaddingQrImage() {
        val scale = resources.displayMetrics.density
        val dpAsPixels: Int = (20 * scale).roundToInt()
        qrImage.setPadding(dpAsPixels, dpAsPixels, dpAsPixels, dpAsPixels)
    }

    companion object {
        private const val TAG = "MainActivityTag"
        private var qrUri: Uri? = null
        private var imageUri: Uri? = null

        fun reset() {
            qrUri = null
            imageUri = null
        }
    }
}