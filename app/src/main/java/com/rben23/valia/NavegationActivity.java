package com.rben23.valia;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.rben23.valia.DAO.UsersDAO;
import com.rben23.valia.appFragments.ChallengeFragment;
import com.rben23.valia.appFragments.ActivitiesFragment;
import com.rben23.valia.appFragments.HomeFragment;
import com.rben23.valia.appFragments.SocialFragment;
import com.rben23.valia.managements.ProfileImageManagement;
import com.rben23.valia.managements.ProfileManagement;
import com.rben23.valia.managements.RequestManagement;
import com.rben23.valia.models.Users;

public class NavegationActivity extends AppCompatActivity {
    private HomeFragment homeFragment;
    private ActivitiesFragment activitiesFragment;
    private SocialFragment socialFragment;
    private ChallengeFragment challengeFragment;

    private TextView txvFrameTitle;
    private ValiaSQLiteHelper vsql;
    private ProfileImageManagement profileImageManagement;
    private Users users;

    // Se marca true porque se carga nada mas iniciar la app
    private Boolean isHomeFragment = true;
    private Boolean isSocialFragment = false;
    private int titleId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bar);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Creamos la conexion
        vsql = ValiaSQLiteHelper.getInstance(this);

        // Instanciamos los fragmentos
        homeFragment = new HomeFragment();
        activitiesFragment = new ActivitiesFragment();
        socialFragment = new SocialFragment();
        challengeFragment = new ChallengeFragment();

        // Inicializamos ProfileImageManagement y obtenemos el usuario actual
        UsersDAO usersDAO = vsql.getUsersDAO();
        users = usersDAO.getCurrentUser();
        profileImageManagement = new ProfileImageManagement(this, users.getUid());

        // Desactivamos el titulo por defecto en la actionbar
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayShowTitleEnabled(false);
        }

        // Cargar Fragmentos
        txvFrameTitle = findViewById(R.id.txvFrameTitle);
        BottomNavigationView bottomNavigationView = findViewById(R.id.navBar);

        // Cargamos el fragmento con el titulo
        titleId = R.string.mnu_titleHome;
        loadFragment(homeFragment);

        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            // Usamos if else porque los elementos se determinan en tiempo de ejecución
            if (item.getItemId() == R.id.homePage) {
                navigateToFragment(homeFragment, R.string.mnu_titleHome, true, false);

            } else if (item.getItemId() == R.id.activitiesPage) {
                navigateToFragment(activitiesFragment, R.string.mnu_titleActivities, false, false);

            } else if (item.getItemId() == R.id.socialPage) {
                navigateToFragment(socialFragment, R.string.mnu_titleSocial, false, true);

            } else if (item.getItemId() == R.id.challengePage) {
                navigateToFragment(challengeFragment, R.string.mnu_titleChallenges, false, false);
            }

            invalidateOptionsMenu();
            return true;
        });
    }

    private void navigateToFragment(Fragment fragment, int titleRes, boolean isHome, boolean isSocial) {
        isHomeFragment = isHome;
        isSocialFragment = isSocial;
        txvFrameTitle.setText(titleRes);
        getSupportFragmentManager().beginTransaction()
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                .replace(R.id.frameLayout, fragment)
                .commit();
    }

    // Gestionar los cambios y enviar el resultado a los fragmentos
    private void loadFragment(Fragment fragment) {
        txvFrameTitle.setText(titleId);
        getSupportFragmentManager().beginTransaction()
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                .replace(R.id.frameLayout, fragment).commit();
    }

    private void setProfileImageInMenuItem(MenuItem profileImage) {
        Bitmap bitmapImageProfile = profileImageManagement.getLocalUserBitmapProfile();

        if (bitmapImageProfile != null) {
            Drawable profileDrawable = new BitmapDrawable(getResources(), bitmapImageProfile);
            profileImage.setIcon(profileDrawable);
        } else {
            profileImage.setIcon(R.drawable.img_default_user_image);
        }
    }

    // Inflar el menú al crear la toolbar
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar, menu);
        MenuItem profileImage = menu.findItem(R.id.toolbarProfileImage);
        MenuItem searchImage = menu.findItem(R.id.toolbarSearchUsers);

        // Establecer la foto de perfil
        setProfileImageInMenuItem(profileImage);

        // Mostramos u ocultamos los elementos
        profileImage.setVisible(isHomeFragment);
        searchImage.setVisible(isSocialFragment);
        return true;
    }

    // Detectar los clics del menú
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        // Al apretar la foto de perfil ir al perfil del usuario
        if (item.getItemId() == R.id.toolbarProfileImage) {
            startActivity(new Intent(this, ProfileManagement.class));
            return true;
        }

        // Al apretar la lupa ir a la actividad de gestión de solicitudes
        if (item.getItemId() == R.id.toolbarSearchUsers) {
            startActivity(new Intent(this, RequestManagement.class));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    // Cerrar la aplicación sin ir a la pantalla de login
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finishAffinity();
    }
}
