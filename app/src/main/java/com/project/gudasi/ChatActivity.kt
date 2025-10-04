package com.project.gudasi

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.ai.client.generativeai.GenerativeModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ChatActivity : AppCompatActivity() {

    private lateinit var etMessageInput: EditText
    private lateinit var btnSend: ImageButton
    private lateinit var rvChatMessages: RecyclerView
    private lateinit var chatAdapter: ChatAdapter
    private val messageList = mutableListOf<ChatMessage>()

    private lateinit var generativeModel: GenerativeModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        etMessageInput = findViewById(R.id.etMessageInput)
        btnSend = findViewById(R.id.btnSend)
        rvChatMessages = findViewById(R.id.rvChatMessages)
        val backButton: ImageButton = findViewById(R.id.backButton)
        backButton.setOnClickListener { finish() }

        try {
            generativeModel = GenerativeModel(
                modelName = "gemini-1.5-flash",
                apiKey = BuildConfig.API_KEY
            )
        } catch (e: Exception) {
            Log.e("Gemini", "Gemini 모델 초기화 실패: ${e.message}")
            addMessage("Gemini 모델 초기화에 실패했습니다. API 키를 확인해주세요.", false)
            return
        }

        chatAdapter = ChatAdapter(messageList)
        rvChatMessages.layoutManager = LinearLayoutManager(this).apply {
            stackFromEnd = true
        }
        rvChatMessages.adapter = chatAdapter

        btnSend.setOnClickListener {
            val userMessage = etMessageInput.text.toString()
            if (userMessage.isNotBlank()) {
                addMessage(userMessage, true)
                sendMessageToGemini(userMessage)
                etMessageInput.text.clear()
            }
        }

        setupBottomNavigation()
    }

    private fun addMessage(text: String, isUser: Boolean) {
        messageList.add(ChatMessage(text, isUser))
        chatAdapter.notifyItemInserted(messageList.size - 1)
        rvChatMessages.scrollToPosition(messageList.size - 1)
    }

    private fun sendMessageToGemini(userMessage: String) {
        lifecycleScope.launch {
            try {
                val response = withContext(Dispatchers.IO) {
                    generativeModel.generateContent(userMessage)
                }
                val botMessage = response.text ?: "응답이 없습니다."
                addMessage(botMessage, false)
            } catch (e: Exception) {
                Log.e("Gemini", "API 호출 중 오류 발생: ${e.message}")
                addMessage("오류가 발생했습니다: ${e.message}", false)
            }
        }
    }

    private fun setupBottomNavigation() {
        val bottomBar = findViewById<View>(R.id.bottom_bar_include)

        val btnHome = bottomBar.findViewById<View>(R.id.homeButton)
        val btnChat = bottomBar.findViewById<View>(R.id.chatButton)
        val btnAppUsage = bottomBar.findViewById<View>(R.id.usageTimeButton)

        val homeIcon = bottomBar.findViewById<ImageView>(R.id.homeIcon)
        val homeText = bottomBar.findViewById<TextView>(R.id.homeText)
        val chatIcon = bottomBar.findViewById<ImageView>(R.id.chatIcon)
        val chatText = bottomBar.findViewById<TextView>(R.id.chatText)
        val usageIcon = bottomBar.findViewById<ImageView>(R.id.usageTimeIcon)
        val usageText = bottomBar.findViewById<TextView>(R.id.usageTimeText)


        val defaultColor = Color.parseColor("#888888")
        val selectedColor = Color.parseColor("#007BFF")

        homeIcon.setColorFilter(defaultColor)
        homeText.setTextColor(defaultColor)
        chatIcon.setColorFilter(selectedColor)
        chatText.setTextColor(selectedColor)
        usageIcon.setColorFilter(defaultColor)
        usageText.setTextColor(defaultColor)

        btnHome.setOnClickListener {
            startActivity(Intent(this, HomeActivity::class.java))
            finish()
        }

        btnAppUsage.setOnClickListener {
            startActivity(Intent(this, UsageStatsActivity::class.java))
            finish()
        }

        btnChat.setOnClickListener {
            // 현재 페이지
        }
    }
}