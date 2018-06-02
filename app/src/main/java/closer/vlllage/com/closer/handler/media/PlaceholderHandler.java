package closer.vlllage.com.closer.handler.media;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.util.Base64;

import closer.vlllage.com.closer.pool.PoolMember;

public class PlaceholderHandler extends PoolMember {
    public int getHeightFromAspectRatio(float width, float aspectRatio) {
        return (int) (width / aspectRatio);
    }

    public BitmapDrawable drawableFromBase64(Resources resources, String image) {
        byte bytes[] = Base64.decode(image, Base64.DEFAULT);

        Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
        return new BitmapDrawable(resources, bitmap);
    }

}
