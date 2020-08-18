package closer.vlllage.com.closer.handler.mqtt.events

import closer.vlllage.com.closer.handler.call.CallEvent

data class CallMqttEvent constructor(
        val event: CallEvent? = null,
)