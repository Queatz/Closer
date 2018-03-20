package closer.vlllage.com.closer.handler;

import android.Manifest;
import android.annotation.SuppressLint;
import android.location.Location;
import android.view.View;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;

import closer.vlllage.com.closer.pool.PoolMember;

public class MapHandler extends PoolMember implements OnMapReadyCallback {

    private GoogleMap map;
    private View mapView;
    private LatLng centerOnMapLoad;
    private OnMapChangedListener onMapChangedListener;
    private OnMapClickedListener onMapClickedListener;
    private OnMapReadyListener onMapReadyListener;
    private OnMapIdleListener onMapIdleListener;

    public void attach(SupportMapFragment mapFragment) {
        mapFragment.getMapAsync(this);
        mapView = mapFragment.getView();
    }

    public void setOnMapChangedListener(OnMapChangedListener onMapChangedListener) {
        this.onMapChangedListener = onMapChangedListener;
    }

    public void setOnMapClickedListener(OnMapClickedListener onMapClickedListener) {
        this.onMapClickedListener = onMapClickedListener;
    }

    public void setOnMapReadyListener(OnMapReadyListener onMapReadyListener) {
        this.onMapReadyListener = onMapReadyListener;
    }

    public void setOnMapIdleListener(OnMapIdleListener onMapIdleListener) {
        this.onMapIdleListener = onMapIdleListener;
    }

    public void centerMap(LatLng latLng) {
        if (map == null) {
            centerOnMapLoad = latLng;
            return;
        }

        map.animateCamera(CameraUpdateFactory.newLatLng(latLng));
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;

        $(LocationHandler.class).getCurrentLocation(this::onLocationFound);
        $(PermissionHandler.class)
                .check(Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION)
                .when(granted -> map.setMyLocationEnabled(granted));

        onMapReadyListener.onMapReady(map);
        map.setOnCameraMoveListener(this::mapChanged);
        map.setOnCameraIdleListener(onMapChangedListener::onMapChanged);
        map.setOnMapClickListener(onMapClickedListener::onMapClicked);
        map.setOnCameraIdleListener(() -> onMapIdleListener.onMapIdle(map.getCameraPosition().target));
        mapView.addOnLayoutChangeListener((v, i1, i2, i3, i4, i5, i6, i7, i8) -> onMapChangedListener.onMapChanged());
        onMapChangedListener.onMapChanged();

        if (centerOnMapLoad != null) {
            map.moveCamera(CameraUpdateFactory.newLatLng(centerOnMapLoad));
            centerOnMapLoad = null;
        }
    }

    private void mapChanged() {
        onMapChangedListener.onMapChanged();

        if (map == null) {
            return;
        }

        if (map.getCameraPosition().zoom >= 18) {
            if (map.getMapType() != GoogleMap.MAP_TYPE_SATELLITE) {
                map.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
            }
        } else if (map.getCameraPosition().zoom <= 3) {
            if (map.getMapType() != GoogleMap.MAP_TYPE_SATELLITE) {
                map.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
            }
        } else {
            if (map.getMapType() != GoogleMap.MAP_TYPE_NORMAL) {
                map.setMapType(GoogleMap.MAP_TYPE_NORMAL);
            }
        }
    }

    private void onLocationFound(Location location) {
        final LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        map.moveCamera(CameraUpdateFactory.newCameraPosition(CameraPosition.fromLatLngZoom(latLng, 13)));
    }

    public interface OnMapChangedListener {
        void onMapChanged();
    }

    public interface OnMapReadyListener {
        void onMapReady(GoogleMap map);
    }

    public interface OnMapClickedListener {
        void onMapClicked(LatLng latLng);
    }

    public interface OnMapIdleListener {
        void onMapIdle(LatLng latLng);
    }
}
