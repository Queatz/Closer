package closer.vlllage.com.closer.handler.group;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.support.annotation.NonNull;
import android.text.Editable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.style.ClickableSpan;
import android.text.style.ImageSpan;
import android.util.TypedValue;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import closer.vlllage.com.closer.R;
import closer.vlllage.com.closer.handler.helpers.ActivityHandler;
import closer.vlllage.com.closer.handler.helpers.ResourcesHandler;
import closer.vlllage.com.closer.handler.phone.NameHandler;
import closer.vlllage.com.closer.handler.phone.PhoneMessagesHandler;
import closer.vlllage.com.closer.pool.PoolMember;
import closer.vlllage.com.closer.store.StoreHandler;
import closer.vlllage.com.closer.store.models.Phone;
import closer.vlllage.com.closer.store.models.Phone_;

public class GroupMessageParseHandler extends PoolMember {

    private Pattern mentionPattern = Pattern.compile("@[0-9]+");

    public String parseString(String groupMessage) {
        return parseString(groupMessage, getDefaultMentionConverter());
    }

    public String parseString(String groupMessage, MentionConverter mentionConverter) {
        StringBuilder builder = new StringBuilder();
        builder.append(groupMessage);

        Matcher matcher = mentionPattern.matcher(groupMessage);

        while (matcher.find()) {
            String match = matcher.group();
            final String mention = match.substring(1);
            builder.replace(matcher.start(), matcher.end(), "@" + mentionConverter.convert(mention));
        }

        return builder.toString();
    }

    public CharSequence parseText(String groupMessage) {
        return parseText(groupMessage, getDefaultMentionConverter(), getDefaultMentionClickListener());
    }

    public CharSequence parseText(String groupMessage, MentionConverter mentionConverter, OnMentionClickListener onMentionClickListener) {
        SpannableStringBuilder builder = new SpannableStringBuilder();
        builder.append(groupMessage);

        Matcher matcher = mentionPattern.matcher(groupMessage);

        while (matcher.find()) {
            String match = matcher.group();
            final String mention = match.substring(1);
            ImageSpan span = makeImageSpan(mentionConverter.convert(mention));
            ClickableSpan clickableSpan = new ClickableSpan() {
                @Override
                public void onClick(@NonNull View widget) {
                    onMentionClickListener.onMentionClick(mention);
                }
            };
            builder.setSpan(span, matcher.start(), matcher.end(), SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE);
            builder.setSpan(clickableSpan, matcher.start(), matcher.end(), SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE);
        }

        return builder;
    }

    public void insertMention(EditText editText, Phone mention) {
        CharSequence replaceString = extractName(editText.getText(), editText.getSelectionStart());

        if (replaceString == null) {
            replaceString = "";
        }

        editText.getText().replace(editText.getSelectionStart() - replaceString.length(), editText.getSelectionStart(), "@" + mention.getId());
        editText.getText().setSpan(makeImageSpan(mention.getName()),
                editText.getSelectionStart() - mention.getId().length() - 1, editText.getSelectionStart(),
                SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE);
    }

    public boolean isMentionSelected(EditText editText) {
        Editable text = editText.getText();
        ImageSpan[] styleSpans = text.getSpans(editText.getSelectionStart(), editText.getSelectionEnd(), ImageSpan.class);
        return styleSpans.length > 0;
    }

    public boolean deleteMention(EditText editText) {
        if (editText.getSelectionStart() <= 0) return true;

        Editable text = editText.getText();
        ImageSpan[] styleSpans = text.getSpans(editText.getSelectionStart() - 1, editText.getSelectionEnd() - 1, ImageSpan.class);

        for(ImageSpan span : styleSpans) {
            int start = text.getSpanStart(span);
            int end = text.getSpanEnd(span);

            editText.getText().delete(start, end);
            editText.setSelection(start);
            return false;
        }

        return true;
    }

    public ImageSpan makeImageSpan(String name) {
        TextView textView = createContactTextView(name);
        BitmapDrawable bitmapDrawable = convertViewToDrawable(textView);
        bitmapDrawable.setBounds(0, 0, bitmapDrawable.getIntrinsicWidth(),bitmapDrawable.getIntrinsicHeight());
        return new ImageSpan(bitmapDrawable);
    }

    public CharSequence extractName(Editable text, int position) {
        if (position > 0 && position <= text.length()) {
            for (int i = position - 1; i >= 0; i--) {
                if (text.charAt(i) == '@') {
                    return text.subSequence(i, position);
                } else if (Character.isWhitespace(text.charAt(i))) {
                    return null;
                }
            }
        }

        return null;
    }

    private MentionConverter getDefaultMentionConverter() {
        return mention -> {
            List<Phone> phoneList = $(StoreHandler.class).getStore().box(Phone.class).find(Phone_.id, mention);
            if (phoneList.isEmpty()) {
                return $(ResourcesHandler.class).getResources().getString(R.string.unknown);
            }
            return $(NameHandler.class).getName(phoneList.get(0));
        };
    }

    private OnMentionClickListener getDefaultMentionClickListener() {
        return mention -> {
            List<Phone> phoneList = $(StoreHandler.class).getStore().box(Phone.class).find(Phone_.id, mention);
            String name;
            if (phoneList.isEmpty()) {
                name = $(ResourcesHandler.class).getResources().getString(R.string.unknown);
            } else {
                name = $(NameHandler.class).getName(phoneList.get(0));
            }

            $(PhoneMessagesHandler.class).openMessagesWithPhone(mention, name, "");
        };
    }

    private TextView createContactTextView(String text) {
        TextView textView = new TextView($(ActivityHandler.class).getActivity());
        textView.setText("@" + text);
        textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, $(ResourcesHandler.class).getResources().getDimension(R.dimen.groupMessageMentionTextSize));
        textView.setTextColor($(ResourcesHandler.class).getResources().getColor(R.color.colorAccentLight));
        textView.setTypeface(textView.getTypeface(), Typeface.BOLD);
        return textView;
    }

    private BitmapDrawable convertViewToDrawable(View view) {
        int spec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
        view.measure(spec, spec);
        view.layout(0, 0, view.getMeasuredWidth(), view.getMeasuredHeight());
        Bitmap b = Bitmap.createBitmap(view.getMeasuredWidth(), view.getMeasuredHeight(),
                Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(b);
        c.translate(-view.getScrollX(), -view.getScrollY());
        view.draw(c);
        view.setDrawingCacheEnabled(true);
        Bitmap cacheBmp = view.getDrawingCache();
        Bitmap viewBmp = cacheBmp.copy(Bitmap.Config.ARGB_8888, true);
        view.destroyDrawingCache();
        return new BitmapDrawable($(ResourcesHandler.class).getResources(), viewBmp);
    }

    public interface MentionConverter {
        String convert(String mention);
    }

    public interface OnMentionClickListener {
        void onMentionClick(String mention);
    }
}
