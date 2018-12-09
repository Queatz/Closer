package closer.vlllage.com.closer.handler.helpers;

import android.support.annotation.LayoutRes;
import android.support.annotation.StyleRes;
import android.support.v7.app.AlertDialog;
import android.view.View;

import closer.vlllage.com.closer.R;

public class AlertConfig {

    private final ShowCallback showCallback;
    private String title;
    private String message;
    private @LayoutRes Integer layoutResId;
    private String positiveButton;
    private ButtonCallback positiveButtonCallback;
    private String negativeButton;
    private ButtonCallback negativeButtonCallback;
    private Integer textView;
    private OnTextViewSubmitCallback onTextViewSubmitCallback;
    private OnAfterViewCreatedCallback onAfterViewCreated;
    private ButtonClickCallback buttonClickCallback;
    private Object alertResult;
    private @StyleRes int theme = R.style.AppTheme_AlertDialog;
    private AlertDialog dialog;

    public AlertConfig(ShowCallback showCallback) {
        this.showCallback = showCallback;
    }

    public void show() {
        showCallback.show(this);
    }

    public String getTitle() {
        return title;
    }

    public AlertConfig setTitle(String title) {
        this.title = title;
        return this;
    }

    public String getMessage() {
        return message;
    }

    public AlertConfig setMessage(String message) {
        this.message = message;
        return this;
    }

    public Integer getLayoutResId() {
        return layoutResId;
    }

    public AlertConfig setLayoutResId(@LayoutRes Integer layoutResId) {
        this.layoutResId = layoutResId;
        return this;
    }

    public String getPositiveButton() {
        return positiveButton;
    }

    public AlertConfig setPositiveButton(String positiveButton) {
        this.positiveButton = positiveButton;
        return this;
    }

    public ButtonCallback getPositiveButtonCallback() {
        return positiveButtonCallback;
    }

    public AlertConfig setPositiveButtonCallback(ButtonCallback positiveButtonCallback) {
        this.positiveButtonCallback = positiveButtonCallback;
        return this;
    }

    public String getNegativeButton() {
        return negativeButton;
    }

    public AlertConfig setNegativeButton(String negativeButton) {
        this.negativeButton = negativeButton;
        return this;
    }

    public ButtonCallback getNegativeButtonCallback() {
        return negativeButtonCallback;
    }

    public AlertConfig setNegativeButtonCallback(ButtonCallback negativeButtonCallback) {
        this.negativeButtonCallback = negativeButtonCallback;
        return this;
    }

    public ButtonClickCallback getButtonClickCallback() {
        return buttonClickCallback;
    }

    public AlertConfig setButtonClickCallback(ButtonClickCallback buttonClickCallback) {
        this.buttonClickCallback = buttonClickCallback;
        return this;
    }

    public Integer getTextViewId() {
        return textView;
    }

    public AlertConfig setTextView(Integer textView, OnTextViewSubmitCallback onTextViewSubmitCallback) {
        this.textView = textView;
        this.onTextViewSubmitCallback = onTextViewSubmitCallback;
        return this;
    }

    public OnAfterViewCreatedCallback getOnAfterViewCreated() {
        return onAfterViewCreated;
    }

    public Object getAlertResult() {
        return alertResult;
    }

    public AlertConfig setAlertResult(Object alertResult) {
        this.alertResult = alertResult;
        return this;
    }

    public OnTextViewSubmitCallback getOnTextViewSubmitCallback() {
        return onTextViewSubmitCallback;
    }

    public AlertConfig setOnAfterViewCreated(OnAfterViewCreatedCallback onAfterViewCreated) {
        this.onAfterViewCreated = onAfterViewCreated;
        return this;
    }

    public int getTheme() {
        return theme;
    }

    public AlertConfig setTheme(int theme) {
        this.theme = theme;
        return this;
    }

    public void setDialog(AlertDialog dialog) {
        this.dialog = dialog;
    }

    public AlertDialog getDialog() {
        return dialog;
    }

    public interface ShowCallback {
        void show(AlertConfig alertConfig);
    }

    public interface ButtonCallback {
        void onClick(Object alertResult);
    }

    public interface ButtonClickCallback {
        boolean onButtonClick(Object alertResult);
    }

    public interface OnAfterViewCreatedCallback {
        void onAfterViewCreated(AlertConfig alertConfig, View view);
    }

    public interface OnTextViewSubmitCallback {
        void onTextViewSubmit(String value);
    }
}
