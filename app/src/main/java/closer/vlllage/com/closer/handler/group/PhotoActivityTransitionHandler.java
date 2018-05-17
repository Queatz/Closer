package closer.vlllage.com.closer.handler.group;

import android.content.Intent;
import android.support.v4.app.ActivityOptionsCompat;
import android.view.View;

import closer.vlllage.com.closer.PhotoActivity;
import closer.vlllage.com.closer.handler.helpers.ActivityHandler;
import closer.vlllage.com.closer.handler.helpers.ApplicationHandler;
import closer.vlllage.com.closer.pool.PoolMember;

import static closer.vlllage.com.closer.PhotoActivity.EXTRA_PHOTO;

public class PhotoActivityTransitionHandler extends PoolMember {
    public void show(View fromView, String photo) {
        Intent intent = new Intent($(ApplicationHandler.class).getApp(), PhotoActivity.class);
        intent.setAction(Intent.ACTION_VIEW);
        intent.putExtra(EXTRA_PHOTO, photo);

        ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(
                $(ActivityHandler.class).getActivity(), fromView, "photo");

        $(ActivityHandler.class).getActivity().startActivity(intent, options.toBundle());
    }
}
