package com.sumasoft.findcoffeeshop.activities;

/**
 * Created by sumasoft on 25/09/17.
 */

import android.Manifest;
import android.app.Dialog;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.location.Location;
import com.google.android.gms.location.LocationListener;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.sumasoft.findcoffeeshop.R;
import com.sumasoft.findcoffeeshop.interfaces.RetrofitMaps;
import com.sumasoft.findcoffeeshop.model.CoffeeShopResponse;
import com.sumasoft.findcoffeeshop.model.Result;
import com.sumasoft.findcoffeeshop.utils.MapUtils;
import com.google.android.gms.location.LocationServices;

import retrofit.Call;
import retrofit.Callback;
import retrofit.Response;
import retrofit.Retrofit;
import retrofit.GsonConverterFactory;

import static android.R.attr.disabledAlpha;
import static android.R.attr.type;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener,GoogleMap.OnMarkerClickListener {

    private GoogleMap mMap;
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    private Location mLastLocation;
    private Marker mCurrLocationMarker;
    private final int PROXIMITY_RADIUS = 10000;
    Response<CoffeeShopResponse> mResponse = null;
    private BitmapDescriptor mCurrentbitmapDescriptor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        //show error dialog if Google Play Services not available
        if (!MapUtils.isGooglePlayServicesAvailable(MapsActivity.this, MapsActivity.this)) {
            Log.d("onCreate", "Google Play Services not available. Ending Test case.");
            finish();
        } else {
            Log.d("onCreate", "Google Play Services available. Continuing.");
        }

        //checked for gps is enabled or not
        MapUtils.isGpsEnabled(MapsActivity.this, MapsActivity.this);

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
        mMap.setMyLocationEnabled(true);
        try {
            if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (ContextCompat.checkSelfPermission(this,
                        Manifest.permission.ACCESS_FINE_LOCATION)
                        == PackageManager.PERMISSION_GRANTED) {
                    buildGoogleApiClient();
                    mMap.setMyLocationEnabled(true);
                }
            } else {
                buildGoogleApiClient();
                mMap.setMyLocationEnabled(true);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();
    }

    private LatLng getCurrentLatLong(Location location) {
        //get current location marker
        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        return latLng;
    }

    @Override
    public void onLocationChanged(Location location) {
        Log.d("onLocationChanged", "entered");

        mLastLocation = location;

        //current latitude and longitude
        LatLng latLng = getCurrentLatLong(location);

        //get Bitmapdescriptor from vector resource
        mCurrentbitmapDescriptor = bitmapDescriptorFromVector(MapsActivity.this, R.drawable.ic_person_pin_circle_blue_24dp);
        if (mCurrLocationMarker != null) {
            mCurrLocationMarker.remove();
        }
        //add current location marker
        mCurrLocationMarker = addMarker(latLng,mCurrentbitmapDescriptor,"Current Location");

        Log.d("onLocationChanged", String.format("latitude:%.3f longitude:%.3f", latLng.latitude, latLng.longitude));

        Log.d("onLocationChanged", "Exit");

        //find coffee shops of near by location
        getCoffeeShopsNearMe(latLng);

        mMap.setOnMarkerClickListener(this);
    }

    private Marker addMarker(LatLng latLng, BitmapDescriptor bitmapDescriptor,String title) {
        Marker marker = null;
        try {
            MarkerOptions markerOptions = new MarkerOptions();
            //position of marker on map
            markerOptions.position(latLng);
            //title to marker
            markerOptions.title(title);

            // Adding icon to the marker
            markerOptions.icon(bitmapDescriptor);

            // Adding Marker to the Map
            marker = mMap.addMarker(markerOptions);

            //move map camera
            mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
            mMap.animateCamera(CameraUpdateFactory.zoomTo(11));
            return marker;
        }catch (Exception e){
            e.printStackTrace();
        }
        return marker;
    }


    private void getCoffeeShopsNearMe(LatLng latLng) {
        String url = "https://maps.googleapis.com/maps/";

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(url)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        RetrofitMaps service = retrofit.create(RetrofitMaps.class);

        Call<CoffeeShopResponse> call = service.getNearbyPlaces("cafe", latLng.latitude + "," + latLng.longitude, PROXIMITY_RADIUS);

        call.enqueue(new Callback<CoffeeShopResponse>() {
            @Override
            public void onResponse(Response<CoffeeShopResponse> response, Retrofit retrofit) {

                try {
                    mMap.clear();

                    if(mCurrLocationMarker != null) {
                        //current latitude and longitude
                        LatLng mlatLng = getCurrentLatLong(mLastLocation);
                        //add current location marker
                        mCurrLocationMarker = addMarker(mlatLng, mCurrentbitmapDescriptor, "Current Location");
                    }

                    mResponse = response;
                    // This loop will go through all the results and add marker on each location.
                    for (int i = 0; i < response.body().getResults().size(); i++) {
                        Double lat = response.body().getResults().get(i).getGeometry().getLocation().getLat();
                        Double lng = response.body().getResults().get(i).getGeometry().getLocation().getLng();
                        LatLng latLng = new LatLng(lat, lng);

                        //get bitmap descriptor from vector resource
                        BitmapDescriptor bitmapDescriptor  = bitmapDescriptorFromVector(MapsActivity.this, R.drawable.ic_map_cafe_brown_24dp);
                        Marker marker = addMarker(latLng,bitmapDescriptor,response.body().getResults().get(i).getName());
                        try {
                            if(marker != null) {
                                if (response.body().getResults().get(i).getId() == null) {
                                    marker.setTag(i);
                                } else {
                                    marker.setTag(response.body().getResults().get(i).getId());
                                }
                            }
                        }catch (Exception e){
                            e.printStackTrace();
                        }

                    }
                } catch (Exception e) {
                    Log.d("onResponse", "There is an error");
                    e.printStackTrace();
                }
            }
            @Override
            public void onFailure(Throwable t) {
                Log.d("onFailure", t.toString());
            }
        });

    }

    //create bitmap descriptor from vector resource
    private BitmapDescriptor bitmapDescriptorFromVector(Context context, int vectorResId) {
        Drawable vectorDrawable = ContextCompat.getDrawable(context, vectorResId);
        vectorDrawable.setBounds(0, 0, vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight());
        Bitmap bitmap = Bitmap.createBitmap(vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        vectorDrawable.draw(canvas);
        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(30000);
        mLocationRequest.setFastestInterval(30000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, MapsActivity.this);
        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        try {
            String tag = "";
            if (marker.getTag() != null) {
                tag = marker.getTag().toString();
            }
            if (mResponse != null) {
                for (int i = 0; i < mResponse.body().getResults().size(); i++) {
                    if (mResponse.body().getResults().get(i).getId().equals(tag) || mResponse.body().getResults().get(i).getId().equals(i)) {
                        showInfoWindow(mResponse.body().getResults().get(i));
                        break;
                    }
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return true;
    }

    private void showInfoWindow(Result result) {
        if(result != null) {
            try {
                boolean isOpenNow = false;
                String name = result.getName();
                String vivinity = result.getVicinity();
                if (result.getOpeningHours() != null) {
                    isOpenNow = result.getOpeningHours().getOpenNow();
                }
                double rating = result.getRating();

                // Create custom dialog object
                final Dialog dialog = new Dialog(MapsActivity.this);
                // Include dialog.xml file
                dialog.setContentView(R.layout.popup_map_info_window);

                dialog.setCancelable(true);
                // Set dialog title
                dialog.setTitle("Information");
                TextView txtName = dialog.findViewById(R.id.txtName);
                TextView txtVicinity = dialog.findViewById(R.id.txtVicinity);
                TextView txtIsOpen = dialog.findViewById(R.id.txtIsOpen);
                TextView txtRating = dialog.findViewById(R.id.txtRating);
                RatingBar ratingbar = dialog.findViewById(R.id.rating);

                //set name of coffee shop
                if (name != null) {
                    txtName.setText(name);
                }
                //set address of coffee shop
                if (vivinity != null) {
                    txtVicinity.setText(vivinity);
                }
                //set open status of coffe shop
                if (isOpenNow) {
                    txtIsOpen.setText(getResources().getString(R.string.open_yes));
                } else {
                    txtIsOpen.setText(getResources().getString(R.string.open_no));
                }
                if (result.getOpeningHours() == null) {
                    txtIsOpen.setVisibility(View.GONE);
                }
                ratingbar.setRating((float) rating);
                txtRating.setText(String.valueOf(rating));
                dialog.show();
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }
}
