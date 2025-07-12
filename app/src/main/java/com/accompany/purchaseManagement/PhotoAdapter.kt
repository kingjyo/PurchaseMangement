package com.accompany.purchaseManagement

import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners

class PhotoAdapter(
    private val existingUrls: List<String>,
    private val newUris: List<Uri>,
    private val onDeleteExisting: ((String) -> Unit)? = null,
    private val onDeleteNew: ((Uri) -> Unit)? = null
) : RecyclerView.Adapter<PhotoAdapter.PhotoViewHolder>() {

    companion object {
        private const val TYPE_EXISTING = 0
        private const val TYPE_NEW = 1
    }

    override fun getItemCount() = existingUrls.size + newUris.size

    override fun getItemViewType(position: Int): Int {
        return if (position < existingUrls.size) TYPE_EXISTING else TYPE_NEW
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PhotoViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_photo_preview, parent, false)
        return PhotoViewHolder(view)
    }

    override fun onBindViewHolder(holder: PhotoViewHolder, position: Int) {
        when (getItemViewType(position)) {
            TYPE_EXISTING -> {
                val url = existingUrls[position]
                holder.bind(url, isUrl = true) {
                    onDeleteExisting?.invoke(url)
                }
            }
            TYPE_NEW -> {
                val uri = newUris[position - existingUrls.size]
                holder.bind(uri, isUrl = false) {
                    onDeleteNew?.invoke(uri)
                }
            }
        }
    }

    class PhotoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val ivPhoto: ImageView = itemView.findViewById(R.id.ivPhoto)
        private val btnDelete: ImageButton = itemView.findViewById(R.id.btnDelete)

        fun bind(data: Any, isUrl: Boolean, onDelete: (() -> Unit)?) {
            // Glide로 이미지 로드
            Glide.with(itemView.context)
                .load(data)
                .transform(CenterCrop(), RoundedCorners(16))
                .placeholder(R.drawable.ic_image_placeholder)
                .error(R.drawable.ic_image_error)
                .into(ivPhoto)

            // 삭제 버튼
            btnDelete.visibility = if (onDelete != null) View.VISIBLE else View.GONE
            btnDelete.setOnClickListener {
                onDelete?.invoke()
            }

            // 클릭시 전체화면으로 보기 (선택사항)
            ivPhoto.setOnClickListener {
                // TODO: 전체화면 이미지 뷰어 구현
            }
        }
    }
}

// 구매신청 화면용 간단한 사진 미리보기 어댑터
class SimplePhotoPreviewAdapter(
    private val photos: List<Any> // Uri 또는 String(URL)
) : RecyclerView.Adapter<SimplePhotoPreviewAdapter.PhotoViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PhotoViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_simple_photo_preview, parent, false)
        return PhotoViewHolder(view)
    }

    override fun onBindViewHolder(holder: PhotoViewHolder, position: Int) {
        holder.bind(photos[position])
    }

    override fun getItemCount() = photos.size

    class PhotoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val ivPhoto: ImageView = itemView.findViewById(R.id.ivPhoto)

        fun bind(photo: Any) {
            Glide.with(itemView.context)
                .load(photo)
                .transform(CenterCrop(), RoundedCorners(12))
                .placeholder(R.drawable.ic_image_placeholder)
                .into(ivPhoto)
        }
    }
}