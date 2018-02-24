package closer.vlllage.com.closer.handler;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.TranslateAnimation;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;

import closer.vlllage.com.closer.R;
import closer.vlllage.com.closer.handler.bubble.MapBubble;
import closer.vlllage.com.closer.pool.PoolMember;
import closer.vlllage.com.closer.util.AnimationDuration;
import closer.vlllage.com.closer.util.KeyboardUtil;

public class ReplyLayoutHandler extends PoolMember {

    private View replyLayout;
    private TextView replyLayoutName;
    private TextView replyLayoutStatus;
    private View sendButton;
    private EditText replyMessage;

    private MapBubble replyingToMapBubble;

    public void attach(View replyLayout) {
        this.replyLayout = replyLayout;
        this.sendButton = replyLayout.findViewById(R.id.sendButton);
        this.replyMessage = replyLayout.findViewById(R.id.message);
        this.replyLayoutName = replyLayout.findViewById(R.id.replyLayoutName);
        this.replyLayoutStatus = replyLayout.findViewById(R.id.replyLayoutStatus);

        sendButton.setOnClickListener(view -> {
            $(NotificationHandler.class).showNotification(replyingToMapBubble);
            showReplyLayout(false);
        });

        replyMessage.setOnEditorActionListener((textView, action, keyEvent) -> {
            if (action == EditorInfo.IME_ACTION_GO) {
                $(NotificationHandler.class).showNotification(replyingToMapBubble);
                showReplyLayout(false);
            }

            return false;
        });

        replyMessage.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                sendButton.setEnabled(!charSequence.toString().trim().isEmpty());
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
    }

    public void replyTo(MapBubble mapBubble) {
        replyingToMapBubble = mapBubble;

        if (replyingToMapBubble.getName().isEmpty()) {
            replyLayoutName.setVisibility(View.GONE);
        } else {
            replyLayoutName.setVisibility(View.VISIBLE);
            replyLayoutName.setText(replyingToMapBubble.getName());
        }

        replyLayoutStatus.setText(replyingToMapBubble.getStatus());
        $(MapHandler.class).centerMap(replyingToMapBubble.getLatLng());
        showReplyLayout(true);
    }

    public boolean isVisible() {
        return replyLayout.getVisibility() != View.GONE;
    }

    public void showReplyLayout(boolean show) {
        if (show) {
            if (replyLayout.getVisibility() == View.VISIBLE && (replyLayout.getAnimation() == null || (replyLayout.getAnimation() != null && replyLayout.getAnimation().getDuration() == AnimationDuration.ENTER_DURATION))) {
                return;
            }
        } else {
            if (replyLayout.getVisibility() == View.GONE) {
                return;
            }
        }

        replyLayout.setVisibility(View.VISIBLE);
        replyMessage.setText("");
        sendButton.setEnabled(false);
        Animation animation;

        replyLayout.clearAnimation();
        RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) replyLayout.getLayoutParams();
        int totalHeight = replyLayout.getMeasuredHeight() + layoutParams.topMargin + layoutParams.bottomMargin;

        if (show) {
            animation = new TranslateAnimation(0, 0, -totalHeight, 0);
            animation.setInterpolator(new AccelerateDecelerateInterpolator());
            animation.setDuration(AnimationDuration.ENTER_DURATION);
            replyLayout.post(() -> {
                replyMessage.requestFocus();
                KeyboardUtil.showKeyboard(replyMessage, true);
            });
        } else {
            animation = new TranslateAnimation(0, 0, replyLayout.getTranslationY(), -totalHeight);
            animation.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {

                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    replyLayout.setVisibility(View.GONE);
                    replyLayout.setAnimation(null);
                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });
            animation.setInterpolator(new DecelerateInterpolator());
            animation.setDuration(AnimationDuration.EXIT_DURATION);
        }

        replyLayout.startAnimation(animation);

        $(StatusLayoutHandler.class).visible(!show);
    }
}
