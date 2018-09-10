package closer.vlllage.com.closer.ui;

import java.util.Date;
import java.util.LinkedList;

public class TimedValue<T> {
    private final int trackMsInPast;
    private LinkedList<Capture<T>> trackedValues = new LinkedList<>();

    public TimedValue(int trackMsInPast) {
        this.trackMsInPast = trackMsInPast;
    }

    public void report(T value) {
        trackedValues.add(new Capture<>(value, new Date()));
        cleanup();
    }

    public T get() {
        cleanup();
        return trackedValues.isEmpty() ? null : trackedValues.getFirst().value;
    }

    public T now() {
        cleanup();
        return trackedValues.isEmpty() ? null : trackedValues.getLast().value;
    }

    private void cleanup() {
        Date now = new Date();
        now.setTime(now.getTime() - trackMsInPast);
        while (!trackedValues.isEmpty()) {
            if (!trackedValues.getFirst().time.before(now)) {
                break;
            }
            trackedValues.removeFirst();
        }
    }

    private static class Capture<T> {
        final T value;
        final Date time;

        public Capture(T value, Date time) {
            this.value = value;
            this.time = time;
        }
    }
}
