package closer.vlllage.com.closer.handler.group;

import android.content.Intent;
import android.graphics.Rect;
import android.view.View;

import closer.vlllage.com.closer.PhotoActivity;
import closer.vlllage.com.closer.handler.helpers.ActivityHandler;
import closer.vlllage.com.closer.handler.helpers.ApplicationHandler;
import closer.vlllage.com.closer.pool.PoolMember;

import static closer.vlllage.com.closer.PhotoActivity.EXTRA_PHOTO;

public class PhotoActivityTransitionHandler extends PoolMember {
    public void show(View view, String photo) {
        Intent intent = new Intent($(ApplicationHandler.class).getApp(), PhotoActivity.class);
        intent.setAction(Intent.ACTION_VIEW);
        intent.putExtra(EXTRA_PHOTO, photo);

        if (view != null) {
            Rect bounds = new Rect();
            view.getGlobalVisibleRect(bounds);

            intent.setSourceBounds(bounds);
        }

        $(ActivityHandler.class).getActivity().startActivity(intent);
    }
}
