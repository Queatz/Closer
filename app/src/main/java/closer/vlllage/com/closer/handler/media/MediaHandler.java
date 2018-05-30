package closer.vlllage.com.closer.handler.media;

import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.io.File;

import closer.vlllage.com.closer.handler.helpers.ActivityHandler;
import closer.vlllage.com.closer.handler.helpers.DefaultAlerts;
import closer.vlllage.com.closer.pool.PoolMember;

import static android.app.Activity.RESULT_OK;

public class MediaHandler extends PoolMember {

    public static final String AUTHORITY = "closer.vlllage.com.closer.fileprovider";
    private static int REQUEST_CODE_MEDIA = 1045;

    private OnMediaSelectedListener onMediaSelectedListener;

    public void getPhoto(OnMediaSelectedListener onMediaSelectedListener) {
        this.onMediaSelectedListener = onMediaSelectedListener;

        Intent mediaIntent = new Intent(Intent.ACTION_GET_CONTENT);
        mediaIntent.setType("image/*");

        File photo;

        try {
            photo = createTemporaryFile("closer", ".jpg");

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

        $(ActivityHandler.class).getActivity().startActivityForResult(mediaIntent, REQUEST_CODE_MEDIA);
    }

    @Nullable
    public File createTemporaryFile(@NonNull String part, @NonNull String ext) throws Exception
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

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode != REQUEST_CODE_MEDIA) {
            return;
        }

        if (resultCode != RESULT_OK) {
            return;
        }

        if (onMediaSelectedListener != null) {
            onMediaSelectedListener.onMediaSelected(data.getData());
        }
    }

    public interface OnMediaSelectedListener {
        void onMediaSelected(Uri mediaUri);
    }
}
