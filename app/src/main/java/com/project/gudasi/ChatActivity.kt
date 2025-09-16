package com.project.gudasi

import android.os.Bundle
import android.util.Log
import android.widget.EditText
import android.widget.ImageButton
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

        // 뷰 초기화
        etMessageInput = findViewById(R.id.etMessageInput)
        btnSend = findViewById(R.id.btnSend)
        rvChatMessages = findViewById(R.id.rvChatMessages)

        // Gemini 모델 초기화
        try {
            generativeModel = GenerativeModel(
                // ✅ 지원되는 모델명으로 변경
                modelName = "models/gemini-1.5-flash",
                apiKey = BuildConfig.API_KEY
            )
        } catch (e: Exception) {
            Log.e("Gemini", "Gemini 모델 초기화 실패: ${e.message}")
            addMessage("Gemini 모델 초기화 실패: ${e.message}", false)
            return
        }

        // RecyclerView 설정
        chatAdapter = ChatAdapter(messageList)
        rvChatMessages.layoutManager = LinearLayoutManager(this).apply {
            stackFromEnd = true // 키보드 올라올 때 스크롤 유지
        }
        rvChatMessages.adapter = chatAdapter

        // 전송 버튼 클릭 리스너
        btnSend.setOnClickListener {
            val userMessage = etMessageInput.text.toString()
            if (userMessage.isNotBlank()) {
                addMessage(userMessage, true)
                sendMessageToGemini(userMessage)
                etMessageInput.text.clear()
            }
        }
    }

    // 메시지를 리스트에 추가하고 화면 업데이트
    private fun addMessage(text: String, isUser: Boolean) {
        messageList.add(ChatMessage(text, isUser))
        chatAdapter.notifyItemInserted(messageList.size - 1)
        rvChatMessages.scrollToPosition(messageList.size - 1) // 마지막 메시지로 스크롤
    }

    private fun sendMessageToGemini(userMessage: String) {
        lifecycleScope.launch {
            try {
                // withContext(Dispatchers.IO)를 사용하여 백그라운드 스레드에서 API 호출
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
}
