package com.accompany.purchaseManagement.adapters

import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.accompany.purchaseManagement.R

/**
 * 사진 목록을 표시하는 RecyclerView 어댑터
 */
class PhotoAdapter(
    private val existingUrls: List<String> = emptyList(),
    private val newUris: MutableList<Uri> = mutableListOf(),
    private val onRemove: (Int) -> Unit
) : RecyclerView.Adapter<PhotoAdapter.PhotoViewHolder>() {
    
    class PhotoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView: ImageView = itemView.findViewById(R.id.imageView)
        val removeButton: View = itemView.findViewById(R.id.btnRemove)
    }
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PhotoViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_photo_preview, parent, false)
        return PhotoViewHolder(view)
    }
    
    override fun onBindViewHolder(holder: PhotoViewHolder, position: Int) {
        // 간단한 구현 - 실제로는 이미지 로딩 라이브러리 사용
        holder.removeButton.setOnClickListener {
            onRemove(position)
        }
    }
    
    override fun getItemCount(): Int {
        return existingUrls.size + newUris.size
    }
}