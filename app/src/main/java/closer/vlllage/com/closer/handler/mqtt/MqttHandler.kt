package closer.vlllage.com.closer.handler.mqtt

import android.util.Log
import closer.vlllage.com.closer.handler.data.PersistenceHandler
import closer.vlllage.com.closer.handler.helpers.ApplicationHandler
import closer.vlllage.com.closer.handler.helpers.JsonHandler
import com.queatz.on.On
import com.queatz.on.OnLifecycle
import io.reactivex.subjects.PublishSubject
import org.eclipse.paho.android.service.MqttAndroidClient
import org.eclipse.paho.client.mqttv3.*
import java.nio.charset.Charset

class MqttHandler constructor(private val on: On) : OnLifecycle {

    val events = PublishSubject.create<MqttEvent>()

    private val queue = mutableListOf<() -> Unit>()

    companion object {
        private lateinit var mqttClient: MqttAndroidClient
        const val TAG = "MqttHandler"
        const val MQTT_URI = "tcp://closer.vlllage.com:1883"
    }

    override fun on() {
        mqttClient = MqttAndroidClient(on<ApplicationHandler>().app, MQTT_URI, on<PersistenceHandler>().phone)

        mqttClient.setCallback(object : MqttCallback {
            override fun messageArrived(groupId: String, message: MqttMessage) {
                events.onNext(on<JsonHandler>().from(String(message.payload, Charset.defaultCharset()), MqttEvent::class.java))
                Log.d(TAG, "Receive message: $message from groupId: $groupId")
            }

            override fun connectionLost(cause: Throwable?) {
                Log.d(TAG, "Connection lost $cause")
            }

            override fun deliveryComplete(token: IMqttDeliveryToken?) {

            }
        })

        val options = MqttConnectOptions()
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

    fun subscribe(groupId: String, qos: Int = 1) {
        if (!mqttClient.isConnected) {
            queue.add { subscribe(groupId, qos) }
            return
        }
        try {
            mqttClient.subscribe(groupId, qos, null, object : IMqttActionListener {
                override fun onSuccess(asyncActionToken: IMqttToken?) {
                    Log.d(TAG, "Subscribed to $groupId")
                }

                override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
                    Log.d(TAG, "Failed to subscribe $groupId")
                }
            })
        } catch (e: MqttException) {
            e.printStackTrace()
        }
    }

    fun unsubscribe(groupId: String) {
        if (!mqttClient.isConnected) return

        try {
            mqttClient.unsubscribe(groupId, null, object : IMqttActionListener {
                override fun onSuccess(asyncActionToken: IMqttToken?) {
                    Log.d(TAG, "Unsubscribed to $groupId")
                }

                override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
                    Log.d(TAG, "Failed to unsubscribe $groupId")
                }
            })
        } catch (e: MqttException) {
            e.printStackTrace()
        }
    }

    fun publish(groupId: String, event: MqttEvent, qos: Int = 1, retained: Boolean = false) {
        if (!mqttClient.isConnected) {
            queue.add { publish(groupId, event, qos, retained) }
            return
        }

        val msg = on<JsonHandler>().to(event)

        try {
            val message = MqttMessage()
            message.payload = msg.toByteArray()
            message.qos = qos
            message.isRetained = retained
            mqttClient.publish(groupId, message, null, object : IMqttActionListener {
                override fun onSuccess(asyncActionToken: IMqttToken?) {
                    Log.d(TAG, "$msg published to $groupId")
                }

                override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
                    Log.d(TAG, "Failed to publish $msg to $groupId")
                }
            })
        } catch (e: MqttException) {
            e.printStackTrace()
        }
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

data class MqttEvent constructor(
        val typing: String? = null,
        val stopTyping: String? = null
)