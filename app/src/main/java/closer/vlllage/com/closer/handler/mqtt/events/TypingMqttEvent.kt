package closer.vlllage.com.closer.handler.mqtt.events

data class TypingMqttEvent constructor(
        val typing: String? = null,
        val stopTyping: String? = null
)