package com.example.offlinemap;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.DownloadManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.os.IBinder;
import android.provider.Settings;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import org.mapsforge.core.model.LatLong;
import org.mapsforge.map.android.graphics.AndroidGraphicFactory;
import org.mapsforge.map.android.util.AndroidUtil;
import org.mapsforge.map.android.view.MapView;
import org.mapsforge.map.datastore.MapDataStore;
import org.mapsforge.map.layer.cache.TileCache;
import org.mapsforge.map.layer.overlay.Circle;
import org.mapsforge.map.layer.renderer.TileRendererLayer;
import org.mapsforge.map.reader.MapFile;
import org.mapsforge.map.rendertheme.InternalRenderTheme;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Locale;

import static java.lang.Thread.sleep;


public class MainActivity extends AppCompatActivity {

    // Name of the map file in device storage
    //private static final String MAP_FILE = "berlin.map";
    //private static final String MAP_LOCATION = "sdcard/Android/data/org.mapsforge.samples.android/files/berlin.map";
    //private static final String MAP_URL = "http://download.mapsforge.org/maps/v5/europe/germany/berlin.map";

    private static MapView mapView;
    private GPSService gpsService;
    LocationManager locationManager;
    public static Location currentLocation;
    TileCache tileCache;
    MapDataStore mapDataStore;
    TileRendererLayer tileRendererLayer;
    Circle positionCircle;
    long downloadID;
    String currentMap = "";
    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;


    // Config for max area.
    public int maxWidth = 50000; // 50 km area
    public int maxHeight = 50000; // 50 km area


