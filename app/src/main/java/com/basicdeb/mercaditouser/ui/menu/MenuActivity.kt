package com.basicdeb.mercaditouser.ui.menu

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import androidx.appcompat.widget.Toolbar
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import androidx.viewpager2.widget.ViewPager2
import com.basicdeb.mercaditouser.R
import com.google.android.material.navigation.NavigationView
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.android.synthetic.main.activity_menu.*

class MenuActivity : AppCompatActivity() {

    private lateinit var mPager: ViewPager2

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.AppTheme)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_menu)

        mPager = menu_viewPager

        val pagerAdapter = ViewPagerAdapter(this)
        mPager.adapter = pagerAdapter

        val tabLayoutMediator = TabLayoutMediator(menu_tabLayout,menu_viewPager,
        TabLayoutMediator.TabConfigurationStrategy{tab, position ->
            when(position){
                0 ->{
                    tab.text = "Home"
                }
                1 ->{
                    tab.text = "Perfil"
                }
            }
        })
        tabLayoutMediator.attach()
    }

    override fun onBackPressed() {
        if (mPager.currentItem == 0) {
            // If the user is currently looking at the first step, allow the system to handle the
            // Back button. This calls finish() on this activity and pops the back stack.
            super.onBackPressed()
        } else {
            // Otherwise, select the previous step.
            mPager.currentItem = mPager.currentItem - 1
        }
    }


}