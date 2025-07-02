package com.rben23.valia.managements;

import static android.view.View.GONE;
import static android.view.View.INVISIBLE;
import static android.view.View.VISIBLE;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Vibrator;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.rben23.valia.Constants;
import com.rben23.valia.R;
import com.rben23.valia.ValiaSQLiteHelper;
import com.rben23.valia.WelcomeActivity;
import com.rben23.valia.models.Users;
import com.rben23.valia.syncDataBase.FirebaseManager;

public class ProfileManagement extends AppCompatActivity {
    private Vibrator vibrator;

    private ValiaSQLiteHelper vsql;
    private FirebaseManager firebaseManager;
    private ProfileImageManagement profileImageManagement;
    private Users users;

    // Modo del formulario
    private long mode;

    // Elementos de la vista
    private ImageView imgUserProfile, imgEditUser, imgEditProfileImage, imgLogOut, goBack;
    private TextView txvFrameTitle;
    private TextView txvUserName;
    private EditText edtUserProperty;
    private EditText edtNameProperty;
    private EditText edtMailProperty;
    private Button btnLogOut;
    private View logOutView;
    private TextView txvLogOut;

    // Botones de guardar y cancelar
    private Button btnSave, btnCancel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        setContentView(R.layout.activity_profile_management);

        // Creamos la conexion si no existe
        initDatabase();
        initView();
        initListeners();
        loadProfileData();
    }

    private void initDatabase() {
        vsql = ValiaSQLiteHelper.getInstance(this);
        users = vsql.getUsersDAO().getCurrentUser();
        firebaseManager = FirebaseManager.getInstance(users.getUid());
        profileImageManagement = new ProfileImageManagement(this, users.getUid());
    }

    private void initView() {
        goBack = findViewById(R.id.imgGoBack);
        imgUserProfile = findViewById(R.id.imgUserProfile);
        txvFrameTitle = findViewById(R.id.txvFrameTitle);
        txvUserName = findViewById(R.id.txvUserName);
        edtUserProperty = findViewById(R.id.edtUserProperty);
        edtNameProperty = findViewById(R.id.edtNameProperty);
        edtMailProperty = findViewById(R.id.edtMailProperty);
        btnSave = findViewById(R.id.btnSave);
        btnCancel = findViewById(R.id.btnCancel);
        imgEditUser = findViewById(R.id.imgEditUser);
        imgEditProfileImage = findViewById(R.id.imgEditProfileImage);
        btnLogOut = findViewById(R.id.btnLogOut);
        logOutView = findViewById(R.id.logOutView);
        imgLogOut = findViewById(R.id.imgLogOut);
        txvLogOut = findViewById(R.id.txvLogOut);
    }

    private void initListeners() {
        // Volvemos a la actividad anterior
        goBack.setOnClickListener(view -> finish());

        // Boton editar
        imgEditUser.setOnClickListener(view -> setMode(Constants.C_EDIT));

        imgUserProfile.setOnClickListener(view -> {
            if (mode == Constants.C_EDIT) {
                selectImage();
            }
        });

        // Botones de guardar y cancelar
        btnSave.setOnClickListener(view -> save());
        btnCancel.setOnClickListener(view -> finish());
        btnLogOut.setOnClickListener(view -> logOut());
    }

    @SuppressLint("Range")
    private void loadProfileData() {
        profileImageManagement.getLocalUserImageProfile(imgUserProfile);

        String idUsuario = users.getUserId();
        txvUserName.setText(idUsuario);
        edtUserProperty.setText(idUsuario);
        edtNameProperty.setText(users.getName());
        edtMailProperty.setText(users.getEmail());
    }

    private void selectImage() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, 1);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1 && resultCode == RESULT_OK && data != null) {
            Uri imageUri = data.getData();

            // Guardar foto
            if (imageUri != null) {
                Bitmap bitmapImage = ProfileImageManagement.convertToBitmap(this, imageUri);
                saveImage(bitmapImage);
            }
        }
    }

    private void saveImage(Bitmap bitmapImage) {
        // Guardar en base de datos
        String base64Image = profileImageManagement.convertBitmapToBase64(bitmapImage);
        users.setProfileImage(base64Image);

        // Establecer la vista previa
        setProfileImage(users.getProfileImage());
    }

    private void setProfileImage(String base64ImageProfile) {
        if (base64ImageProfile == null || base64ImageProfile.isEmpty()) {
            imgUserProfile.setImageResource(R.drawable.img_default_user_image);
            return;
        }

        Bitmap bitmapImageProfile = profileImageManagement.convertBase64ToBitmap(base64ImageProfile);

        if (bitmapImageProfile == null) {
            imgUserProfile.setImageResource(R.drawable.img_default_user_image);
            return;
        }

        Bitmap circularImageProfile = profileImageManagement.getCircularBitmap(bitmapImageProfile);

        if (circularImageProfile == null) {
            imgUserProfile.setImageResource(R.drawable.img_default_user_image);
            return;
        }

        imgUserProfile.setImageBitmap(circularImageProfile);
    }

    private void save() {
        if (mode == Constants.C_EDIT) {
            String user = edtUserProperty.getText().toString();
            String name = edtNameProperty.getText().toString();
            String email = edtMailProperty.getText().toString();

            if (user.isEmpty() || name.isEmpty() || email.isEmpty()) {
                Toast.makeText(ProfileManagement.this, R.string.fieldsRequired, Toast.LENGTH_SHORT).show();
                return;
            }

            users.setUserId(user);
            users.setName(name);
            users.setEmail(email);

            long result = vsql.getUsersDAO().update(users);

            if (result > 0) {
                firebaseManager.getUsersSyncManager().synchronizeCurrentUserToRemoteDatabase();
                Toast.makeText(ProfileManagement.this, R.string.recordSaved, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(ProfileManagement.this, R.string.recordNotSaved, Toast.LENGTH_SHORT).show();
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
        vibrator.vibrate(50);
        edtUserProperty.setEnabled(editionMode);
        edtNameProperty.setEnabled(editionMode);

        if (editionMode) {
            txvFrameTitle.setText(R.string.mnu_titleEditProfile);

            btnSave.setVisibility(VISIBLE);
            btnCancel.setVisibility(VISIBLE);

            imgEditUser.setVisibility(INVISIBLE);
            imgEditProfileImage.setVisibility(VISIBLE);
            logOutView.setVisibility(GONE);
            btnLogOut.setVisibility(GONE);
            imgLogOut.setVisibility(GONE);
            txvLogOut.setVisibility(GONE);
        } else {
            txvFrameTitle.setText(R.string.mnu_titleProfile);

            btnSave.setVisibility(GONE);
            btnCancel.setVisibility(GONE);

            imgEditUser.setVisibility(VISIBLE);
            imgEditProfileImage.setVisibility(GONE);
            logOutView.setVisibility(VISIBLE);
            btnLogOut.setVisibility(VISIBLE);
            imgLogOut.setVisibility(VISIBLE);
            txvLogOut.setVisibility(VISIBLE);
        }
    }

    private void logOut() {
        // Cerramos sesion en firebase
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        firebaseAuth.signOut();

        clearUserData();
        FirebaseManager.resetInstance();

        // Volvemos a la pantalla de inicio
        Intent intent = new Intent(ProfileManagement.this, WelcomeActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    // Borramos solo los datos esenciales del usuario dejando las rutas y actividades.
    private void clearUserData() {
        vsql.getDB().beginTransaction();
        try {
            vsql.getFriendsDAO().deleteAllByUserId(users.getUid());
            vsql.getMessagesDAO().deleteAllByUserId(users.getUid());
            vsql.getResultsChallengesDAO().deleteAllByUserId(users.getUid());
            vsql.getChallengesDAO().deleteAllByUserId(users.getUid());
            vsql.getFriendRequestsDAO().deleteAllByUserId(users.getUid());
            vsql.getUsersDAO().delete(users.getUid());

            vsql.getDB().setTransactionSuccessful();
            Log.i("DB CLEAR :", "Se han borrado los datos del usuario");
        } catch (Exception e) {
            Log.e("DB CLEAR :", "Error limpiando los datos del usuario", e);
        } finally {
            vsql.getDB().endTransaction();
        }
    }
}