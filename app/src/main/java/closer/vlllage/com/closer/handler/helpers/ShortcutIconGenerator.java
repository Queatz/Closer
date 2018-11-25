package closer.vlllage.com.closer.handler.helpers;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;

import com.vdurmont.emoji.EmojiParser;

import java.util.List;

import closer.vlllage.com.closer.pool.PoolMember;

public class ShortcutIconGenerator extends PoolMember {
    public Bitmap generate(String text, float textSize, int textColor, int bkgColor, int bkgLightColor) {

        List<String> emojis = EmojiParser.extractEmojis(text);
        if (!emojis.isEmpty()) {
            text = emojis.get(0);
        } else if (text.length() > 2) {
            text = text.substring(0, 2);
        }

        TextPaint paint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
        paint.setTextSize(textSize);
        paint.setColor(textColor);
        paint.setShadowLayer(8, 0, 0, Color.parseColor("#66000000"));
        paint.setTextAlign(Paint.Align.LEFT);

        int size = Math.max((int) (paint.measureText(text) + 0.5f), (int) (-paint.ascent() + paint.descent() + 0.5f));

        Bitmap image = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(image);

        LinearGradient gradient = new LinearGradient(0, 0, 0, size, bkgLightColor,
                bkgColor, android.graphics.Shader.TileMode.CLAMP);

        Paint bkgPaint = new Paint();
        bkgPaint.setStyle(Paint.Style.FILL);
        bkgPaint.setColor(bkgColor);
        bkgPaint.setDither(true);
        bkgPaint.setShader(gradient);
        canvas.drawCircle(canvas.getWidth() / 2f, canvas.getHeight() / 2f, canvas.getWidth() / 2f, bkgPaint);

        paint.setTextSize(textSize * 0.75f);
        StaticLayout lsLayout = new StaticLayout(text, paint, size, Layout.Alignment.ALIGN_NORMAL, 1, 0, false);
        canvas.translate(canvas.getWidth() * 0.125f, canvas.getWidth() * 0.125f);
        lsLayout.draw(canvas);
        canvas.save();
        canvas.restore();

        return image;
    }
}
