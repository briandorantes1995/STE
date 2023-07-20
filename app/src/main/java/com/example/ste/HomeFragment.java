package com.example.ste;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;


public class HomeFragment extends Fragment {

    String Name;
    String Rolusuario;
    String Token;

    SharedPreferences sh;

    ImageView credencial;

    JSONObject DatosQR = new JSONObject();

    String data;

    public static final String TAG = "YOUR-TAG-NAME";

    private OkHttpClient client;

    boolean Onboard;





    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View view =  inflater.inflate(R.layout.fragment_home2, container, false);
//Recuperar datos
        sh = this.getActivity().getSharedPreferences("MySharedPref", Context.MODE_PRIVATE);
        Name = sh.getString("name", "");
        Rolusuario = sh.getString("role", "");
        Token = sh.getString("token", "");
        client = new OkHttpClient();
        try {
            getOnboard();
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }

        InputStream URLcontent = null;
        try {
            DatosQR.put("token",Token);
            URLcontent = (InputStream) new URL("http://api.qrserver.com/v1/create-qr-code/?data="+DatosQR+"&size=100x100&color=0-0-255").getContent();
        } catch (IOException | JSONException e) {
            throw new RuntimeException(e);
        }
        Drawable image = Drawable.createFromStream(URLcontent, "your source link");
        credencial = view.findViewById(R.id.credencialqr);
        if(!Onboard){
            credencial.setImageDrawable(image);
        }else{
            credencial.setImageResource(R.drawable.checkin);
        }

        return view;
    }

    public void getOnboard() throws IOException, JSONException {
        Request request = new Request.Builder()
                .url("https://api-ste.smartte.com.mx/apiv2/onboardStatus")
                .addHeader("Authorization", "Bearer " + Token)
                .build();

        Call call = client.newCall(request);
        Response response = call.execute();
        data = response.body().string();
        JSONObject json = new JSONObject(data);
        Onboard = json.getBoolean("onboard");
    }
}