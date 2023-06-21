package com.example.ste;

import android.graphics.drawable.Drawable;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

public class HomeFragment extends Fragment {

    String Name;
    Integer Rolusuario;
    String Token;

    ImageView credencial;



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
        }//fin recuperar datos

        InputStream URLcontent = null;
        try {
            URLcontent = (InputStream) new URL("https://api-ste.smartte.com.mx/img/student_da/1212773.jpg").getContent();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        Drawable image = Drawable.createFromStream(URLcontent, "your source link");
        credencial = view.findViewById(R.id.credencialqr);
        credencial.setImageDrawable(image);


        return view;
    }
}