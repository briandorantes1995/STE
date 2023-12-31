package com.example.ste;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.ImageDecoder;
import android.net.Uri;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.fragment.app.Fragment;

import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.IOException;


import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class PaymentFragment extends Fragment {
    String Name;
    String Rolusuario;
    String Token;

    SharedPreferences sh;

    Button BSelectImage;

    Button Upload;

    ImageView Pago;

    Bitmap selectedImageBitmap;

    OkHttpClient client = new OkHttpClient();

    Context thiscontext;

    int SELECT_PICTURE = 200;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_payment, container, false);
        thiscontext = container.getContext();
        //Inicio Recuperar Datos usuario
        sh = this.getActivity().getSharedPreferences("MySharedPref", Context.MODE_PRIVATE);
        Name = sh.getString("name", "");
        Rolusuario = sh.getString("role", "");
        Token = sh.getString("token", "");
        //fin recuperar datos


        BSelectImage = view.findViewById(R.id.subirarchivo);
        Pago = view.findViewById(R.id.comprobante);
       Upload = view.findViewById(R.id.enviar);

        // handle the Choose Image button to trigger
        // the image chooser function
        BSelectImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                imageChooser();
            }
        });


        Upload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    Post(selectedImageBitmap,Token);
                    Toast.makeText(thiscontext, "se ha enviado el pago correctamente", Toast.LENGTH_SHORT).show();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        });




    return view;
    }//fin create

    // this function is triggered when
    // the Select Image Button is clicked
    private void imageChooser()
    {
        Intent i = new Intent();
        i.setType("image/*");
        i.setAction(Intent.ACTION_GET_CONTENT);

        launchSomeActivity.launch(i);
    }//selector de imagen

    ActivityResultLauncher<Intent> launchSomeActivity
            = registerForActivityResult(
            new ActivityResultContracts
                    .StartActivityForResult(),
            result -> {
                if (result.getResultCode()
                        == Activity.RESULT_OK) {
                    Intent data = result.getData();
                    // do your operation from here....
                    if (data != null
                            && data.getData() != null) {
                        Uri selectedImageUri = data.getData();
                        ImageDecoder.Source source = ImageDecoder.createSource(getActivity().getContentResolver(), selectedImageUri);
                        try {
                            selectedImageBitmap = ImageDecoder.decodeBitmap(source);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        Pago.setImageBitmap(
                                selectedImageBitmap);
                    }
                }
            });//fin de seleccion de imagen


    public void Post(Bitmap Image,String userToken) throws Exception {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        Image.compress(Bitmap.CompressFormat.JPEG, 100, stream);
        byte[] byteArray = stream.toByteArray();

        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("payment_file", "image.jpg",
                        RequestBody.create(byteArray,MediaType.parse("image/jpg")))
                .build();

        Request request = new Request.Builder()
                .url("https://api-ste.smartte.com.mx/apiv2/upload_file")
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
    }


}//fin clase