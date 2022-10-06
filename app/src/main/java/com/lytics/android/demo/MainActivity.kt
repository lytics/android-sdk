package com.lytics.android.demo

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.lytics.android.Lytics

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        title = "Lytics Demo"

        val textView: TextView = findViewById(R.id.textView)
        textView.setText("${Lytics.currentUser}")
    }
}