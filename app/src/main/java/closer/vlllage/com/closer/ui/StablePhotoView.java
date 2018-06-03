package closer.vlllage.com.closer.ui;

import android.content.Context;
import android.graphics.Matrix;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;

import com.github.chrisbanes.photoview.PhotoView;

public class StablePhotoView extends PhotoView {
    public StablePhotoView(Context context) {
        super(context);
    }

    public StablePhotoView(Context context, AttributeSet attr) {
        super(context, attr);
    }

    public StablePhotoView(Context context, AttributeSet attr, int defStyle) {
        super(context, attr, defStyle);
    }

    @Override
    public void setImageDrawable(Drawable drawable) {
        Matrix imageMatrix = new Matrix();
        getSuppMatrix(imageMatrix);
        super.setImageDrawable(drawable);
        setSuppMatrix(imageMatrix);
    }
}
