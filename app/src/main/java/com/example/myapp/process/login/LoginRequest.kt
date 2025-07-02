package com.example.myapp.process.login


data class LoginRequest(
    val username: String,
    val password: String
)

data class LoginResponse(
    val message: String,
    val data: Token
)

data class Token(
    val token: String
)

data class RegisterResponse(
    val message: String
)

data class RegisterRequest(
    val name: String,
    val username: String,
    val password: String,
    val repeatpassword: String
)

