package closer.vlllage.com.closer.handler;

import android.Manifest;
import android.annotation.SuppressLint;
import android.location.Location;
import android.view.View;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;

import java.util.Collection;

import closer.vlllage.com.closer.handler.bubble.MapBubble;
import closer.vlllage.com.closer.pool.PoolMember;

public class MapHandler extends PoolMember implements OnMapReadyCallback {

    private static final float DEFAULT_ZOOM = 15F;

    private GoogleMap map;
    private View mapView;
    private LatLng centerOnMapLoad;
    private OnMapChangedListener onMapChangedListener;
    private OnMapClickedListener onMapClickedListener;
    private OnMapLongClickedListener onMapLongClickedListener;
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

    public void setOnMapLongClickedListener(OnMapLongClickedListener onMapLongClickedListener) {
        this.onMapLongClickedListener = onMapLongClickedListener;
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

        map.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, DEFAULT_ZOOM));
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;

        if (centerOnMapLoad == null) {
            locateMe();
        }

        updateMyLocationEnabled();

        onMapReadyListener.onMapReady(map);
        map.setOnCameraMoveListener(this::mapChanged);
        map.setOnCameraIdleListener(onMapChangedListener::onMapChanged);
        map.setOnMapClickListener(onMapClickedListener::onMapClicked);
        map.setOnMapLongClickListener(onMapLongClickedListener::onMapLongClicked);
        map.setOnCameraIdleListener(() -> onMapIdleListener.onMapIdle(map.getCameraPosition().target));
        mapView.addOnLayoutChangeListener((v, i1, i2, i3, i4, i5, i6, i7, i8) -> onMapChangedListener.onMapChanged());
        onMapChangedListener.onMapChanged();

        if (centerOnMapLoad != null) {
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(centerOnMapLoad, DEFAULT_ZOOM));
            centerOnMapLoad = null;
        }
    }

    public void locateMe() {
        $(LocationHandler.class).getCurrentLocation(this::onLocationFound);
    }

    @SuppressLint("MissingPermission")
    public void updateMyLocationEnabled() {
        if (map == null) {
            return;
        }

        $(PermissionHandler.class)
                .check(Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION)
                .when(granted -> map.setMyLocationEnabled(granted));
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
        CameraPosition cameraPosition = CameraPosition.builder()
                .target(latLng)
                .zoom(DEFAULT_ZOOM)
                .tilt(45)
                .build();
        map.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
    }

    public LatLng getCenter() {
        if (map == null) {
            return null;
        }

        return map.getCameraPosition().target;
    }

    public void centerOn(Collection<MapBubble> mapBubbles) {
        if (mapBubbles.isEmpty()) {
            return;
        }

        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        for (MapBubble mapBubble : mapBubbles) {
            builder.include(mapBubble.getLatLng());
        }

        try {
            CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(builder.build(), Math.min(
                    $(ActivityHandler.class).getActivity().getWindow().getDecorView().getWidth(),
                    $(ActivityHandler.class).getActivity().getWindow().getDecorView().getHeight()
            ) / 4);
            map.animateCamera(cu, 225, null);
        } catch (Throwable ignored) {
            // com.google.maps.api.android.lib6.common.apiexception.c:
            // Error using newLatLngBounds(LatLngBounds, int):
            // View size is too small after padding is applied.
        }
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

    public interface OnMapLongClickedListener {
        void onMapLongClicked(LatLng latLng);
    }

    public interface OnMapIdleListener {
        void onMapIdle(LatLng latLng);
    }
}
