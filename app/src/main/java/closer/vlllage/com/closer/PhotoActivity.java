package closer.vlllage.com.closer;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import closer.vlllage.com.closer.pool.PoolActivity;

public class PhotoActivity extends PoolActivity {
    public static final String EXTRA_PHOTO = "photo";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo);
        ImageView photo = findViewById(R.id.photo);

        if (getIntent() != null) {
            Picasso.get().load(getIntent().getStringExtra(EXTRA_PHOTO))
                    .into(photo);
        }

        photo.setOnClickListener(view -> finish());
    }
}
