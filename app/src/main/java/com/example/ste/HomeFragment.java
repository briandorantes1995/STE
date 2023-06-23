package com.example.ste;

import android.graphics.drawable.Drawable;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;


public class HomeFragment extends Fragment {

    String Name;
    Integer Rolusuario;
    String Token;

    ImageView credencial;

    JSONObject DatosQR = new JSONObject();

    public static final String TAG = "YOUR-TAG-NAME";





    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View view =  inflater.inflate(R.layout.fragment_home2, container, false);
//Recuperar datos
        if (getArguments() != null) {
            Name= getArguments().getString("Name",null);
            Rolusuario= getArguments().getInt("Rol");
            Token= getArguments().getString("Token",null);
        }

        InputStream URLcontent = null;
        try {
            DatosQR.put("nombre",Name);
            DatosQR.put("token",Token);
            URLcontent = (InputStream) new URL("http://api.qrserver.com/v1/create-qr-code/?data="+DatosQR+"&size=100x100&color=0-0-255").getContent();
        } catch (IOException | JSONException e) {
            throw new RuntimeException(e);
        }
        Drawable image = Drawable.createFromStream(URLcontent, "your source link");
        credencial = view.findViewById(R.id.credencialqr);
        credencial.setImageDrawable(image);


        return view;
    }
}