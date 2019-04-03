package com.prashant.neosoftdemo.Activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.prashant.neosoftdemo.Adapter.WeatherAdapter;
import com.prashant.neosoftdemo.Application.MyApplication;
import com.prashant.neosoftdemo.Pojjo.Place;
import com.prashant.neosoftdemo.R;
import com.prashant.neosoftdemo.Utils.CommonUtils;
import com.prashant.neosoftdemo.Utils.PreferenceSettings;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, com.google.android.gms.location.LocationListener
{

    private final static int GPS_CONNECTION_REQUEST = 1123;
    private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 200;
    private static final String TAG = "MainActivity";
    private static final String API_KEY = "0212c12e1e9411ba55d3d57e43c413e0";
    private Location mCurrentLocation, previousLocation;

    // Google client to interact with Google API
    private GoogleApiClient mGoogleApiClient;

    // boolean flag to toggle periodic location updates
    private boolean mRequestingLocationUpdates = false;

    private LocationRequest mLocationRequest;

    // Location updates intervals in sec
    private static int UPDATE_INTERVAL = 1000; // 10 sec
    private static int FATEST_INTERVAL = 500; // 5 sec
    private static int DISPLACEMENT = 1; // 10 meters
    boolean IS_INITIALIZED = false, IS_LOCATION_FETCHED = false, FLAG = false;
    private Double currentLatitude,currentLongitude,prevLatitude,prevLongitude;
    private ProgressBar progressBar;
    ArrayList<Place> placeList;
    private RecyclerView recyclerView;
    private WeatherAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        placeList = new ArrayList<>();

        if (!CommonUtils.checkGps(MainActivity.this)) {
            showGpsDialog();
        }
        else
        {
            if (checkPlayServices()) {

                // Building the GoogleApi client
                buildGoogleApiClient();

                createLocationRequest();
            }
        }
    }

    private void initialize()
    {
        IS_INITIALIZED = true;


//        mRequestingLocationUpdates = true;
//        startLocationUpdates();

        progressBar = (ProgressBar) findViewById(R.id.progressbar);
        recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());

        if(CommonUtils.isNetworkConnected(MainActivity.this)) {
            callApi(currentLatitude, currentLongitude);
        }
        else
        {
            Toast.makeText(MainActivity.this,""+ getResources().getString(R.string.internet_conn)
                    ,Toast.LENGTH_SHORT).show();
        }
    }

    public void showGpsDialog() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(MainActivity.this);
        alertDialog.setTitle("" + getResources().getString(R.string.need_device_location));
        alertDialog.setMessage("" + getResources().getString(R.string.enable_gps));
        alertDialog.setIcon(R.drawable.ic_settings);
        alertDialog.setPositiveButton("" + getResources().getString(R.string.yes), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivityForResult(intent,GPS_CONNECTION_REQUEST);
                dialog.dismiss();
            }
        });
        /*alertDialog.setNegativeButton("" + getResources().getString(R.string.cancel), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                showGpsDialog();
            }
        });*/
        Dialog dialog = alertDialog.create();
        dialog.setCanceledOnTouchOutside(false);
        dialog.setCancelable(false);
        dialog.show();
    }


    private boolean checkPlayServices() {
        int resultCode = GooglePlayServicesUtil
                .isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                GooglePlayServicesUtil.getErrorDialog(resultCode, this,
                        PLAY_SERVICES_RESOLUTION_REQUEST).show();
            } else {
                Toast.makeText(getApplicationContext(),
                        "This device is not supported.", Toast.LENGTH_LONG)
                        .show();
                finish();
            }
            return false;
        }
        return true;
    }

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API).build();
    }


    @SuppressLint("RestrictedApi")
    protected void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(UPDATE_INTERVAL);
        mLocationRequest.setFastestInterval(FATEST_INTERVAL);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
