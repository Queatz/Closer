package closer.vlllage.com.closer.handler.group;

import android.net.Uri;

import java.io.IOException;

import closer.vlllage.com.closer.api.PhotoUploadBackend;
import closer.vlllage.com.closer.handler.data.ApiHandler;
import closer.vlllage.com.closer.handler.helpers.ActivityHandler;
import closer.vlllage.com.closer.handler.helpers.DefaultAlerts;
import closer.vlllage.com.closer.handler.helpers.DisposableHandler;
import closer.vlllage.com.closer.pool.PoolMember;

public class PhotoUploadGroupMessageHandler extends PoolMember {
    public void createGroupMessageFromUri(Uri photoUri, String groupId) {
        try {
            $(DisposableHandler.class).add($(ApiHandler.class).uploadPhoto(
                    $(ActivityHandler.class).getActivity().getContentResolver().openInputStream(photoUri))
                    .subscribe(photoId -> {
                boolean success = $(GroupMessageAttachmentHandler.class).sharePhoto(getPhotoPathFromId(photoId), groupId);
                if (!success) {
                    $(DefaultAlerts.class).thatDidntWork();
                }
            }, error -> $(DefaultAlerts.class).thatDidntWork()));
        } catch (IOException e) {
            e.printStackTrace();
            $(DefaultAlerts.class).thatDidntWork();
        }
    }

    private String getPhotoPathFromId(String photoId) {
        return PhotoUploadBackend.BASE_URL + photoId;
    }
}
