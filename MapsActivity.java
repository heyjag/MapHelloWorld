package com.praya.dbds;

import android.content.Context;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

public class MapsActivity extends FragmentActivity implements LocationListener {
    private GoogleMap mMap; // Might be null if Google Play services APK is not available.
    private LocationManager locationManager;
    float minZoom = 9.0f;
    LatLng startPosition, curPosition;
    PolylineOptions lineOptions;
    LatLngBounds screenMapBounds;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        lineOptions = new PolylineOptions();
        lineOptions.width(10);
        lineOptions.color(Color.RED);

        setUpGPS();
        setUpMapIfNeeded();
    }

    @Override
    protected void onResume() {
        super.onResume();
        setUpMapIfNeeded();
    }

    /************* Called after each 3 sec **********/
    @Override
    public void onLocationChanged(Location location)
    {
        if (startPosition == null)
        {
            startPosition = new LatLng(location.getLatitude(), location.getLongitude());
            mMap.moveCamera(CameraUpdateFactory.newLatLng(startPosition));
            mMap.animateCamera(CameraUpdateFactory.zoomTo(9));
            screenMapBounds = mMap.getProjection().getVisibleRegion().latLngBounds;
            System.out.println("1Jaga " + startPosition);
        }

        curPosition = new LatLng(location.getLatitude(), location.getLongitude());
        lineOptions.add(curPosition);
        mMap.addPolyline(lineOptions);
        if (!(screenMapBounds.contains(curPosition)))
        {
            float posDist = findDistance(startPosition, curPosition);
            float mapDist = findDistance(screenMapBounds.northeast, screenMapBounds.southwest);
            System.out.println("2Jaga posDist = " + posDist + " mapDist = " + mapDist);

            if(posDist > mapDist)
            {
                mMap.setOnMapLoadedCallback(new GoogleMap.OnMapLoadedCallback() {
                    @Override
                    public void onMapLoaded() {

                        LatLngBounds.Builder bld = new LatLngBounds.Builder();
                        bld.include(startPosition);
                        bld.include(curPosition);
                        LatLngBounds bounds = bld.build();

                        mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, 30));
                        System.out.println("3Jaga " + mMap.getCameraPosition().zoom + " " + minZoom);
                        /*if (mMap.getCameraPosition().zoom > minZoom) {
                            mMap.animateCamera(CameraUpdateFactory.zoomTo(9));
                            System.out.println("4Jaga " + mMap.getCameraPosition().zoom);
                        }*/
                        screenMapBounds = mMap.getProjection().getVisibleRegion().latLngBounds;
                    }
                });
            }
        }
    }

    public float findDistance(LatLng start, LatLng end)
    {
        Location startLoc = new Location("");
        startLoc.setLatitude(start.latitude);
        startLoc.setLongitude(start.longitude);
        Location endLoc = new Location("");
        endLoc.setLatitude(end.latitude);
        endLoc.setLongitude(end.longitude);

        return(startLoc.distanceTo(endLoc));
    }

    @Override
    public void onProviderDisabled(String provider) {
        /******** Called when User off Gps *********/
        Toast.makeText(getBaseContext(), "Gps turned off ", Toast.LENGTH_LONG).show();
    }

    @Override
    public void onProviderEnabled(String provider) {
        /******** Called when User on Gps  *********/
        Toast.makeText(getBaseContext(), "Gps turned on ", Toast.LENGTH_LONG).show();
        setUpGPS();
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        //
    }

    public void setUpGPS()
    {
        /********** get Gps location service LocationManager object ***********/
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        /*************** CAL METHOD requestLocationUpdates ***************/
        // Parameters :
        //   First(provider)    :  the name of the provider with which to register
        //   Second(minTime)    :  the minimum time interval for notifications,
        //                         in milliseconds. This field is only used as a hint
        //                         to conserve power, and actual time between location
        //                         updates may be greater or lesser than this value.
        //   Third(minDistance) :  the minimum distance interval for notifications, in meters
        //   Fourth(listener)   :  a {#link LocationListener} whose onLocationChanged(Location)
        //                         method will be called for each location update
        locationManager.requestLocationUpdates( LocationManager.GPS_PROVIDER,
                3000,   // 3 sec
                0, this);
        /********* After registration onLocationChanged method  ********/
        /********* called periodically after each 3 sec ***********/
    }

    /**
     * Sets up the map if it is possible to do so (i.e., the Google Play services APK is correctly
     * installed) and the map has not already been instantiated.. This will ensure that we only ever
     * call {@link #setUpMap()} once when {@link #mMap} is not null.
     * <p/>
     * If it isn't installed {@link SupportMapFragment} (and
     * {@link com.google.android.gms.maps.MapView MapView}) will show a prompt for the user to
     * install/update the Google Play services APK on their device.
     * <p/>
     * A user can return to this FragmentActivity after following the prompt and correctly
     * installing/updating/enabling the Google Play services. Since the FragmentActivity may not
     * have been completely destroyed during this process (it is likely that it would only be
     * stopped or paused), {@link #onCreate(Bundle)} may not be called again so we should call this
     * method in {@link #onResume()} to guarantee that it will be called.
     */
    private void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the map.
        if (mMap == null) {
            // Try to obtain the map from the SupportMapFragment.
            mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
                    .getMap();
            // Check if we were successful in obtaining the map.
            if (mMap != null) {
                setUpMap();
            }
        }
    }

    /**
     * This is where we can add markers or lines, add listeners or move the camera. In this case, we
     * just add a marker near Africa.
     * <p/>
     * This should only be called once and when we are sure that {@link #mMap} is not null.
     */
    private void setUpMap()
    {
/*        final LatLng posStart = new LatLng(11.01684, 76.95583);
        LatLng posTemp = new LatLng(11.01684, 76.95583);
        PolylineOptions lineOptions = new PolylineOptions();
        for (int i=0; i<=10; i++)
        {
            posTemp = new LatLng((11.01684 + i*0.1), (76.95583 + i*0.1));
            lineOptions.add(posTemp);
            lineOptions.width(10);
            lineOptions.color(Color.RED);
            mMap.addPolyline(lineOptions);
        }
        final LatLng posFinal = posTemp;

        mMap.setOnMapLoadedCallback(new GoogleMap.OnMapLoadedCallback() {
            @Override
            public void onMapLoaded() {
                LatLngBounds.Builder bld = new LatLngBounds.Builder();
                bld.include(posStart);
                bld.include(posFinal);
                LatLngBounds bounds = bld.build();
                mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, 30));
                System.out.println("Jaga " + mMap.getCameraPosition().zoom);
            }
        });*/
        final LatLng posCoimbatore = new LatLng(11.01684, 76.95583);
        mMap.moveCamera(CameraUpdateFactory.newLatLng(posCoimbatore));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(9));
        screenMapBounds = mMap.getProjection().getVisibleRegion().latLngBounds;
    }
}
