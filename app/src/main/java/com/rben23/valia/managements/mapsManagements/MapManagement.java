package com.rben23.valia.managements.mapsManagements;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

import com.rben23.valia.BaseActivity;
import com.rben23.valia.R;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.CornerPathEffect;
import android.graphics.Paint;
import android.media.AudioManager;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.provider.Settings;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import org.osmdroid.api.IMapController;
import org.osmdroid.config.Configuration;
import org.osmdroid.library.BuildConfig;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.CustomZoomButtonsController;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Polyline;

import java.io.File;
import java.util.ArrayList;
import java.util.Locale;

public class MapManagement extends BaseActivity {
    private static final int REQUEST_PERMISSIONS_REQUEST_CODE = 1;

    private Vibrator vibrator;
    private AudioManager audioManager;

    protected static boolean isRecordingRoute;
    protected static MapView mapView;
    private CustomLocationOverlay locationOverlay;
    protected static Polyline userPath;
    protected static ArrayList<GeoPoint> routePoints = new ArrayList<>();

    // Inicializar elementos de vista
    private TextView txvRouteName;
    private Button btnStartFinishRoute;
    private View infoRouteView;
    private Chronometer chrRouteTime;
    private TextView txvRouteDistance;
    private ImageView goBack;
    private ImageView imgGPSArrow;
    private Switch schLandscape;
    private ConstraintLayout landscapeModeView;
    private ConstraintLayout landscapeView;
    private TextView txvRouteDistanceKm;
    private ImageView imgRouteIcon;

    // Otras variables de clase
    private float totalDistanceKm = 0.0f;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Configuramos Osmdroid y después setContentView
        configOsmdroid();
        setContentView(R.layout.activity_map_management);

        // Inicializamos
        initSystemServices();
        initViews();
        initListeners();

        // Inicializamos MapView
        IMapController mapController = initMapView();
        configLocation(mapController);

        // Establecemos el grabado de ruta a false
        isRecordingRoute = false;

        // Establecer los datos de la ruta
        imgRouteIcon.setImageResource(getIntent().getIntExtra("activityIcon", -1));
        txvRouteName.setText(getIntent().getIntExtra("activityName", -1));

