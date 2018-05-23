package closer.vlllage.com.closer.handler.map;

import android.view.View;

import closer.vlllage.com.closer.handler.helpers.ConnectionErrorHandler;
import closer.vlllage.com.closer.pool.PoolMember;

public class NetworkConnectionViewHandler extends PoolMember {

    private static final long NETWORK_CONNECTION_ERROR_TIMEOUT_MS = 5000;

    private View connectionErrorView;
    private Runnable hideCallback = new Runnable() {
        @Override
        public void run() {
            if (connectionErrorView != null) {
                connectionErrorView.setVisibility(View.GONE);
            }
        }
    };

    public void attach(View connectionErrorView) {
        this.connectionErrorView = connectionErrorView;
        $(ConnectionErrorHandler.class).setOnConnectionErrorListener(this::show);
    }

    private void show() {
        connectionErrorView.setVisibility(View.VISIBLE);
        connectionErrorView.removeCallbacks(hideCallback);
        connectionErrorView.postDelayed(hideCallback, NETWORK_CONNECTION_ERROR_TIMEOUT_MS);
    }
}
