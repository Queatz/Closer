package closer.vlllage.com.closer.api.models

import closer.vlllage.com.closer.store.models.Pin

class PinResult : ModelResult() {
    var from: String? = null
    var to: String? = null
    var message: GroupMessageResult? = null

    companion object {

        fun from(pinResult: PinResult): Pin {
            val pin = Pin()
            pin.id = pinResult.id
            updateFrom(pin, pinResult)
            return pin
        }

        fun updateFrom(pin: Pin, pinResult: PinResult): Pin {
            pin.from = pinResult.from
            pin.to = pinResult.to
            return pin
        }
    }
}
