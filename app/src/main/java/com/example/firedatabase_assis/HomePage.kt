package com.example.firedatabase_assis

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.ItemTouchUIUtil
import androidx.viewpager.widget.ViewPager
import com.example.firedatabase_assis.databinding.ActivityHomepageBinding
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2

class HomePage : AppCompatActivity() {
    private lateinit var binding : ActivityHomepageBinding;
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomepageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.bottomNavBar.setOnItemReselectedListener { menuItem->
            when(menuItem.itemId) {
                R.id.bottom_menu_home->{

                }
                R.id.bottom_menu_explore->{

                }
                R.id.bottom_menu_communities->{

                }
                R.id.bottom_menu_profile->{

                }
                R.id.bottom_menu_settings->{

                }
            }
            false
        }
    }
}

