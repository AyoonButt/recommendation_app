package com.example.firedatabase_assis

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.SwitchCompat


class SettingsActivity : AppCompatActivity() {

    private lateinit var nightModeSwitch: SwitchCompat
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        val btTime = findViewById<Button>(R.id.btTime)
        btTime.setOnClickListener(){
            openTime(it)
        }

        val edituser = findViewById<Button>(R.id.Edituser)
        edituser.setOnClickListener(){
            openUser(it)
        }

        val preferences = findViewById<Button>(R.id.btPreferences)
        preferences.setOnClickListener(){
            openPreferences(it)
        }

        val back_to_main = findViewById<Button>(R.id.backSettings)
        back_to_main.setOnClickListener(){
            backtomain(it)
        }

        sharedPreferences = getSharedPreferences("NighModeSett", Context.MODE_PRIVATE)
        nightModeSwitch = findViewById<SwitchCompat>(R.id.nightmode)
        val editor = sharedPreferences.edit()

        nightModeSwitch.setOnCheckedChangeListener { buttonView, isChecked ->

            if(!isChecked)
            {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                editor.putBoolean("night",false)
                editor.apply()
            }
            else
            {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                editor.putBoolean("night", true)
                editor.apply()
            }
        }

    }

    private fun openTime (view: View){
        val intent = Intent(this,TimeActivity::class.java)
        startActivity(intent)
    }

    private fun openUser(view: View){
        val intent =Intent(this,UserActivity::class.java)
        startActivity(intent)
    }

    private fun openPreferences(view: View){
        val intent = Intent(this,PreferencesActivity::class.java)
        startActivity(intent)
    }

    private fun backtomain(view: View){
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
    }
}
