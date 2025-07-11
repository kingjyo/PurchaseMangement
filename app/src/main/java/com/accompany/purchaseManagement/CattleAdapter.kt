package com.accompany.purchaseManagement

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView

class CattleAdapter(
    private val context: Context,
    private val items: List<Cattle>,
    private val onItemClick: ((Cattle) -> Unit)?
) : BaseAdapter() {

    fun filterList(filtered: List<Cattle>) {
        (items as MutableList).clear()
        (items as MutableList).addAll(filtered)
        notifyDataSetChanged()
    }

    override fun getCount(): Int = items.size
    override fun getItem(position: Int): Any = items[position]
    override fun getItemId(position: Int): Long = position.toLong()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view = convertView ?: LayoutInflater.from(context).inflate(
            R.layout.item_cattle, parent, false
        )
        val cattle = items[position]

        view.findViewById<TextView>(R.id.tvCattleId).text = "관리번호: ${cattle.관리번호}"
        view.findViewById<TextView>(R.id.tvTagNumber).text = "개체번호: ${cattle.개체번호}"
        view.findViewById<TextView>(R.id.tvBirth).text = "생년월일: ${cattle.생년월일} (${cattle.월령}개월)"
        view.findViewById<TextView>(R.id.tvKind).text = "개체구분: ${cattle.개체구분}"
        view.findViewById<TextView>(R.id.tvWeight).text = "체중: ${cattle.체중}"
        view.findViewById<TextView>(R.id.tvBarn).text = "축사: ${cattle.축사}"
        view.findViewById<TextView>(R.id.tvStatus).text = "상태: ${cattle.상태}"
        view.findViewById<TextView>(R.id.tvSex).text = "성별: ${cattle.성별}"

        view.setOnClickListener { onItemClick?.invoke(cattle) }
        return view
    }
}
