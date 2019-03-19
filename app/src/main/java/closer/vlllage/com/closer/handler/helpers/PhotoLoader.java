package closer.vlllage.com.closer.handler.helpers;

import android.widget.ImageView;

import com.squareup.picasso.Callback;

import closer.vlllage.com.closer.pool.PoolMember;
import jp.wasabeef.picasso.transformations.BlurTransformation;

public class PhotoLoader extends PoolMember {

    public void softLoad(String photoUrl, ImageView imageView) {
        $(ImageHandler.class).get().load(photoUrl + "?s=32")
                .noPlaceholder()
                .transform(new BlurTransformation(imageView.getContext(), 2))
                .into(imageView, new Callback() {
            @Override
            public void onSuccess() {
                $(ImageHandler.class).get().load(photoUrl + "?s=512").noPlaceholder().into(imageView);
            }

            @Override
            public void onError(Exception e) {
                e.printStackTrace();
                onSuccess();
            }
        });
    }
}
