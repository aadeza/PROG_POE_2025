package com.example.prog_poe_2025


import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.CompoundButton
import android.widget.LinearLayout
import android.widget.Switch
import android.widget.Toast
import android.widget.ToggleButton
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat


class Settings : AppCompatActivity() {

    private lateinit var switchTheme: Switch

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_settings)


        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        // Find views
        val profileSection = findViewById<LinearLayout>(R.id.llProfile)
        val currencySection = findViewById<LinearLayout>(R.id.llCurrency)
        val notificationsSection = findViewById<LinearLayout>(R.id.llNotifications)
        val themeSection = findViewById<LinearLayout>(R.id.llTheme)
        val logoutSection = findViewById<LinearLayout>(R.id.llLogout)
        val switchTheme = findViewById<Switch>(R.id.switchDarkMode)
        val mainLayout = findViewById<View>(R.id.main)


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

        fun updateBackgroundImage(isDarkMode: Boolean) {
            if (isDarkMode) {
                mainLayout.setBackgroundResource(R.drawable.dark_background)
            } else {
                mainLayout.setBackgroundResource(R.drawable.register)
            }
        }

        val isDarkModeOn =
            AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES
        switchTheme.isChecked = isDarkModeOn
        updateBackgroundImage(isDarkModeOn)
        // Theme switch toggled
        switchTheme.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                Toast.makeText(this, "Dark mode activated", Toast.LENGTH_SHORT).show()
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                Toast.makeText(this, "Light mode activated", Toast.LENGTH_SHORT).show()
            }
            updateBackgroundImage(isChecked)


        }


        // Logout clicked
        logoutSection.setOnClickListener {
            startActivity(Intent(this, Register::class.java))
            finish()
        }
    }
}
