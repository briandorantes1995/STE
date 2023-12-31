package com.example.ste;

import android.content.Context;
import android.os.Bundle;

import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;
import android.util.SparseArray;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.Toast;

import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;
import org.json.JSONObject;
import java.io.IOException;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class QrReaderFragment extends Fragment {

    private CameraSource cameraSource;
    private SurfaceView cameraView;
    private final int MY_PERMISSIONS_REQUEST_CAMERA = 1;
    private String token = "";
    private String tokenanterior = "";

    String Token;

    String lastScannedQR;

    Context thiscontext;
    OkHttpClient client = new OkHttpClient();

    View view;

    JSONObject json;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_qr_reader, container, false);
        cameraView = (SurfaceView) view.findViewById(R.id.camera_view);
        thiscontext = view.getContext();
        initQR();
        return view;
    }


    public void initQR() {

        // creo el detector qr
        BarcodeDetector barcodeDetector =
                new BarcodeDetector.Builder(thiscontext)
                        .setBarcodeFormats(Barcode.ALL_FORMATS)
                        .build();

        // creo la camara
        cameraSource = new CameraSource
                .Builder(thiscontext, barcodeDetector)
                .setRequestedPreviewSize(1600, 1024)
                .setAutoFocusEnabled(true) //you should add this feature
                .build();

        // listener de ciclo de vida de la camara
        cameraView.getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {

                // verifico si el usuario dio los permisos para la camara
                if (ActivityCompat.checkSelfPermission(thiscontext, android.Manifest.permission.CAMERA)
                        != PackageManager.PERMISSION_GRANTED) {

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        // verificamos la version de ANdroid que sea al menos la M para mostrar
                        // el dialog de la solicitud de la camara
                        if(shouldShowRequestPermissionRationale(
                                android.Manifest.permission.CAMERA));
                        requestPermissions(new String[]{android.Manifest.permission.CAMERA},
                                MY_PERMISSIONS_REQUEST_CAMERA);
                    }
                    return;
                } else {
                    try {
                        cameraSource.start(cameraView.getHolder());
                    } catch (IOException ie) {
                        Log.e("CAMERA SOURCE", ie.getMessage());
                    }
                }
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
                cameraSource.stop();
            }
        });

        // preparo el detector de QR
        barcodeDetector.setProcessor(new Detector.Processor<Barcode>() {
            @Override
            public void release() {
            }


            @Override
            public void receiveDetections(Detector.Detections<Barcode> detections) {
                final SparseArray<Barcode> barcodes = detections.getDetectedItems();
                if (barcodes.size() > 0) {
                    // Obtenemos el token
                    token = barcodes.valueAt(0).displayValue.toString();
                    lastScannedQR = "";

                    // Verificamos si el token escaneado es diferente del último token escaneado
                    if (!token.equals(lastScannedQR)) {
                        // Guardamos el último token escaneado
                        lastScannedQR = token;
                        Log.i("token", token);

                        try {
                            json = new JSONObject(token);
                            Token = json.getString("token");
                            Post(Token);
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(thiscontext, "QR escaneado exitosamente", Toast.LENGTH_SHORT).show();
                                }
                            });
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    } else {
                        // Si el token es el mismo que el anterior, mostramos un toast indicando que ya se escaneó previamente
                        Toast.makeText(thiscontext, "QR escaneado previamente", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

    }//fin qr


    public void Post( String userToken) throws Exception {
        RequestBody requestBody = new FormBody.Builder()
                .build();

        Request request = new Request.Builder()
                .url("https://api-ste.smartte.com.mx/apiv2/updateOnboard")
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



