package com.basicdeb.mercaditouser.ui.menu

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.basicdeb.mercaditouser.ui.MenuFragment
import com.basicdeb.mercaditouser.ui.cerrar.CerrarFragment
import com.basicdeb.mercaditouser.ui.listado.ListadoFragment

class ViewPagerAdapter(fa:FragmentActivity) :FragmentStateAdapter(fa) {

    override fun getItemCount(): Int = 2

    override fun createFragment(position: Int): Fragment {
        return when(position){
            0 -> {MenuFragment()}
            1 -> {CerrarFragment()}
            else -> {ListadoFragment()}
        }
    }
}