package com.example.ste;

import static android.content.Context.MODE_PRIVATE;

import android.Manifest;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class LocationFragment extends Fragment implements OnMapReadyCallback {
    private static final String TAG = "UbicacioChofer";
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    private static final int DEFAULT_ZOOM = 15;

    private View view;
    private FusedLocationProviderClient fusedLocationClient;
    private GoogleMap map;
    private CameraPosition cameraPosition;
    private boolean locationPermissionGranted;
    private Location lastKnownLocation;
    private SupportMapFragment mapFragment;
    private LocationCallback locationCallback;

    private OkHttpClient httpClient;

    private SharedPreferences sharedPref;
    private String token;

    private double latitude;
    private double longitude;

    private Handler handler;
    private static final long UPDATE_INTERVAL = 10 * 1000; // 10 segundos

    private final LatLng defaultLocation = new LatLng(-33.8523341, 151.2106085);

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_location2, container, false);
        sharedPref = requireActivity().getSharedPreferences("MySharedPref", MODE_PRIVATE);
        token = sharedPref.getString("token", null);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext());
        mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map2);
        getLocationPermission();
        return view;
    }

    public void getLocation(String userToken) {
        Request request = new Request.Builder()
                .url("https://api-ste.smartte.com.mx/apiv2/location")
                .addHeader("Authorization", "Bearer " + userToken)
                .build();

        httpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Log.e(TAG, "Error en la solicitud GET: " + e.getMessage(), e);
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    String responseData = response.body().string();
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
            }
        });
    }

    private void updateMapLocation(double latitude, double longitude) {
        if (map != null) {
            LatLng latLng = new LatLng(latitude, longitude);
            map.clear(); // Limpiar los marcadores existentes en el mapa
            map.addMarker(new MarkerOptions().position(latLng).title("Current Location"));
            map.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, DEFAULT_ZOOM));
        }
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        map = googleMap;
        getDeviceLocation();
        updateLocationUI();
        startLocationUpdates();
        handler = new Handler();
        getPeriodicLocationUpdates();
    }

    private void getLocationPermission() {
        if (ContextCompat.checkSelfPermission(requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            locationPermissionGranted = true;
            initializeMap();
        } else {
            ActivityCompat.requestPermissions(requireActivity(),
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }
    }

    private void initializeMap() {
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        locationPermissionGranted = false;
        if (requestCode == PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                locationPermissionGranted = true;
                initializeMap();
            }
        }
    }

    private void updateLocationUI() {
        if (map == null) {
            return;
        }
        try {
            if (locationPermissionGranted) {
                map.setMyLocationEnabled(true);
                map.getUiSettings().setMyLocationButtonEnabled(true);
            } else {
                map.setMyLocationEnabled(false);
                map.getUiSettings().setMyLocationButtonEnabled(false);
                lastKnownLocation = null;
                getLocationPermission();
            }
        } catch (SecurityException e) {
            Log.e(TAG, "Exception: " + e.getMessage(), e);
        }
    }

    private void getDeviceLocation() {
        try {
            if (locationPermissionGranted) {
                Task<Location> locationResult = fusedLocationClient.getLastLocation();
                locationResult.addOnCompleteListener(requireActivity(), task -> {
                    if (task.isSuccessful()) {
                        lastKnownLocation = task.getResult();
                        if (lastKnownLocation != null) {
                            map.moveCamera(CameraUpdateFactory.newLatLngZoom(
                                    new LatLng(lastKnownLocation.getLatitude(),
                                            lastKnownLocation.getLongitude()), DEFAULT_ZOOM));
                        } else {
                            Log.d(TAG, "Current location is null. Using defaults.");
                            map.moveCamera(CameraUpdateFactory.newLatLngZoom(defaultLocation, DEFAULT_ZOOM));
                            map.getUiSettings().setMyLocationButtonEnabled(false);
                        }
                    } else {
                        Log.e(TAG, "Exception: " + task.getException());
                    }
                });
            }
        } catch (SecurityException e) {
            Log.e(TAG, "Exception: " + e.getMessage(), e);
        }
    }

    private void startLocationUpdates() {
        try {
            LocationRequest locationRequest = LocationRequest.create();
            locationRequest.setInterval(10000);
            locationRequest.setFastestInterval(10000);
            locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

            locationCallback = new LocationCallback() {
                @Override
                public void onLocationResult(LocationResult locationResult) {
                    super.onLocationResult(locationResult);
                    if (locationResult != null) {
                        Location location = locationResult.getLastLocation();
                        if (location != null) {
                            lastKnownLocation = location;
                            latitude = location.getLatitude();
                            longitude = location.getLongitude();
                            updateMapLocation(latitude, longitude);
                        }
                    }
                }
            };

            if (locationPermissionGranted) {
                fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());
            }
        } catch (SecurityException e) {
            Log.e(TAG, "Exception: " + e.getMessage(), e);
        }
    }

    private void getPeriodicLocationUpdates() {
        handler.postDelayed(() -> {
            try {
                getLocation(token);
                handler.postDelayed(this::getPeriodicLocationUpdates, UPDATE_INTERVAL);
            } catch (Exception e) {
                Log.e(TAG, "Error al obtener la ubicación: " + e.getMessage(), e);
            }
        }, UPDATE_INTERVAL);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (fusedLocationClient != null && locationCallback != null) {
            fusedLocationClient.removeLocationUpdates(locationCallback);
        }
    }
}
