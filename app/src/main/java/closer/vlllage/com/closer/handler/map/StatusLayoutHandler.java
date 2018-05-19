package closer.vlllage.com.closer.handler.map;

import android.content.res.Resources;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;

import closer.vlllage.com.closer.R;
import closer.vlllage.com.closer.handler.data.AccountHandler;
import closer.vlllage.com.closer.handler.helpers.ActivityHandler;
import closer.vlllage.com.closer.handler.helpers.KeyboardHandler;
import closer.vlllage.com.closer.pool.PoolMember;

public class StatusLayoutHandler extends PoolMember {

    private View myStatusLayout;
    private Button myStatusVisibleButton;
    private EditText myStatusEditText;

    private boolean amIVisible;
    private String tentativeStatus;
    private String currentStatus;

    public void attach(View myStatusLayout) {
        this.myStatusLayout = myStatusLayout;
        this.myStatusVisibleButton = myStatusLayout.findViewById(R.id.visibleButton);
        this.myStatusEditText = myStatusLayout.findViewById(R.id.currentStatus);

        myStatusEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence text, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence text, int start, int before, int count) {
                tentativeStatus = text.toString();
                updateStatusButton();
            }

            @Override
            public void afterTextChanged(Editable text) {

            }
        });

        myStatusEditText.setOnFocusChangeListener((view, focused) -> {
            if (focused) {
                tentativeStatus = myStatusEditText.getText().toString();
            } else {
                tentativeStatus = null;
            }
        });

        myStatusVisibleButton.setOnClickListener(v -> {
            if (tentativeStatus == null || tentativeStatus.trim().isEmpty()) {
                amIVisible = false;
                $(AccountHandler.class).updateActive(amIVisible);
                myStatusEditText.setText("");
            } else {
                setCurrentStatus(tentativeStatus);
                tentativeStatus = null;
                amIVisible = true;
                $(AccountHandler.class).updateActive(amIVisible);
            }

            updateStatusButton();
            $(KeyboardHandler.class).showKeyboard(myStatusEditText, false);
        });

        amIVisible = $(AccountHandler.class).getActive();
        myStatusEditText.setText($(AccountHandler.class).getStatus());

        myStatusEditText.setOnEditorActionListener((textView, action, keyEvent) -> {
            if (action == EditorInfo.IME_ACTION_GO) {
                myStatusVisibleButton.callOnClick();
            }

            return false;
        });

        if (amIVisible) {
            tentativeStatus = null;
        }

        updateStatusButton();
    }

    private void updateStatusButton() {
        Resources resources = $(ActivityHandler.class).getActivity().getResources();

        if (!amIVisible) {
            if (tentativeStatus == null || tentativeStatus.trim().isEmpty()) {
                myStatusVisibleButton.setText(R.string.off);
                myStatusVisibleButton.setTextColor(resources.getColor(R.color.disabled));
            } else {
                myStatusVisibleButton.setText(R.string.turn_on);
                myStatusVisibleButton.setTextColor(resources.getColor(R.color.colorPrimary));
            }
        } else {
            if (tentativeStatus == null || tentativeStatus.trim().isEmpty()) {
                myStatusVisibleButton.setText(R.string.turn_off);
                myStatusVisibleButton.setTextColor(resources.getColor(R.color.disabled));
            } else {
                myStatusVisibleButton.setText(R.string.update_name);
                myStatusVisibleButton.setTextColor(resources.getColor(R.color.colorPrimary));
            }
        }
    }

    public void visible(boolean visible) {
        myStatusLayout.setVisibility(View.VISIBLE);

        Animation animation;

        if (visible) {
            animation = new ScaleAnimation(0, 1, 0, 1, Animation.RELATIVE_TO_SELF, .5f, Animation.RELATIVE_TO_SELF, .5f);
        } else {
            animation = new ScaleAnimation(1, 0, 1, 0, Animation.RELATIVE_TO_SELF, .5f, Animation.RELATIVE_TO_SELF, .5f);
            animation.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {

                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    myStatusLayout.setVisibility(View.GONE);
                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });
        }

        animation.setDuration(45);
        animation.setInterpolator(new AccelerateDecelerateInterpolator());

        myStatusLayout.startAnimation(animation);
    }

    public void setCurrentStatus(String currentStatus) {
        this.currentStatus = currentStatus;
        $(AccountHandler.class).updateStatus(currentStatus);

        if ($(MyBubbleHandler.class).getMyBubble() != null) {
            $(MapHandler.class).centerMap($(MyBubbleHandler.class).getMyBubble().getLatLng());
        }
    }
}
