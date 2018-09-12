package closer.vlllage.com.closer.handler.group;

import closer.vlllage.com.closer.R;
import closer.vlllage.com.closer.handler.data.ApiHandler;
import closer.vlllage.com.closer.handler.helpers.AlertHandler;
import closer.vlllage.com.closer.handler.helpers.ApplicationHandler;
import closer.vlllage.com.closer.handler.helpers.CameraHandler;
import closer.vlllage.com.closer.handler.helpers.DefaultAlerts;
import closer.vlllage.com.closer.handler.helpers.DisposableHandler;
import closer.vlllage.com.closer.handler.helpers.MenuHandler;
import closer.vlllage.com.closer.handler.helpers.ResourcesHandler;
import closer.vlllage.com.closer.handler.media.MediaHandler;
import closer.vlllage.com.closer.pool.PoolMember;
import closer.vlllage.com.closer.store.StoreHandler;
import closer.vlllage.com.closer.store.models.Group;

public class PhysicalGroupUpgradeHandler extends PoolMember {
    public void convertToHub(Group group) {
        $(AlertHandler.class).make()
                .setTitle($(ResourcesHandler.class).getResources().getString(R.string.set_name))
                .setLayoutResId(R.layout.input_modal)
                .setTextView(R.id.input, result -> {
                    $(DisposableHandler.class).add($(ApiHandler.class).convertToHub(group.getId(), result).subscribe(successResult -> {
                        group.setName(result);
                        $(StoreHandler.class).getStore().box(Group.class).put(group);
                    }, error -> $(DefaultAlerts.class).thatDidntWork()));
                })
                .setPositiveButton($(ResourcesHandler.class).getResources().getString(R.string.set_name))
                .show();
    }

    public void setBackground(Group group) {
        $(MenuHandler.class).show(
                new MenuHandler.MenuOption(R.drawable.ic_camera_black_24dp, R.string.take_photo, () -> {
                    $(CameraHandler.class).showCamera((photoUri -> $(PhotoUploadGroupMessageHandler.class).upload(photoUri, photoId -> handlePhoto(group, photoId))));
                }),
                new MenuHandler.MenuOption(R.drawable.ic_photo_black_24dp, R.string.upload_photo, () -> {
                    $(MediaHandler.class).getPhoto((photoUri -> $(PhotoUploadGroupMessageHandler.class).upload(photoUri, photoId -> handlePhoto(group, photoId))));
                }));
    }

    private void handlePhoto(Group group, String photoId) {
        String photo = $(PhotoUploadGroupMessageHandler.class).getPhotoPathFromId(photoId);
        $(ApplicationHandler.class).getApp().$(DisposableHandler.class).add($(ApiHandler.class).setGroupPhoto(group.getId(), photo).subscribe(
                successResult -> {
                    if (successResult.success) {
                        group.setPhoto(photo);
                        $(StoreHandler.class).getStore().box(Group.class).put(group);
                    } else {
                        $(DefaultAlerts.class).thatDidntWork();
                    }
                },
                error -> $(DefaultAlerts.class).thatDidntWork()
        ));
    }
}
