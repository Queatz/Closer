package closer.vlllage.com.closer.handler.helpers;

import closer.vlllage.com.closer.pool.PoolMember;

public class ConnectionErrorHandler extends PoolMember {

    private OnConnectionErrorListener onConnectionErrorListener;

    public void setOnConnectionErrorListener(OnConnectionErrorListener onConnectionErrorListener) {
        this.onConnectionErrorListener = onConnectionErrorListener;
    }

    public void connectionError() {
        if (onConnectionErrorListener != null) {
            onConnectionErrorListener.onConnectionError();
            return;
        }

        $(DefaultAlerts.class).syncError();
    }

    public interface OnConnectionErrorListener {
        void onConnectionError();
    }
}
