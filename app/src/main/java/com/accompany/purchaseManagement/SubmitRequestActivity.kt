package com.accompany.purchaseManagement

import QuantityFragment
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale



class SubmitRequestActivity : AppCompatActivity() {

    private lateinit var purposeFragment: PurposeFragment
    private lateinit var quantityFragment: QuantityFragment
    private lateinit var equipmentNameFragment: EquipmentNameFragment
    private lateinit var noteFragment: NoteFragment
    private lateinit var locationFragment: LocationFragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_submit_request)

        // Fragment 초기화
        purposeFragment = PurposeFragment()
        quantityFragment = QuantityFragment()
        equipmentNameFragment = EquipmentNameFragment()
        noteFragment = NoteFragment()
        locationFragment = LocationFragment()

        // Fragment를 Activity에 추가 (각각 다른 container에 추가하는 것이 좋음)
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainerPurpose, purposeFragment)
            .replace(R.id.fragmentContainerQuantity, quantityFragment)
            .replace(R.id.fragmentContainerEquipmentName, equipmentNameFragment)
            .replace(R.id.fragmentContainerNote, noteFragment)
            .replace(R.id.fragmentContainerLocation, locationFragment)
            .commit()

        // 제출 버튼 클릭 시 처리
        val fabSubmit: FloatingActionButton = findViewById(R.id.fabSubmit)
        fabSubmit.setOnClickListener {
            submitRequest()
        }
    }

    // 입력된 데이터를 Google Sheets로 전송
    private fun submitRequest() {
        val purpose = purposeFragment.getPurpose()
        val quantity = quantityFragment.getQuantity()
        val equipmentName = equipmentNameFragment.getEquipmentName()
        val note = noteFragment.getNote()
        val location = locationFragment.getLocation()

        // 입력값이 비어있지 않다면
        if (purpose.isNotEmpty() && quantity.isNotEmpty() && equipmentName.isNotEmpty() && location.isNotEmpty()) {
            lifecycleScope.launch {
                val result = GoogleSheetsHelper(applicationContext).submitToGoogleSheets(
                    applicantName = "홍길동",
                    applicantDepartment = "농업부서",
                    equipmentName = equipmentName,
                    location = location,
                    purpose = purpose,
                    note = note,
                    requestDate = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.KOREA).format(Date()),
                    hasPhoto = false,
                    photoUrls = ""
                )

                if (result) {
                    Toast.makeText(applicationContext, "구매신청이 완료되었습니다.", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(applicationContext, "구매신청 실패", Toast.LENGTH_SHORT).show()
                }
            }
        } else {
            Toast.makeText(applicationContext, "모든 필드를 입력해주세요.", Toast.LENGTH_SHORT).show()
        }
    }
}
