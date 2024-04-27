package com.example.firedatabase_assis


import android.content.ContentValues
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.firedatabase_assis.databinding.ActivityUserBinding
import com.example.firedatabase_assis.databinding.ActivityWelcomeWindowBinding


class UserActivity : AppCompatActivity(){
    private lateinit var bind: ActivityUserBinding
    private lateinit var dbhelp: DB_class
    //SQLiteOpenHelper()

//    companion object{
//        internal val KEY_NAME = "name"
//    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bind = ActivityUserBinding.inflate(layoutInflater)
        setContentView(bind.root)

        dbhelp = DB_class(applicationContext)

        val value=intent.getStringExtra("name")
        bind.Edituser.text = value

        bind.btUpdateUser.setOnClickListener{
            val newName = bind.Edituser.text.toString()
            updateNameInDatabase(newName)
        }
    }

    private fun updateNameInDatabase(newName: String){
        val db = dbhelp.writableDatabase
        val values = ContentValues().apply {
            put(DB_class.KEY_NAME, newName)
        }
        db.update(DB_class.TABLE_CONTACTS, values, null, null)
        db.close()
    }
}