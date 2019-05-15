package closer.vlllage.com.closer.handler.helpers

import android.graphics.*
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import com.queatz.on.On
import com.vdurmont.emoji.EmojiParser

class ShortcutIconGenerator constructor(private val on: On) {
    fun generate(text: String, textSize: Float, textColor: Int, bkgColor: Int, bkgLightColor: Int): Bitmap {
        var text = text

        val emojis = EmojiParser.extractEmojis(text)
        if (!emojis.isEmpty()) {
            text = emojis[0]
        } else if (text.length > 2) {
            text = text.substring(0, 2)
        }

        val paint = TextPaint(Paint.ANTI_ALIAS_FLAG)
        paint.textSize = textSize
        paint.color = textColor
        paint.setShadowLayer(8f, 0f, 0f, Color.parseColor("#66000000"))
        paint.textAlign = Paint.Align.LEFT

        val size = Math.max((paint.measureText(text) + 0.5f).toInt(), (-paint.ascent() + paint.descent() + 0.5f).toInt())

        val image = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(image)

        val gradient = LinearGradient(0f, 0f, 0f, size.toFloat(), bkgLightColor,
                bkgColor, android.graphics.Shader.TileMode.CLAMP)

        val bkgPaint = Paint()
        bkgPaint.style = Paint.Style.FILL
        bkgPaint.color = bkgColor
        bkgPaint.isDither = true
        bkgPaint.shader = gradient
        canvas.drawCircle(canvas.width / 2f, canvas.height / 2f, canvas.width / 2f, bkgPaint)

        paint.textSize = textSize * 0.75f
        val lsLayout = StaticLayout(text, paint, size, Layout.Alignment.ALIGN_NORMAL, 1f, 0f, false)
        canvas.translate(canvas.width * 0.125f, canvas.width * 0.125f)
        lsLayout.draw(canvas)
        canvas.save()
        canvas.restore()

        return image
    }
}
