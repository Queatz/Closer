package closer.vlllage.com.closer.handler.map;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.TranslateAnimation;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import closer.vlllage.com.closer.R;
import closer.vlllage.com.closer.handler.data.ApiHandler;
import closer.vlllage.com.closer.handler.bubble.MapBubble;
import closer.vlllage.com.closer.handler.helpers.DefaultAlerts;
import closer.vlllage.com.closer.handler.helpers.DisposableHandler;
import closer.vlllage.com.closer.handler.helpers.KeyboardHandler;
import closer.vlllage.com.closer.handler.helpers.OutboundHandler;
import closer.vlllage.com.closer.handler.helpers.ResourcesHandler;
import closer.vlllage.com.closer.handler.helpers.Val;
import closer.vlllage.com.closer.pool.PoolMember;
import closer.vlllage.com.closer.ui.AnimationDuration;

public class ReplyLayoutHandler extends PoolMember {

    private View replyLayout;
    private TextView replyLayoutName;
    private TextView replyLayoutStatus;
    private View sendButton;
    private EditText replyMessage;
    private ImageButton getDirectionsButton;

    private MapBubble replyingToMapBubble;

    public void attach(View replyLayout) {
        this.replyLayout = replyLayout;
        this.sendButton = replyLayout.findViewById(R.id.sendButton);
        this.replyMessage = replyLayout.findViewById(R.id.message);
        this.replyLayoutName = replyLayout.findViewById(R.id.replyLayoutName);
        this.replyLayoutStatus = replyLayout.findViewById(R.id.replyLayoutStatus);
        this.getDirectionsButton = replyLayout.findViewById(R.id.getDirectionsButton);

        sendButton.setOnClickListener(view -> reply());

        replyMessage.setOnEditorActionListener((textView, action, keyEvent) -> {
            if (action == EditorInfo.IME_ACTION_GO) {
                reply();
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

        getDirectionsButton.setOnClickListener(view -> {
            $(OutboundHandler.class).openDirections(replyingToMapBubble.getLatLng());
        });
    }

    private void reply() {
        $(DisposableHandler.class).add($(ApiHandler.class).sendMessage(replyingToMapBubble.getPhone(), replyMessage.getText().toString()).subscribe(successResult -> {
            if (!successResult.success) {
                $(DefaultAlerts.class).thatDidntWork();
            }
        }, error -> $(DefaultAlerts.class).thatDidntWork()));
        replyMessage.setText("");
        showReplyLayout(false);
    }

    public void replyTo(MapBubble mapBubble) {
        replyingToMapBubble = mapBubble;

        replyLayoutName.setText($(Val.class).of(replyingToMapBubble.getName(), $(ResourcesHandler.class).getResources().getString(R.string.app_name)));

        replyLayoutStatus.setText(replyingToMapBubble.getStatus());

        if (replyingToMapBubble.getLatLng() != null) {
            $(MapHandler.class).centerMap(replyingToMapBubble.getLatLng());
        }

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
        ViewGroup.MarginLayoutParams layoutParams = (ViewGroup.MarginLayoutParams) replyLayout.getLayoutParams();
        int totalHeight = replyLayout.getMeasuredHeight() + layoutParams.topMargin + layoutParams.bottomMargin;

        if (show) {
            animation = new TranslateAnimation(0, 0, -totalHeight, 0);
            animation.setInterpolator(new AccelerateDecelerateInterpolator());
            animation.setDuration(AnimationDuration.ENTER_DURATION);
            replyLayout.post(() -> {
                replyMessage.requestFocus();
                $(KeyboardHandler.class).showKeyboard(replyMessage, true);
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
            $(KeyboardHandler.class).showKeyboard(replyMessage, false);
        }

        replyLayout.startAnimation(animation);
    }

    public int getHeight() {
        return replyLayout.getMeasuredHeight();
    }
}
