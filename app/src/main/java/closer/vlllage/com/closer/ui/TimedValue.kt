package closer.vlllage.com.closer.ui

import java.util.*

class TimedValue<T>(private val trackMsInPast: Int) {
    private val trackedValues = LinkedList<Capture<T>>()

    fun report(value: T) {
        trackedValues.add(Capture(value, Date()))
        cleanup()
    }

    fun get(): T? {
        cleanup()
        return if (trackedValues.isEmpty()) null else trackedValues.first.value
    }

    fun now(): T? {
        cleanup()
        return if (trackedValues.isEmpty()) null else trackedValues.last.value
    }

    private fun cleanup() {
        val now = Date()
        now.time = now.time - trackMsInPast
        while (!trackedValues.isEmpty()) {
            if (!trackedValues.first.time.before(now)) {
                break
            }
            trackedValues.removeFirst()
        }
    }

    private class Capture<T>(internal val value: T, internal val time: Date)
}
