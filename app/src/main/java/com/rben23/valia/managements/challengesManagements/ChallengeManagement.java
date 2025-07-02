package com.rben23.valia.managements.challengesManagements;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Vibrator;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.rben23.valia.Constants;
import com.rben23.valia.DAO.ChallengesDAO;
import com.rben23.valia.DAO.UsersDAO;
import com.rben23.valia.R;
import com.rben23.valia.ValiaSQLiteHelper;
import com.rben23.valia.adapters.FriendsChallengesAdaper;
import com.rben23.valia.models.Friends;
import com.rben23.valia.models.Challenges;
import com.rben23.valia.models.Users;
import com.rben23.valia.syncDataBase.FirebaseManager;

import java.util.List;

public class ChallengeManagement extends AppCompatActivity {
    private Vibrator vibrator;

    private ValiaSQLiteHelper vsql;
    private FirebaseManager firebaseManager;
    private ChallengesDAO challengesDAO;
    private UsersDAO usersDAO;
    private Challenges challenges;
    private Users users;

    // Modo del formulario
    private long mode;

    // Elementos de la vista
    private TextView txvFrameTitle;
    private ImageView imgEditChallenge, imgDelete, goBack;
    private EditText edtNameProperty;
    private EditText edtKmProperty;
    private EditText edtIniDateProperty;
    private Spinner spnUserProperty;
    private View deleteChallengeView;
    private TextView txvDelete;
    private Button btnDelete, btnSave, btnCancel;

