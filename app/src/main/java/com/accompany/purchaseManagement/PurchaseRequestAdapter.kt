package com.accompany.purchaseManagement

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import java.text.SimpleDateFormat
import java.util.*

class PurchaseRequestAdapter(
    private val context: Context,
    private val requests: List<PurchaseRequest>,
    private val onItemClick: ((PurchaseRequest) -> Unit)?
) : BaseAdapter() {

    override fun getCount(): Int = requests.size

    override fun getItem(position: Int): Any = requests[position]

    override fun getItemId(position: Int): Long = requests[position].id

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view = convertView ?: LayoutInflater.from(context)
            .inflate(R.layout.item_purchase_request, parent, false)

        val request = requests[position]

        val tvApplicantName: TextView = view.findViewById(R.id.tvApplicantName)
        val tvApplicantDepartment: TextView = view.findViewById(R.id.tvApplicantDepartment)
        val tvEquipmentName: TextView = view.findViewById(R.id.tvEquipmentName)
        val tvLocation: TextView = view.findViewById(R.id.tvLocation)
        val tvPurpose: TextView = view.findViewById(R.id.tvPurpose)
        val tvRequestDate: TextView = view.findViewById(R.id.tvRequestDate)
        val tvStatus: TextView = view.findViewById(R.id.tvStatus)

        // 데이터 설정
        tvApplicantName.text = "신청자: ${request.applicantName}"
        tvApplicantDepartment.text = "소속: ${request.applicantDepartment}"
        tvEquipmentName.text = "장비: ${request.equipmentName}"
        tvLocation.text = "장소: ${request.location}"
        tvPurpose.text = "용도: ${request.purpose}"

        // 날짜 포맷팅
        try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.KOREA)
            val outputFormat = SimpleDateFormat("MM/dd HH:mm", Locale.KOREA)
            val date = inputFormat.parse(request.requestDate)
            tvRequestDate.text = outputFormat.format(date ?: Date())
        } catch (e: Exception) {
            tvRequestDate.text = request.requestDate.take(16) // 앞 16자리만 표시
        }

        // 상태 설정
        tvStatus.text = request.status

        // 상태에 따른 색상 변경
        when (request.status) {
            "대기중" -> {
                tvStatus.setTextColor(context.getColor(android.R.color.holo_orange_dark))
                tvStatus.setBackgroundResource(R.drawable.status_background)
            }
            "완료", "승인" -> {
                tvStatus.setTextColor(context.getColor(android.R.color.holo_green_dark))
                tvStatus.setBackgroundResource(R.drawable.status_background)
            }
            "거부" -> {
                tvStatus.setTextColor(context.getColor(android.R.color.holo_red_dark))
                tvStatus.setBackgroundResource(R.drawable.status_background)
            }
            else -> {
                tvStatus.setTextColor(context.getColor(android.R.color.darker_gray))
                tvStatus.setBackgroundResource(R.drawable.status_background)
            }
        }

        // 클릭 리스너 설정 (null이 아닌 경우에만)
        onItemClick?.let { clickListener ->
            view.setOnClickListener {
                clickListener(request)
            }
            // 클릭 가능한 경우 배경 변경
            view.setBackgroundResource(R.drawable.item_background_clickable)
        } ?: run {
            // 클릭 불가능한 경우 기본 배경
            view.setBackgroundResource(R.drawable.item_background)
            view.setOnClickListener(null)
        }

        return view
    }
}