package com.example.prog_poe_2025


import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.CompoundButton
import android.widget.LinearLayout
import android.widget.Switch
import android.widget.Toast
import android.widget.ToggleButton
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

class Settings : AppCompatActivity() {

    private lateinit var switchTheme: Switch
    private lateinit var currencyViewModel: CurrencyViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_settings)

        val profileSection = findViewById<LinearLayout>(R.id.llProfile)
        val currencySection = findViewById<LinearLayout>(R.id.llCurrency)
        val notificationsSection = findViewById<LinearLayout>(R.id.llNotifications)
        val themeSection = findViewById<LinearLayout>(R.id.llTheme)
        val logoutSection = findViewById<LinearLayout>(R.id.llLogout)
        switchTheme = findViewById(R.id.switchDarkMode)

        currencyViewModel = CurrencyViewModel(application)

        // Profile clicked
        profileSection.setOnClickListener {
            Toast.makeText(this, "Profile settings coming soon!", Toast.LENGTH_SHORT).show()
        }

        // Currency clicked
        currencySection.setOnClickListener {
            showCurrencySelectionPopup()  // <-- actually call the popup function!
        }

        // Notifications clicked
        notificationsSection.setOnClickListener {
            Toast.makeText(this, "Notifications settings coming soon!", Toast.LENGTH_SHORT).show()
        }

        // Theme Switch
        val isDarkModeOn = AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES
        switchTheme.isChecked = isDarkModeOn

        switchTheme.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                Toast.makeText(this, "Dark mode activated", Toast.LENGTH_SHORT).show()
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                Toast.makeText(this, "Light mode activated", Toast.LENGTH_SHORT).show()
            }
        }

        // Logout clicked
        logoutSection.setOnClickListener {
            startActivity(Intent(this, Register::class.java))
            finish()
        }
    }

    private fun showCurrencySelectionPopup() {
        val currencyOptions = arrayOf("USD", "ZAR", "EUR", "GBP")
        val builder = AlertDialog.Builder(this)

        builder.setTitle("Select Currency")
        builder.setItems(currencyOptions) { _, which ->
            val selectedCurrency = currencyOptions[which]

            lifecycleScope.launch {
                currencyViewModel.saveCurrencyCode(selectedCurrency)
                Toast.makeText(this@Settings, "Currency changed to $selectedCurrency", Toast.LENGTH_SHORT).show()
            }
        }

        builder.setNegativeButton("Cancel") { dialog, _ ->
            dialog.dismiss()
        }

        val dialog = builder.create()
        dialog.show()
    }
}

