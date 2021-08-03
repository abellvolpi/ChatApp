package com.example.chatapp.ui

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.example.chatapp.R

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(findViewById(R.id.toolbar))
    }

//    override fun onNewIntent(intent: Intent?) {
//        super.onNewIntent(intent)
//        getIntent().getStringExtra("PORTA")?.let { Log.e("TESTE", it) }
//    }
}
