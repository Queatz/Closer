package closer.vlllage.com.closer.handler.helpers;

import android.content.Intent;
import android.graphics.Bitmap;
import android.provider.MediaStore;

import closer.vlllage.com.closer.pool.PoolMember;

public class CameraHandler extends PoolMember {

    private static int REQUEST_CODE_CAMERA = 1044;

    public void showCamera() {
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        $(ActivityHandler.class).getActivity().startActivityForResult(cameraIntent, REQUEST_CODE_CAMERA);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode != REQUEST_CODE_CAMERA) {
            return;
        }

        if (!data.hasExtra("data")) {
            return;
        }

        Bitmap image = (Bitmap) data.getExtras().get("data");
        $(DefaultAlerts.class).message("Image captured (" + image.getWidth() + ", " + image.getHeight() + ") - feature not yet supported.");
    }
}
