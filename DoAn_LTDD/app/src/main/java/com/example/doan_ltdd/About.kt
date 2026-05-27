package com.example.doan_ltdd

import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity

class AboutActivity : AppCompatActivity() {

    private lateinit var btnBack: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_about)

        setControl()
        setEvent()
    }

    private fun setControl() {
        btnBack = findViewById(R.id.btnBackAbout)
    }

    private fun setEvent() {
        btnBack.setOnClickListener {
            finish() // Đóng activity quay về màn hình trước
        }
    }
}