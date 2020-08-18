package closer.vlllage.com.closer.handler.group

import closer.vlllage.com.closer.handler.data.PersistenceHandler
import closer.vlllage.com.closer.handler.helpers.ApplicationHandler
import closer.vlllage.com.closer.handler.helpers.DisposableHandler
import closer.vlllage.com.closer.handler.mqtt.MqttHandler
import closer.vlllage.com.closer.handler.mqtt.events.TypingMqttEvent
import com.queatz.on.On
import com.queatz.on.OnLifecycle
import io.reactivex.subjects.BehaviorSubject

class TypingHandler constructor(private val on: On) : OnLifecycle {

    var isTyping = false
        set(value) {
            val changed = value != field
            field = value

            if (changed) {
                send()
            }
        }

    val whoIsTyping = BehaviorSubject.createDefault<Set<String>>(setOf())

    private var groupId: String? = null

    private val mqtt = on<ApplicationHandler>().app.on<MqttHandler>()
    private val disposableGroup = on<DisposableHandler>().group()

    fun setGroup(groupId: String) {
        disposableGroup.clear()

        if (isTyping) {
            isTyping = false
            send()
        }

        this.groupId?.let { mqtt.unsubscribe("group/$it/typing") }

        this.groupId = groupId

        whoIsTyping.onNext(setOf())

        mqtt.subscribe("group/$groupId/typing")

        mqtt.events("group/$groupId/typing", TypingMqttEvent::class).subscribe({
            val modified = whoIsTyping.value!!.toMutableSet()

            it.stopTyping?.let { modified.remove(it) }
            it.typing?.let { modified.add(it) }

            whoIsTyping.onNext(modified)
        }, {}).also { disposableGroup.add(it) }
    }

    override fun off() {
        disposableGroup.clear()
        groupId?.let { mqtt.unsubscribe("group/$it/typing") }
    }

    private fun send() {
        groupId ?: return

        val me = on<PersistenceHandler>().phoneId!!

        if (isTyping) {
            mqtt.publish("group/$groupId/typing", TypingMqttEvent(typing = me))
        } else {
            mqtt.publish("group/$groupId/typing", TypingMqttEvent(stopTyping = me))
        }
    }
}