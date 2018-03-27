package closer.vlllage.com.closer.util;

import android.view.View;

public class AlertConfig<T> {

    private final ShowCallback showCallback;
    private String title;
    private String message;
    private Integer layoutResId;
    private String positiveButton;
    private ButtonCallback<T> positiveButtonCallback;
    private String negativeButton;
    private ButtonCallback<T> negativeButtonCallback;
    private Integer textView;
    private OnTextViewSubmitCallback onTextViewSubmitCallback;
    private OnAfterViewCreatedCallback onAfterViewCreated;
    private AlertResult<T> alertResult;

    public AlertConfig(ShowCallback showCallback) {
        this.showCallback = showCallback;
    }

    public void show() {
        showCallback.show(this);
    }

    public String getTitle() {
        return title;
    }

    public AlertConfig<T> setTitle(String title) {
        this.title = title;
        return this;
    }

    public String getMessage() {
        return message;
    }

    public AlertConfig<T> setMessage(String message) {
        this.message = message;
        return this;
    }

    public Integer getLayoutResId() {
        return layoutResId;
    }

    public AlertConfig<T> setLayoutResId(Integer layoutResId) {
        this.layoutResId = layoutResId;
        return this;
    }

    public String getPositiveButton() {
        return positiveButton;
    }

    public AlertConfig<T> setPositiveButton(String positiveButton) {
        this.positiveButton = positiveButton;
        return this;
    }

    public ButtonCallback<T> getPositiveButtonCallback() {
        return positiveButtonCallback;
    }

    public AlertConfig<T> setPositiveButtonCallback(ButtonCallback<T> positiveButtonCallback) {
        this.positiveButtonCallback = positiveButtonCallback;
        return this;
    }

    public String getNegativeButton() {
        return negativeButton;
    }

    public AlertConfig<T> setNegativeButton(String negativeButton) {
        this.negativeButton = negativeButton;
        return this;
    }

    public ButtonCallback<T> getNegativeButtonCallback() {
        return negativeButtonCallback;
    }

    public AlertConfig<T> setNegativeButtonCallback(ButtonCallback<T> negativeButtonCallback) {
        this.negativeButtonCallback = negativeButtonCallback;
        return this;
    }

    public Integer getTextViewId() {
        return textView;
    }


    public AlertConfig<T> setTextView(Integer textView, OnTextViewSubmitCallback onTextViewSubmitCallback) {
        this.textView = textView;
        this.onTextViewSubmitCallback = onTextViewSubmitCallback;
        return this;
    }

    public OnAfterViewCreatedCallback getOnAfterViewCreated() {
        return onAfterViewCreated;
    }

    public AlertResult<T> getAlertResult() {
        return alertResult;
    }

    public AlertConfig<T> setAlertResult(AlertResult<T> alertResult) {
        this.alertResult = alertResult;
        return this;
    }

    public OnTextViewSubmitCallback getOnTextViewSubmitCallback() {
        return onTextViewSubmitCallback;
    }

    public AlertConfig<T> setOnAfterViewCreated(OnAfterViewCreatedCallback onAfterViewCreated) {
        this.onAfterViewCreated = onAfterViewCreated;
        return this;
    }

    public interface ShowCallback {
        void show(AlertConfig alertConfig);
    }

    public interface ButtonCallback<T> {
        void onClick(AlertResult<T> alertResult);
    }

    public interface OnAfterViewCreatedCallback {
        void onAfterViewCreated(View view);
    }

    public interface OnTextViewSubmitCallback {
        void onTextViewSubmit(String value);
    }

    public static class AlertResult<T> {
        private T result;

        public T getResult() {
            return result;
        }

        public AlertResult<T> setResult(T result) {
            this.result = result;
            return this;
        }
    }
}
