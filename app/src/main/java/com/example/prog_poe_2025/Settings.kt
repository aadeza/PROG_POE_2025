package com.example.prog_poe_2025


import android.content.Intent
import android.os.Bundle
import android.widget.CompoundButton
import android.widget.LinearLayout
import android.widget.Switch
import android.widget.Toast
import android.widget.ToggleButton
import androidx.appcompat.app.AppCompatActivity


class Settings : AppCompatActivity() {

    private lateinit var switchTheme: Switch

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        // Find views
        val profileSection = findViewById<LinearLayout>(R.id.llProfile)
        val currencySection = findViewById<LinearLayout>(R.id.llCurrency)
        val notificationsSection = findViewById<LinearLayout>(R.id.llNotifications)
        val themeSection = findViewById<LinearLayout>(R.id.llTheme)
        val logoutSection = findViewById<LinearLayout>(R.id.llLogout)
        switchTheme = findViewById<Switch>(R.id.switchDarkMode)

        // Profile clicked
        profileSection.setOnClickListener {
            Toast.makeText(this, "Profile settings coming soon!", Toast.LENGTH_SHORT).show()
        }

        // Currency clicked
        currencySection.setOnClickListener {
            Toast.makeText(this, "Change currency feature coming soon!", Toast.LENGTH_SHORT).show()
        }

        // Notifications clicked
        notificationsSection.setOnClickListener {
            Toast.makeText(this, "Change currency feature coming soon!", Toast.LENGTH_SHORT).show()
        }

        // Theme switch toggled
        switchTheme.setOnCheckedChangeListener { _: CompoundButton, isChecked: Boolean ->
            if (isChecked) {
                // Switch to Dark Mode (you can customize this more)
                Toast.makeText(this, "Dark mode activated", Toast.LENGTH_SHORT).show()
                // Save preference and apply dark theme
            } else {
                Toast.makeText(this, "Light mode activated", Toast.LENGTH_SHORT).show()
                // Save preference and apply light theme
            }
        }

        // Logout clicked
        logoutSection.setOnClickListener {
            startActivity(Intent(this, Register::class.java))
            finish()
        }
    }
}
