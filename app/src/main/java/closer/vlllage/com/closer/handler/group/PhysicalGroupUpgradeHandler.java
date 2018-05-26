package closer.vlllage.com.closer.handler.group;

import closer.vlllage.com.closer.R;
import closer.vlllage.com.closer.handler.data.ApiHandler;
import closer.vlllage.com.closer.handler.helpers.AlertHandler;
import closer.vlllage.com.closer.handler.helpers.CameraHandler;
import closer.vlllage.com.closer.handler.helpers.DefaultAlerts;
import closer.vlllage.com.closer.handler.helpers.DisposableHandler;
import closer.vlllage.com.closer.handler.helpers.ResourcesHandler;
import closer.vlllage.com.closer.pool.PoolMember;
import closer.vlllage.com.closer.store.models.Group;

public class PhysicalGroupUpgradeHandler extends PoolMember {
    public void convertToHub(Group group) {
        $(AlertHandler.class).make()
                .setTitle($(ResourcesHandler.class).getResources().getString(R.string.set_name))
                .setLayoutResId(R.layout.input_modal)
                .setTextView(R.id.input, result -> {
                    $(DisposableHandler.class).add($(ApiHandler.class).convertToHub(group.getId(), result).subscribe(successResult -> {
                        group.setName(result);
                    }, error -> $(DefaultAlerts.class).thatDidntWork()));
                })
                .setPositiveButton($(ResourcesHandler.class).getResources().getString(R.string.set_name))
                .show();
    }

    public void setBackground(Group group) {
        $(CameraHandler.class).showCamera((photoUri -> {
            $(PhotoUploadGroupMessageHandler.class).upload(photoUri, photoId -> {
                String photo = $(PhotoUploadGroupMessageHandler.class).getPhotoPathFromId(photoId);
                $(DisposableHandler.class).add($(ApiHandler.class).setGroupPhoto(group.getId(), photo).subscribe(
                        successResult -> {
                            if (successResult.success) {
                                group.setPhoto(photo);
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
