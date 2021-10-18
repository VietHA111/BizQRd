package com.germsoftcs.bizqrd.model

import android.util.Log
import androidx.lifecycle.ViewModel

class InfoViewModel : ViewModel() {
    private val TAG = "InfoViewModel"

    override fun onCleared() {
        super.onCleared()
        Log.i(TAG, "$InfoViewModel destroyed")
    }

    companion object {

    }

    init {
        Log.i(TAG, "InfoViewModel initialized")
    }
}