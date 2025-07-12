package com.accompany.purchaseManagement

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter

class PurchaseRequestPagerAdapter(activity: FragmentActivity) : FragmentStateAdapter(activity) {

    override fun getItemCount(): Int = 6

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> EquipmentNameFragmentV2()  // 음성 지원 버전
            1 -> QuantityFragment()
            2 -> LocationFragment()
            3 -> PurposeFragmentV2()         // 음성 지원 버전
            4 -> NoteFragmentV2()            // 음성 지원 버전
            5 -> PhotoFragment()
            else -> EquipmentNameFragmentV2()
        }
    }
}