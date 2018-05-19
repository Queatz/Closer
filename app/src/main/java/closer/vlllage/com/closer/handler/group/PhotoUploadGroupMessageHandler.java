package closer.vlllage.com.closer.handler.group;

import android.net.Uri;
import android.support.annotation.NonNull;

import java.io.IOException;

import closer.vlllage.com.closer.api.PhotoUploadBackend;
import closer.vlllage.com.closer.handler.data.ApiHandler;
import closer.vlllage.com.closer.handler.helpers.ActivityHandler;
import closer.vlllage.com.closer.handler.helpers.DefaultAlerts;
import closer.vlllage.com.closer.handler.helpers.DisposableHandler;
import closer.vlllage.com.closer.pool.PoolMember;

public class PhotoUploadGroupMessageHandler extends PoolMember {
    public void upload(@NonNull Uri photoUri, @NonNull OnPhotoUploadedListener onPhotoUploadedListener) {
        try {
            $(DisposableHandler.class).add($(ApiHandler.class).uploadPhoto(
                    $(ActivityHandler.class).getActivity().getContentResolver().openInputStream(photoUri))
                    .subscribe(onPhotoUploadedListener::onPhotoUploaded, error -> $(DefaultAlerts.class).thatDidntWork()));
        } catch (IOException e) {
            e.printStackTrace();
            $(DefaultAlerts.class).thatDidntWork();
        }
    }

    String getPhotoPathFromId(String photoId) {
        return PhotoUploadBackend.BASE_URL + photoId;
    }

    public interface OnPhotoUploadedListener {
        void onPhotoUploaded(String photoId);
    }
}