    private long idChallenge;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_challenge_management);

        initSystem();
        retrieveIntentExtras();
        initDatabase();
        initViews();
        initListeners();

        // Llenamos los campos
        assignData();
    }

    private void initDatabase() {
        // Creamos la conexion si no existe
        vsql = ValiaSQLiteHelper.getInstance(this);

        usersDAO = vsql.getUsersDAO();
        users = usersDAO.getCurrentUser();
        firebaseManager = FirebaseManager.getInstance(users.getUid());

        challengesDAO = vsql.getChallengesDAO();
        try (Cursor retosCursor = challengesDAO.getRecord(idChallenge)) {
            challenges = challengesDAO.toChallenges(retosCursor);
        }
    }

    private void initSystem() {
        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
    }

    private void retrieveIntentExtras() {
        idChallenge = getIntent().getIntExtra("idChallenge", -1);
    }

    private void initViews() {
        goBack = findViewById(R.id.imgGoBack);
        txvFrameTitle = findViewById(R.id.txvFrameTitle);
        imgEditChallenge = findViewById(R.id.imgEditChallenge);
        edtNameProperty = findViewById(R.id.edtNameProperty);
        edtKmProperty = findViewById(R.id.edtKmProperty);
        edtIniDateProperty = findViewById(R.id.edtIniDateProperty);
        spnUserProperty = findViewById(R.id.spnUserProperty);
        deleteChallengeView = findViewById(R.id.deleteChallengeView);
        txvDelete = findViewById(R.id.txvDelete);
        imgDelete = findViewById(R.id.imgDelete);
        btnDelete = findViewById(R.id.btnDelete);
        btnSave = findViewById(R.id.btnSave);
        btnCancel = findViewById(R.id.btnCancel);
    }

    private void initListeners() {
        // Volvemos a la actividad anterior
        goBack.setOnClickListener(view -> finish());

        // Boton editar
        imgEditChallenge.setOnClickListener(view -> setMode(Constants.C_EDIT));

        // Boton Guardar y Cancelar
        btnSave.setOnClickListener(view -> save());
        btnCancel.setOnClickListener(view -> finish());

        // Boton Eliminar
        btnDelete.setOnClickListener(view -> deleteChallenge(idChallenge));

        // Listener del Spinner
        spnUserProperty.setOnTouchListener((view, motionEvent) -> true);
    }

    private void assignData() {
        edtNameProperty.setText(challenges.getDescription());
        edtKmProperty.setText(Double.toString(challenges.getTotalKm()));
        edtIniDateProperty.setText(challenges.getDate());

        // Inflamos el Spinner
        List<Friends> friendList = vsql.getFriendsDAO().getFriendsByUser(users.getUid());
        FriendsChallengesAdaper friendAdapter = new FriendsChallengesAdaper(this, friendList, users.getUid());
        friendAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        spnUserProperty.setAdapter(friendAdapter);
        spnUserProperty.setSelection(getSelectedIndex(friendList));
    }

    // Calculamos el indice del amigo
    private int getSelectedIndex(List<Friends> friendList) {
        for (int i = 0; i < friendList.size(); i++) {
            Friends amigo = friendList.get(i);
            String friendUid = users.getUid().equals(amigo.getIdUser()) ? amigo.getIdFriend() : amigo.getIdUser();

            if (users.getUid().equals(challenges.getIdAddressee())) {
                if (friendUid.equals(challenges.getIdUser())) {
                    return i;
                }
            } else {
                if (friendUid.equals(challenges.getIdAddressee())) {
                    return i;
                }
            }
        }
        return 0;
    }

    private void save() {
        if (mode == Constants.C_EDIT) {
            String challengeName = edtNameProperty.getText().toString();
            String challengeKm = edtKmProperty.getText().toString();

            if (challengeName.isEmpty() || challengeKm.isEmpty()) {
                Toast.makeText(ChallengeManagement.this, R.string.fieldsRequired, Toast.LENGTH_SHORT).show();
                return;
            }

            challenges.setDescription(challengeName);
            challenges.setTotalKm(Double.parseDouble(challengeKm));

            long challengeResult = challengesDAO.update(challenges);
            firebaseManager.getChallengesSyncManager().synchronizeToRemoteDatabase(() -> Log.d("SyncToRemote", "Reto sincronizado con Firebase"));

            if (challengeResult > 0) {
                Toast.makeText(ChallengeManagement.this, R.string.recordSaved, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(ChallengeManagement.this, R.string.recordNotSaved, Toast.LENGTH_SHORT).show();
            }
        }

        setResult(RESULT_OK);
        finish();
    }

    // Establecer el modo del formulario
    private void setMode(long mode) {
        this.mode = mode;
        setEdition(mode == Constants.C_EDIT);
    }

    // Configurar el modo de edición
    private void setEdition(boolean editionMode) {
        if (vibrator != null) {
            vibrator.vibrate(50);
        }
        edtNameProperty.setEnabled(editionMode);

        if (editionMode) {
            txvFrameTitle.setText(R.string.txv_editChallenge);

            btnSave.setVisibility(VISIBLE);
            btnCancel.setVisibility(VISIBLE);

            deleteChallengeView.setVisibility(GONE);
            imgDelete.setVisibility(GONE);
            txvDelete.setVisibility(GONE);
            btnDelete.setVisibility(GONE);

            imgEditChallenge.setVisibility(GONE);
        }

        if (!editionMode) {
            txvFrameTitle.setText(R.string.txv_showActivity);

            btnSave.setVisibility(GONE);
            btnCancel.setVisibility(GONE);

            deleteChallengeView.setVisibility(VISIBLE);
            imgDelete.setVisibility(VISIBLE);
            txvDelete.setVisibility(VISIBLE);
            btnDelete.setVisibility(VISIBLE);

            imgEditChallenge.setVisibility(VISIBLE);
        }
    }

    // Eliminar reto
    private void deleteChallenge(long idChallenge) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.titleAlert);
        builder.setMessage(R.string.alertMessageChallenges);
        builder.setPositiveButton(R.string.btn_accionAlert, (dialog, which) -> {

                    // Eliminar de firebase
                    firebaseManager.getChallengesSyncManager().deleteInRemoteDatabase(idChallenge);

                    // Eliminar en local
                    try (Cursor retosCursor = challengesDAO.getRecord(idChallenge)) {
                        challengesDAO.toChallenges(retosCursor);
                        challengesDAO.delete(idChallenge);
                    }
                    finish();
                }).setNegativeButton(R.string.btn_cancelAlert, (dialog, which) -> dialog.dismiss())
                .show();
    }

}