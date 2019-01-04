package closer.vlllage.com.closer.handler.helpers;

import android.widget.ImageView;

import closer.vlllage.com.closer.pool.PoolMember;
import jp.wasabeef.picasso.transformations.CropCircleTransformation;

public class PhotoHelper extends PoolMember {
    public void loadCircle(ImageView imageView, String url) {
        $(ImageHandler.class).get().load(url)
                .noPlaceholder()
                .transform(new CropCircleTransformation())
                .into(imageView);
    }
}
