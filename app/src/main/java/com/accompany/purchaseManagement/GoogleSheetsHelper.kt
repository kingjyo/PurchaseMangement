package com.accompany.purchaseManagement

import android.content.Context
import android.util.Log
import kotlinx.coroutines.*
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.create
import java.net.URLEncoder
import java.text.SimpleDateFormat
import java.util.*

class GoogleSheetsHelper(private val context: Context) {

    companion object {
        private const val TAG = "GoogleSheetsHelper"
        private const val BASE_URL = "https://script.google.com/macros/s/AKfycbw9wp9dk_pdcwJHK8Im1n9db--dNu8lqSO9IQzZa1edlIJXOGyMa4HWs3pCBABRM3NVLA/exec/"
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
        quantity: String,
        location: String,
        purpose: String,
        note: String,
        requestDate: String,
        hasPhoto: Boolean = false,  // 기본값은 false
        photoUrls: String = ""
    ): Boolean = withContext(Dispatchers.IO) {
        // POST 데이터 준비
        val postData = hashMapOf<String, String>(
            "applicantName" to (applicantName.ifEmpty { "미설정" }),
            "applicantDepartment" to (applicantDepartment.ifEmpty { "미설정" }),
            "equipmentName" to (equipmentName.ifEmpty { "미설정" }),
            "quantity" to quantity,
            "location" to (location.ifEmpty { "미설정" }),
            "purpose" to (purpose.ifEmpty { "미설정" }),
            "note" to (note.ifEmpty { "없음" }),
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
                quantity = quantity,  // 수량
                location = location,
                purpose = purpose,
                note = note,
                status = "대기중",
                photoUrls = photoUrls
            ).execute()

            Log.d("Submit", "API Request - purpose: $purpose")

            if (response.isSuccessful) {
                Log.i("GoogleSheetsHelper", "Google Sheets에 데이터 전송 성공")
                return@withContext true
            } else {
                Log.e("GoogleSheetsHelper", "Google Sheets 전송 실패: ${response.code()} - ${response.message()}")
                Log.e("GoogleSheetsHelper", "Response Body: ${response.body()}")
                return@withContext false
            }
        } catch (e: Exception) {
            Log.e("GoogleSheetsHelper", "Google Sheets 전송 중 오류: ${e.message}")
            return@withContext false
        }
    }
}



