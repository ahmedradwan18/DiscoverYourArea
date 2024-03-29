package com.example.discoveryourarea;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.List;

public class MapsActivity extends FragmentActivity implements
        OnMapReadyCallback
        , GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener {
    private double latitude, longitude;
    private GoogleMap mMap;
    private GoogleApiClient googleApiClient;
    private LocationRequest locationRequest;
    private Location lastLocation;
    private Marker currentUserLocationMarker;
    private static final int Request_User_location_code = 99;
    private int proximityradius = 10000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            checkUserLocationPermtion();
        }

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    public void onClick(View view) {
        String hospital = "hospital", school = "school", restaurant = "restaurant";
        Object transferData[] = new Object[2];
        getNearbyPlaces getNearbyPlaces = new getNearbyPlaces();

        switch (view.getId()) {
            case R.id.search_address:
                EditText addressEDT = findViewById(R.id.location_search);
                String address = addressEDT.getText().toString();
                List<Address> addressList = null;
                if (!TextUtils.isEmpty(address)) {
                    Geocoder geocoder = new Geocoder(this);
                    MarkerOptions userMarkerOptions = new MarkerOptions();
                    try {
                        addressList = geocoder.getFromLocationName(address, 6);
                        if (addressList != null) {
                            for (int i = 0; i < addressList.size(); i++) {
                                Address userAddress = addressList.get(i);
                                LatLng latLng = new LatLng(userAddress.getLatitude(), userAddress.getLongitude());
                                userMarkerOptions.position(latLng);
                                userMarkerOptions.title(address);
                                userMarkerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
                                currentUserLocationMarker = mMap.addMarker(userMarkerOptions);
                                mMap.addMarker(userMarkerOptions);
                                mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
                                mMap.moveCamera(CameraUpdateFactory.zoomTo(10));
                            }
                        } else
                            Toast.makeText(this, "Location not found...", Toast.LENGTH_SHORT).show();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else
                    Toast.makeText(this, "write any location name...", Toast.LENGTH_SHORT).show();
                break;

            case R.id.hospitals_nearby:
                mMap.clear();
                String url = getUrl(latitude, longitude, hospital);
                transferData[0] = mMap;
                transferData[1] = url;
                getNearbyPlaces.execute(transferData);
                Toast.makeText(this, "Searching for nearby hospitals...", Toast.LENGTH_SHORT).show();
                Toast.makeText(this, "showing nearby hospitals...", Toast.LENGTH_SHORT).show();
                break;

            case R.id.restaurants_nearby:
                mMap.clear();
                 url = getUrl(latitude, longitude, restaurant);
                transferData[0] = mMap;
                transferData[1] = url;
                getNearbyPlaces.execute(transferData);
                Toast.makeText(this, "Searching for nearby restaurants...", Toast.LENGTH_SHORT).show();
                Toast.makeText(this, "showing nearby restaurants...", Toast.LENGTH_SHORT).show();
                break;
            case R.id.schools_nearby:
                mMap.clear();
                 url = getUrl(latitude, longitude, school);
                transferData[0] = mMap;
                transferData[1] = url;
                getNearbyPlaces.execute(transferData);
                Toast.makeText(this, "Searching for nearby schools...", Toast.LENGTH_SHORT).show();
                Toast.makeText(this, "showing nearby schools...", Toast.LENGTH_SHORT).show();
                break;


        }


    }

    private String getUrl(double latitude, double longitude, String nearbyPlace) {
        StringBuilder googleUrl = new StringBuilder("https://maps.googleapis.com/maps/api/place/nearbysearch/json?");
        googleUrl.append("location=" + latitude + "," + longitude);
        googleUrl.append("&radius=" + proximityradius);
        googleUrl.append("&type=" + nearbyPlace);
        googleUrl.append("&sensor=true");
        googleUrl.append("&key=" + "AIzaSyDtIWXQDUA1ufc_Vff3qbz522DnZ26Nk9w");

        return googleUrl.toString();


    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

            buildGoogleApiClient();
            mMap.setMyLocationEnabled(true);

        }
    }

    public boolean checkUserLocationPermtion() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, Request_User_location_code);
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, Request_User_location_code);

            }
            return false;
        }

        return true;
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case Request_User_location_code:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                        if (googleApiClient == null) {
                            buildGoogleApiClient();
                        }
                        mMap.setMyLocationEnabled(true);
                    }
                } else Toast.makeText(this, "Permission Denied...", Toast.LENGTH_SHORT).show();
                return;

        }

    }

    protected synchronized void buildGoogleApiClient() {

        googleApiClient = new GoogleApiClient.Builder(this).
                addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API).build();
        googleApiClient.connect();


    }


    @Override
    public void onLocationChanged(Location location) {
        latitude=location.getLatitude();
        longitude=location.getLongitude();

        lastLocation = location;
        if (currentUserLocationMarker != null) {
            currentUserLocationMarker.remove();
        }
        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(latLng);
        markerOptions.title("User Current Location");
        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ROSE));
        currentUserLocationMarker = mMap.addMarker(markerOptions);
        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        mMap.moveCamera(CameraUpdateFactory.zoomBy(12));
        if (googleApiClient != null) {
            LocationServices.FusedLocationApi.removeLocationUpdates(googleApiClient, this);

        }

    }


    @Override
    public void onConnected(@Nullable Bundle bundle) {
        locationRequest = new LocationRequest();
        locationRequest.setInterval(1100);
        locationRequest.setFastestInterval(1100);
        locationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, this);


        }


    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }
}
