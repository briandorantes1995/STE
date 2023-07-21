package com.example.ste;


import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.material.navigation.NavigationView;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class HomeChofer extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private DrawerLayout drawerLayout;
    String Name;
    String Rolusuario;
    String Token;

    SharedPreferences sh;
    SharedPreferences.Editor editor;

    TextView Usuario;
    TextView Roles;

    OkHttpClient client = new OkHttpClient();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //Datos pasados por activity de login
        sh = getSharedPreferences("MySharedPref", MODE_PRIVATE);
        editor = sh.edit();
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
        if(item.getItemId() == R.id.nav_logout) {
            Toast.makeText(this, "Logout!", Toast.LENGTH_SHORT).show();
            clearSession();
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

    private void clearSession() {
        try {
            Post(Token);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        editor.remove("name");
        editor.remove("last_name");
        editor.remove("id");
        editor.remove("matricula");
        editor.remove("payment_verifed");
        editor.remove("expiration_at");
        editor.remove("onboard");
        editor.remove("role");
        editor.remove("route_id");
        editor.remove("token");
        editor.apply();
        startActivity(new Intent(this, MainActivity.class));
    }


    public void Post( String userToken) throws Exception {
        RequestBody requestBody = new FormBody.Builder()
                .build();

        Request request = new Request.Builder()
                .url("https://api-ste.smartte.com.mx/V4/students/onboard")
                .addHeader("cache-control", "no-cache")
                .addHeader("Authorization" , "Bearer " + userToken)
                .post(requestBody)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {

            }
        });
    }//fin post

}