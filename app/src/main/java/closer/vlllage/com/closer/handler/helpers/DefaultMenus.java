package closer.vlllage.com.closer.handler.helpers;

import closer.vlllage.com.closer.R;
import closer.vlllage.com.closer.handler.group.PhotoUploadGroupMessageHandler;
import closer.vlllage.com.closer.handler.media.MediaHandler;
import closer.vlllage.com.closer.pool.PoolActivity;
import closer.vlllage.com.closer.pool.PoolMember;

public class DefaultMenus extends PoolMember {
    public void uploadPhoto(PhotoUploadGroupMessageHandler.OnPhotoUploadedListener onPhotoUploadedListener) {
        $(MenuHandler.class).show(
                new MenuHandler.MenuOption(R.drawable.ic_camera_black_24dp, R.string.take_photo, () -> {
                    ((PoolActivity) $(ActivityHandler.class).getActivity()).getPool()
                            .$(CameraHandler.class)
                            .showCamera((photoUri -> $(PhotoUploadGroupMessageHandler.class).upload(photoUri, onPhotoUploadedListener)));
                }),
                new MenuHandler.MenuOption(R.drawable.ic_photo_black_24dp, R.string.upload_photo, () -> {
                    ((PoolActivity) $(ActivityHandler.class).getActivity()).getPool()
                            .$(MediaHandler.class)
                            .getPhoto((photoUri -> $(PhotoUploadGroupMessageHandler.class).upload(photoUri, onPhotoUploadedListener)));
                }));
    }
}
