package com.rben23.valia.managements.challengesManagements;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.rben23.valia.R;
import com.rben23.valia.ValiaSQLiteHelper;
import com.rben23.valia.adapters.FriendsChallengesAdaper;
import com.rben23.valia.models.Friends;
import com.rben23.valia.models.Challenges;
import com.rben23.valia.models.Users;
import com.rben23.valia.syncDataBase.FirebaseManager;

import java.util.ArrayList;
import java.util.List;

public class NewChallengeManagement extends AppCompatActivity {
    private ValiaSQLiteHelper vsql;
    private FirebaseManager firebaseManager;
    private String currentUserUid;
    private String selectedFriendUid;
    boolean friendsEmpty = true;

    private EditText edtNameProperty;
    private EditText edtKmProperty;
    private Spinner spnUserProperty;
    private ImageView goBack;

    // Botones de guardar y cancelar
    private Button btnStartChallenge, btnCancel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_challenge_management);

        // Instanciamos
        initDatabase();
        initViews();
        initListeners();

        // Configuramos el Spinner con los amigos
        setFriendsSpinner();
    }

    private void initDatabase() {
        vsql = ValiaSQLiteHelper.getInstance(this);
        Users getCurrentUser = vsql.getUsersDAO().getCurrentUser();
        currentUserUid = getCurrentUser.getUid();
        firebaseManager = FirebaseManager.getInstance(currentUserUid);
    }

    private void initViews() {
        // Elementos de la vista
        goBack = findViewById(R.id.imgGoBack);
        edtNameProperty = findViewById(R.id.edtNameProperty);
        edtKmProperty = findViewById(R.id.edtKmProperty);
        spnUserProperty = findViewById(R.id.spnUserProperty);
        btnStartChallenge = findViewById(R.id.btnStartChallenge);
        btnCancel = findViewById(R.id.btnCancel);
    }

    private void initListeners() {
        // Volvemos a la actividad anterior
        goBack.setOnClickListener(view -> finish());

        // Boton Empezar y Cancelar
        btnStartChallenge.setOnClickListener(view -> startChallenge());
        btnCancel.setOnClickListener(view -> finish());
    }

    // Llenamos el spinner con los amigos
    private void setFriendsSpinner() {
        List<Friends> friendList = vsql.getFriendsDAO().getFriendsByUser(currentUserUid);
        friendsEmpty = (friendList == null || friendList.isEmpty());

        if (friendsEmpty) {
            List<String> emptyList = new ArrayList<>();
            emptyList.add(getString(R.string.withoutFriendsChallenge));

            ArrayAdapter<String> emptyAdapter = new ArrayAdapter<>(
                    this, android.R.layout.simple_spinner_item, emptyList
            );

            emptyAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spnUserProperty.setAdapter(emptyAdapter);
        } else {
            FriendsChallengesAdaper friendsChallengesAdaper = new FriendsChallengesAdaper(this, friendList, currentUserUid);
            friendsChallengesAdaper.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spnUserProperty.setAdapter(friendsChallengesAdaper);
        }
    }

    // Creamos el reto
    private void startChallenge() {
        // Verificamos que los campos no estén vacios
        if (friendsEmpty) {
            Toast.makeText(this, getString(R.string.tst_emptyFriendsList), Toast.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.isEmpty(edtNameProperty.getText()) || TextUtils.isEmpty(edtKmProperty.getText())) {
            Toast.makeText(this, getString(R.string.tst_incompleteFields), Toast.LENGTH_SHORT).show();
            return;
        }

        // Cogemos el uid del usuario amigo
        selectedFriendUid = getSelectedFriendUid();
        if (selectedFriendUid == null) {
            Toast.makeText(this, getString(R.string.tst_emptyFriendsList), Toast.LENGTH_SHORT).show();
            return;
        }

        // Creamos el reto
        Challenges challenges = new Challenges();
        challenges.setIdUser(currentUserUid);
        challenges.setIdAddressee(selectedFriendUid);
        challenges.setDescription(edtNameProperty.getText().toString());
        challenges.setTotalKm(Double.parseDouble(edtKmProperty.getText().toString()));

        // Guardamos el reto
        vsql.getChallengesDAO().insert(challenges);
        firebaseManager.getChallengesSyncManager().synchronizeToRemoteDatabase(() -> Log.d("SyncToRemote", "Reto sincronizado con Firebase"));
        finish();
    }

    // Extraemos el UID del amigo seleccionado en el spinner
    private String getSelectedFriendUid() {
        Object selectedItem = spnUserProperty.getSelectedItem();
        if (selectedItem instanceof Friends) {
            Friends friend = (Friends) selectedItem;
            return currentUserUid.equals(friend.getIdUser()) ? friend.getIdFriend() : friend.getIdUser();
        }
        return null;
    }
}
