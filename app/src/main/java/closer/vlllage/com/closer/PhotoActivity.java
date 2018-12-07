package closer.vlllage.com.closer;

import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.transition.Transition;
import android.view.View;
import android.view.WindowManager;

import com.github.chrisbanes.photoview.PhotoView;
import com.squareup.picasso.Callback;

import closer.vlllage.com.closer.handler.helpers.DefaultAlerts;
import closer.vlllage.com.closer.handler.helpers.DisposableHandler;
import closer.vlllage.com.closer.handler.helpers.ImageHandler;
import closer.vlllage.com.closer.handler.helpers.ResourcesHandler;
import closer.vlllage.com.closer.handler.helpers.SystemShareHandler;
import closer.vlllage.com.closer.pool.PoolActivity;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.BehaviorSubject;
import jp.wasabeef.picasso.transformations.RoundedCornersTransformation;

import static android.view.Window.FEATURE_ACTIVITY_TRANSITIONS;

public class PhotoActivity extends PoolActivity {
    public static final String EXTRA_PHOTO = "photo";
    private PhotoView photo;
    private BehaviorSubject<Boolean> enterAnimationCompleteObservable = BehaviorSubject.create();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().requestFeature(FEATURE_ACTIVITY_TRANSITIONS);
        getWindow().getSharedElementEnterTransition().addListener(new Transition.TransitionListener() {
            @Override
            public void onTransitionStart(Transition transition) {
            }

            @Override
            public void onTransitionEnd(Transition transition) {
                if (enterAnimationCompleteObservable.hasValue()) return;
                enterAnimationCompleteObservable.onNext(true);
            }

            @Override
            public void onTransitionCancel(Transition transition) {

            }

            @Override
            public void onTransitionPause(Transition transition) {

            }

            @Override
            public void onTransitionResume(Transition transition) {

            }
        });
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo);
        photo = findViewById(R.id.photo);

        if (getIntent() != null) {
            String photoUrl = getIntent().getStringExtra(EXTRA_PHOTO);
            $(ImageHandler.class).get().load(photoUrl)
                    .transform(new RoundedCornersTransformation($(ResourcesHandler.class).getResources().getDimensionPixelSize(R.dimen.imageCorners), 0))
                    .into(photo, new Callback() {
                        @Override
                        public void onSuccess() {
                            $(DisposableHandler.class).add(enterAnimationCompleteObservable
                                    .subscribeOn(Schedulers.computation())
                                    .observeOn(AndroidSchedulers.mainThread())
                                    .subscribe(value -> {
                                        loadFullRes(photoUrl);
                                    }, Throwable::printStackTrace));
                        }

                        @Override
                        public void onError(Exception e) {
                            onSuccess();
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
        $(ImageHandler.class).get().load(photoUrl.split("\\?")[0] + "?s=1600")
                .noPlaceholder()
                .transform(new RoundedCornersTransformation($(ResourcesHandler.class).getResources().getDimensionPixelSize(R.dimen.imageCorners), 0))
                .into(photo);
    }
}
