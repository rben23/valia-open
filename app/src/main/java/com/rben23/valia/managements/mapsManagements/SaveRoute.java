package com.rben23.valia.managements.mapsManagements;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;


import com.rben23.valia.BaseActivity;
import com.rben23.valia.DAO.ActivitiesDAO;
import com.rben23.valia.DAO.RoutesDAO;
import com.rben23.valia.DAO.UsersDAO;
import com.rben23.valia.NavegationActivity;
import com.rben23.valia.R;
import com.rben23.valia.ValiaSQLiteHelper;
import com.rben23.valia.managements.challengesManagements.CheckChallengeManagement;
import com.rben23.valia.models.Activities;
import com.rben23.valia.models.Routes;
import com.rben23.valia.models.Users;


public class SaveRoute extends BaseActivity {
    private ValiaSQLiteHelper vsql;
    private UsersDAO usersDAO;
    private ActivitiesDAO activitiesDAO;
    private RoutesDAO routesDAO;

    private String routeName;
    private String routePoints;
    private float totalDistance;
    private String routeTime;
    private Button btnSaveRoute , btnExit;

    private EditText edtRouteNameProperty;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_save_route);

        initDatabase();
        initViews();
        retrieveIntentExtras();
        initListeners();
    }

    private void initDatabase() {
        // Creamos la conexion si no existe
        vsql = ValiaSQLiteHelper.getInstance(this);
        usersDAO = vsql.getUsersDAO();
        activitiesDAO = vsql.getActivitiesDAO();
        routesDAO = vsql.getRoutesDAO();
    }

    private void initViews() {
        // Elementos de la vista
        edtRouteNameProperty = findViewById(R.id.edtRouteNameProperty);
        btnSaveRoute = findViewById(R.id.btnSaveRoute);
        btnExit = findViewById(R.id.btnExit);
    }

    private void initListeners() {
        btnSaveRoute.setOnClickListener(view -> saveRoute());
        btnExit.setOnClickListener(view -> goToMain());
    }

    private void retrieveIntentExtras() {
        Intent intent = getIntent();
        if (intent != null) {
            routeName = intent.getStringExtra("routeName");
            routePoints = intent.getStringExtra("routePoints");
            totalDistance = intent.getFloatExtra("routeDistance", 0f);
            routeTime = intent.getStringExtra("routeTime");
        }
    }

    // Guardamos los datos en la BD
    private void saveRoute() {
        // Verificamos que se ha insertado un nombre para la ruta
        String customRouteName = edtRouteNameProperty.getText().toString().trim();
        if (customRouteName.isEmpty()) {
            Toast.makeText(this, getString(R.string.tst_incompleteFields), Toast.LENGTH_SHORT).show();
            return;
        }

        Users users = usersDAO.getCurrentUser();

        // Establecemos la actividad a insertar
        Activities activities = new Activities();
        activities.setIdUser(users.getUid());
        activities.setName(routeName);
        activities.setIdType(0);

        // Insertamos la actividad y recuperamos el ID generado
        long idActividades = activitiesDAO.insert(activities);
        activities.setIdActivity((int) idActividades);

        // Establecemos la ruta a insertar
        Routes routes = new Routes();
        routes.setIdActivity((int) idActividades);
        routes.setName(edtRouteNameProperty.getText().toString());
        routes.setPathRoute(routePoints);
        routes.setTotalKm(totalDistance);
        routes.setTiemDuration(routeTime);

        // Insertamos los valores
        routesDAO.insert(routes);

        Toast.makeText(this, getString(R.string.tst_routeSaved), Toast.LENGTH_SHORT).show();
        checkChallenges();
        goToMain();
    }

    private void checkChallenges() {
        CheckChallengeManagement checkChallengeManagement = new CheckChallengeManagement(vsql);
        checkChallengeManagement.checkActiveChallenges();
    }

    // Volvemos al inicio
    private void goToMain() {
        Intent intent = new Intent(SaveRoute.this, NavegationActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }
}
