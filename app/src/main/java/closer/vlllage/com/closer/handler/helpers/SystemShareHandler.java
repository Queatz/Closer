package closer.vlllage.com.closer.handler.helpers;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.support.v4.content.FileProvider;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import closer.vlllage.com.closer.R;
import closer.vlllage.com.closer.handler.media.MediaHandler;
import closer.vlllage.com.closer.pool.PoolMember;

public class SystemShareHandler extends PoolMember {

    public void share(Bitmap bitmap) {
        if (bitmap == null) {
            $(DefaultAlerts.class).thatDidntWork();
            return;
        }

        File file;
        try {
            file = $(MediaHandler.class).createTemporaryFile("closer-photo", ".jpg");

            if (file == null) {
                $(DefaultAlerts.class).thatDidntWork();
                return;
            }

            FileOutputStream stream = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 91, stream);
            stream.close();
        } catch (IOException e) {
            e.printStackTrace();
            $(DefaultAlerts.class).thatDidntWork();
            return;
        }

        Uri contentUri = FileProvider.getUriForFile($(ActivityHandler.class).getActivity(), MediaHandler.AUTHORITY, file);

        if (contentUri == null) {
            $(DefaultAlerts.class).thatDidntWork();
            return;
        }

        Intent shareIntent = new Intent();
        shareIntent.setAction(Intent.ACTION_SEND);
        shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        shareIntent.setDataAndType(contentUri, $(ApplicationHandler.class).getApp().getContentResolver().getType(contentUri));
        shareIntent.putExtra(Intent.EXTRA_STREAM, contentUri);
        shareIntent.setType("image/jpg");
        $(ActivityHandler.class).getActivity().startActivity(Intent.createChooser(shareIntent, $(ResourcesHandler.class).getResources().getString(R.string.share_to)));
    }
}
