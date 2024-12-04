package com.opsc7311poe.gourmetguru_opscpoe

import android.media.RingtoneManager
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {

    private lateinit var bottomNavView: BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        bottomNavView = findViewById(R.id.bottom_navigation)

        // Set "Home" as the selected item
        bottomNavView.selectedItemId = R.id.navHome

        // Load the HomeFragment initially
        replaceFragment(HomeFragment())

        bottomNavView.setOnItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.navHome -> {
                    Log.d("MainActivity", "Home selected")
                    replaceFragment(HomeFragment())
                    true
                }
                R.id.navMealPlan -> {
                    Log.d("MainActivity", "Meal Plan selected")
                    replaceFragment(MealPlanFragment())
                    true
                }
                R.id.navSearch -> {
                    Log.d("MainActivity", "Search selected")
                    replaceFragment(SearchFragment())
                    true
                }
                R.id.navMyProfile -> {
                    Log.d("MainActivity", "My Profile selected")
                    replaceFragment(MyProfileFragment())
                    true
                }
                R.id.navMyRecipe -> {
                    Log.d("MainActivity", "My Recipe selected")
                    replaceFragment(MyRecipesFragment())
                    true
                }
                else -> false
            }
        }

        // Set default fragment if there's no saved instance state
        if (savedInstanceState == null) {
            replaceFragment(HomeFragment())
        }

        // Stop the alarm sound if the app is opened from notification
        val notificationSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
        val ringtone = RingtoneManager.getRingtone(this, notificationSound)
        if (ringtone.isPlaying) {
            ringtone.stop()
        }

    }


    private fun replaceFragment(fragment: Fragment) {
        Log.d("MainActivity", "Replacing fragment: ${fragment::class.java.simpleName}")
        supportFragmentManager.beginTransaction()
            .replace(R.id.frame_container, fragment)
            .commit()
    }
}
