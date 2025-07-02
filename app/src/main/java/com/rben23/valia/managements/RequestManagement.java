package com.rben23.valia.managements;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.rben23.valia.adapters.RequestListAdapter;
import com.rben23.valia.adapters.UsersAdapter;
import com.rben23.valia.R;
import com.rben23.valia.ValiaSQLiteHelper;
import com.rben23.valia.models.FriendRequests;
import com.rben23.valia.models.Users;
import com.rben23.valia.syncDataBase.FirebaseManager;

import java.util.ArrayList;
import java.util.List;

public class RequestManagement extends AppCompatActivity {
    private ValiaSQLiteHelper vsql;

    private RecyclerView recyclerUsers;
    private UsersAdapter usersAdapter;
    private List<Users> usersList;
    private Users user;
    private FirebaseManager firebaseManager;

    private RecyclerView recyclerRequest;
    private RequestListAdapter requestListAdapter;
    private List<FriendRequests> requestsList;

    private EditText edtSearchUsers;
    private ConstraintLayout searchUsersEmpty;
    private ConstraintLayout requestsEmpty;
    private ImageView goBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_request_management);

        // Inicializamos
        initDatabase();
        initViews();
        initListeners();
        initListAndAdapter();
        addRequestList();

        updateUI();
    }

    private void initDatabase() {
        vsql = ValiaSQLiteHelper.getInstance(this);
        user = vsql.getUsersDAO().getCurrentUser();
        firebaseManager = FirebaseManager.getInstance(user.getUserId());
    }

    private void initViews() {
        goBack = findViewById(R.id.imgGoBack);
        edtSearchUsers = findViewById(R.id.edtSearchUsers);
        recyclerUsers = findViewById(R.id.recyclerUsers);
        searchUsersEmpty = findViewById(R.id.searchUsersEmpty);
        recyclerRequest = findViewById(R.id.recyclerRequest);
        requestsEmpty = findViewById(R.id.requestsEmpty);
    }

    private void initListeners() {
        // Volvemos a la actividad anterior
        goBack.setOnClickListener(view -> finish());
        // Escuchamos cambios en editText
        textChangedListener();
    }

    private void initListAndAdapter() {
        // Inicializamos la lista y el adaptador
        recyclerUsers.setLayoutManager(new LinearLayoutManager(this));
        usersList = new ArrayList<>();
        usersAdapter = new UsersAdapter(usersList);
        recyclerUsers.setAdapter(usersAdapter);
    }

    private void addRequestList() {
        // Rellenamos la lista de solicitudes
        requestsList = vsql.getFriendRequestsDAO().getFriendRequestsByAddressee(user.getUid(), 0);
        recyclerRequest.setLayoutManager(new LinearLayoutManager(this));
        requestListAdapter = new RequestListAdapter(this, requestsList);
        recyclerRequest.setAdapter(requestListAdapter);
    }

    // Escuchamos cambios en editText
    private void textChangedListener() {
        edtSearchUsers.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                searchUsers(charSequence.toString().trim());
            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (editable.toString().trim().isEmpty()) {
                    recyclerUsers.setVisibility(GONE);
                    searchUsersEmpty.setVisibility(VISIBLE);
                } else {
                    recyclerUsers.setVisibility(VISIBLE);
                    searchUsersEmpty.setVisibility(GONE);
                }
            }
        });
    }

    // Buscamos los usuarios y los añadimo a la lista
    private void searchUsers(String user) {
        firebaseManager.getUsersSyncManager().searchUsers(user, () -> {
            List<Users> foundUsers = firebaseManager.getUsersSyncManager().getLastSearchResult();
            usersList.clear();
            usersList.addAll(foundUsers);
            usersAdapter.notifyDataSetChanged();
        });
    }

    // Obtenemos la lista y mostramos u ocultamos los views
    private void updateUI() {
        new Thread(() -> {
            List<FriendRequests> data = ValiaSQLiteHelper.getFriendRequestsDAO().getFriendRequestsByAddressee(user.getUid(), 0);

            runOnUiThread(() -> {
                if (data != null && !data.isEmpty()) {
                    recyclerRequest.setVisibility(VISIBLE);
                    requestsEmpty.setVisibility(GONE);

                    requestListAdapter.updateItems(data);
                } else {
                    recyclerRequest.setVisibility(GONE);
                    requestsEmpty.setVisibility(VISIBLE);

                    requestListAdapter.updateItems(data);
                }
            });
        }).start();
    }

    @Override
    protected void onResume() {
        super.onResume();
        List<FriendRequests> newData = ValiaSQLiteHelper.getFriendRequestsDAO().getFriendRequestsByAddressee(user.getUid(), 0);
        requestListAdapter.updateItems(newData);
        requestListAdapter.notifyDataSetChanged();
        updateUI();
    }
}