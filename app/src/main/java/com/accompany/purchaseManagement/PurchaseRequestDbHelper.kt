package com.accompany.purchaseManagement

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import java.text.SimpleDateFormat
import java.util.*
import com.google.gson.annotations.SerializedName

data class PurchaseRequest(
    val id: Long = 0,
    @SerializedName("신청자명") val applicantName: String,
    @SerializedName("소속") val applicantDepartment: String,
    @SerializedName("장비명") val equipmentName: String,
    @SerializedName("장소") val location: String,
    @SerializedName("용도") val purpose: String,
    @SerializedName("기타사항") val note: String,
    @SerializedName("신청일시") val requestDate: String,
    @SerializedName("상태") val status: String,
)


class PurchaseRequestDbHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        const val DATABASE_VERSION = 1
        const val DATABASE_NAME = "PurchaseRequest.db"
        const val TABLE_NAME = "purchase_requests"

        const val COLUMN_ID = "id"
        const val COLUMN_APPLICANT_NAME = "applicant_name"
        const val COLUMN_APPLICANT_DEPARTMENT = "applicant_department"
        const val COLUMN_EQUIPMENT_NAME = "equipment_name"
        const val COLUMN_LOCATION = "location"
        const val COLUMN_PURPOSE = "purpose"
        const val COLUMN_NOTE = "note"
        const val COLUMN_REQUEST_DATE = "request_date"
        const val COLUMN_STATUS = "status"
    }

    override fun onCreate(db: SQLiteDatabase) {
        val createTable = """
            CREATE TABLE $TABLE_NAME (
                $COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_APPLICANT_NAME TEXT NOT NULL,
                $COLUMN_APPLICANT_DEPARTMENT TEXT NOT NULL,
                $COLUMN_EQUIPMENT_NAME TEXT NOT NULL,
                $COLUMN_LOCATION TEXT NOT NULL,
                $COLUMN_PURPOSE TEXT NOT NULL,
                $COLUMN_NOTE TEXT,
                $COLUMN_REQUEST_DATE TEXT NOT NULL,
                $COLUMN_STATUS TEXT NOT NULL
            )
        """.trimIndent()

        db.execSQL(createTable)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_NAME")
        onCreate(db)
    }

    // 구매신청 데이터 삽입
    fun insertPurchaseRequest(
        applicantName: String,
        applicantDepartment: String,
        equipmentName: String,
        location: String,
        purpose: String,
        note: String,
        requestDate: String,
        status: String
    ): Boolean {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_APPLICANT_NAME, applicantName)
            put(COLUMN_APPLICANT_DEPARTMENT, applicantDepartment)
            put(COLUMN_EQUIPMENT_NAME, equipmentName)
            put(COLUMN_LOCATION, location)
            put(COLUMN_PURPOSE, purpose)
            put(COLUMN_NOTE, note)
            put(COLUMN_REQUEST_DATE, requestDate)
            put(COLUMN_STATUS, status)
        }

        val result = db.insert(TABLE_NAME, null, values)
        db.close()
        return result != -1L
    }

    // 대기중인 신청 목록 조회
    fun getAllPendingRequests(): List<PurchaseRequest> {
        val requests = mutableListOf<PurchaseRequest>()
        val db = this.readableDatabase
        val cursor = db.query(
            TABLE_NAME,
            null,
            "$COLUMN_STATUS = ?",
            arrayOf("대기중"),
            null,
            null,
            "$COLUMN_REQUEST_DATE DESC"
        )

        cursor?.use {
            while (it.moveToNext()) {
                requests.add(cursorToPurchaseRequest(it))
            }
        }
        db.close()
        return requests
    }

    // 전체 신청 목록 조회
    fun getAllRequests(): List<PurchaseRequest> {
        val requests = mutableListOf<PurchaseRequest>()
        val db = this.readableDatabase
        val cursor = db.query(
            TABLE_NAME,
            null,
            null,
            null,
            null,
            null,
            "$COLUMN_REQUEST_DATE DESC"
        )

        cursor?.use {
            while (it.moveToNext()) {
                requests.add(cursorToPurchaseRequest(it))
            }
        }
        db.close()
        return requests
    }

    // 신청 상태 업데이트
    fun updateRequestStatus(id: Long, status: String): Boolean {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_STATUS, status)
        }

        val result = db.update(TABLE_NAME, values, "$COLUMN_ID = ?", arrayOf(id.toString()))
        db.close()
        return result > 0
    }

    // 자동 데이터 정리 (설정값 사용)
    fun deleteOldRecords(): Int {
        val db = this.writableDatabase
        val calendar = Calendar.getInstance().apply {
            add(Calendar.MONTH, -AppConfig.AUTO_CLEANUP_MONTHS)
        }
        val cutoffDate = SimpleDateFormat("yyyy-MM-dd", Locale.KOREA).format(calendar.time)

        val deletedRows = db.delete(
            TABLE_NAME,
            "$COLUMN_REQUEST_DATE < ?",
            arrayOf(cutoffDate)
        )

        db.close()
        return deletedRows
    }

    // 전체 데이터 삭제 (관리자용)
    fun deleteAllRecords(): Boolean {
        val db = this.writableDatabase
        val result = db.delete(TABLE_NAME, null, null)
        db.close()
        return result > 0
    }

    // 데이터 개수 확인
    fun getRecordCount(): Int {
        val db = this.readableDatabase
        val cursor = db.rawQuery("SELECT COUNT(*) FROM $TABLE_NAME", null)
        var count = 0

        cursor?.use {
            if (it.moveToFirst()) {
                count = it.getInt(0)
            }
        }

        db.close()
        return count
    }

    // 가장 오래된 데이터 날짜
    fun getOldestRecordDate(): String? {
        val db = this.readableDatabase
        val cursor = db.query(
            TABLE_NAME,
            arrayOf(COLUMN_REQUEST_DATE),
            null,
            null,
            null,
            null,
            "$COLUMN_REQUEST_DATE ASC",
            "1"
        )

        var oldestDate: String? = null
        cursor?.use {
            if (it.moveToFirst()) {
                oldestDate = it.getString(0)
            }
        }

        db.close()
        return oldestDate
    }

    // Cursor를 PurchaseRequest 객체로 변환
    private fun cursorToPurchaseRequest(cursor: Cursor): PurchaseRequest {
        return PurchaseRequest(
            id = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_ID)),
            applicantName = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_APPLICANT_NAME)),
            applicantDepartment = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_APPLICANT_DEPARTMENT)),
            equipmentName = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_EQUIPMENT_NAME)),
            location = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_LOCATION)),
            purpose = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PURPOSE)),
            note = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NOTE)),
            requestDate = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_REQUEST_DATE)),
            status = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_STATUS))
        )
    }
}