    private final LocationListener listener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            // A new location update is received.  Do something useful with it.  In this case,
            // we're sending the update to a handler which then updates the UI with the new
            // location.
            Log.d("LOCATION UPDATE", "Lat: "+location.getLatitude() + " Lon: " + location.getLongitude());
            currentLocation = location;
            updateMap();
        }
        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

        }
        @Override
        public void onProviderEnabled(String provider) {

        }
        @Override
        public void onProviderDisabled(String provider) {

        }
    };


    void updatePositionCircle() {
        if(positionCircle != null && mapView.getLayerManager().getLayers().contains(positionCircle)) {
            mapView.getLayerManager().getLayers().remove(positionCircle);
        }
        positionCircle = OfflineMapUtils.createCircle(new LatLong(currentLocation.getLatitude(), currentLocation.getLongitude()));
        mapView.getLayerManager().getLayers().add(positionCircle);
        mapView.getLayerManager().redrawLayers();
    }

    public void updateMap() {
        mapView.setCenter(new LatLong(currentLocation.getLatitude(), currentLocation.getLongitude()));
        getMapFor(currentLocation);
        updatePositionCircle();
    }


    public boolean checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {

                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
                new AlertDialog.Builder(this)
                        .setTitle("Foo")
                        .setMessage("bar")
                        .setPositiveButton("YES!", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                //Prompt the user once explanation has been shown
                                ActivityCompat.requestPermissions(MainActivity.this,
                                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                        MY_PERMISSIONS_REQUEST_LOCATION);
                            }
                        })
                        .create()
                        .show();
            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);
            }
            return false;
        } else {
            return true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted, yay! Do the
                    // location-related task you need to do.
                    if (ContextCompat.checkSelfPermission(this,
                            Manifest.permission.ACCESS_FINE_LOCATION)
                            == PackageManager.PERMISSION_GRANTED) {
                        //Request location updates:
                        // Retrieve a list of location providers that have fine accuracy, no monetary cost, etc
                        Criteria criteria = new Criteria();
                        criteria.setAccuracy(Criteria.ACCURACY_FINE);
                        criteria.setCostAllowed(false);
                        String providerName = locationManager.getBestProvider(criteria, true);

                         // If no suitable provider is found, null is returned.
                        if (providerName == null) {
                            return;
                        }
                        Log.d("LOCATION", "Registering Location listener");
                        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                                1000,          // 1-second interval.
                                10,             // 10 meters.
                                listener);
                    }
                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;
            }
        }
    }
    private void getMapFor(Location loc) {
        if(loc == null) {
            return;
        }
        Geocoder gcd = new Geocoder(this, Locale.getDefault());
        List<Address> addresses = null;
        try {
            addresses = gcd.getFromLocation(currentLocation.getLatitude(), currentLocation.getLongitude(), 1);
        } catch (IOException e) {
            e.printStackTrace();
        }
        Address addr;
        String country;
        String state;
        String city;
        String map_candidate;
        final String new_map;
        if(addresses == null || addresses.size() == 0) {
            // If we can not map location to a state, we just take the broad map
            new_map = "germany.map";
            map_candidate = "http://download.mapsforge.org/maps/v5/europe/" + new_map;
        } else {
            addr = addresses.get(0);
            country = addr.getCountryName().toLowerCase();
            state = addr.getAdminArea().toLowerCase();
            city = addr.getLocality().toLowerCase();
            new_map = OfflineMapUtils.replaceUmlaute(country+"/"+state+".map");
            map_candidate = "http://download.mapsforge.org/maps/v5/europe/" + new_map;
        }
        Log.d("Map downloader", "Candidate: "+map_candidate);
        File f = new File(getExternalFilesDir(null), new_map);
        if(f.exists()) { // Map file already downloaded
            Log.d("Map downloader", "Map already exists in "+getExternalFilesDir(null) + "/" + new_map);
            setMap(new_map);
            return;
        } else {
            DownloadManager downloadmanager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
            Uri uri = Uri.parse(map_candidate);

            DownloadManager.Request request = new DownloadManager.Request(uri);
            request.setTitle(new_map);
            request.setDescription("Downloading");
            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
            request.setVisibleInDownloadsUi(false);
            request.setDestinationUri(Uri.parse("file://" + getExternalFilesDir(null) + "/" + new_map));

            BroadcastReceiver onDownloadComplete = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    //Fetching the download id received with the broadcast
                    long id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);
                    //Checking if the received broadcast is for our enqueued download by matching download id
                    if (downloadID == id) {
                        Log.d("Map downloader", "Download Completed!");
                        setMap(new_map);
                    }
                }
            };

            registerReceiver(onDownloadComplete, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
            downloadID = downloadmanager.enqueue(request);
        }
    }

    void setMap(String mapName) {
        if (!currentMap.equals(mapName)) {
            Log.d("MAP", "Updating map from "+currentMap+ " to "+mapName);
            updateMap(this, mapName);
            currentMap = mapName;
        }
        Log.d("MAP", "Not Updating map from "+currentMap);
    }
    void updateMap(Context c, String mapName) {
        /*
         * We then make some simple adjustments, such as showing a scale bar and zoom controls.
         */
        mapView.getMapScaleBar().setVisible(true);
        mapView.setBuiltInZoomControls(true);

        tileCache = AndroidUtil.createTileCache(c, "mapcache",
                mapView.getModel().displayModel.getTileSize(), 1f,
                mapView.getModel().frameBufferModel.getOverdrawFactor());


        /*
         * Now we need to set up the process of displaying a map. A map can have several layers,
         * stacked on top of each other. A layer can be a map or some visual elements, such as
         * markers. Here we only show a map based on a mapsforge map file. For this we need a
         * TileRendererLayer. A TileRendererLayer needs a TileCache to hold the generated map
         * tiles, a map file from which the tiles are generated and Rendertheme that defines the
         * appearance of the map.
         */
        Log.d("DEBUG", mapName);
        File mapFile = new File(getExternalFilesDir(null), mapName);
        mapDataStore = new MapFile(mapFile);
        tileRendererLayer = new TileRendererLayer(tileCache, mapDataStore,
                mapView.getModel().mapViewPosition, AndroidGraphicFactory.INSTANCE);
        tileRendererLayer.setXmlRenderTheme(InternalRenderTheme.DEFAULT);

        /*
         * On its own a tileRendererLayer does not know where to display the map, so we need to
         * associate it with our mapView.
         */
        mapView.getLayerManager().getLayers().add(tileRendererLayer);
        /*
         * The map also needs to know which area to display and at what zoom level.
         */
        mapView.setCenter(new LatLong(currentLocation.getLatitude(), currentLocation.getLongitude()));
        mapView.setZoomLevel((byte) 14);
        updatePositionCircle();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.game_menu, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.manage_files:
                // Go to map files managerActivityForItemOne.class)
                // startActivity(new Intent("com.example.offlinemap.SelectionListActivity"));
                startActivity(new Intent(this, SelectionListActivity.class));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        checkLocationPermission();

        gpsService = new GPSService(this);
        currentLocation = gpsService.getLocation();
        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        try {
            getMapFor(currentLocation);
        } catch (Exception e) {
            e.printStackTrace();
        }
        super.onCreate(savedInstanceState);

        SelectionsValues.loadFiles(this);
            /*
             * Before you make any calls on the mapsforge library, you need to initialize the
             * AndroidGraphicFactory. Behind the scenes, this initialization process gathers a bit of
             * information on your device, such as the screen resolution, that allows mapsforge to
             * automatically adapt the rendering for the device.
             * If you forget this step, your app will crash. You can place this code, like in the
             * Samples app, in the Android Application class. This ensures it is created before any
             * specific activity. But it can also be created in the onCreate() method in your activity.
             */
            AndroidGraphicFactory.createInstance(getApplication());
        /*
             * A MapView is an Android View (or ViewGroup) that displays a mapsforge map. You can have
             * multiple MapViews in your app or even a single Activity. Have a look at the mapviewer.xml
             * on how to create a MapView using the Android XML Layout definitions. Here we create a
             * MapView on the fly and make the content view of the activity the MapView. This means
             * that no other elements make up the content of this activity.
             */
            mapView = new MapView(this);
            setContentView(mapView);

            mapView.getMapScaleBar().setVisible(true);
            mapView.setBuiltInZoomControls(true);

            /*
            try {
                //setMap(MAP_FILE);
                updateMap(this, MAP_FILE);

            } catch (Exception e) {
                e.printStackTrace();
            }*/
        }

        @Override
        protected void onDestroy() {
            /*
             * Whenever your activity exits, some cleanup operations have to be performed lest your app
             * runs out of memory.
             */
            mapView.destroyAll();
            AndroidGraphicFactory.clearResourceMemoryCache();
            super.onDestroy();
        }

    class GPSService extends Service implements LocationListener {

        private final Context mContext;

        // Flag for GPS status
        boolean isGPSEnabled = false;

        // Flag for network status
        boolean isNetworkEnabled = false;

        // Flag for GPS status
        boolean canGetLocation = false;

        Location location; // Location
        double latitude; // Latitude
        double longitude; // Longitude

        // The minimum distance to change Updates in meters
        private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 10; // 10 meters

        // The minimum time between updates in milliseconds
        private static final long MIN_TIME_BW_UPDATES = 1000; // 1 Secoond

        // Declaring a Location Manager
        protected LocationManager locationManager;

        public GPSService(Context context) {
            this.mContext = context;
            getLocation();
        }

        @SuppressLint("MissingPermission")
        public Location getLocation() {
            try {
                locationManager = (LocationManager) mContext
                        .getSystemService(LOCATION_SERVICE);

                // Getting GPS status
                isGPSEnabled = locationManager
                        .isProviderEnabled(LocationManager.GPS_PROVIDER);

                // Getting network status
                isNetworkEnabled = locationManager
                        .isProviderEnabled(LocationManager.NETWORK_PROVIDER);

                if (!isGPSEnabled && !isNetworkEnabled) {
                    // No network provider is enabled
                } else {
                    this.canGetLocation = true;
                    // If GPS enabled, get latitude/longitude using GPS Services
                    if (isGPSEnabled) {
                        if (location == null) {
                            locationManager.requestLocationUpdates(
                                    LocationManager.GPS_PROVIDER,
                                    MIN_TIME_BW_UPDATES,
                                    MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
                            Log.d("GPS Enabled", "GPS Enabled");
                            if (locationManager != null) {
                                location = locationManager
                                        .getLastKnownLocation(LocationManager.GPS_PROVIDER);
                                if (location != null) {
                                    latitude = location.getLatitude();
                                    longitude = location.getLongitude();
                                }
                            }
                        }
                    }
                }
            }
            catch (Exception e) {
                e.printStackTrace();
            }

            return location;
        }


        /**
         * Stop using GPS listener
         * Calling this function will stop using GPS in your app.
         * */
        public void stopUsingGPS(){
            if(locationManager != null){
                locationManager.removeUpdates(GPSService.this);
            }
        }


        /**
         * Function to get latitude
         * */
        public double getLatitude(){
            if(location != null){
                latitude = location.getLatitude();
            }

            // return latitude
            return latitude;
        }


        /**
         * Function to get longitude
         * */
        public double getLongitude(){
            if(location != null){
                longitude = location.getLongitude();
            }

            // return longitude
            return longitude;
        }

        /**
         * Function to check GPS/Wi-Fi enabled
         * @return boolean
         * */
        public boolean canGetLocation() {
            return this.canGetLocation;
        }


        /**
         * Function to show settings alert dialog.
         * On pressing the Settings button it will launch Settings Options.
         * */
        public void showSettingsAlert(){
            AlertDialog.Builder alertDialog = new AlertDialog.Builder(mContext);

            // Setting Dialog Title
            alertDialog.setTitle("GPS is settings");

            // Setting Dialog Message
            alertDialog.setMessage("GPS is not enabled. Do you want to go to settings menu?");

            // On pressing the Settings button.
            alertDialog.setPositiveButton("Settings", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog,int which) {
                    Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    mContext.startActivity(intent);
                }
            });

            // On pressing the cancel button
            alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            });

            // Showing Alert Message
            alertDialog.show();
        }


        @Override
        public void onLocationChanged(Location location) {
            this.location = location;
            this.latitude = location.getLatitude();
            this.longitude = location.getLongitude();
            currentLocation = location;
            updateMap();
        }


        @Override
        public void onProviderDisabled(String provider) {
        }


        @Override
        public void onProviderEnabled(String provider) {
        }


        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
        }



        @Nullable
        @Override
        public IBinder onBind(Intent intent) {
            return null;
        }
    }

    }



