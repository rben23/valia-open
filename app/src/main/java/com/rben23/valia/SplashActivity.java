package com.rben23.valia;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.rben23.valia.models.Users;
import com.rben23.valia.syncDataBase.FirebaseManager;

public class SplashActivity extends BaseActivity {
    private ValiaSQLiteHelper vsql;
    private FirebaseAuth firebaseAuth;
    private FirebaseManager firebaseManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        // Preparar y sincronizar Bases de datos
        prepareDatabases();

        // Inicializamos Firebase Authenticaition
        firebaseAuth = FirebaseAuth.getInstance();

        // Si el usuario esta registrado se redirige a la pantalla principal
        if (firebaseAuth.getCurrentUser() != null) {
            navigateToActivity(NavegationActivity.class, 1500, true);
        } else {
            navigateToActivity(WelcomeActivity.class, 1500, true);
        }
    }

    private void prepareDatabases() {
        // Llamada ValiaSQLiteHelper
        vsql = ValiaSQLiteHelper.getInstance(this);

        Users currentUser = vsql.getUsersDAO().getCurrentUser();
        if (currentUser == null) {
            navigateToActivity(WelcomeActivity.class, 1500, true);
            return;
        }

        // Llamada FirebaseManager
        firebaseManager = FirebaseManager.getInstance(currentUser.getUid());
        firebaseManager.getUsersSyncManager().synchronizeToLocalDatabase(() -> Log.i("USERS SYNC: ", "Sincronización completa"));
    }

    private void navigateToActivity(Class<?> targetActivity, long delayMillis, boolean finishCurrent) {
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            Intent intent = new Intent(SplashActivity.this, targetActivity);
            startActivity(intent);
            if (finishCurrent) {
                finish();
            }
        }, delayMillis);
    }
}