package com.example.firedatabase_assis

import android.os.Bundle
import android.widget.CheckBox
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class PreferencesActivity : AppCompatActivity() {

    private lateinit var btAction: CheckBox
    private lateinit var btComedy: CheckBox
    private lateinit var btDrama: CheckBox
    private lateinit var btFantasy: CheckBox
    private lateinit var btHorror: CheckBox
    private lateinit var btMystery: CheckBox
    private lateinit var btThriller: CheckBox
    private lateinit var btRomance: CheckBox

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_preference)

        btAction   = findViewById<CheckBox>(R.id.checkBox)
        btComedy   = findViewById<CheckBox>(R.id.checkBox2)
        btDrama    = findViewById<CheckBox>(R.id.checkBox3)
        btFantasy  = findViewById<CheckBox>(R.id.checkBox4)
        btHorror   = findViewById<CheckBox>(R.id.checkBox5)
        btMystery  = findViewById<CheckBox>(R.id.checkBox6)
        btThriller = findViewById<CheckBox>(R.id.checkBox7)
        btRomance  = findViewById<CheckBox>(R.id.checkBox8)
    }
}