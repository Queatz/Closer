package closer.vlllage.com.closer.handler;

import android.Manifest;
import android.annotation.SuppressLint;
import android.location.Location;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

import closer.vlllage.com.closer.pool.PoolMember;

public class LocationHandler extends PoolMember {

    private FusedLocationProviderClient fusedLocationProvider;

    @Override
    protected void onPoolInit() {
        fusedLocationProvider = LocationServices.getFusedLocationProviderClient(
                $(ActivityHandler.class).getActivity()
        );
    }

    @SuppressLint("MissingPermission")
    public void getCurrentLocation(LocationCallback callback) {
        $(PermissionHandler.class)
                .check(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
                .when(granted -> {
                    if (granted) {
                        fusedLocationProvider.getLastLocation().addOnCompleteListener(task -> {
                            if (!task.isSuccessful() ||task.getResult() == null) {
                                waitForLocation(callback);
                                return;
                            }

                            callback.onLocationFound(task.getResult());
                        });
                    }
                });
    }

    @SuppressLint("MissingPermission")
    private void waitForLocation(LocationCallback callback) {
        LocationRequest locationRequest = LocationRequest.create()
                .setExpirationDuration(10000)
                .setNumUpdates(1);

        fusedLocationProvider.requestLocationUpdates(locationRequest, new com.google.android.gms.location.LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult.getLastLocation() == null) {
                    return;
                }

                callback.onLocationFound(locationResult.getLastLocation());
            }
        }, $(ActivityHandler.class).getActivity().getMainLooper());
    }

    public interface LocationCallback {
        void onLocationFound(Location location);
    }
}