//        mLocationRequest.setSmallestDisplacement(DISPLACEMENT);
    }

    protected void startLocationUpdates() {

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
        {

            return;
        }
        try {
            Log.e("start location", " update");
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.e("on", " start");
        if (mGoogleApiClient != null) {
            mGoogleApiClient.connect();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        Log.e("on", " resume");

        if(mGoogleApiClient != null)
        {
            if(!mGoogleApiClient.isConnected())
            {
                mGoogleApiClient.connect();
            }

            if (mGoogleApiClient.isConnected() && mRequestingLocationUpdates) {
                startLocationUpdates();
            }
        }
        else
        {
            if (checkPlayServices()) {

                // Building the GoogleApi client
                buildGoogleApiClient();

                createLocationRequest();
            }
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.e("on", " stop");
        if (mGoogleApiClient.isConnected())
        {
            if(mRequestingLocationUpdates) {
                stopLocationUpdates();
            }
            mGoogleApiClient.disconnect();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.e("on", " pause");
        if (mGoogleApiClient.isConnected())
        {
            if(mRequestingLocationUpdates) {
                stopLocationUpdates();
            }
        }

    }

    protected void stopLocationUpdates() {
        LocationServices.FusedLocationApi.removeLocationUpdates(
                mGoogleApiClient, this);

    }

    @Override
    public void onConnectionFailed(ConnectionResult result) {
        Log.e("Placelist", "Connection failed: ConnectionResult.getErrorCode() = "
                + result.getErrorCode());
    }

    @Override
    public void onConnected(Bundle arg0) {

        // Once connected with google api, get the location
        //displayLocation();
        Log.e("on", " connected " + mRequestingLocationUpdates);

        if (CommonUtils.checkGps(MainActivity.this))
        {
            displayLocation();
        }
    }

    @Override
    public void onConnectionSuspended(int arg0) {
        mGoogleApiClient.connect();
    }

    @Override
    public void onLocationChanged(Location location) {
        // Assign the new location

        try {
            if (mCurrentLocation != null) {
                previousLocation = mCurrentLocation;
                prevLatitude = mCurrentLocation.getLatitude();
                prevLongitude = mCurrentLocation.getLongitude();

                mCurrentLocation = location;
                Log.e("onLocationChanged1", ": " + location.getLatitude() + " " + location.getLongitude()
                        + " " + location.getAccuracy());


                displayLocation();
            }
            else
            {
                previousLocation = location;
                prevLatitude = location.getLatitude();
                prevLongitude = location.getLongitude();

                mCurrentLocation = location;
                Log.e("onLocationChanged2", ": " + location.getLatitude() + " " + location.getLongitude()
                        + " " + location.getAccuracy());


                displayLocation();

            }
        }
        catch (Exception e)
        {

        }
    }

    public void fetchLocation()
    {
        if(!IS_INITIALIZED) {
            if (IS_LOCATION_FETCHED) {
                displayLocation();
            } else {
                getLocationFromNetwork();
            }
        }
    }

    private void getLocationFromNetwork()
    {
        try
        {
            boolean gps_enabled = false;
            boolean network_enabled = false;

            LocationManager lm = (LocationManager) getApplicationContext()
                    .getSystemService(Context.LOCATION_SERVICE);

            gps_enabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
            network_enabled = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

            Location net_loc = null, gps_loc = null, finalLoc = null;

            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED)
            {

                return;
            }

            if (gps_enabled)
            {
                gps_loc = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                mCurrentLocation = gps_loc;
            }
            if (network_enabled)
            {
                net_loc = lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                mCurrentLocation = net_loc;
            }

            if (gps_loc != null && net_loc != null)
            {

                //smaller the number more accurate result will
                if (gps_loc.getAccuracy() > net_loc.getAccuracy())
                {
                    finalLoc = net_loc;
                    mCurrentLocation = finalLoc;
                }
                else
                {
                    finalLoc = gps_loc;
                    mCurrentLocation = finalLoc;
                }

                // I used this just to get an idea (if both avail, its upto you which you want
                // to take as I've taken location with more accuracy)
                IS_LOCATION_FETCHED = true;
                displayLocation();
            }
            else
            {

                if (gps_loc != null)
                {
                    finalLoc = gps_loc;
                    mCurrentLocation = finalLoc;
                    IS_LOCATION_FETCHED = true;
                    displayLocation();
                }
                else if (net_loc != null)
                {
                    finalLoc = net_loc;
                    mCurrentLocation = finalLoc;
                    IS_LOCATION_FETCHED = true;
                    displayLocation();
                }
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }


    private void displayLocation()
    {


        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED)
        {

            return;
        }

        if(!mRequestingLocationUpdates) {
            mCurrentLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        }

        Log.e(TAG, "Location Accuraccy " + mCurrentLocation.getAccuracy());

        if (mCurrentLocation != null && mCurrentLocation.getAccuracy() < 100)
        {
            currentLatitude = mCurrentLocation.getLatitude();
            currentLongitude = mCurrentLocation.getLongitude();

            if(!IS_INITIALIZED)
            {
                initialize();
            }
            else
            {
                if(CommonUtils.isNetworkConnected(MainActivity.this)) {
                    callApi(currentLatitude, currentLongitude);
                }
                else
                {
                    Toast.makeText(MainActivity.this,""+ getResources().getString(R.string.internet_conn)
                            ,Toast.LENGTH_SHORT).show();
                }

            }

            Log.e(TAG, "displayLocation: " + currentLatitude + " " + currentLongitude + " " + mRequestingLocationUpdates);



        }
        else
        {

            mRequestingLocationUpdates = true;
            startLocationUpdates();

        }

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == GPS_CONNECTION_REQUEST)
        {
            if (!CommonUtils.checkGps(MainActivity.this))
            {
                showGpsDialog();
            }
            else
            {
                if (checkPlayServices()) {

                    // Building the GoogleApi client
                    buildGoogleApiClient();

                    createLocationRequest();
                }
            }
        }
    }

    private void callApi(double lat,double lng)
    {
        String tag_json_obj = "json_obj_req";

        String url = "http://api.openweathermap.org/data/2.5/find?lat="+lat+"&lon="+lng+"&cnt=10&appid="+ API_KEY;

        progressBar.setVisibility(View.VISIBLE);
        Log.e(TAG, "callApi: "+ url);

        JsonObjectRequest jsonObjReq = new JsonObjectRequest(Request.Method.GET,
                url, null,
                new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        Log.e(TAG, response.toString());
                        progressBar.setVisibility(View.GONE);

                        try {
                            JSONArray data = response.getJSONArray("list");
                            if(data != null && data.length() > 0)
                            {
                                placeList = CommonUtils.parseResponse(data);
                                if(placeList.size() > 0)
                                {
                                    mAdapter = new WeatherAdapter(placeList);
                                    recyclerView.setAdapter(mAdapter);
                                }
                                else
                                {
                                    Toast.makeText(MainActivity.this,"Something went wrong",Toast.LENGTH_SHORT).show();
                                }
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "Error: " + error.getMessage());
                // hide the progress dialog
                progressBar.setVisibility(View.GONE);
            }
        });

// Adding request to request queue
        MyApplication.getInstance().addToRequestQueue(jsonObjReq, tag_json_obj);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id){
            case R.id.item1:
                if(CommonUtils.isNetworkConnected(MainActivity.this)) {
                    callApi(currentLatitude, currentLongitude);
                }
                else
                {
                    Toast.makeText(MainActivity.this,""+ getResources().getString(R.string.internet_conn)
                            ,Toast.LENGTH_SHORT).show();
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
