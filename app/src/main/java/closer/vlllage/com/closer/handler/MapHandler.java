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

        pool(LocationHandler.class).getCurrentLocation(this::onLocationFound);
        pool(PermissionHandler.class)
                .check(Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION)
                .when(granted -> map.setMyLocationEnabled(granted));

        onMapReadyListener.onMapReady(map);
        map.setOnCameraMoveListener(onMapChangedListener::onMapChanged);
        map.setOnCameraIdleListener(onMapChangedListener::onMapChanged);
        map.setOnMapClickListener(onMapClickedListener::onMapClicked);
        mapView.addOnLayoutChangeListener((v, i1, i2, i3, i4, i5, i6, i7, i8) -> onMapChangedListener.onMapChanged());
        onMapChangedListener.onMapChanged();

        if (centerOnMapLoad != null) {
            map.moveCamera(CameraUpdateFactory.newLatLng(centerOnMapLoad));
            centerOnMapLoad = null;
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
}
