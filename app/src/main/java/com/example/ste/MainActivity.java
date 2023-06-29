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

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    Post(username.getText().toString(),password.getText().toString());
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        });
    }


    public void Post(String User,String Password) throws Exception {
        RequestBody formBody = new FormBody.Builder()
                .add("user", User)
                .add("password", Password)
                .build();
        Request request = new Request.Builder()
                .url("https://api-ste.smartte.com.mx/apiv2/login")
                .post(formBody)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);
            Toast.makeText(getApplicationContext(), "Has iniciado Sesion", Toast.LENGTH_SHORT).show();
            data = response.body().string();
            JSONObject json = new JSONObject(data);
            Token = json.getString("token");
            JSONObject Usuario = json.getJSONObject("user");
            Name = Usuario.getString("name");
            LastName = Usuario.getString("last_name");
            Rol = Usuario.getInt("id");
            Matricula = Usuario.getInt("matricula");
            Payment = Usuario.getBoolean("payment_verifed");
            Date = Usuario.getInt("expiration_at");
            onBoard = Usuario.getBoolean("onboard");
            role = Usuario.getString("role");
            if (Token != null) {
                editor.putString("name",Name);
                editor.putString("last_name",LastName);
                editor.putInt("id",Rol);
                editor.putInt("matricula",Matricula);
                editor.putBoolean("payment_verifed",Payment);
                editor.putInt("expiration_at",Date);
                editor.putBoolean("onboard",onBoard);
                editor.putString("role",role);
                editor.putString("token",Token);
                editor.apply();
                Intent user = new Intent(this, Home.class);
                Intent chofer = new Intent(this, HomeChofer.class);
                if(role.equals("chofer")){
                    startActivity(chofer);
                }else{
                    startActivity(user);
                }

            }
        }
    }//fin post

}