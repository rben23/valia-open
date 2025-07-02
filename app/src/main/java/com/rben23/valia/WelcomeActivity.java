package com.rben23.valia;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.rben23.valia.login.LoginActivity;
import com.rben23.valia.login.RegisterActivity;
import com.rben23.valia.syncDataBase.FirebaseManager;

public class WelcomeActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wellcome);

        Button btnRegister = findViewById(R.id.btnRegister);
        Button btnLogIn = findViewById(R.id.btnLogIn);

        // Ir a la ventanad de registro
        btnRegister.setOnClickListener(view -> {
            startActivity(new Intent(WelcomeActivity.this, RegisterActivity.class));
        });

        // Ir a la ventanad de inicio de sesion
        btnLogIn.setOnClickListener(view -> {
            startActivity(new Intent(WelcomeActivity.this, LoginActivity.class));
        });
    }
}