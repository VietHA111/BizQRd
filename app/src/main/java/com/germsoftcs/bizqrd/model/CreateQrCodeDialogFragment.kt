package com.germsoftcs.bizqrd.model
//
//import android.app.AlertDialog
//import android.app.Dialog
//import android.content.DialogInterface
//import android.os.Bundle
//import androidx.fragment.app.DialogFragment
//
//class CreateQrCodeDialogFragment : DialogFragment() {
//    override fun onCreateDialog(savedInstanceState: Bundle): Dialog {
//        return activity?.let {
//            // Use the Builder class for convenient dialog construction
//            val builder = AlertDialog.Builder(it)
//            builder.setMessage(R.string.dialog_fire_missiles)
//                .setPositiveButton(R.string.fire,
//                    DialogInterface.OnClickListener { dialog, id ->
//                        // FIRE ZE MISSILES!
//                    })
//                .setNegativeButton(R.string.cancel,
//                    DialogInterface.OnClickListener { dialog, id ->
//                        // User cancelled the dialog
//                    })
//            // Create the AlertDialog object and return it
//            builder.create()
//        } ?: throw IllegalStateException("Activity cannot be null")
//    }
//}