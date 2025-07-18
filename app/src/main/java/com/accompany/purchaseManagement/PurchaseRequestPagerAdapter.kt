package com.accompany.purchaseManagement

import QuantityFragment
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter

class PurchaseRequestPagerAdapter(activity: FragmentActivity) : FragmentStateAdapter(activity) {

    // Fragment 캐시
    private val fragmentCache = mutableMapOf<Int, Fragment>()

    override fun getItemCount(): Int = 6

    override fun createFragment(position: Int): Fragment {
        // 이미 생성된 Fragment가 있으면 재사용
        return fragmentCache.getOrPut(position) {
            when (position) {
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

    // Fragment를 가져오는 메서드 추가
    fun getFragment(position: Int): Fragment? = fragmentCache[position]
}