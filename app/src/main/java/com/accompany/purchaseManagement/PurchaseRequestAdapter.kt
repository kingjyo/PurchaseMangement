package com.accompany.purchaseManagement

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.chip.Chip
import java.text.SimpleDateFormat
import java.util.*
import com.accompany.purchaseManagement.R

class PurchaseRequestAdapterV2(
    private val requests: List<PurchaseRequestV2>,
    private val currentUser: GoogleAuthHelper.UserInfo?,
    private val onItemClick: (PurchaseRequestV2) -> Unit,
    private val onEditClick: ((PurchaseRequestV2) -> Unit)? = null
) : RecyclerView.Adapter<PurchaseRequestAdapterV2.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_purchase_request_v2, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(requests[position])
    }

    override fun getItemCount() = requests.size

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvApplicantName: TextView = itemView.findViewById(R.id.tvApplicantName)
        private val tvApplicantDepartment: TextView = itemView.findViewById(R.id.tvApplicantDepartment)
        private val tvEquipmentInfo: TextView = itemView.findViewById(R.id.tvEquipmentInfo)
        private val tvPurpose: TextView = itemView.findViewById(R.id.tvPurpose)
        private val tvLocation: TextView = itemView.findViewById(R.id.tvLocation)
        private val tvRequestDate: TextView = itemView.findViewById(R.id.tvRequestDate)
        private val tvPhotoCount: TextView = itemView.findViewById(R.id.tvPhotoCount)
        private val chipStatus: Chip = itemView.findViewById(R.id.chipStatus)
        private val btnEdit: ImageButton = itemView.findViewById(R.id.btnEdit)

        fun bind(request: PurchaseRequestV2) {
            // 신청자 정보
            tvApplicantName.text = request.applicantName
            tvApplicantDepartment.text = request.applicantDepartment

            // 장비 정보
            tvEquipmentInfo.text = "🔧 ${request.equipmentName} (${request.quantity}개)"

            // 용도
            tvPurpose.text = "📝 ${request.purpose}"

            // 장소 (있을 경우만)
            if (request.location.isNotEmpty()) {
                tvLocation.visibility = View.VISIBLE
                tvLocation.text = "📍 ${request.location}"
            } else {
                tvLocation.visibility = View.GONE
            }

            // 날짜 포맷팅
            try {
                val inputFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.KOREA)
                val outputFormat = SimpleDateFormat("MM/dd HH:mm", Locale.KOREA)
                val date = inputFormat.parse(request.requestDate)
                tvRequestDate.text = "🕐 ${outputFormat.format(date ?: Date())}"
            } catch (e: Exception) {
                tvRequestDate.text = "🕐 ${request.requestDate}"
            }

            // 사진 개수
            if (request.photoUrls.isNotEmpty()) {
                tvPhotoCount.visibility = View.VISIBLE
                tvPhotoCount.text = "📸 ${request.photoUrls.size}"
            } else {
                tvPhotoCount.visibility = View.GONE
            }

            // 상태
            val status = PurchaseStatus.fromString(request.status)
            chipStatus.text = "${status.emoji} ${status.displayName}"
            chipStatus.setChipBackgroundColorResource(
                when (status) {
                    PurchaseStatus.PENDING -> R.color.status_pending
                    PurchaseStatus.CONFIRMED -> R.color.status_confirmed
                    PurchaseStatus.IN_APPROVAL -> R.color.status_in_approval
                    PurchaseStatus.APPROVED -> R.color.status_approved
                    PurchaseStatus.PRE_PROCESSED -> R.color.status_pre_processed
                    PurchaseStatus.COMPLETED -> R.color.status_completed
                }
            )

            // 수정 버튼 (본인 + 수정 가능한 상태)
            val canEdit = request.applicantEmail == currentUser?.email && request.isModifiable()
            btnEdit.visibility = if (canEdit && onEditClick != null) View.VISIBLE else View.GONE
            btnEdit.setOnClickListener {
                onEditClick?.invoke(request)
            }

            // 아이템 클릭
            itemView.setOnClickListener {
                onItemClick(request)
            }
        }
    }
}