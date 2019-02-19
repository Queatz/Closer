package closer.vlllage.com.closer.api.models;

import closer.vlllage.com.closer.store.models.Pin;

public class PinResult extends ModelResult {
    public String from;
    public String to;
    public GroupMessageResult message;

    public static Pin from(PinResult pinResult) {
        Pin pin = new Pin();
        pin.setId(pinResult.id);
        updateFrom(pin, pinResult);
        return pin;
    }

    public static Pin updateFrom(Pin pin, PinResult pinResult) {
        pin.setFrom(pinResult.from);
        pin.setTo(pinResult.to);
        return pin;
    }
}
