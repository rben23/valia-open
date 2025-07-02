package com.rben23.valia.managements;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

import android.annotation.SuppressLint;
import android.content.Context;
import android.database.Cursor;
import android.graphics.CornerPathEffect;
import android.graphics.Paint;
import android.os.Bundle;
import android.os.Vibrator;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.rben23.valia.adapters.ActivityTypes;
import com.rben23.valia.adapters.SpinnerActivitiesAdapter;
import com.rben23.valia.Constants;
import com.rben23.valia.DAO.ActivitiesDAO;
import com.rben23.valia.DAO.RoutesDAO;
import com.rben23.valia.R;
import com.rben23.valia.ValiaSQLiteHelper;
import com.rben23.valia.models.Activities;
import com.rben23.valia.models.Routes;

import org.osmdroid.config.Configuration;
import org.osmdroid.library.BuildConfig;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Polyline;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ActivityManagement extends AppCompatActivity {
    private Vibrator vibrator;

    private ValiaSQLiteHelper vsql;
    private RoutesDAO routesDAO;
    private ActivitiesDAO activitiesDAO;
    private Routes routes = new Routes();
    private Activities activities = new Activities();

    // Modo del formulario
    private long mode;

    // Elementos de la vista
    private TextView txvFrameTitle;
    private ImageView imgEditRoute, imgDelete, goBack;
    private MapView routeMapView;
    private TextView txvRouteTime;
    private TextView txvRouteDistance;
    private EditText edtRouteNameProperty;
    private Spinner spnActivityNameProperty;
    private EditText edtDateProperty;
    private View deleteRouteView;
    private TextView txvDelete;
    private Button btnDelete;

    // Botones de guardar y cancelar
    private Button btnSave;
    private Button btnCancel;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Configuramos el mapa antes de inflar la vista
        configOsmdroid();
        setContentView(R.layout.activity_management);

        initSystem();
        initDatabase();
        retrieveDataFromIntent();
        initViews();
        initListeners();

        // Recuperamos el Id y pintamos la ruta
        getAndPaintRoute();
    }

    private void initListeners() {
        // Volvemos a la actividad anterior
        goBack.setOnClickListener(view -> finish());
        // Boton editar
        imgEditRoute.setOnClickListener(view -> setMode(Constants.C_EDIT));

        // Boton Guardar y Cancelar
        btnSave.setOnClickListener(view -> save());
        btnCancel.setOnClickListener(view -> finish());

        // Boton Eliminar
        btnDelete.setOnClickListener(view -> deleteActivity(routes.getIdRoute()));

        // Listener del Spinner
        spnActivityNameProperty.setOnTouchListener((view, motionEvent) -> mode != Constants.C_EDIT);
    }

    private void initViews() {
        // Obtener elementos de la vista
        goBack = findViewById(R.id.imgGoBack);
        routeMapView = findViewById(R.id.routeMapView);
        txvFrameTitle = findViewById(R.id.txvFrameTitle);
        imgEditRoute = findViewById(R.id.imgEditRoute);
        txvRouteTime = findViewById(R.id.txvRouteTime);
        txvRouteDistance = findViewById(R.id.txvRouteDistance);
        edtRouteNameProperty = findViewById(R.id.edtRouteNameProperty);
        spnActivityNameProperty = findViewById(R.id.spnActivityNameProperty);
        edtDateProperty = findViewById(R.id.edtDateProperty);
        deleteRouteView = findViewById(R.id.deleteRouteView);
        txvDelete = findViewById(R.id.txvDelete);
        imgDelete = findViewById(R.id.imgDelete);
        btnDelete = findViewById(R.id.btnDelete);
        btnSave = findViewById(R.id.btnSave);
        btnCancel = findViewById(R.id.btnCancel);
    }

    private void initSystem() {
        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
    }

    private void initDatabase() {
        // Creamos la conexion si no existe
        vsql = ValiaSQLiteHelper.getInstance(this);

        // Inicializamos y realizamos las consultas a la BD
        routesDAO = vsql.getRoutesDAO();
        activitiesDAO = vsql.getActivitiesDAO();
    }

    private void retrieveDataFromIntent() {
        long idRoute = getIntent().getIntExtra("idRoute", -1);
        if (idRoute == -1) {
            Toast.makeText(this, "ID de ruta no válido.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        try (Cursor rutasCursor = routesDAO.getRecord(idRoute)) {
            routes = routesDAO.toRoutes(rutasCursor);
        }
        try (Cursor actividadesCursor = activitiesDAO.getRecord(routes.getIdActivity())) {
            activities = activitiesDAO.toActivities(actividadesCursor);
        }
    }

    // Configuramos Osmdroid para mostrar el mapa
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

    // Recogemos el string con la ruta y pintamos la ruta en el mapa
    private void getAndPaintRoute() {
        if (routes == null || activities == null) return;
        assignData();

        // Pasamos el String de coordenadas a una Lista de puntos para dibujarlos
        List<GeoPoint> routePoints = parseRoute(routes.getPathRoute());
        if (routePoints != null && !routePoints.isEmpty()) {
            routeMapView.getOverlayManager().clear();
            paintRoute(routePoints);
        }
    }

    // Asignamos los valores a los campos correspondientes
    private void assignData() {
        txvRouteTime.setText(routes.getTiemDuration());
        String distance = String.format(Locale.ENGLISH, "%.3f", routes.getTotalKm());
        txvRouteDistance.setText(distance);
        edtRouteNameProperty.setText(routes.getName());
        edtDateProperty.setText(activities.getDate());

        // Inflamos el Spinner
        ActivityTypes[] activityTypes = ActivityTypes.values();
        ActivityTypes selectedType = ActivityTypes.fromName(activities.getName(), this);
        SpinnerActivitiesAdapter spinnerActivitiesAdapter =
                new SpinnerActivitiesAdapter(this, activityTypes);
        spnActivityNameProperty.setAdapter(spinnerActivitiesAdapter);

        // Establecemos el item almacenado
        int selectedIndex = 0;
        for (int i = 0; i < activityTypes.length; i++) {
            if (activityTypes[i] == selectedType) {
                selectedIndex = i;
                break;
            }
        }
        spnActivityNameProperty.setSelection(selectedIndex);
    }

    // Pasamos las coordenadas almacenadas a un array para posteriormente mostrarlas
    private List<GeoPoint> parseRoute(String route) {
        List<GeoPoint> routePoints = new ArrayList<>();
        if (route == null || route.isEmpty()) return routePoints;

        String[] points = route.split(";");
        for (String point : points) {
            String[] latLon = point.split(",");

            if (latLon.length == 2) {
                try {
                    double lat = Double.parseDouble(latLon[0]);
                    double lon = Double.parseDouble(latLon[1]);
                    routePoints.add(new GeoPoint(lat, lon));
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                }
            }
        }
        return routePoints;
    }

    @SuppressLint("ClickableViewAccessibility")
    private void paintRoute(List<GeoPoint> routePoints) {
        Polyline polyline = new Polyline(routeMapView);
        polyline.setPoints(routePoints);

        Paint paint = polyline.getPaint();
        paint.setColor(getResources().getColor(R.color.colorNavActive));
        paint.setStrokeWidth(30f);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setStrokeJoin(Paint.Join.ROUND);
        paint.setPathEffect(new CornerPathEffect(10f));

        routeMapView.getOverlayManager().add(polyline);
        routeMapView.getController().setZoom(20.0);
        routeMapView.getController().setCenter(routePoints.get(0));
        routeMapView.setBuiltInZoomControls(true);
        routeMapView.setMultiTouchControls(true);

        // Evento para cancelar el scroll de la vista si hay mas de un dedo en la pantalla
        routeMapView.setOnTouchListener((view, motionEvent) -> {
            // Si detectamos mas de un dedo quitamos el scroll
            if (motionEvent.getPointerCount() > 1) {
                view.getParent().requestDisallowInterceptTouchEvent(true);
            }
            return false;
        });
    }

    // Establecer el modo del formulario
    private void setMode(long mode) {
        this.mode = mode;
        setEdition(mode == Constants.C_EDIT);
    }

    // Configurar el modo de edición
    private void setEdition(boolean editionMode) {
        vibrator.vibrate(50);
        edtRouteNameProperty.setEnabled(editionMode);
        spnActivityNameProperty.setEnabled(editionMode);

        if (editionMode) {
            txvFrameTitle.setText(R.string.txv_editActivity);

            btnSave.setVisibility(VISIBLE);
            btnCancel.setVisibility(VISIBLE);

            deleteRouteView.setVisibility(GONE);
            imgDelete.setVisibility(GONE);
            txvDelete.setVisibility(GONE);
            btnDelete.setVisibility(GONE);

            imgEditRoute.setVisibility(GONE);
        } else {
            txvFrameTitle.setText(R.string.txv_showActivity);

            btnSave.setVisibility(GONE);
            btnCancel.setVisibility(GONE);

            deleteRouteView.setVisibility(VISIBLE);
            imgDelete.setVisibility(VISIBLE);
            txvDelete.setVisibility(VISIBLE);
            btnDelete.setVisibility(VISIBLE);

            imgEditRoute.setVisibility(VISIBLE);
        }
    }

    // Guardamos los cambios editados
    private void save() {
        if (mode == Constants.C_EDIT) {
            String routeName = edtRouteNameProperty.getText().toString();
            ActivityTypes selectedType = (ActivityTypes) spnActivityNameProperty.getSelectedItem();
            String selectedTypeName = getString(selectedType.getName());

            if (routeName.isEmpty() || selectedTypeName.isEmpty()) {
                Toast.makeText(ActivityManagement.this, R.string.fieldsRequired, Toast.LENGTH_SHORT).show();
                return;
            }

            routes.setName(routeName);
            activities.setName(selectedTypeName);

            long routeResult = routesDAO.update(routes);
            long activityResult = activitiesDAO.update(activities);

            if (routeResult > 0 && activityResult > 0) {
                Toast.makeText(ActivityManagement.this, R.string.recordSaved, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(ActivityManagement.this, R.string.recordNotSaved, Toast.LENGTH_SHORT).show();
            }
        }

        setResult(RESULT_OK);
        finish();
    }

    // Eliminamos la actividad
    private void deleteActivity(long idRoute) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.titleAlert));
        builder.setMessage(R.string.alertMessageActivity);
        builder.setPositiveButton(getString(R.string.btn_accionAlert), (dialog, which) -> {
            try (Cursor rutasCursor = routesDAO.getRecord(idRoute)) {
                Routes routes = routesDAO.toRoutes(rutasCursor);

                routesDAO.delete(idRoute);
                activitiesDAO.delete(routes.getIdActivity());
            }
            finish();
        }).setNegativeButton(getString(R.string.btn_cancelAlert), (dialog, which) -> dialog.dismiss()).show();
    }

    @Override
    protected void onPause() {
        super.onPause();
        routeMapView.getOverlayManager().clear();
        routeMapView.invalidate();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        routeMapView.getOverlayManager().clear();
        routeMapView.invalidate();
    }
}