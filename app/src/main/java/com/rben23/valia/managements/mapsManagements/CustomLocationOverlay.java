package com.rben23.valia.managements.mapsManagements;

import static com.rben23.valia.managements.mapsManagements.MapManagement.mapView;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Point;
import android.location.Location;

import com.rben23.valia.R;
import com.rben23.valia.managements.mapsManagements.MapInterfaces.OnDistanceUpdateListener;
import com.rben23.valia.managements.mapsManagements.MapInterfaces.OnLocationUpdateListener;

import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.Projection;
import org.osmdroid.views.overlay.mylocation.IMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CustomLocationOverlay extends MyLocationNewOverlay {
    private OnLocationUpdateListener locationUpdateListener;
    private OnDistanceUpdateListener distanceListener;

    private List<GeoPoint> locationHistory = new ArrayList<>();
    private Location lastLocation;
    private KalmanFilter kalmanLat;
    private KalmanFilter kalmanLon;
    private float totalDistanceMeters = 0f;
    private Bitmap customMarker;
    private Location currentFilteredLocation;

    // Hilo de actualizaciones
    private final ExecutorService locationUpdateThread;

    public CustomLocationOverlay(MapView mapView) {
        super(mapView);

        kalmanLat = new KalmanFilter(0.00001, 0.0001);
        kalmanLon = new KalmanFilter(0.00001, 0.0001);
        lastLocation = null;
        locationUpdateThread = Executors.newSingleThreadExecutor();

        // Establecemos la imagen de cursor personalizada
        setCursorImage(mapView);
        super.enableMyLocation();
    }

    @Override
    protected void drawMyLocation(Canvas canvas, Projection pj, Location lastFix) {
        Location locationToDraw = (currentFilteredLocation != null) ? currentFilteredLocation : lastFix;
        if (locationToDraw == null) return;

        // Convertimos la ubicacion en un geopoint
        GeoPoint myLocation = new GeoPoint(locationToDraw.getLatitude(), locationToDraw.getLongitude());

        // Convertimos el GeoPoint a coordenadas de pantalla
        Point screenPoint = new Point();
        pj.toPixels(myLocation, screenPoint);

        // Calculamos la posicion de dibujo centrando el bitmap
        int markerWidth = customMarker.getWidth();
        int markerHeight = customMarker.getHeight();
        float x = screenPoint.x - markerWidth / 2f;
        float y = screenPoint.y - markerHeight / 2f;

        // Dibujamos bitmap personalizado
        canvas.drawBitmap(customMarker, x, y, null);
    }

    public void setOnLocationUpdateListener(OnLocationUpdateListener listener) {
        this.locationUpdateListener = listener;
    }

    public void setOnDistanceUpdateListener(OnDistanceUpdateListener listener) {
        this.distanceListener = listener;
    }

    @Override
    public void onLocationChanged(Location location, IMyLocationProvider source) {
        super.onLocationChanged(location, source);

        locationUpdateThread.submit(() -> {
            // Filtramos los datos del GPS para mayor precision
            if (location.getAccuracy() > 10) return;
            if (location.hasSpeed() && location.getSpeed() > 50) return;

            // Obtener la ubicacion del usuario
            GeoPoint rawLocation = new GeoPoint(location.getLatitude(), location.getLongitude());
            GeoPoint smoothLocation = smoothLocation(rawLocation);

            // Aplicamos el filtro Kalman
            double filteredLat = kalmanLat.update(smoothLocation.getLatitude());
            double filteredLon = kalmanLon.update(smoothLocation.getLongitude());

            // Calculamos la distancia del ultimo registro
            if (calculateDistance(filteredLat, filteredLon)) return;

            // Actualizamos la ubicación filtrada
            if (currentFilteredLocation == null) {
                currentFilteredLocation = new Location(location);
            }

            currentFilteredLocation.setLatitude(filteredLat);
            currentFilteredLocation.setLongitude(filteredLon);

            // Se invoca al Callback de actualizacion
            GeoPoint filteredGeoPoint = new GeoPoint(filteredLat, filteredLon);
            if (locationUpdateListener != null || distanceListener != null) {
                mapView.post(() -> {
                    if (locationUpdateListener != null) {
                        locationUpdateListener.onLocationUpdate(filteredGeoPoint);
                    }
                    if (distanceListener != null) {
                        float distanceInKm = totalDistanceMeters / 1000f;
                        distanceListener.onDistanceUpdateListener(distanceInKm);
                    }
                });
            }
        });
    }

    // Suavizamos la ubicacion recogiendo la media de las ultimas 3 ubicaciones
    private GeoPoint smoothLocation(GeoPoint newLocation) {
        if (Math.abs(newLocation.getLatitude()) > 90 || Math.abs(newLocation.getLongitude()) > 180) {
            return newLocation;
        }

        locationHistory.add(newLocation);

        // Mantenemos las ultimas 3 ubicaciones
        if (locationHistory.size() > 3) {
            locationHistory.remove(0);
        }

        double avgLat = 0, avgLon = 0;
        for (GeoPoint point : locationHistory) {
            avgLat += point.getLatitude();
            avgLon += point.getLongitude();
        }
        return new GeoPoint(avgLat / locationHistory.size(), avgLon / locationHistory.size());
    }

    // Calculamos la distancia hasta el ultimo punto que se ha aceptado y el nuevo
    private boolean calculateDistance(double filteredLat, double filteredLon) {
        if (lastLocation == null) {
            lastLocation = new Location("");
            lastLocation.setLatitude(filteredLat);
            lastLocation.setLongitude(filteredLon);
            return false;
        }

        Location newLoc = new Location("");
        newLoc.setLatitude(filteredLat);
        newLoc.setLongitude(filteredLon);

        // Verificamos la distancia con los valores filtrados
        float distance = lastLocation.distanceTo(newLoc);

        // Si la distancia es muy grande
        if (distance > 500) return true;

        // Se suma si ha empezado la ruta
        if (MapManagement.isRecordingRoute) {
            totalDistanceMeters += distance;
        }

        // Actualizamos lastLocation con los valores filtrados
        lastLocation.setLatitude(filteredLat);
        lastLocation.setLongitude(filteredLon);
        return false;
    }

    // Detenemos correctamente el hilo
    public void disableLocationUpdates() {
        super.disableMyLocation();
        locationUpdateThread.shutdownNow();
    }

    private void setCursorImage(MapView mapView) {
        Bitmap arrowBitmap = BitmapFactory.decodeResource(mapView.getContext().getResources(), R.drawable.img_app_route_cursor)
                .copy(Bitmap.Config.ARGB_8888, true);
        customMarker = Bitmap.createScaledBitmap(arrowBitmap, 50, 50, false);

        setPersonIcon(customMarker);
        setPersonHotspot(customMarker.getWidth() / 2f, customMarker.getHeight() / 2f);
    }
}
