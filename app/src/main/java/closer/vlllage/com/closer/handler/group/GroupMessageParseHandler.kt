package closer.vlllage.com.closer.handler.group

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Typeface
import android.graphics.drawable.BitmapDrawable
import android.text.Editable
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.style.ClickableSpan
import android.text.style.ImageSpan
import android.util.TypedValue
import android.view.View
import android.widget.EditText
import android.widget.TextView
import closer.vlllage.com.closer.R
import closer.vlllage.com.closer.handler.helpers.ActivityHandler
import closer.vlllage.com.closer.handler.helpers.ResourcesHandler
import closer.vlllage.com.closer.handler.phone.NameHandler
import closer.vlllage.com.closer.handler.phone.PhoneMessagesHandler
import closer.vlllage.com.closer.store.StoreHandler
import closer.vlllage.com.closer.store.models.Phone
import closer.vlllage.com.closer.store.models.Phone_
import com.queatz.on.On
import java.util.regex.Pattern

class GroupMessageParseHandler constructor(private val on: On) {

    private val mentionPattern = Pattern.compile("@[0-9]+")

    private val defaultMentionConverter: MentionConverter
        get() = { mention ->
            val phoneList = on<StoreHandler>().store.box(Phone::class.java).query().equal(Phone_.id, mention).build().find()
            if (phoneList.isEmpty()) {
                on<ResourcesHandler>().resources.getString(R.string.unknown)
            }
            else {
                on<NameHandler>().getName(phoneList[0])
            }
        }

    private val defaultMentionClickListener: OnMentionClickListener
        get() = { mention ->
            on<PhoneMessagesHandler>().openMessagesWithPhone(mention, on<NameHandler>().getName(mention), "")
        }

    @JvmOverloads
    fun parseString(groupMessage: String, mentionConverter: MentionConverter = defaultMentionConverter): String {
        val builder = StringBuilder()
        builder.append(groupMessage)

        val matcher = mentionPattern.matcher(groupMessage)

        while (matcher.find()) {
            val match = matcher.group()
            val mention = match.substring(1)
            builder.replace(matcher.start(), matcher.end(), "@" + mentionConverter.invoke(mention))
        }

        return builder.toString()
    }

    @JvmOverloads
    fun parseText(groupMessage: String, mentionConverter: MentionConverter = defaultMentionConverter, onMentionClickListener: OnMentionClickListener = defaultMentionClickListener): CharSequence {
        val builder = SpannableStringBuilder()
        builder.append(groupMessage)

        val matcher = mentionPattern.matcher(groupMessage)

        while (matcher.find()) {
            val match = matcher.group()
            val mention = match.substring(1)
            val span = makeImageSpan(mentionConverter.invoke(mention))
            val clickableSpan = object : ClickableSpan() {
                override fun onClick(widget: View) {
                    onMentionClickListener.invoke(mention)
                }
            }
            builder.setSpan(span, matcher.start(), matcher.end(), SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE)
            builder.setSpan(clickableSpan, matcher.start(), matcher.end(), SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE)
        }

        return builder
    }

    fun insertMention(editText: EditText, mention: Phone) {
        var replaceString = extractName(editText.text, editText.selectionStart)

        if (replaceString == null) {
            replaceString = ""
        }

        editText.text.replace(editText.selectionStart - replaceString.length, editText.selectionStart, "@" + mention.id!!)
        editText.text.setSpan(makeImageSpan(mention.name),
                editText.selectionStart - mention.id!!.length - 1, editText.selectionStart,
                SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE)
    }

    fun isMentionSelected(editText: EditText): Boolean {
        val text = editText.text
        val styleSpans = text.getSpans(editText.selectionStart, editText.selectionEnd, ImageSpan::class.java)
        return styleSpans.size > 0
    }

    fun deleteMention(editText: EditText): Boolean {
        if (editText.selectionStart <= 0) return true

        val text = editText.text
        val styleSpans = text.getSpans(editText.selectionStart - 1, editText.selectionEnd - 1, ImageSpan::class.java)

        for (span in styleSpans) {
            val start = text.getSpanStart(span)
            val end = text.getSpanEnd(span)

            editText.text.delete(start, end)
            editText.setSelection(start)
            return false
        }

        return true
    }

    fun makeImageSpan(name: String?): ImageSpan {
        val textView = createContactTextView(name)
        val bitmapDrawable = convertViewToDrawable(textView)
        bitmapDrawable.setBounds(0, 0, bitmapDrawable.intrinsicWidth, bitmapDrawable.intrinsicHeight)
        return ImageSpan(bitmapDrawable)
    }

    fun extractName(text: Editable, position: Int): CharSequence? {
        if (position > 0 && position <= text.length) {
            for (i in position - 1 downTo 0) {
                if (text[i] == '@') {
                    return text.subSequence(i, position)
                } else if (Character.isWhitespace(text[i])) {
                    return null
                }
            }
        }

        return null
    }

    private fun createContactTextView(text: String?): TextView {
        val textView = TextView(on<ActivityHandler>().activity)
        textView.text = "@" + text!!
        textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, on<ResourcesHandler>().resources.getDimension(R.dimen.groupMessageMentionTextSize))
        textView.setTextColor(on<ResourcesHandler>().resources.getColor(R.color.colorAccentLight))
        textView.setTypeface(textView.typeface, Typeface.BOLD)
        return textView
    }

    private fun convertViewToDrawable(view: View): BitmapDrawable {
        val spec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
        view.measure(spec, spec)
        view.layout(0, 0, view.measuredWidth, view.measuredHeight)
        val b = Bitmap.createBitmap(view.measuredWidth, view.measuredHeight,
                Bitmap.Config.ARGB_8888)
        val c = Canvas(b)
        c.translate((-view.scrollX).toFloat(), (-view.scrollY).toFloat())
        view.draw(c)
        view.isDrawingCacheEnabled = true
        val cacheBmp = view.drawingCache
        val viewBmp = cacheBmp.copy(Bitmap.Config.ARGB_8888, true)
        view.destroyDrawingCache()
        return BitmapDrawable(on<ResourcesHandler>().resources, viewBmp)
    }
}

typealias MentionConverter = (mention: String) -> String
typealias OnMentionClickListener = (mention: String) -> Unit
