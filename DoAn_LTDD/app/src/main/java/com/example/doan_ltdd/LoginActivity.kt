package com.example.doan_ltdd

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {
    private lateinit var edtUsername: EditText
    private lateinit var edtPassword: EditText
    private lateinit var btnLogin: Button
    private lateinit var btnRegister: TextView
    private lateinit var database: AppDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_login)

        database = AppDatabase.getDatabase(this)

        setControl()
        setEvent()
    }

    private fun setControl() {
        edtUsername = findViewById(R.id.edtUser)
        edtPassword = findViewById(R.id.edtPassword)
        btnLogin = findViewById(R.id.btnLogin)
        btnRegister = findViewById(R.id.btnRegister)
    }

    private fun setEvent() {
        val dao = database.userDao()

        // 1. Xử lý nút Đăng nhập
        btnLogin.setOnClickListener {
            val username = edtUsername.text.toString().trim()
            val password = edtPassword.text.toString().trim()

            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            lifecycleScope.launch {
                val user = dao.login(username, password)
                if (user != null) {
                    val intent = Intent(this@LoginActivity, MainActivity::class.java)
                    intent.putExtra("USERNAME_KEY", user.username)
                    intent.putExtra("USER_ROLE_KEY", user.role) // [MỚI] Truyền Role
                    startActivity(intent)
                    finish()
                } else {
                    Toast.makeText(this@LoginActivity, "Sai thông tin!", Toast.LENGTH_SHORT).show()
                }
            }
        }

        // 2. Xử lý nút Đăng ký -> MỞ DIALOG [Cập nhật mới]
        btnRegister.setOnClickListener {
            showRegisterDialog()
        }
    }

    // Hàm hiển thị Dialog Đăng ký
    private fun showRegisterDialog() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_register, null)
        val dialog = AlertDialog.Builder(this).setView(dialogView).create()

        // Ánh xạ các view trong dialog
        val etRegUser = dialogView.findViewById<EditText>(R.id.etRegUser)
        val etRegPass = dialogView.findViewById<EditText>(R.id.etRegPass)
        val etRegConfirm = dialogView.findViewById<EditText>(R.id.etRegConfirmPass) // ID trong dialog_register.xml
        val btnRegSubmit = dialogView.findViewById<Button>(R.id.btnRegSubmit)

        btnRegSubmit.setOnClickListener {
            val user = etRegUser.text.toString().trim()
            val pass = etRegPass.text.toString().trim()
            val confirm = etRegConfirm.text.toString().trim()

            // Validate dữ liệu
            if (user.isEmpty() || pass.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập tên và mật khẩu", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (pass != confirm) {
                Toast.makeText(this, "Mật khẩu xác nhận không khớp!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Gọi Database xử lý
            lifecycleScope.launch {
                val dao = database.userDao()
                val count = dao.checkUserExists(user)

                if (count > 0) {
                    Toast.makeText(this@LoginActivity, "Tên đăng nhập đã tồn tại!", Toast.LENGTH_SHORT).show()
                } else {
                    val role = if (user.lowercase() == "admin") "ADMIN" else "USER"

                    val newUser = User(
                        username = user,
                        password = pass,
                        fullName = user,
                        email = "",
                        role = role // Lưu role vào DB
                    )
                    dao.registerUser(newUser)

                    Toast.makeText(this@LoginActivity, "Đăng ký thành công! Hãy đăng nhập.", Toast.LENGTH_SHORT).show()
                    dialog.dismiss() // Đóng dialog sau khi đăng ký xong
                }
            }
        }

        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent) // Làm đẹp nền (bo góc)
        dialog.show()
    }
}