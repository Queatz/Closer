package closer.vlllage.com.closer.ui;

import android.support.annotation.NonNull;
import android.view.MotionEvent;
import android.view.View;

public class DragTriggerView {

    public DragTriggerView(@NonNull final View view, @NonNull OnDragEventListener onDragEventListener) {

        view.setClickable(true);
        view.setOnTouchListener(new View.OnTouchListener() {

            private TimedValue<Float> trackedX = new TimedValue<>(75);
            private TimedValue<Float> trackedY = new TimedValue<>(75);

            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                switch (motionEvent.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        trackedX.report(motionEvent.getRawX());
                        trackedY.report(motionEvent.getRawY());
                        onDragEventListener.onDragStart(trackedX, trackedY);
                        break;
                    case MotionEvent.ACTION_MOVE:
                        trackedX.report(motionEvent.getRawX());
                        trackedY.report(motionEvent.getRawY());
                        onDragEventListener.onDragUpdate(trackedX, trackedY);
                        break;
                    case MotionEvent.ACTION_UP:
                        trackedX.report(motionEvent.getRawX());
                        trackedY.report(motionEvent.getRawY());
                        onDragEventListener.onDragRelease(trackedX, trackedY);
                        break;
                }

                return true;
            }
        });
    }

    public interface OnDragEventListener {
        void onDragStart(TimedValue<Float> x, TimedValue<Float> y);
        void onDragRelease(TimedValue<Float> x, TimedValue<Float> y);
        void onDragUpdate(TimedValue<Float> x, TimedValue<Float> y);
    }
}
