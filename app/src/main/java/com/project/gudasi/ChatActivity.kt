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
import com.google.ai.client.generativeai.type.GoogleGenerativeAIException
import com.google.firebase.ktx.Firebase
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.ktx.remoteConfig
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
    private lateinit var remoteConfig: FirebaseRemoteConfig

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        etMessageInput = findViewById(R.id.etMessageInput)
        btnSend = findViewById(R.id.btnSend)
        rvChatMessages = findViewById(R.id.rvChatMessages)
        val backButton: ImageButton = findViewById(R.id.backButton)
        backButton.setOnClickListener { finish() }

        // 버튼을 초기에 비활성화
        btnSend.isEnabled = false
        etMessageInput.hint = "모델을 초기화하는 중입니다..."

        // Gemini 모델 초기화를 Remote Config 로부터 시작
        setupRemoteConfigAndInitGemini()

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

    private fun setupRemoteConfigAndInitGemini() {
        remoteConfig = Firebase.remoteConfig
        // 기본값 설정 (선택 사항이지만 권장)
        val defaults = mapOf("gemini_api_key" to "")
        remoteConfig.setDefaultsAsync(defaults)

        remoteConfig.fetchAndActivate()
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val apiKey = remoteConfig.getString("gemini_api_key")
                    if (apiKey.isNotBlank()) {
                        initializeGeminiModel(apiKey)
                    } else {
                        Log.e("Gemini", "Firebase Remote Config에서 API 키를 가져오지 못했습니다.")
                        addMessage("모델 초기화에 실패했습니다. API 키를 확인해주세요.", false)
                        etMessageInput.hint = "모델 초기화 실패"
                    }
                } else {
                    Log.e("Gemini", "Remote Config fetch 실패", task.exception)
                    addMessage("모델 설정 정보를 가져오는 데 실패했습니다.", false)
                    etMessageInput.hint = "모델 초기화 실패"
                }
            }
    }

    private fun initializeGeminiModel(apiKey: String) {
        try {
            generativeModel = GenerativeModel(
                modelName = "models/gemini-2.0-flash-lite",
                apiKey = apiKey // Remote Config에서 가져온 키 사용
            )
            // 모델 초기화 성공 시 UI 활성화
            runOnUiThread {
                btnSend.isEnabled = true
                etMessageInput.hint = "메시지를 입력하세요..."
            }
            Log.d("Gemini", "Gemini 모델 초기화 성공")
        } catch (e: Exception) {
            Log.e("Gemini", "Gemini 모델 초기화 실패: ${e.message}")
            runOnUiThread {
                addMessage("Gemini 모델 초기화에 실패했습니다.", false)
                etMessageInput.hint = "모델 초기화 실패"
            }
        }
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
            } catch (e: GoogleGenerativeAIException) {
                // Gemini 관련 특정 예외 처리 (더 자세한 오류 확인 가능)
                Log.e("Gemini", "API 호출 중 Gemini 오류 발생: ${e.message}")
                withContext(Dispatchers.Main) {
                    addMessage("API 오류가 발생했습니다: ${e.message}", false)
                }
            } catch (e: Exception) {
                // 그 외 일반적인 예외 처리
                Log.e("Gemini", "API 호출 중 알 수 없는 오류 발생: ${e.message}")
                withContext(Dispatchers.Main) {
                    addMessage("오류가 발생했습니다: ${e.message}", false)
                }
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