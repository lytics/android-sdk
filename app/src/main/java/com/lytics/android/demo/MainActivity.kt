package com.lytics.android.demo

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import com.lytics.android.Lytics
import com.lytics.android.events.LyticsEvent

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        title = "Lytics Demo"

        val textView: TextView = findViewById(R.id.textView)
        textView.setText("${Lytics.currentUser}")

        val button: AppCompatButton = findViewById(R.id.button)
        button.setOnClickListener {
            Lytics.track(LyticsEvent(name="test event"))
        }
    }
}