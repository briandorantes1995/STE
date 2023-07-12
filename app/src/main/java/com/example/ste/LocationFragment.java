package com.example.ste;

import static android.content.Context.MODE_PRIVATE;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;


public class LocationFragment extends Fragment implements OnMapReadyCallback {
    private static final String TAG = "UbicacioChofer";
    private static final int DEFAULT_ZOOM = 15;

    private View view;
    private GoogleMap map;
    private CameraPosition cameraPosition;
    private SupportMapFragment mapFragment;

    private OkHttpClient httpClient;

    private SharedPreferences sharedPref;
    private String token;

    private double latitude;
    private double longitude;

    Integer ruta;

    private Handler handler;
    private static final long UPDATE_INTERVAL = 10 * 1000; // 10 segundos


    private final LatLng defaultLocation = new LatLng(-33.8523341, 151.2106085);

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_location2, container, false);
        sharedPref = requireActivity().getSharedPreferences("MySharedPref", MODE_PRIVATE);
        token = sharedPref.getString("token", null);
        ruta = sharedPref.getInt("route_id", 0);
        mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map2);
        initializeMap(); // Cambiado desde getLocation()
        httpClient = new OkHttpClient();
        handler = new Handler();
        return view;
    }


    public void getLocation() {
        HttpUrl.Builder urlBuilder = HttpUrl.parse("https://api-ste.smartte.com.mx/apiv2/geolocation").newBuilder();
        urlBuilder.addQueryParameter("id", ruta.toString());
        String url = urlBuilder.build().toString();
        Request request = new Request.Builder()
                .url(url)
                .addHeader("Authorization", "Bearer " + token)
                .get()
                .build();

        // Ejecutar la solicitud en el hilo principal
        new Handler(Looper.getMainLooper()).post(() -> {
            try {
                Response response = httpClient.newCall(request).execute();

                if (response.isSuccessful()) {
                    String responseData = response.body().string();
                    Log.e(TAG, responseData);
                    try {
                        JSONObject json = new JSONObject(responseData);
                        latitude = json.getDouble("latitude");
                        longitude = json.getDouble("longitude");
                        updateMapLocation(latitude, longitude);
                    } catch (JSONException e) {
                        Log.e(TAG, "Error al analizar la respuesta JSON: " + e.getMessage(), e);
                    }
                } else {
                    Log.e(TAG, "Error en la solicitud GET: " + response.code() + " " + response.message());
                }
            } catch (IOException e) {
                Log.e(TAG, "Error en la solicitud GET: " + e.getMessage(), e);
            }
        });
    }


    private void updateMapLocation(double latitude, double longitude) {
        if (map != null) {
            LatLng latLng = new LatLng(latitude, longitude);
            map.clear(); // Limpiar los marcadores existentes en el mapa
            map.addMarker(new MarkerOptions().position(latLng).title("Autobus").icon(BitmapFromVector(
                   getActivity(),
                    R.drawable.marcadorautobus)));
            map.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, DEFAULT_ZOOM));
        }
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        map = googleMap;
        getLocation(); // Llama a getLocation() para obtener la ubicación inicial
        startPeriodicLocationUpdates();
    }

    private void initializeMap() {
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    private void startPeriodicLocationUpdates() {
        handler.post(() -> {
            try {
                getLocation();
            } catch (Exception e) {
                Log.e(TAG, "Error al obtener la ubicación: " + e.getMessage(), e);
            } finally {
                handler.postDelayed(this::startPeriodicLocationUpdates, UPDATE_INTERVAL);
            }
        });
    }


    private BitmapDescriptor
    BitmapFromVector(Context context, int vectorResId)
    {
        // below line is use to generate a drawable.
        Drawable vectorDrawable = ContextCompat.getDrawable(
                context, vectorResId);

        // below line is use to set bounds to our vector
        // drawable.
        vectorDrawable.setBounds(
                0, 0, vectorDrawable.getIntrinsicWidth(),
                vectorDrawable.getIntrinsicHeight());

        // below line is use to create a bitmap for our
        // drawable which we have added.
        Bitmap bitmap = Bitmap.createBitmap(
                vectorDrawable.getIntrinsicWidth(),
                vectorDrawable.getIntrinsicHeight(),
                Bitmap.Config.ARGB_8888);

        // below line is use to add bitmap in our canvas.
        Canvas canvas = new Canvas(bitmap);

        // below line is use to draw our
        // vector drawable in canvas.
        vectorDrawable.draw(canvas);

        // after generating our bitmap we are returning our
        // bitmap.
        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }
}





