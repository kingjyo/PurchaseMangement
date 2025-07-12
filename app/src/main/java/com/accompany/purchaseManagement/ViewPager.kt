package com.accompany.purchaseManagement

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter

class PurchaseRequestPagerAdapter(activity: FragmentActivity) : FragmentStateAdapter(activity) {

    override fun getItemCount(): Int = 6

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> EquipmentNameFragment()
            1 -> QuantityFragment()
            2 -> LocationFragment()
            3 -> PurposeFragment()
            4 -> NoteFragment()
            5 -> PhotoFragment()
            else -> EquipmentNameFragment()
        }
    }
}