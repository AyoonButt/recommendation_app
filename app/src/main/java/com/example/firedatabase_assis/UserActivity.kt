package com.example.firedatabase_assis


import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.firedatabase_assis.databinding.ActivityUserBinding
import com.example.firedatabase_assis.databinding.ActivityWelcomeWindowBinding


class UserActivity : AppCompatActivity(){
    private lateinit var bind: ActivityUserBinding
    var dbhelp = DB_class(applicationContext)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user)

        val value=intent.getStringExtra("name")
        bind.Edituser.text = value
    }


}