package closer.vlllage.com.closer.ui;

import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;

import closer.vlllage.com.closer.pool.PoolMember;

public class Animate extends PoolMember {
    public void alpha(View view, boolean show) {
        view.clearAnimation();

        if (show) {
            if (view.getVisibility() == View.GONE) {
                AlphaAnimation animation = new AlphaAnimation(0, 1);
                animation.setDuration(195);
                view.startAnimation(animation);
                view.setVisibility(View.VISIBLE);
            } else {
                AlphaAnimation animation = new AlphaAnimation(view.getAlpha(), 1);
                animation.setDuration(195);
                view.startAnimation(animation);
            }
        } else {
            if (view.getVisibility() == View.GONE) {

            } else {
                AlphaAnimation animation = new AlphaAnimation(view.getAlpha(), 0);
                animation.setDuration(225);
                animation.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        view.setVisibility(View.GONE);
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {

                    }
                });
                view.startAnimation(animation);
            }
        }
    }
}
