package closer.vlllage.com.closer.handler.mqtt

import android.util.Log
import closer.vlllage.com.closer.handler.data.PersistenceHandler
import closer.vlllage.com.closer.handler.helpers.ApplicationHandler
import closer.vlllage.com.closer.handler.helpers.DefaultAlerts
import closer.vlllage.com.closer.handler.helpers.JsonHandler
import closer.vlllage.com.closer.handler.mqtt.events.CallMqttEvent
import closer.vlllage.com.closer.handler.mqtt.events.TypingMqttEvent
import com.google.gson.JsonObject
import com.queatz.on.On
import com.queatz.on.OnLifecycle
import io.reactivex.subjects.PublishSubject
import org.eclipse.paho.android.service.MqttAndroidClient
import org.eclipse.paho.client.mqttv3.*
import java.nio.charset.Charset
import kotlin.reflect.KClass

class MqttHandler constructor(private val on: On) : OnLifecycle {

    private val events = PublishSubject.create<MqttChannelEvent>()
    private val queue = mutableListOf<() -> Unit>()

    companion object {
        private lateinit var mqttClient: MqttAndroidClient
        const val TAG = "MqttHandler"
        const val MQTT_URI = "tcp://closer.vlllage.com:1883"
    }

    override fun on() {
        mqttClient = MqttAndroidClient(on<ApplicationHandler>().app, MQTT_URI, on<PersistenceHandler>().phone)

        mqttClient.setCallback(object : MqttCallback {
            override fun messageArrived(channel: String, message: MqttMessage) {
                handleMessage(channel, on<JsonHandler>().from(String(message.payload, Charset.defaultCharset()), MqttEvent::class.java))
            }

            override fun connectionLost(cause: Throwable?) {
                Log.d(TAG, "Connection lost $cause")
            }

            override fun deliveryComplete(token: IMqttDeliveryToken?) {

            }
        })

        val options = MqttConnectOptions()
        options.maxInflight = 1000
        options.isAutomaticReconnect = true

        try {
            mqttClient.connect(options, null, object : IMqttActionListener {
                override fun onSuccess(asyncActionToken: IMqttToken?) {
                    Log.d(TAG, "Connection success")

                    queue.forEach { it() }
                }

                override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
                    Log.d(TAG, "Connection failure: $exception")
                }
            })
        } catch (e: MqttException) {
            e.printStackTrace()
        }
    }

    fun <T : Any> events(channel: String, kClass: KClass<T>) = events
            .filter { it.channel == channel && it.event::class == kClass }
            .map { it.event as T }!!

    fun subscribe(channel: String, qos: Int = 1, callback: (() -> Unit)? = null) {
        if (!mqttClient.isConnected) {
            queue.add { subscribe(channel, qos) }
            return
        }

        try {
            mqttClient.subscribe(channel, qos, null, object : IMqttActionListener {
                override fun onSuccess(asyncActionToken: IMqttToken?) {
                    Log.d(TAG, "Subscribed to $channel")
                    callback?.invoke()
                }

                override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
                    Log.d(TAG, "Failed to subscribe $channel")
                }
            })
        } catch (e: MqttException) {
            e.printStackTrace()
        }
    }

    fun unsubscribe(channel: String) {
        if (!mqttClient.isConnected) return

        try {
            mqttClient.unsubscribe(channel, null, object : IMqttActionListener {
                override fun onSuccess(asyncActionToken: IMqttToken?) {
                    Log.d(TAG, "Unsubscribed to $channel")
                }

                override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
                    Log.d(TAG, "Failed to unsubscribe $channel")
                }
            })
        } catch (e: MqttException) {
            e.printStackTrace()
        }
    }

    fun publish(channel: String, event: Any, qos: Int = 1, retained: Boolean = false) {
        if (!mqttClient.isConnected) {
            queue.add { publish(channel, event, qos, retained) }
            return
        }

        val msg = on<JsonHandler>().to(MqttEvent(
                event::class.simpleName!!,
                data = on<JsonHandler>().toJsonTree(event).asJsonObject
        ))

        try {
            val message = MqttMessage()
            message.payload = msg.toByteArray()
            message.qos = qos
            message.isRetained = retained
            mqttClient.publish(channel, message, null, object : IMqttActionListener {
                override fun onSuccess(asyncActionToken: IMqttToken?) {
                    Log.d(TAG, "$msg published to $channel")
                }

                override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
                    Log.d(TAG, "Failed to publish $msg to $channel\n$exception")
                }
            })
        } catch (e: MqttException) {
            e.printStackTrace()
        }
    }

    private fun handleMessage(channel: String, mqttEvent: MqttEvent) {
        events.onNext(MqttChannelEvent(
                channel,
                on<JsonHandler>().from(mqttEvent.data!!, when (mqttEvent.type) {
                    TypingMqttEvent::class.simpleName -> TypingMqttEvent::class.java
                    CallMqttEvent::class.simpleName -> CallMqttEvent::class.java
                    else -> {
                        on<DefaultAlerts>().syncError()
                        return
                    }
                })
        ))
    }

    override fun off() {
        try {
            if (!mqttClient.isConnected) return

            mqttClient.disconnect(null, object : IMqttActionListener {
                override fun onSuccess(asyncActionToken: IMqttToken?) {
                    Log.d(TAG, "Disconnected")
                }

                override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
                    Log.d(TAG, "Failed to disconnect")
                }
            })
        } catch (e: MqttException) {
            e.printStackTrace()
        }
    }
}

private data class MqttEvent constructor(
        val type: String,
        val data: JsonObject? = null
)

private data class MqttChannelEvent constructor(
        val channel: String,
        val event: Any
)
