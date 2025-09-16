package com.project.gudasi

data class ChatMessage(
    val text: String,
    val isUser: Boolean // true면 사용자, false면 봇
)