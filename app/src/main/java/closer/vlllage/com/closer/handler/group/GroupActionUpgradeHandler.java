package closer.vlllage.com.closer.handler.group;

import closer.vlllage.com.closer.handler.data.ApiHandler;
import closer.vlllage.com.closer.handler.helpers.ApplicationHandler;
import closer.vlllage.com.closer.handler.helpers.CameraHandler;
import closer.vlllage.com.closer.handler.helpers.DefaultAlerts;
import closer.vlllage.com.closer.handler.helpers.DisposableHandler;
import closer.vlllage.com.closer.handler.media.MediaHandler;
import closer.vlllage.com.closer.pool.PoolMember;
import closer.vlllage.com.closer.store.StoreHandler;
import closer.vlllage.com.closer.store.models.GroupAction;

public class GroupActionUpgradeHandler extends PoolMember {

    public void setPhotoFromMedia(GroupAction groupAction) {
        $(MediaHandler.class).getPhoto(photoUri -> $(PhotoUploadGroupMessageHandler.class).upload(photoUri, photoId -> {
            String photo = $(PhotoUploadGroupMessageHandler.class).getPhotoPathFromId(photoId);
            $(ApplicationHandler.class).getApp().$(DisposableHandler.class).add($(ApiHandler.class)
                    .setGroupActionPhoto(groupAction.getId(), photo).subscribe(
                            successResult -> {
                                if (successResult.success) {
                                    groupAction.setPhoto(photo);
                                    $(StoreHandler.class).getStore().box(GroupAction.class).put(groupAction);
                                } else {
                                    $(DefaultAlerts.class).thatDidntWork();
                                }
                            },
                            error -> $(DefaultAlerts.class).thatDidntWork()
                    ));
        }));
    }

    public void setPhotoFromCamera(GroupAction groupAction) {
        $(CameraHandler.class).showCamera((photoUri -> {
            $(PhotoUploadGroupMessageHandler.class).upload(photoUri, photoId -> {
                String photo = $(PhotoUploadGroupMessageHandler.class).getPhotoPathFromId(photoId);
                $(ApplicationHandler.class).getApp().$(DisposableHandler.class).add($(ApiHandler.class)
                        .setGroupActionPhoto(groupAction.getId(), photo).subscribe(
                        successResult -> {
                            if (successResult.success) {
                                groupAction.setPhoto(photo);
                                $(StoreHandler.class).getStore().box(GroupAction.class).put(groupAction);
                            } else {
                                $(DefaultAlerts.class).thatDidntWork();
                            }
                        },
                        error -> $(DefaultAlerts.class).thatDidntWork()
                ));
            });
        }));
    }
}
