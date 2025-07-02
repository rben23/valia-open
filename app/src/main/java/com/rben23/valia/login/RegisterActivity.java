package com.rben23.valia.login;

import static android.view.View.GONE;
import static android.view.View.INVISIBLE;
import static android.view.View.VISIBLE;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.rben23.valia.BaseActivity;
import com.rben23.valia.NavegationActivity;
import com.rben23.valia.R;
import com.rben23.valia.ValiaSQLiteHelper;
import com.rben23.valia.WelcomeActivity;
import com.rben23.valia.models.Users;
import com.rben23.valia.syncDataBase.FirebaseManager;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class RegisterActivity extends BaseActivity {
    private ValiaSQLiteHelper vsql;

    private View vwDangerLogin;
    private TextView txtDangerLogin;
    private LinearLayout loadingLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        vsql = ValiaSQLiteHelper.getInstance(this);

        // Elementos de la vista
        Button btnRegister = findViewById(R.id.btnRegister);
        Button btnExit = findViewById(R.id.btnExit);
        TextInputEditText edtUser = findViewById(R.id.edtUser);
        TextInputEditText edtName = findViewById(R.id.edtName);
        TextInputEditText edtEmail = findViewById(R.id.edtEmail);
        TextInputEditText edtPassword = findViewById(R.id.edtPassword);
        vwDangerLogin = findViewById(R.id.vwDangerLogin);
        txtDangerLogin = findViewById(R.id.txtDangerLogin);
        loadingLayout = findViewById(R.id.loadingLayout);

        // Boton de registrar
        onRegisterButtonClick(btnRegister, edtUser, edtName, edtEmail, edtPassword);

        // Boton volver atrás
        onExitButtonClick(btnExit);
    }

    private void onRegisterButtonClick(Button btnRegister, TextInputEditText edtUser, TextInputEditText edtName,
                                       TextInputEditText edtEmail, TextInputEditText edtPassword) {
        btnRegister.setOnClickListener(view -> {
            // Reiniciar mensajes de error
            vwDangerLogin.setVisibility(GONE);
            loadingLayout.setVisibility(VISIBLE);
            txtDangerLogin.setText("");

            // Coger el texto de los elementos
            String user = edtUser.getText().toString();
            String name = edtName.getText().toString();
            String email = edtEmail.getText().toString();
            String password = edtPassword.getText().toString();

            // Comprobar si los campos son nulos
            if (itsEmpty(user) || itsEmpty(name) || itsEmpty(email) || itsEmpty(password)) {
                // ERROR Campos incompletos
                txtDangerLogin.setText(R.string.txv_incompleteFields);
                vwDangerLogin.setVisibility(VISIBLE);
                loadingLayout.setVisibility(INVISIBLE);
                return; // Salir del flujo
            }

            validateInFirebase(user, name, email, password);
        });
    }

    private void validateInFirebase(String user, String name, String email, String password) {
        // Conectar con firebase
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();

        firebaseAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                FirebaseUser currentUser = firebaseAuth.getCurrentUser();

                if (currentUser != null) {
                    String uid = currentUser.getUid();
                    String creationDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss",
                            Locale.getDefault()).format(new Date());

                    Users users = new Users();
                    users.setUid(uid);
                    users.setUserId(user);
                    users.setName(name);
                    users.setEmail(email);
                    users.setCreationDate(creationDate);

                    long localId = vsql.getUsersDAO().insert(users);
                    if (localId > 0) {
                        saveInFirebase(users);

                        // Redirigimos al usuario
                        goToHomeFragment();
                    } else {
                        txtDangerLogin.setText(R.string.txv_ErrorRegisteringUser);
                        vwDangerLogin.setVisibility(VISIBLE);
                        loadingLayout.setVisibility(INVISIBLE);
                    }

                }
            } else {
                String errorMessage = "";
                Exception exception = task.getException();
                if (exception instanceof FirebaseAuthException) {
                    String errorCode = ((FirebaseAuthException) exception).getErrorCode();
                    switch (errorCode) {
                        case "ERROR_WEAK_PASSWORD":
                            errorMessage = getString(R.string.txv_insecurePassword);
                            break;
                        case "ERROR_EMAIL_ALREADY_IN_USE":
                            errorMessage = getString(R.string.txv_userAlreadyExists);
                            break;
                        case "ERROR_INVALID_EMAIL":
                            errorMessage = getString(R.string.txv_invalidEmail);
                            break;
                        default:
                            errorMessage = getString(R.string.txv_ErrorRegisteringUser);
                            break;
                    }
                } else {
                    errorMessage = getString(R.string.txv_ErrorRegisteringUser);
                }
                txtDangerLogin.setText(errorMessage);
                vwDangerLogin.setVisibility(VISIBLE);
                loadingLayout.setVisibility(INVISIBLE);
            }
        });
    }

    private void goToHomeFragment() {
        //Quitamos pantalla de carga y vamos a la actividad
        Intent intent = new Intent(RegisterActivity.this, NavegationActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        loadingLayout.setVisibility(INVISIBLE);
        startActivity(intent);
        finish();
    }

    private void saveInFirebase(Users users) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users").child(users.getUid());

        reference.setValue(users)
                .addOnSuccessListener(aVoid -> Log.d("FIREBASE MANAGER", "Usuario registrado correctamente"))
                .addOnFailureListener(e -> Log.e("FIREBASE MANAGER", "Error al registrar al usuario"));
    }

    private static boolean itsEmpty(String text) {
        return TextUtils.isEmpty(text);
    }

    private void onExitButtonClick(Button btnExit) {
        btnExit.setOnClickListener(view -> {
            Intent intent = new Intent(RegisterActivity.this, WelcomeActivity.class);
            startActivity(intent);
        });
    }
}
