package com.rben23.valia.login;

import static android.view.View.GONE;
import static android.view.View.INVISIBLE;
import static android.view.View.VISIBLE;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.rben23.valia.BaseActivity;
import com.rben23.valia.NavegationActivity;
import com.rben23.valia.R;
import com.rben23.valia.WelcomeActivity;
import com.rben23.valia.syncDataBase.FirebaseManager;

public class LoginActivity extends BaseActivity {
    View vwDangerLogin;
    TextView txtDangerLogin;
    LinearLayout loadingLayout;
    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Inicializamos Firebase Authenticaition
        firebaseAuth = FirebaseAuth.getInstance();

        // Elementos de la vista
        Button btnLogIn = findViewById(R.id.btnLogIn);
        Button btnExit = findViewById(R.id.btnExit);
        TextInputEditText edtEmail = findViewById(R.id.edtEmail);
        TextInputEditText edtPassword = findViewById(R.id.edtPassword);
        vwDangerLogin = findViewById(R.id.vwDangerLogin);
        txtDangerLogin = findViewById(R.id.txtDangerLogin);
        loadingLayout = findViewById(R.id.loadingLayout);

        // Boton de iniciar sesion
        onLoginButtonClick(btnLogIn, edtEmail, edtPassword);

        // Boton de volver atras
        onExitButtonClick(btnExit);
    }

    private void onExitButtonClick(Button btnExit) {
        btnExit.setOnClickListener(view -> {
            Intent intent = new Intent(LoginActivity.this, WelcomeActivity.class);
            startActivity(intent);
        });
    }

    private void onLoginButtonClick(Button btnLogIn, TextInputEditText edtUser, TextInputEditText edtPassword) {
        btnLogIn.setOnClickListener(view -> {
            // Reiniciar mensajes de error
            vwDangerLogin.setVisibility(GONE);
            loadingLayout.setVisibility(INVISIBLE);
            txtDangerLogin.setText("");

            // Coger el texto de los elementos
            String user = edtUser.getText().toString();
            String password = edtPassword.getText().toString();

            if (!user.trim().isEmpty() && !password.trim().isEmpty()) {
                // Inicializar Firebase
                validateInFirebase(user, password);
            } else {
                loadingLayout.setVisibility(GONE);
                txtDangerLogin.setText(R.string.txv_incompleteFields);
                vwDangerLogin.setVisibility(VISIBLE);
            }

        });
    }

    private void validateInFirebase(String user, String password) {
        loadingLayout.setVisibility(VISIBLE);

        // Conectar con firebase
        firebaseAuth.signInWithEmailAndPassword(user, password).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                FirebaseUser currentUser = firebaseAuth.getCurrentUser();
                if (currentUser != null) {
                    // Sincronizar con firebase
                    FirebaseManager firebaseManager = FirebaseManager.getInstance(currentUser.getUid());
                    firebaseManager.syncAllToLocalDatabase(() -> {

                        // Redirigimos al usuario
                        goToHomeFragment();
                    });
                }
            } else {
                loadingLayout.setVisibility(GONE);
                txtDangerLogin.setText(R.string.txv_incorrectUserPassword);
                vwDangerLogin.setVisibility(VISIBLE);
            }
        });
    }

    private void goToHomeFragment() {
        Intent intent = new Intent(LoginActivity.this, NavegationActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}