package closer.vlllage.com.closer.handler.helpers;

import android.content.Intent;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.FileProvider;
import android.widget.Toast;

import java.io.File;

import closer.vlllage.com.closer.handler.group.GroupHandler;
import closer.vlllage.com.closer.handler.group.PhotoUploadGroupMessageHandler;
import closer.vlllage.com.closer.pool.PoolMember;

import static android.app.Activity.RESULT_OK;

public class CameraHandler extends PoolMember {

    private static final String AUTHORITY = "closer.vlllage.com.closer.fileprovider";
    private static int REQUEST_CODE_CAMERA = 1044;

    private Uri photoUri;

    public void showCamera() {
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        File photo;

        try {
            photo = createTemporaryFile("picture", ".jpg");

            if (photo == null) {
                return;
            }

            photo.delete();
        }
        catch(Exception e) {
            e.printStackTrace();
            Toast.makeText($(ActivityHandler.class).getActivity(), "Please check SD card! Image shot is impossible!", Toast.LENGTH_SHORT).show();
            return;
        }

        photoUri = FileProvider.getUriForFile($(ActivityHandler.class).getActivity(), AUTHORITY, photo);

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

        $(PhotoUploadGroupMessageHandler.class).createGroupMessageFromUri(photoUri, $(GroupHandler.class).getGroup().getId());
    }

    @Nullable
    private File createTemporaryFile(@NonNull String part, @NonNull String ext) throws Exception
    {
        File tempDir= $(ActivityHandler.class).getActivity().getCacheDir();
        tempDir = new File(tempDir.getAbsolutePath() + "/shared/");
        if (!tempDir.exists()) {
            if (!tempDir.mkdirs()) {
                return null;
            }
        }
        return File.createTempFile(part, ext, tempDir);
    }
}