        // Solicitar permisos
        requestPermissionsIfNecessary(new String[]{Manifest.permission.ACCESS_FINE_LOCATION});
    }

    private void initSystemServices() {
        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
    }

    private void initViews() {
        goBack = findViewById(R.id.imgGoBack);
        txvRouteName = findViewById(R.id.txvRouteName);
        imgRouteIcon = findViewById(R.id.imgRouteIcon);
        infoRouteView = findViewById(R.id.infoRouteView);
        btnStartFinishRoute = findViewById(R.id.btnStartFinishRoute);
        txvRouteDistance = findViewById(R.id.txvRouteDistance);
        chrRouteTime = findViewById(R.id.chrRouteTime);
        imgGPSArrow = findViewById(R.id.imgGPSArrow);
        schLandscape = findViewById(R.id.schLandscape);
        landscapeModeView = findViewById(R.id.landscapeModeView);
        landscapeView = findViewById(R.id.landscapeView);
        txvRouteDistanceKm = findViewById(R.id.txvRouteDistanceKm);
        mapView = findViewById(R.id.routeMapView);
    }

    private void initListeners() {
        // Volvemos a la actividad anterior
        goBack.setOnClickListener(view -> {
            closeMap();
            finish();
        });

        // Si se inicia se pintar la ruta en el mapa
        btnStartFinishRoute.setOnClickListener(onClick -> {
            startRoute();
            vibrateButtonPress();
        });

        // Landscape ON / OFF
        schLandscape.setOnCheckedChangeListener((buttonView, isChecked) -> {
            landscapeManagement(isChecked);
        });
    }

    @NonNull
    private IMapController initMapView() {
        mapView.getZoomController().setVisibility(CustomZoomButtonsController.Visibility.NEVER);
        mapView.setMultiTouchControls(true);
        mapView.setTilesScaledToDpi(true);
        mapView.setBuiltInZoomControls(true);

        IMapController mapController = mapView.getController();
        mapController.setZoom(14);
        return mapController;
    }

    private void configLocation(IMapController mapController) {
        locationOverlay = new CustomLocationOverlay(mapView);
        locationOverlay.enableMyLocation();
        locationOverlay.enableFollowLocation();

        // Configuramos el callback de actualización de ubicación
        locationOverlay.setOnLocationUpdateListener(newLocation -> {
            // Si se está grabando la ruta, agregamos el punto al Polyline
            if (isRecordingRoute && userPath != null) {
                userPath.addPoint(newLocation);
                routePoints.add(newLocation);
                mapView.invalidate();
            }
        });

        // Configuramos el callback de distancia para actualizar el TextView
        locationOverlay.setOnDistanceUpdateListener(totDistanceKm -> {
            totalDistanceKm = totDistanceKm;
            if (totalDistanceKm >= 1.00) {
                txvRouteDistanceKm.setText(getString(R.string.txv_kmText));
                txvRouteDistance.setText(String.format(Locale.ENGLISH, "%.1f", totalDistanceKm));
            }
            txvRouteDistance.setText(String.format(Locale.ENGLISH, "%.2f", totalDistanceKm));
        });

        // Establecer la vista en la ubicación del usuario (La primera vez)
        locationOverlay.runOnFirstFix(() -> mapView.post(() -> {
            GeoPoint userLocation = locationOverlay.getMyLocation();
            if (userLocation != null) {
                mapController.setCenter(userLocation);
            }
        }));

        mapView.getOverlays().add(locationOverlay);
    }

    // Configuración de Osmdroid
    private void configOsmdroid() {
        File osmdroidBasePath = new File(getExternalFilesDir(null), "osmdroid");
        File osmdroidTileCache = new File(osmdroidBasePath, "tiles");

        Configuration.getInstance().setOsmdroidBasePath(osmdroidBasePath);
        Configuration.getInstance().setOsmdroidTileCache(osmdroidTileCache);
        Configuration.getInstance().setCacheMapTileCount((short) 0);
        Configuration.getInstance().setTileFileSystemCacheMaxBytes(0);
        Configuration.getInstance().setUserAgentValue(BuildConfig.APPLICATION_ID);
        Configuration.getInstance().load(getApplicationContext(), getSharedPreferences("osmdroid", MODE_PRIVATE));
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private void startRoute() {
        if (!isRecordingRoute) {
            isRecordingRoute = true;
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
            paintRoute();

            // Mostrar Views
            infoRouteView.setVisibility(VISIBLE);
            imgGPSArrow.setVisibility(VISIBLE);
            goBack.setVisibility(GONE);
            landscapeModeView.setVisibility(GONE);

            // Estilos del botón finalizar
            btnStartFinishRoute.setText(R.string.btn_finishRoute);
            btnStartFinishRoute.setBackgroundResource(R.drawable.red_background);
            btnStartFinishRoute.setTextColor(getColor(R.color.white));

            // Empezar el cronometro
            chrRouteTime.setBase(SystemClock.elapsedRealtime());
            chrRouteTime.start();
        } else {
            chrRouteTime.stop();
            isRecordingRoute = false;
            goToSaveRoute();

            closeMap();
            finish();
        }
    }

    private void paintRoute() {
        userPath = new Polyline();
        Paint paint = userPath.getPaint();
        paint.setColor(getResources().getColor(R.color.colorNavActive));
        paint.setStrokeWidth(30f);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setStrokeJoin(Paint.Join.ROUND);
        paint.setPathEffect(new CornerPathEffect(10f));

        mapView.getOverlays().add(userPath);
    }

    // Activar o desactivar Landscape
    private void landscapeManagement(boolean isChecked) {
        if (isChecked) {
            landscapeView.setVisibility(VISIBLE);
            muteVibratePhone();
        } else {
            landscapeView.setVisibility(GONE);
            unmuteVibratePhone();
        }
    }

    // Muteamos el vibrador y el sonido
    private void muteVibratePhone() {
        // Si no tienemos permisos, abrimos la configuración para que el usuario lo conceda
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager != null && !notificationManager.isNotificationPolicyAccessGranted()) {
            startActivity(new Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS));
            return;
        }

        if (audioManager != null) {
            audioManager.setRingerMode(AudioManager.RINGER_MODE_SILENT);
        }

        vibrateSwitchOnEffect();
    }

    private void vibrateSwitchOnEffect() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            VibrationEffect effect = VibrationEffect.createWaveform(
                    new long[]{10, 50},
                    new int[]{VibrationEffect.DEFAULT_AMPLITUDE, VibrationEffect.DEFAULT_AMPLITUDE},
                    -1
            );
            vibrator.vibrate(effect);
        } else {
            vibrator.vibrate(50);
        }
    }

    // Desmuteamos el vibrador y el sonido
    private void unmuteVibratePhone() {
        if (audioManager != null) {
            audioManager.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
        }
        vibrateSwitchOffEffect();
    }

    private void vibrateSwitchOffEffect() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            VibrationEffect effect = VibrationEffect.createWaveform(
                    new long[]{50, 10},
                    new int[]{VibrationEffect.DEFAULT_AMPLITUDE, VibrationEffect.DEFAULT_AMPLITUDE / 2},
                    -1
            );
            vibrator.vibrate(effect);
        } else {
            vibrator.vibrate(50);
        }
    }

    private void vibrateButtonPress() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            VibrationEffect effect = VibrationEffect.createWaveform(
                    new long[]{50, 100},
                    new int[]{VibrationEffect.DEFAULT_AMPLITUDE, VibrationEffect.DEFAULT_AMPLITUDE},
                    -1
            );
            vibrator.vibrate(effect);
        } else {
            vibrator.vibrate(100);
        }
    }

    // Solicitamos los permisos
    private void requestPermissionsIfNecessary(String[] permissions) {
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, permissions, REQUEST_PERMISSIONS_REQUEST_CODE);
                return;
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_PERMISSIONS_REQUEST_CODE) {
            for (int result : grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, getString(R.string.tst_permissionRequest), Toast.LENGTH_LONG).show();
                    return;
                }
            }
            locationOverlay.enableMyLocation();
        }
    }

    // Guardamos los datos en la BD
    private void goToSaveRoute() {
        Intent intent = new Intent(MapManagement.this, SaveRoute.class);
        intent.putExtra("routeName", txvRouteName.getText().toString());
        intent.putExtra("routePoints", pointsToString());
        intent.putExtra("routeDistance", totalDistanceKm);
        intent.putExtra("routeTime", chrRouteTime.getText().toString());
        startActivity(intent);
    }

    // Pasamos los datos a string para guardarlos en la BD
    private String pointsToString() {
        StringBuilder stringBuilder = new StringBuilder();

        // Convertimos cada punto en "lat,long;"
        for (GeoPoint point : routePoints) {
            stringBuilder.append(point.getLatitude()).append(",")
                    .append(point.getLongitude()).append(";");
        }

        // Eliminamos el ultimo punto y coma
        if (stringBuilder.length() > 0) {
            stringBuilder.deleteCharAt(stringBuilder.length() - 1);
        }
        return stringBuilder.toString();
    }

    @Override
    protected void onPause() {
        super.onPause();

        // Quitamos el FLAG de pantalla siempre encendida
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // Cerramos MapView
        closeMap();
    }

    private void closeMap() {
        // Cerramos MapView
        if (mapView != null) {
            mapView.onDetach();
        }

        if (locationOverlay != null) {
            locationOverlay.disableLocationUpdates();
        }

        Configuration.getInstance().save(getApplicationContext(),
                getSharedPreferences("osmdroid", MODE_PRIVATE));
    }
}