package com.accompany.purchaseManagement

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.google.android.material.chip.Chip
import java.text.SimpleDateFormat
import java.util.*
import com.accompany.purchaseManagement.data.models.User

class PurchaseRequestListAdapter(
    private val context: Context,
    private val requests: MutableList<PurchaseRequestV2>,
    private val currentUser: User?,
    private val onItemClick: ((PurchaseRequestV2) -> Unit)? = null,
    private val onEditClick: ((PurchaseRequestV2) -> Unit)? = null
) : BaseAdapter() {

    override fun getCount() = requests.size
    override fun getItem(position: Int) = requests[position]
    override fun getItemId(position: Int) = position.toLong()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val viewHolder: ViewHolder
        val view: View

        if (convertView == null) {
            view = LayoutInflater.from(context).inflate(R.layout.item_purchase_request_v2, parent, false)
            viewHolder = ViewHolder(view)
            view.tag = viewHolder
        } else {
            view = convertView
            viewHolder = view.tag as ViewHolder
        }

        val request = getItem(position)
        viewHolder.bind(request)

        // 아이템 전체 클릭 리스너
        view.setOnClickListener {
            onItemClick?.invoke(request)
        }

        // 수정 버튼 클릭 리스너
        viewHolder.btnEdit.setOnClickListener {
            onEditClick?.invoke(request)
        }

        return view
    }

    inner class ViewHolder(private val view: View) {
        val tvApplicantName: TextView = view.findViewById(R.id.tvApplicantName)
        val tvApplicantDepartment: TextView = view.findViewById(R.id.tvApplicantDepartment)
        val tvEquipmentInfo: TextView = view.findViewById(R.id.tvEquipmentInfo)
        val tvPurpose: TextView = view.findViewById(R.id.tvPurpose)
        val tvLocation: TextView = view.findViewById(R.id.tvLocation)
        val tvRequestDate: TextView = view.findViewById(R.id.tvRequestDate)
        val tvPhotoCount: TextView = view.findViewById(R.id.tvPhotoCount)
        val chipStatus: Chip = view.findViewById(R.id.chipStatus)
        val btnEdit: ImageButton = view.findViewById(R.id.btnEdit)

        fun bind(request: PurchaseRequestV2) {
            tvApplicantName.text = request.applicantName
            tvApplicantDepartment.text = request.applicantDepartment

            tvEquipmentInfo.text = "🔧 ${request.equipmentName} (${request.quantity}개)"

            tvPurpose.text = "📝 ${request.purpose}"

            if (request.location.isNotEmpty()) {
                tvLocation.visibility = View.VISIBLE
                tvLocation.text = "📍 ${request.location}"
            } else {
                tvLocation.visibility = View.GONE
            }

            try {
                val inputFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.KOREA)
                val outputFormat = SimpleDateFormat("MM/dd HH:mm", Locale.KOREA)
                val date = inputFormat.parse(request.requestDate)
                tvRequestDate.text = "🕐 ${outputFormat.format(date ?: Date())}"
            } catch (e: Exception) {
                tvRequestDate.text = "🕐 ${request.requestDate}"
            }

            if (request.photoUrls.isNotEmpty()) {
                tvPhotoCount.visibility = View.VISIBLE
                tvPhotoCount.text = "📸 ${request.photoUrls.size}"
            } else {
                tvPhotoCount.visibility = View.GONE
            }

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

            val canEdit = request.applicantEmail == currentUser?.email && request.isModifiable()
            btnEdit.visibility = if (canEdit && onEditClick != null) View.VISIBLE else View.GONE
        }
    }
}
