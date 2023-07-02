package com.example.ste;


import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.material.navigation.NavigationView;

public class HomeChofer extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private DrawerLayout drawerLayout;
    String Name;
    String Rolusuario;
    String Token;

    SharedPreferences sh;

    TextView Usuario;
    TextView Roles;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //Datos pasados por activity de login
        sh = getSharedPreferences("MySharedPref", MODE_PRIVATE);
        Name = sh.getString("name", "");
        Rolusuario = sh.getString("role", "");
        Token = sh.getString("token", "");

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_chofer);
        Toolbar toolbar = findViewById(R.id.toolbar); //Ignore red line errors
        setSupportActionBar(toolbar);
        drawerLayout = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        //Info del header
        View header = navigationView.getHeaderView(0);
        Usuario = header.findViewById(R.id.nombreusuario);
        Roles = header.findViewById(R.id.Rol);
        Usuario.setText(Name);
        Roles.setText(Rolusuario);

        //fin info del header
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.open_nav,
                R.string.close_nav);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new QrReaderFragment()).commit();
            navigationView.setCheckedItem(R.id.nav_lector);
        }
    }//fin on create

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        if(item.getItemId() == R.id.nav_lector) {
            Fragment Home = new QrReaderFragment();
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, Home).commit();
        }
        if(item.getItemId() == R.id.nav_ubicacion) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new UbicacioChofer()).commit();
        }

        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }
    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

}