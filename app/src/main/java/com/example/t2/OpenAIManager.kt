package com.example.t2

import android.util.Log
import com.aallam.openai.api.BetaOpenAI
import com.aallam.openai.api.chat.ChatCompletionRequest
import com.aallam.openai.api.chat.ChatMessage
import com.aallam.openai.api.chat.ChatRole
import com.aallam.openai.api.model.ModelId
import com.aallam.openai.client.OpenAI
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class OpenAIManager {
    private val apiKey = BuildConfig.OPENAI_API_KEY
    private val openAI = OpenAI(apiKey)

    // This list stores the conversation history
    private val chatHistory = mutableListOf<ChatMessage>()

    @OptIn(BetaOpenAI::class)
    suspend fun askOpenAI(userInput: String): String = withContext(Dispatchers.IO) {
        try {
            // Add user input to history
            chatHistory.add(ChatMessage(role = ChatRole.User, content = userInput))

            val systemPrompt = """
            You are a smart Telugu AI tutor assistant.
            based on the app name or context of the user input open the respective app.
            example :
            if user says youtube lo song play ch-ey 
            it must understand and open youtube.
ఎప్పుడూ తెలుగులోనే సమాధానం ఇవ్వాలి.

మీరు ముందటి సంభాషణ (previous context) గుర్తుంచుకోవాలి మరియు దానికి అనుసరించి follow-up ప్రశ్నలకు సమాధానం ఇవ్వాలి.

కఠిన నియమాలు (STRICT RULES):

వివరణను స్పష్టమైన సంఖ్యలతో (1, 2, 3...) దశలుగా విభజించాలి.

ప్రతి సమాధానంలో ఒకే ఒక దశ మాత్రమే ఇవ్వాలి.

ప్రతి దశ తర్వాత తప్పనిసరిగా ఇలా చెప్పాలి:
👉 తదుపరి దశ కోసం 'next' అని లేదా ముగించడానికి 'stop' అని చెప్పండి.

యూజర్ ఏదైనా యాప్ ఓపెన్ చేయమంటే లేదా సెర్చ్ చేయమంటే (ఉదా: "Open YouTube", "Search for Biryani", "open it", "search that"), ముందు సందేశాన్ని చూసి ఏది ఓపెన్ చేయాలో లేదా వెతకాలో నిర్ణయించాలి.

యాప్ ఓపెన్ చేయాల్సి వచ్చినప్పుడు లేదా సెర్చ్ చేయాల్సి వచ్చినప్పుడు, సమాధానం చివర ఈ ఫార్మాట్‌లో ట్యాగ్ జోడించాలి:

[ACTION:app_name:query]

ఉదాహరణ:
సరే, నేను యూట్యూబ్‌లో బిర్యానీ కోసం వెతుకుతున్నాను.
[ACTION:youtube:biryani]""".trimIndent()

            val chatCompletionRequest = ChatCompletionRequest(
                model = ModelId("gpt-4o-mini"),
                messages = listOf(ChatMessage(role = ChatRole.System, content = systemPrompt)) + chatHistory
            )

            val response = openAI.chatCompletion(chatCompletionRequest)
            val assistantContent = response.choices.first().message?.content ?: ""

            // Add AI response to history so it remembers next time
            chatHistory.add(ChatMessage(role = ChatRole.Assistant, content = assistantContent))

            return@withContext assistantContent
        } catch (e: Exception) {
            return@withContext "Error: ${e.message}"
        }
    }
}
