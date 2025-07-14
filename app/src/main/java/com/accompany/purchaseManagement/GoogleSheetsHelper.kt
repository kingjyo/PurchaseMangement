package com.accompany.purchaseManagement

import android.content.Context
import android.util.Log
import kotlinx.coroutines.*
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import com.accompany.purchaseManagement.api.GoogleSheetsApi
import retrofit2.create
import java.net.URLEncoder
import java.text.SimpleDateFormat
import java.util.*

class GoogleSheetsHelper(private val context: Context) {

    companion object {
        private const val TAG = "GoogleSheetsHelper"
        private const val BASE_URL = "https://script.google.com/macros/s/AKfycbxqugzxUsgEz3rEjqKVtOkZb7vau1dS0O0Ec8H6Xc4HAorzOtaAbP_2o4ELYdRX32GTsQ/exec/"
    }

    // Retrofit 초기화
    private val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private val api: GoogleSheetsApi = retrofit.create()

    // Google Sheets에 구매신청 데이터 전송
    suspend fun submitToGoogleSheets(
        applicantName: String,
        applicantDepartment: String,
        equipmentName: String,
        location: String,
        purpose: String,
        note: String,
        requestDate: String,
        hasPhoto: Boolean = false,  // 기본값은 false
        photoUrls: String = ""
    ): Boolean = withContext(Dispatchers.IO) {
        // POST 데이터 준비
        val postData = hashMapOf<String, String>(
            "applicantName" to applicantName,
            "applicantDepartment" to applicantDepartment,
            "equipmentName" to equipmentName,
            "location" to location,
            "purpose" to purpose,
            "note" to note,
            "requestDate" to requestDate,
            "status" to "대기중",
            "hasPhoto" to if (hasPhoto) "📸 있음" else "없음",
            "photoUrls" to photoUrls
        )

        try {
            // Retrofit을 통해 API 호출
            val response = api.addPurchaseRequest(
                requestTime = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.KOREA).format(Date()),
                applicantName = applicantName,
                department = applicantDepartment,
                equipmentName = equipmentName,
                quantity = "1",  // 수량을 처리하는 부분이 없으므로 기본값으로 설정
                location = location,
                purpose = purpose,
                note = note,
                status = "대기중",
                photoUrls = photoUrls
            ).execute()

            if (response.isSuccessful) {
                Log.i(TAG, "Google Sheets에 데이터 전송 성공")
                return@withContext true
            } else {
                Log.e(TAG, "Google Sheets 전송 실패: ${response.code()}")
                return@withContext false
            }
        } catch (e: Exception) {
            Log.e(TAG, "Google Sheets 전송 중 오류: ${e.message}", e)
            return@withContext false
        }
    }
}
