package closer.vlllage.com.closer.handler.helpers;

import android.view.View;

import closer.vlllage.com.closer.pool.PoolMember;

public class ViewAttributeHandler extends PoolMember {
    public void linkPadding(View targetView, View sourceView) {
        sourceView.addOnLayoutChangeListener((v, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom) -> {
            targetView.setPadding(sourceView.getPaddingLeft(), sourceView.getPaddingTop(),
                    sourceView.getPaddingRight(), sourceView.getPaddingBottom());
        });
    }
}
