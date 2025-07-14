package com.accompany.purchaseManagement

import android.net.Uri
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class PurchaseViewModel : ViewModel() {
    val equipmentName = MutableLiveData<String>("")
    val quantity = MutableLiveData<String>("1")
    val location = MutableLiveData<String>("")
    val purpose = MutableLiveData<String>("")
    val note = MutableLiveData<String>("")
    val photoUri = MutableLiveData<List<Uri>>(emptyList())

    // 유효성 검사 메서드들
    fun isEquipmentNameValid(): Boolean = !equipmentName.value.isNullOrEmpty()

    fun isQuantityValid(): Boolean {
        val qty = quantity.value?.toIntOrNull()
        return qty != null && qty > 0
    }

    fun isPurposeValid(): Boolean = !purpose.value.isNullOrEmpty()
}