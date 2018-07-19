package com.example.sunny.memoriableplaces;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.List;
import java.util.Locale;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback,GoogleMap.OnMapLongClickListener {

    private GoogleMap mMap;
    LocationManager locationManager;
    LocationListener locationListener;

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                locationManager.requestLocationUpdates(locationManager.GPS_PROVIDER, 0, 0, locationListener);
            }
        }
    }

    public void centerMap(LatLng latLng, String title) {
        mMap.clear();
        mMap.addMarker(new MarkerOptions().position(latLng).title(title));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 4));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);




    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setOnMapLongClickListener(this);
        // Add a marker in Sydney and move the camera

        Intent intent = getIntent();
        int listViewIndex = intent.getIntExtra("PlaceNumber",0);
       Log.i("List view index",String.valueOf(listViewIndex));
        if(listViewIndex==0)
        {
            locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

            locationListener = new LocationListener() {
                @Override
                public void onLocationChanged(Location location) {
                    centerMap(new LatLng(location.getLatitude(), location.getLongitude()), "Your Location");

                }

                @Override
                public void onStatusChanged(String s, int i, Bundle bundle) {

                }

                @Override
                public void onProviderEnabled(String s) {

                }

                @Override
                public void onProviderDisabled(String s) {

                }
            };

            if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)== PackageManager.PERMISSION_GRANTED)
            {
                Log.i("inside","if");

                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,0,0,locationListener);
            }else
            {
                ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION},1);
            }
        }else
        {
            Log.i("inside"," else");
            centerMap(MainActivity.addressArray.get(listViewIndex),MainActivity.list.get(listViewIndex));
        }
    }

    public String getLocation(LatLng latLng) {
        Geocoder geocoder = new Geocoder(getApplicationContext(), Locale.getDefault());
        String address="";
        try {
                List<Address> AddressList = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1);
                if(AddressList!=null&&AddressList.size()>0)
                {

                    if(AddressList.get(0).getFeatureName()!=null)
                    {
                        if(AddressList.get(0).getSubAdminArea()!=null)
                           address+=AddressList.get(0).getSubAdminArea()+", ";
                        address+=AddressList.get(0).getFeatureName()+", ";
                    }


                    if(AddressList.get(0).getCountryName()!=null)
                    {
                        if(AddressList.get(0).getAdminArea()!=null)
                            address+=" "+AddressList.get(0).getAdminArea()+", ";
                        address+=AddressList.get(0).getCountryName();
                    }

                    //address+=AddressList.get(0).toString();
                }

                Log.i("ADDRESS",address);

            }
           catch (Exception e)
           {
               Log.i("Exception",e.toString());
           }

      return address;
    }
    @Override
    public void onMapLongClick(LatLng latLng) {
        mMap.addMarker(new MarkerOptions().position(latLng).title("Your Memoriable Place"));
        String address = getLocation(latLng);
        if(address!=null&&address!="")
        {
            mMap.addMarker(new MarkerOptions().position(latLng).title(address));
            MainActivity.list.add(address);
            MainActivity.addressArray.add(latLng);
            MainActivity.adapter.notifyDataSetChanged();
            addData(address,latLng);

        }
        else
            Toast.makeText(this,"Cant find address",Toast.LENGTH_LONG).show();
    }

    protected void addData(String address,LatLng latLng)
    {
        try
        {
            SQLiteDatabase db= this.openOrCreateDatabase("places",MODE_PRIVATE,null);
            db.execSQL("INSERT INTO placesTable (place,latitude,longitude) VALUES ('"+address+"',"+String.valueOf(latLng.latitude)+","+String.valueOf(latLng.longitude)+")");
            Log.i("Inserted","into db");
        }catch (Exception e)
        {
            Log.i("MapsActivity Sql Error",e.toString());
        }
    }
}
