package closer.vlllage.com.closer.ui

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.text.style.DynamicDrawableSpan
import java.lang.ref.WeakReference


class TextImageSpan(private var bitmapDrawable: BitmapDrawable) : DynamicDrawableSpan() {

    private var mDrawableRef: WeakReference<Drawable>? = null

    fun update(drawable: BitmapDrawable) {
        bitmapDrawable = drawable
        mDrawableRef = null
    }

    override fun getSize(paint: Paint, text: CharSequence?,
                         start: Int, end: Int,
                         fm: Paint.FontMetricsInt?): Int {
        val rect: Rect = cachedDrawable.bounds
        if (fm != null) {
            val pfm: Paint.FontMetricsInt = paint.fontMetricsInt
            // keep it the same as paint's fm
            fm.ascent = pfm.ascent
            fm.descent = pfm.descent
            fm.top = pfm.top
            fm.bottom = pfm.bottom
        }

        return rect.right
    }

    override fun draw(canvas: Canvas, text: CharSequence?,
                      start: Int, end: Int, x: Float,
                      top: Int, y: Int, bottom: Int, paint: Paint) {
        canvas.save()
        val transY = y + paint.fontMetrics.top
        canvas.translate(x, transY)
        cachedDrawable.draw(canvas)
        canvas.restore()
    }

    // Redefined locally because it is a private member from DynamicDrawableSpan
    private val cachedDrawable: Drawable get() {
            val wr: WeakReference<Drawable>? = mDrawableRef
            var d: Drawable? = null
            if (wr != null) d = wr.get()
            if (d == null) {
                d = drawable
                mDrawableRef = WeakReference(d)
            }
            return d
        }

    override fun getDrawable() = bitmapDrawable
}