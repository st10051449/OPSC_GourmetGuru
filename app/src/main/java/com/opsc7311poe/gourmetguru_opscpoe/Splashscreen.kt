package com.opsc7311poe.gourmetguru_opscpoe

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity

class Splashscreen : AppCompatActivity() {

    private val splashScreenTimeout: Long = 2000

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splashscreen)

        Handler(Looper.getMainLooper()).postDelayed({
            val intent = Intent(this, Login::class.java) //Login
            startActivity(intent)
            finish()
        }, splashScreenTimeout)
    }
}
