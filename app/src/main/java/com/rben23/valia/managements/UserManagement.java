package com.rben23.valia.managements;

import static android.view.View.GONE;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.rben23.valia.R;
import com.rben23.valia.ValiaSQLiteHelper;
import com.rben23.valia.models.FriendRequests;
import com.rben23.valia.models.Users;
import com.rben23.valia.syncDataBase.FirebaseManager;

import java.util.Objects;

public class UserManagement extends AppCompatActivity {
    private ValiaSQLiteHelper vsql;
    private Users user;

    private TextView txvFrameTitle;
    private ImageView imgUserProfile, goBack;
    private TextView txvUserName;
    private Button btnRequest;
    private EditText edtNameProperty;
    private EditText edtMailProperty;

    // Intent info
    String addresseeUid, userId, name, email;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_management);

        // Inicializamos
        initDatabase();
        initViews();
        initListeners();
        retrieveIntent();

        // Recuperamos y asignamos datos
        asignData(userId, name, email);
        getProfileImage(addresseeUid);

        // Gestionamos la visibilidad y acciones del boton
        checkCurrentStatus(user.getUid(), addresseeUid);
    }

    private void initDatabase() {
        // Creamos la conexion si no existe
        vsql = ValiaSQLiteHelper.getInstance(this);
        user = vsql.getUsersDAO().getCurrentUser();
    }

    private void initListeners() {
        // Volvemos a la actividad anterior
        goBack.setOnClickListener(view -> finish());
    }

    private void initViews() {
        // Elementos de la vista
        goBack = findViewById(R.id.imgGoBack);
        txvFrameTitle = findViewById(R.id.txvFrameTitle);
        imgUserProfile = findViewById(R.id.imgUserProfile);
        txvUserName = findViewById(R.id.txvUserName);
        btnRequest = findViewById(R.id.btnRequest);
        edtNameProperty = findViewById(R.id.edtNameProperty);
        edtMailProperty = findViewById(R.id.edtMailProperty);
    }

    private void retrieveIntent() {
        // Recuperamos el intent
        Intent intent = getIntent();
        addresseeUid = intent.getStringExtra("userUid");
        userId = intent.getStringExtra("userId");
        name = intent.getStringExtra("userName");
        email = intent.getStringExtra("userEmail");
    }

    private void asignData(String userId, String name, String email) {
        // Asignamos los campos a las variables
        txvFrameTitle.setText(userId);
        txvUserName.setText(userId);
        edtNameProperty.setText(name);
        edtMailProperty.setText(email);
    }

    private void getProfileImage(String addresseeUid) {
        // Coger la foto de firebase
        ProfileImageManagement profileImageManagement = new ProfileImageManagement(this, addresseeUid);
        profileImageManagement.getRemoteUserImageProfile(imgUserProfile);
    }

    // Comprobamos el estado de la solicitud
    private void checkCurrentStatus(String userUid, String addresseeUid) {
        int currentStatus = vsql.getFriendRequestsDAO().getFriendRequestStatus(userUid, addresseeUid);

        if (Objects.equals(userUid, addresseeUid)) {
            btnRequest.setVisibility(GONE);
            return;
        }

        switch (currentStatus) {
            case -1:
                btnRequestManagement(addresseeUid);
                break;
            case 0:
                btnRequest.setEnabled(false);
                btnRequest.setText(R.string.btn_requestPending);
                break;
            case 1:
                btnRequest.setEnabled(false);
                btnRequest.setText(R.string.btn_requestAccepted);
                break;
        }

    }

    // Creamos la solicitud de amistad
    private void btnRequestManagement(String userUid) {
        btnRequest.setOnClickListener(view -> {
            Users usuario = vsql.getUsersDAO().getCurrentUser();
            String idUser = usuario.getUid();
            String idAddressee = userUid;

            Log.e("ADDRESSEE ID:", idAddressee);

            FriendRequests friendRequests = new FriendRequests();
            friendRequests.setIdUser(idUser);
            friendRequests.setIdAddressee(idAddressee);
            friendRequests.setIdState(0);

            try {
                long result = vsql.getFriendRequestsDAO().insert(friendRequests);

                Cursor cursor = vsql.getFriendRequestsDAO().getRecord(result);
                FriendRequests prueba = vsql.getFriendRequestsDAO().toFriendRequests(cursor);
                cursor.close();
                Log.e("SOLICITUD ADDRESSEE ID:", prueba.getIdAddressee());


                if (result != -1) {
                    friendRequests.setIdRequest((int) result);
                    FirebaseManager firebaseManager = FirebaseManager.getInstance(usuario.getUid());
                    firebaseManager.getFriendRequestsSyncManager().synchronizeToRemoteDatabase(() -> Log.i("Request List sync", "Sincronizacion completada"));

                    btnRequest.setEnabled(false);
                    btnRequest.setText(R.string.btn_requestPending);
                    Toast.makeText(view.getContext(), R.string.tst_requestSent, Toast.LENGTH_SHORT).show();
                }
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(view.getContext(), R.string.tst_ErrorSendingRequest, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
