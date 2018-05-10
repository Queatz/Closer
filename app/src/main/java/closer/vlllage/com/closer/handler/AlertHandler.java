package closer.vlllage.com.closer.handler;

import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.TextView;
import android.widget.Toast;

import closer.vlllage.com.closer.pool.PoolMember;
import closer.vlllage.com.closer.util.AlertConfig;
import closer.vlllage.com.closer.util.KeyboardUtil;

public class AlertHandler extends PoolMember {

    public AlertConfig make() {
        return new AlertConfig(this::showAlertConfig);
    }

    private void showAlertConfig(final AlertConfig alertConfig) {
        if (!$(ActivityHandler.class).isPresent()) {
            if (alertConfig.getMessage() != null) {
                Toast.makeText($(ApplicationHandler.class).getApp(), alertConfig.getMessage(), Toast.LENGTH_SHORT).show();
            }
            return;
        }

        final AlertDialog.Builder dialogBuilder = new AlertDialog.Builder($(ActivityHandler.class).getActivity(), alertConfig.getTheme());
        TextView textView = null;
        if (alertConfig.getLayoutResId() != null) {
            View view = View.inflate($(ActivityHandler.class).getActivity(), alertConfig.getLayoutResId(), null);

            if (alertConfig.getTextViewId() != null) {
                textView = view.findViewById(alertConfig.getTextViewId());
                TextView finalTextView = textView;
                textView.post(textView::requestFocus);
                textView.post(() -> KeyboardUtil.showKeyboard(finalTextView, true));
                dialogBuilder.setOnDismissListener(dialogInterface -> KeyboardUtil.showKeyboard(finalTextView, false));
            }

            if (alertConfig.getOnAfterViewCreated() != null) {
                alertConfig.getOnAfterViewCreated().onAfterViewCreated(alertConfig, view);
            }

            dialogBuilder.setView(view);
        }

        if (alertConfig.getPositiveButton() != null) {
            TextView finalTextView = textView;
            dialogBuilder.setPositiveButton(alertConfig.getPositiveButton(), (d, w) -> {
                if (alertConfig.getOnTextViewSubmitCallback() != null && finalTextView != null) {
                    alertConfig.getOnTextViewSubmitCallback().onTextViewSubmit(finalTextView.getText().toString());
                }

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

        if (textView != null) {
            textView.setOnEditorActionListener((v, actionId, event) -> {
                if (actionId == EditorInfo.IME_ACTION_GO) {
                    alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).callOnClick();
                    return true;
                }

                return false;
            });
        }

        if (alertConfig.getButtonClickCallback() != null) {
            alertDialog.setOnShowListener(dialog -> {
                alertDialog.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener(view -> {
                    if (alertConfig.getButtonClickCallback().onButtonClick(alertConfig.getAlertResult())) {
                        if (alertConfig.getPositiveButtonCallback() != null) {
                            alertConfig.getPositiveButtonCallback().onClick(alertConfig.getAlertResult());
                            dialog.dismiss();
                        }
                    }
                });
                alertDialog.getButton(DialogInterface.BUTTON_NEGATIVE).setOnClickListener(view -> {
                    if (alertConfig.getButtonClickCallback().onButtonClick(alertConfig.getAlertResult())) {
                        if (alertConfig.getNegativeButtonCallback() != null) {
                            alertConfig.getNegativeButtonCallback().onClick(alertConfig.getAlertResult());
                            dialog.dismiss();
                        }
                    }
                });
            });
        }

        alertDialog.show();
    }

    public interface OnSubmitCallback {
        void onSubmit(String input);
    }
}
