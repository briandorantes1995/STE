package com.example.ste;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.StrictMode;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.auth0.android.jwt.JWT;

import org.json.JSONObject;

import java.io.IOException;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;


public class MainActivity extends AppCompatActivity {

    String data;

    String Token;


    String Name;

    Integer Rol;

    String LastName;

    Integer Matricula;

    Boolean Payment;

    Boolean onBoard;

    String role;

    Integer Date;
    Integer Route;

    OkHttpClient client = new OkHttpClient();
    EditText username;
    EditText password;
    Button loginButton;

    SharedPreferences sharedPref;
    SharedPreferences.Editor editor;


    protected void onCreate(Bundle savedInstanceState) {
        StrictMode.ThreadPolicy policy = new
                StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        super.onCreate(savedInstanceState);
        //guardar info en shared preferences
        sharedPref = getSharedPreferences("MySharedPref", MODE_PRIVATE);
        editor = sharedPref.edit();
        //fin
        setContentView(R.layout.activity_main);
        username = findViewById(R.id.usuario);
        password = findViewById(R.id.password);
        loginButton = findViewById(R.id.iniciarsesion);
        checkSession();


        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    Post(username.getText().toString(), password.getText().toString());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

    }


    public void Post(String User, String Password) {
        RequestBody formBody = new FormBody.Builder()
                .add("user", User)
                .add("password", Password)
                .build();
        Request request = new Request.Builder()
                .url("https://api-ste.smartte.com.mx/apiv2/login")
                .post(formBody)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                if (response.code() == 401) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(MainActivity.this, "Usuario o contraseña incorrectos", Toast.LENGTH_SHORT).show();
                        }
                    });
                } else {
                    throw new IOException("Unexpected code " + response);
                }
            } else {
                data = response.body().string();
                JSONObject json = new JSONObject(data);
                Token = json.getString("token");
                JWT jwt = new JWT(Token);
                Name = jwt.getClaim("name").asString();
                LastName = jwt.getClaim("last_name").asString();
                Rol = jwt.getClaim("id").asInt();
                Matricula = jwt.getClaim("matricula").asInt();
                Payment = jwt.getClaim("payment_verifed").asBoolean();
                Date = jwt.getClaim("expiration_at").asInt();
                onBoard = jwt.getClaim("onboard").asBoolean();
                role = jwt.getClaim("role").asString();
                Route = jwt.getClaim("route_id").asInt();
                if (Token != null) {
                    editor.putString("token", Token);
                    editor.putString("name", Name);
                    editor.putString("last_name", LastName);
                    editor.putInt("id", Rol);
                    editor.putInt("matricula", Matricula);
                    editor.putBoolean("payment_verifed", Payment);
                    editor.putInt("expiration_at", Date);
                    editor.putBoolean("onboard", onBoard);
                    editor.putString("role", role);
                    editor.putInt("route_id", Route);
                    editor.apply();
                    if (role.equals("chofer")) {
                        startActivity(new Intent(this, HomeChofer.class));
                    } else {
                        startActivity(new Intent(this, Home.class));
                    }
                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(MainActivity.this, "Has iniciado sesión", Toast.LENGTH_SHORT).show();
                    }
                });
                // ...
            }
        } catch (IOException e) {
            e.printStackTrace();
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(MainActivity.this, "Error al conectarse al servidor", Toast.LENGTH_SHORT).show();
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(MainActivity.this, "Usuario o Contraseña Incorrectos", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
//fin post

    private void checkSession() {
        String token = sharedPref.getString("token", null);
        Integer Date = sharedPref.getInt("expiration_at",0);
        if (token != null && Date != 0 && isTokenValid(Date)) {
            // Si hay un token almacenado, iniciar la actividad correspondiente según el rol del usuario
            String role = sharedPref.getString("role", null);
            if (role != null) {
                if (role.equals("chofer")) {
                    startActivity(new Intent(this, HomeChofer.class));
                } else {
                    startActivity(new Intent(this, Home.class));
                }
                finish(); // Finalizar la actividad actual
            }
        }
    }//fin check sesion


    public static boolean isTokenValid(Integer Fecha) {
        // Obtener el tiempo actual en segundos (UNIX timestamp)
        long currentTime = System.currentTimeMillis() / 1000;
        // Comparar si el token ha expirado
        return Fecha > currentTime;

    }

}