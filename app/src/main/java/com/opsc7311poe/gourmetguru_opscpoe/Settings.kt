package com.opsc7311poe.gourmetguru_opscpoe

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import com.google.firebase.auth.FirebaseAuth




//Dark mode stuff

import androidx.appcompat.app.AppCompatDelegate
import android.content.SharedPreferences
import android.preference.PreferenceManager
import android.widget.Switch
import com.google.firebase.database.ValueEventListener


class Settings : Fragment() {

    private lateinit var btnLogout: TextView
    private lateinit var btnChangePassword: TextView
    private lateinit var btnDeleteAccount: TextView
    private lateinit var btnChangeLang: TextView
    private lateinit var btnBack: ImageView
    private lateinit var auth: FirebaseAuth
    private lateinit var switchNotifications: Switch


    //Dark mode stuff
    private lateinit var switchDarkMode: Switch
    private lateinit var sharedPreferences: SharedPreferences


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {


        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_settings, container, false)

        // In your settings fragment/activity
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)

        // Initialize notification switch and shared preferences
        switchNotifications = view.findViewById(R.id.switchNotifications)
        val notificationEnabled = sharedPreferences.getBoolean("NotificationsEnabled", true)
        switchNotifications.isChecked = notificationEnabled

        // Toggle notification preference on switch change
        switchNotifications.setOnCheckedChangeListener { _, isChecked ->
            val editor = sharedPreferences.edit()
            editor.putBoolean("NotificationsEnabled", isChecked)
            editor.apply()
        }


        auth = FirebaseAuth.getInstance()
        btnLogout = view.findViewById(R.id.txtlogout)

        btnLogout.setOnClickListener() {
            auth.signOut()
            val intent = Intent(activity, Login::class.java)
            startActivity(intent)
            activity?.finish()

        }

        //back button functionality
        btnBack = view.findViewById(R.id.btnBack)
        btnBack.setOnClickListener() {
            replaceFragment(MyProfileFragment())
        }

        //handling change password button
        btnChangePassword = view.findViewById(R.id.txtchangepass)

        btnChangePassword.setOnClickListener() {
            replaceFragment(changePassword())

        }

        //handling delete account button
        btnDeleteAccount = view.findViewById(R.id.txtdelaccount)

        btnDeleteAccount.setOnClickListener() {
            replaceFragment(DeleteAccount())
        }

        //handling change language button
        btnChangeLang = view.findViewById(R.id.txtchangelang)

        btnChangeLang.setOnClickListener() {
            replaceFragment(ChangeLanguageFragment())
        }


        //MORE DARK MODE STUFF
        switchDarkMode = view.findViewById(R.id.switchDarkMode)
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)

        // Set the initial state of the dark mode switch based on user preference
        val isDarkModeOn = sharedPreferences.getBoolean("DarkMode", false)
        switchDarkMode.isChecked = isDarkModeOn

       // OpenAI, 2024. ChatGPT. [online]. Available at: https://chat.openai.com [Accessed 1 October 2024].
        // Set up dark mode switch listener
        switchDarkMode.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                // Enable dark mode
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                saveDarkModePreference(true)
            } else {
                // Disable dark mode
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                saveDarkModePreference(false)
            }
        }



        return view
    }

    private fun replaceFragment(fragment: Fragment) {
        Log.d("Settings Fragment", "Replacing fragment: ${fragment::class.java.simpleName}")
        parentFragmentManager.beginTransaction()
            .replace(R.id.frame_container, fragment)
            .addToBackStack(null)
            .commit()
    }


    //dark mode method
    private fun saveDarkModePreference(isEnabled: Boolean) {
        val editor = sharedPreferences.edit()
        editor.putBoolean("DarkMode", isEnabled)
        editor.apply()
    }


}