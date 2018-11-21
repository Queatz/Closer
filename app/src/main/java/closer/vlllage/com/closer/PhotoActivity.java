package closer.vlllage.com.closer;

import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.WindowManager;

import com.github.chrisbanes.photoview.PhotoView;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import closer.vlllage.com.closer.handler.helpers.DefaultAlerts;
import closer.vlllage.com.closer.handler.helpers.ResourcesHandler;
import closer.vlllage.com.closer.handler.helpers.SystemShareHandler;
import closer.vlllage.com.closer.pool.PoolActivity;
import jp.wasabeef.picasso.transformations.RoundedCornersTransformation;

public class PhotoActivity extends PoolActivity {
    public static final String EXTRA_PHOTO = "photo";
    private PhotoView photo;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo);
        photo = findViewById(R.id.photo);
        String photoUrl = getIntent().getStringExtra(EXTRA_PHOTO);

        if (getIntent() != null) {
            Picasso.get().load(photoUrl)
                    .transform(new RoundedCornersTransformation($(ResourcesHandler.class).getResources().getDimensionPixelSize(R.dimen.imageCorners), 0))
                    .into(photo, new Callback() {
                        @Override
                        public void onSuccess() {
                            loadFullRes(photoUrl);
                        }

                        @Override
                        public void onError(Exception e) {
                            loadFullRes(photoUrl);
                        }
                    });
        }

        photo.setOnClickListener(view -> finishAfterTransition());

        findViewById(R.id.actionShare).setOnClickListener(view -> {
            if (photo.getDrawable() instanceof BitmapDrawable) {
                $(SystemShareHandler.class).share(((BitmapDrawable) photo.getDrawable()).getBitmap());
            } else {
                $(DefaultAlerts.class).thatDidntWork();
            }
        });
    }

    private void loadFullRes(String photoUrl) {
        Picasso.get().load(photoUrl.split("\\?")[0] + "?s=1600")
                .noPlaceholder()
                .transform(new RoundedCornersTransformation($(ResourcesHandler.class).getResources().getDimensionPixelSize(R.dimen.imageCorners), 0))
                .into(photo);
    }
}
