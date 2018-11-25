package closer.vlllage.com.closer.handler.helpers;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;

import closer.vlllage.com.closer.pool.PoolMember;

public class ShortcutIconGenerator extends PoolMember {
    public Bitmap generate(String text, float textSize, int textColor, int bkgColor) {
        TextPaint paint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
        paint.setTextSize(textSize);
        paint.setColor(textColor);
        paint.setShadowLayer(4, 0, 0, Color.BLACK);
        paint.setTextAlign(Paint.Align.LEFT);

        int width = (int) (paint.measureText(text) + 0.5f);
        int height = (int) (-paint.ascent() + paint.descent() + 0.5f);

        if (height > width) {
            width = height;
        } else {
            height = width;
        }

        Bitmap image = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(image);

        Paint bkgPaint = new Paint();
        bkgPaint.setStyle(Paint.Style.FILL);
        bkgPaint.setColor(bkgColor);
        canvas.drawCircle(canvas.getWidth() / 2f, canvas.getHeight() / 2f, canvas.getWidth() / 2f, bkgPaint);

        paint.setTextSize(textSize * 0.75f);
        StaticLayout lsLayout = new StaticLayout(text, paint, width * 2, Layout.Alignment.ALIGN_NORMAL, 1, 0, false);
        canvas.translate(canvas.getWidth() * 0.125f, canvas.getWidth() * 0.125f);
        lsLayout.draw(canvas);
        canvas.save();
        canvas.restore();

        return image;
    }
}
