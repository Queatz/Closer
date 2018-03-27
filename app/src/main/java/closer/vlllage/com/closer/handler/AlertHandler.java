package closer.vlllage.com.closer.handler;

import android.support.annotation.LayoutRes;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;

import closer.vlllage.com.closer.R;
import closer.vlllage.com.closer.pool.PoolMember;
import closer.vlllage.com.closer.util.AlertConfig;
import closer.vlllage.com.closer.util.KeyboardUtil;

public class AlertHandler extends PoolMember {

    @Deprecated
    public void showAlert(@LayoutRes int layout, @StringRes int buttonTitle,
                          @StringRes int title, @Nullable String prefillInput,
                          @Nullable OnSubmitCallback onSubmitCallback) {
        View view = View.inflate($(ActivityHandler.class).getActivity(), layout, null);
        EditText nameEditText = view.findViewById(R.id.input);
        if (prefillInput != null) {
            nameEditText.setText(prefillInput);
        }
        nameEditText.post(nameEditText::requestFocus);
        nameEditText.post(() -> KeyboardUtil.showKeyboard(nameEditText, true));

        final AlertDialog dialog = new AlertDialog.Builder($(ActivityHandler.class).getActivity(), R.style.AppTheme_AlertDialog)
                .setView(view)
                .setPositiveButton(buttonTitle, (d, w) -> {
                    String input = nameEditText.getText().toString();

                    if (onSubmitCallback != null) {
                        onSubmitCallback.onSubmit(input);
                    }
                })
                .create();

        if (title != 0) {
            dialog.setTitle(title);
        }

        dialog.show();

        nameEditText.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_GO) {
                dialog.getButton(AlertDialog.BUTTON_POSITIVE).callOnClick();
                return true;
            }

            return false;
        });
    }

    @Deprecated
    public void showAlert(@StringRes int message, @StringRes int title, int button,
                          @Nullable OnSubmitCallback onSubmitCallback) {
        final AlertDialog dialog = new AlertDialog.Builder($(ActivityHandler.class).getActivity(), R.style.AppTheme_AlertDialog)
                .setPositiveButton(button, (d, w) -> {
                    if (onSubmitCallback != null) {
                        onSubmitCallback.onSubmit(null);
                    }
                })
                .create();

        if (message != 0) {
            dialog.setMessage($(ResourcesHandler.class).getResources().getString(message));
        }

        if (title != 0) {
            dialog.setTitle(title);
        }

        dialog.show();
    }

    public AlertConfig makeAlert() {
        return new AlertConfig<>(this::showAlertConfig);
    }

    public <T> AlertConfig<T> makeAlert(Class<T> clazz) {
        return new AlertConfig<T>(this::showAlertConfig);
    }

    public AlertConfig<String> makeInputAlert() {
        return new AlertConfig<String>(this::showAlertConfig);
    }

    private <T> void showAlertConfig(final AlertConfig<T> alertConfig) {
        final AlertDialog.Builder dialogBuilder = new AlertDialog.Builder($(ActivityHandler.class).getActivity(), R.style.AppTheme_AlertDialog);
        TextView textView = null;
        if (alertConfig.getLayoutResId() != null) {
            View view = View.inflate($(ActivityHandler.class).getActivity(), alertConfig.getLayoutResId(), null);

            if (alertConfig.getTextViewId() != null) {
                textView = view.findViewById(alertConfig.getTextViewId());
                TextView finalTextView = textView;
                textView.post(textView::requestFocus);
                textView.post(() -> KeyboardUtil.showKeyboard(finalTextView, true));

                if (alertConfig.getOnTextViewSubmitCallback() != null) {
                    dialogBuilder.setOnDismissListener(dialogInterface -> {
                        alertConfig.getOnTextViewSubmitCallback().onTextViewSubmit(finalTextView.getText().toString());
                    });
                }
            }

            if (alertConfig.getOnAfterViewCreated() != null) {
                alertConfig.getOnAfterViewCreated().onAfterViewCreated(view);
            }

            dialogBuilder.setView(view);
        }

        if (alertConfig.getPositiveButton() != null) {
            dialogBuilder.setPositiveButton(alertConfig.getPositiveButton(), (d, w) -> {
                if (alertConfig.getPositiveButtonCallback() != null) {
                    alertConfig.getPositiveButtonCallback().onClick(alertConfig.getAlertResult());
                }
            });
        }

        if (alertConfig.getNegativeButton() != null) {
            dialogBuilder.setNegativeButton(alertConfig.getNegativeButton(), (d, w) -> {
                if (alertConfig.getNegativeButtonCallback() != null) {
                    alertConfig.getNegativeButtonCallback().onClick(alertConfig.getAlertResult());
                }
            });
        }

        if (alertConfig.getMessage() != null) {
            dialogBuilder.setMessage(alertConfig.getMessage());
        }

        if (alertConfig.getTitle() != null) {
            dialogBuilder.setTitle(alertConfig.getTitle());
        }

        AlertDialog alertDialog = dialogBuilder.create();

        if (textView != null && textView instanceof EditText) {
            ((EditText) textView).setOnEditorActionListener((v, actionId, event) -> {
                if (actionId == EditorInfo.IME_ACTION_GO) {
                    alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).callOnClick();
                    return true;
                }

                return false;
            });
        }

        alertConfig.show();
    }

    public interface OnSubmitCallback {
        void onSubmit(String input);
    }
}
