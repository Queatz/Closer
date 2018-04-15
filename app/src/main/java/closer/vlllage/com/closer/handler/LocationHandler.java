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
        if ($(ActivityHandler.class).isPresent()) {
            fusedLocationProvider = LocationServices.getFusedLocationProviderClient(
                    $(ActivityHandler.class).getActivity()
            );
        } else {
            fusedLocationProvider = LocationServices.getFusedLocationProviderClient(
                    $(ApplicationHandler.class).getApp()
            );
        }
    }

    public void getCurrentLocation(LocationCallback callback) {
        getCurrentLocation(callback, null);
    }

    @SuppressLint("MissingPermission")
    public void getCurrentLocation(LocationCallback callback, LocationUnavailableCallback locationUnavailableCallback) {
        $(PermissionHandler.class)
                .check(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
                .when(granted -> {
                    if (granted) {
                        fusedLocationProvider.getLastLocation().addOnCompleteListener(task -> {
                            if (!task.isSuccessful() || task.getResult() == null) {
                                waitForLocation(callback, locationUnavailableCallback);
                                return;
                            }

                            callback.onLocationFound(task.getResult());
                        });
                    } else if (locationUnavailableCallback != null) {
                        locationUnavailableCallback.onLocationUnavailable();
                    }
                });
    }

    @SuppressLint("MissingPermission")
    private void waitForLocation(LocationCallback callback, LocationUnavailableCallback locationUnavailableCallback) {
        LocationRequest locationRequest = LocationRequest.create()
                .setExpirationDuration(10000)
                .setNumUpdates(1);

        fusedLocationProvider.getLocationAvailability().addOnCompleteListener(task -> {
            if (task.isSuccessful() && !task.getResult().isLocationAvailable()) {
                if (locationUnavailableCallback != null) {
                    locationUnavailableCallback.onLocationUnavailable();
                }
            } else {
                fusedLocationProvider.requestLocationUpdates(locationRequest, new com.google.android.gms.location.LocationCallback() {
                    @Override
                    public void onLocationResult(LocationResult locationResult) {
                        if (locationResult.getLastLocation() == null) {
                            if (locationUnavailableCallback != null) {
                                locationUnavailableCallback.onLocationUnavailable();
                            }

                            return;
                        }

                        callback.onLocationFound(locationResult.getLastLocation());
                    }
                }, $(ActivityHandler.class).getActivity().getMainLooper());
            }
        });
    }

    public interface LocationCallback {
        void onLocationFound(Location location);
    }

    public interface LocationUnavailableCallback {
        void onLocationUnavailable();
    }
}
