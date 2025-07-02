package com.example.myapp.activity

import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.example.myapp.databinding.ActivitySignUpBinding
import com.example.myapp.process.RetrofitClient
import com.example.myapp.process.login.RegisterRequest
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch
import org.json.JSONObject

class SignUpActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySignUpBinding
    private lateinit var btnSignIn: Button
    private lateinit var imgbtnReturn: ImageButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        initViews()
        setupView()
    }

    private fun initViews() {
        btnSignIn = binding.btnRegister
        imgbtnReturn = binding.imgbtnReturn
    }

    private fun setupView() {
        btnSignIn.setOnClickListener {
            val name = binding.edtName.text.toString().trim()
            val username = binding.edtUsername.text.toString().trim()
            val password = binding.edtPassword.text.toString().trim()
            val repeatPassword = binding.edtRepeatPassword.text.toString().trim()

            lifecycleScope.launch {
                try {
                    val request = RegisterRequest(name, username, password, repeatPassword)
                    val response = RetrofitClient.authService.register(request)

                    val message = when {
                        response.isSuccessful && response.code() == 201 -> {
                            response.body()?.message ?: "Registration successful."
                        }
                        else -> {
                            val errorBody = response.errorBody()?.string()
                            if (!errorBody.isNullOrEmpty()) {
                                try {
                                    val json = JSONObject(errorBody)
                                    json.optString("message", "Registration failed.")
                                } catch (e: Exception) {
                                    "Registration failed."
                                }
                            } else {
                                "Registration failed."
                            }
                        }
                    }

                    Snackbar.make(binding.root, message, Snackbar.LENGTH_SHORT).show()

                    if (response.isSuccessful) {
                        btnSignIn.postDelayed({ finish() }, 1500)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    Snackbar.make(
                        binding.root,
                        "Server or connection error: ${e.message}",
                        Snackbar.LENGTH_SHORT
                    ).show()
                }
            }

        }
        imgbtnReturn.setOnClickListener {
            finish()
        }
    }

}
