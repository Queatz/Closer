package closer.vlllage.com.closer.handler;

import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.v4.app.ActivityCompat;

import java.util.HashSet;
import java.util.Set;

import closer.vlllage.com.closer.pool.PoolMember;
import io.reactivex.disposables.Disposable;
import io.reactivex.subjects.PublishSubject;

public class PermissionHandler extends PoolMember {

    private static final int REQUEST_CODE_PERMISSION = 1009293;
    private PublishSubject<String> permissionChanges = PublishSubject.create();

    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode != REQUEST_CODE_PERMISSION) {
            return;
        }

        for (String permission : permissions) {
            permissionChanges.onNext(permission);
        }
    }

    public LocationCheck check(String... permissions) {
        LocationCheck check;

        if (has(permissions)) {
            check = new LocationCheck(true);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            check = new LocationCheck(permissions);
            $(ActivityHandler.class).getActivity().requestPermissions(permissions, REQUEST_CODE_PERMISSION);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            check = new LocationCheck(permissions);
            ActivityCompat.requestPermissions($(ActivityHandler.class).getActivity(), permissions, REQUEST_CODE_PERMISSION);
        } else {
            check = new LocationCheck(has(permissions));
        }

        return check;
    }

    public boolean has(String... permissions) {
        Activity activity = $(ActivityHandler.class).getActivity();

        for (String permission : permissions) {
            if (ActivityCompat.checkSelfPermission(activity, permission) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }

        return true;
    }

    public class LocationCheck {

        private Boolean override;
        private String[] permissions;
        private final Set<String> permissionsGranted = new HashSet<>();
        private PermissionCallback callback;
        private Disposable disposable;

        LocationCheck(String[] permissions) {
            this.permissions = permissions;
        }

        private LocationCheck(boolean granted) {
            override = granted;
        }

        public void when(PermissionCallback callback) {
            if (override != null) {
                callback.onPermissionResult(override);
                return;
            }

            if (permissions == null || permissions.length == 0) {
                callback.onPermissionResult(true);
                return;
            }

            this.callback = callback;

            subscribe();
        }

        private void subscribe() {
            disposable = permissionChanges.filter(permission -> {
                for (String p : permissions) {
                    if (p.equals(permission)) {
                        return true;
                    }
                }

                return false;
            }).subscribe(
                    permission -> {
                        if (has(permission)) {
                            permissionsGranted.add(permission);
                            if (permissionsGranted.size() == permissions.length) {
                                callback.onPermissionResult(true);
                                $(DisposableHandler.class).dispose(disposable);
                            }
                        } else {
                            callback.onPermissionResult(false);
                            $(DisposableHandler.class).dispose(disposable);
                        }
                    }
            );

            $(DisposableHandler.class).add(disposable);
        }
    }

    public interface PermissionCallback {
        void onPermissionResult(boolean granted);
    }
}
