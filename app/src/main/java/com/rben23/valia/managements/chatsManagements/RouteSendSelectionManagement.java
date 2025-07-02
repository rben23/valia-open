package com.rben23.valia.managements.chatsManagements;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.ImageView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.rben23.valia.adapters.ListActivitiesRoutesAdapter;
import com.rben23.valia.R;
import com.rben23.valia.ValiaSQLiteHelper;
import com.rben23.valia.models.Messages;
import com.rben23.valia.models.joins.ActivityRouteJoin;
import com.rben23.valia.syncDataBase.FirebaseManager;

import java.util.ArrayList;
import java.util.List;

public class RouteSendSelectionManagement extends AppCompatActivity {

    private FirebaseManager firebaseManager;
    private ValiaSQLiteHelper vsql;

    private ListActivitiesRoutesAdapter listActivitiesRoutesAdapter;
    private ConstraintLayout savedActivitiesEmpty;
    private RecyclerView showActivities;
    private ImageView goBack;

    private String userUid;
    private String addresseeUid;
    private String conversationUid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_route_send_selection_management);

        // Creamos la conexion si no existe
        retrieveIntentExtras();
        initDatabase();
        initViews();
        initListeners();

        // Configuramos RecyclerView
        showActivities.setLayoutManager(new LinearLayoutManager(this));

        // Añadimos el adaptador a recyclerView
        buildStringToSend();
    }

    private void retrieveIntentExtras() {
        Intent intent = getIntent();
        String userUid = intent.getStringExtra("userUid");
        String addresseeUid = intent.getStringExtra("addresseeUid");
        conversationUid = intent.getStringExtra("conversationUid");
        Log.e("ConversationUid", conversationUid);

        if (!TextUtils.isEmpty(userUid) && !TextUtils.isEmpty(addresseeUid)) {
            this.userUid = userUid;
            this.addresseeUid = addresseeUid;
        }
    }

    private void initDatabase() {
        vsql = ValiaSQLiteHelper.getInstance(this);
        firebaseManager = FirebaseManager.getInstance(userUid);
    }

    private void initViews() {
        goBack = findViewById(R.id.imgGoBack);
        savedActivitiesEmpty = findViewById(R.id.savedActivitiesEmpty);
        showActivities = findViewById(R.id.showActivities);
    }

    private void initListeners() {
        goBack.setOnClickListener(view -> finish());
    }

    private void buildStringToSend() {
        listActivitiesRoutesAdapter = new ListActivitiesRoutesAdapter(new ArrayList<>(), item -> {
            // Construimos la cadena con la informacion de la ruta
            String routeData = item.getRouteName() + "#"
                    + item.getPathRoute() + "#"
                    + item.getTiemDuration() + "#"
                    + item.getTotalKm();

            String activityData = item.getIdUser() + "#"
                    + item.getIdType() + "#"
                    + item.getDate() + "#"
                    + userUid;

            String dataToSend = activityData + "!" + routeData;

            sendRouteMessage(dataToSend);
            finish();
        });
        showActivities.setAdapter(listActivitiesRoutesAdapter);

        updateUI();
    }

    private void sendRouteMessage(String routeData) {
        Messages messages = new Messages();
        messages.setIdSender(userUid);
        messages.setIdAddressee(addresseeUid);
        messages.setMessage(getString(R.string.tst_routeSended));
        messages.setIdType(1);
        messages.setPathFile(routeData);

        vsql.getMessagesDAO().insert(messages);
        firebaseManager.getMessagesSyncManager(conversationUid).synchronizeToRemoteDatabase(() -> Log.d("SyncToRemote", "Mensaje sincronizado con Firebase"));
    }

    // Obtenemos la lista y mostramos u ocultamos los views
    private void updateUI() {
        new Thread(() -> {
            List<ActivityRouteJoin> data = vsql.getRoutesDAO().getActivityRoutes();

            runOnUiThread(() -> {
                if (data != null && !data.isEmpty()) {
                    showActivities.setVisibility(VISIBLE);
                    savedActivitiesEmpty.setVisibility(GONE);

                    listActivitiesRoutesAdapter.updateItems(data);
                } else {
                    showActivities.setVisibility(GONE);
                    savedActivitiesEmpty.setVisibility(VISIBLE);

                    listActivitiesRoutesAdapter.updateItems(new ArrayList<>());
                }
            });
        }).start();
    }
}