package closer.vlllage.com.closer.handler;

import android.support.v7.app.AlertDialog;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;

import closer.vlllage.com.closer.R;
import closer.vlllage.com.closer.pool.PoolMember;
import closer.vlllage.com.closer.util.KeyboardUtil;

public class SetNameHandler extends PoolMember {
    public void modifyName() {
        View view = View.inflate($(ActivityHandler.class).getActivity(), R.layout.set_name_modal, null);
        EditText nameEditText = view.findViewById(R.id.name);
        nameEditText.setText($(AccountHandler.class).getName());
        nameEditText.post(nameEditText::requestFocus);
        nameEditText.post(() -> KeyboardUtil.showKeyboard(nameEditText, true));

        final AlertDialog dialog = new AlertDialog.Builder($(ActivityHandler.class).getActivity())
                .setView(view)
                .setPositiveButton(R.string.update_name, (d, w) -> {
                    String name = nameEditText.getText().toString();
                    $(AccountHandler.class).updateName(name);
                })
                .create();

        dialog.show();

        nameEditText.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_GO) {
                dialog.getButton(AlertDialog.BUTTON_POSITIVE).callOnClick();
                return true;
            }

            return false;
        });
    }
}
