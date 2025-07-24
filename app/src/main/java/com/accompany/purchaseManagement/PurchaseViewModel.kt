package com.accompany.purchaseManagement

import android.net.Uri
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class PurchaseViewModel : ViewModel() {
    val equipmentName = MutableLiveData<String>("")
    val quantity = MutableLiveData<String>("1")  // 기본 "1"
    val photoUri = MutableLiveData<List<Uri>>(emptyList())

}