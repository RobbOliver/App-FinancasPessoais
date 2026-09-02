package com.robson.financas.ui.fiscal.scanning

import androidx.lifecycle.ViewModel
import com.robson.financas.util.NetworkAvailabilityChecker
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class QrScannerViewModel @Inject constructor(
    private val networkAvailabilityChecker: NetworkAvailabilityChecker,
) : ViewModel() {
    fun isConnected(): Boolean = networkAvailabilityChecker.isConnected()
}
