package com.example.ste;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;

import org.json.JSONException;
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

    private static final String TAG = "QrReaderFragment";
    private static final int MY_PERMISSIONS_REQUEST_CAMERA = 1;

    private Context context;
    private SurfaceView cameraView;
    private CameraSource cameraSource;
    private BarcodeDetector barcodeDetector;
    private String previousToken = "";

    private OkHttpClient client = new OkHttpClient();

    boolean qrDetectionEnabled;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_qr_reader, container, false);
        context = view.getContext();
        cameraView = view.findViewById(R.id.camera_view);
        initQR();
        return view;
    }

    private void initQR() {
        barcodeDetector = new BarcodeDetector.Builder(context)
                .setBarcodeFormats(Barcode.QR_CODE)
                .build();

        cameraSource = new CameraSource.Builder(context, barcodeDetector)
                .setRequestedPreviewSize(1600, 1024)
                .setAutoFocusEnabled(true)
                .build();

        cameraView.getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(@NonNull SurfaceHolder holder) {
                if (ActivityCompat.checkSelfPermission(context, Manifest.permission.CAMERA)
                        != PackageManager.PERMISSION_GRANTED) {
                    requestCameraPermission();
                } else {
                    try {
                        startCamera();
                    } catch (IOException e) {
                        Log.e(TAG, "Error al iniciar la cámara: " + e.getMessage(), e);
                    }
                }
            }

            @Override
            public void surfaceChanged(@NonNull SurfaceHolder holder, int format, int width, int height) {
            }

            @Override
            public void surfaceDestroyed(@NonNull SurfaceHolder holder) {
                stopCamera();
            }
        });

        // ...

        qrDetectionEnabled = true;

        barcodeDetector.setProcessor(new Detector.Processor<Barcode>() {
            @Override
            public void release() {
            }

            @Override
            public void receiveDetections(@NonNull Detector.Detections<Barcode> detections) {
            }

            // Remove @Override annotation here
            public void onNewItem(int id, Barcode item) {
                if (!qrDetectionEnabled) {
                    return;
                }

                qrDetectionEnabled = false;

                String token = item.displayValue;
                if (!token.equals(previousToken)) {
                    previousToken = token;
                    Log.i(TAG, "Token: " + token);
                    try {
                        JSONObject json = new JSONObject(token);
                        String userToken = json.getString("token");
                        post(userToken);
                        showToast("QR escaneado exitosamente");
                    } catch (JSONException e) {
                        Log.e(TAG, "JSONException: " + e.getMessage(), e);
                    }
                } else{
                    showToast("QR escaneado previamente");
                }

                // Enable QR detection after a delay
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        qrDetectionEnabled = true;
                    }
                }, 2000); // Adjust the delay as needed
            }
        });

// ...

    }

    private void requestCameraPermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), Manifest.permission.CAMERA)) {
            Toast.makeText(context, "Se requiere permiso de la cámara para escanear códigos QR", Toast.LENGTH_SHORT).show();
        }
        requestPermissions(new String[]{Manifest.permission.CAMERA}, MY_PERMISSIONS_REQUEST_CAMERA);
    }

    private void startCamera() throws IOException {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        cameraSource.start(cameraView.getHolder());
    }

    private void stopCamera() {
        cameraSource.stop();
    }

    private void post(String userToken) {
        RequestBody requestBody = new FormBody.Builder().build();
        Request request = new Request.Builder()
                .url("https://api-ste.smartte.com.mx/apiv2/updateOnboard")
                .addHeader("cache-control", "no-cache")
                .addHeader("Authorization", "Bearer " + userToken)
                .post(requestBody)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Log.e(TAG, "Error en la solicitud POST: " + e.getMessage(), e);
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                // Maneja la respuesta aquí si es necesario
            }
        });
    }

    private void showToast(String message) {
        getActivity().runOnUiThread(() -> {
            Handler handler = new Handler();
            handler.postDelayed(() -> Toast.makeText(context, message, Toast.LENGTH_SHORT).show(), 0);
        });
    }

}



