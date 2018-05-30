package closer.vlllage.com.closer.handler.helpers;

import android.content.Intent;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.content.FileProvider;

import java.io.File;

import closer.vlllage.com.closer.handler.media.MediaHandler;
import closer.vlllage.com.closer.pool.PoolMember;

import static android.app.Activity.RESULT_OK;

public class CameraHandler extends PoolMember {

    private static int REQUEST_CODE_CAMERA = 1044;

    private Uri photoUri;

    private OnPhotoCapturedListener onPhotoCapturedListener;

    public void showCamera(@NonNull OnPhotoCapturedListener onPhotoCapturedListener) {
        this.onPhotoCapturedListener = onPhotoCapturedListener;

        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        File photo;

        try {
            photo = $(MediaHandler.class).createTemporaryFile("picture", ".jpg");

            if (photo == null) {
                return;
            }

            photo.delete();
        }
        catch(Exception e) {
            e.printStackTrace();
            $(DefaultAlerts.class).thatDidntWork();
            return;
        }

        photoUri = FileProvider.getUriForFile($(ActivityHandler.class).getActivity(), MediaHandler.AUTHORITY, photo);

        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, MediaStore.Images.Media.INTERNAL_CONTENT_URI.getPath());
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
        $(ActivityHandler.class).getActivity().startActivityForResult(cameraIntent, REQUEST_CODE_CAMERA);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode != REQUEST_CODE_CAMERA) {
            return;
        }

        if (resultCode != RESULT_OK) {
            return;
        }

        if (onPhotoCapturedListener != null) {
            onPhotoCapturedListener.onPhotoCaptured(photoUri);
        }
    }

    public interface OnPhotoCapturedListener {
        void onPhotoCaptured(Uri photoUri);
    }
}
