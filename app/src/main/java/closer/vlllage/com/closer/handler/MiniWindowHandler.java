package closer.vlllage.com.closer.handler;

import android.support.constraint.ConstraintLayout;
import android.view.View;

import closer.vlllage.com.closer.R;
import closer.vlllage.com.closer.pool.PoolMember;

public class MiniWindowHandler extends PoolMember {
    public void attach(View toggleView, View windowView) {
        toggleView.setOnClickListener(view -> {
            ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) windowView.getLayoutParams();
            int miniWindowHeight = $(ResourcesHandler.class).getResources().getDimensionPixelSize(R.dimen.miniWindowHeight);
            int miniWindowTopMargin = $(ResourcesHandler.class).getResources().getDimensionPixelSize(R.dimen.miniWindowTopMargin);
            if (miniWindowHeight == params.matchConstraintMaxHeight) {
                params.matchConstraintMaxHeight = miniWindowHeight * 3;
                params.topMargin = 0;
            } else {
                params.matchConstraintMaxHeight = miniWindowHeight;
                params.topMargin = miniWindowTopMargin;
            }

            windowView.setLayoutParams(params);
        });
    }
}
