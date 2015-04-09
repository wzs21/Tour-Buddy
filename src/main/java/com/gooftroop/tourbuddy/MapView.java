package com.gooftroop.tourbuddy;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceGroup;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

public class MapView extends FragmentActivity implements GoogleMap.OnMarkerClickListener, GoogleMap.OnMapClickListener {

    private GoogleMap mMap;

    private Activity curActivity = this;

    private boolean SHOW_BUILDING_BOUNDS = true;

    private ProgressBar progressBarDirectionsLoading;

    private TextView textViewProgressText;

    /**
     * Maintains a Master List of All Campus location and whether or not they have been visited
     */
    private HashMap<CampusLocation, Boolean> locationToVisited = new HashMap<CampusLocation, Boolean>();

    private String[] mDrawerItemNames;
    private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;

    private HashMap<LatLng, CampusLocation> markerLatLngToLocation = new HashMap<LatLng, CampusLocation>();

    DetailPageAdapter pageAdapter;

    private int curLocation = 0;

    private int counter = 0;

    private CampusLocation previous = null;
    //Holds two campus locations
    private CampusLocation[] directionHolder = new CampusLocation[]{null, null};
    //current polyline direction holder
    private Polyline polyline;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity_images_layout);

        setVariables();

        //Initialize to Sweeney for App Demo
        setPageViewer(3);
        mDrawerItemNames = getResources().getStringArray(R.array.nav_drawer_items);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerList = (ListView) findViewById(R.id.left_drawer);

        // Set the adapter for the list view
        mDrawerList.setAdapter(new ArrayAdapter<String>(this,
                R.layout.drawer_list_item, mDrawerItemNames));

        // Set the list's click listener
        mDrawerList.setOnItemClickListener(new DrawerItemClickListener());

        DataSource db = new DataSource(curActivity);
        db.open();

        ArrayList<CampusLocation> dbLocations = db.getAllLocations();
        for (int i = 0; i<dbLocations.size(); i++)
        {
            locationToVisited.put(dbLocations.get(i), dbLocations.get(i).getVisited());
            markerLatLngToLocation.put(dbLocations.get(i).getMarkerLocation(), dbLocations.get(i));
        }

        db.close();
        setUpMapIfNeeded();
        setupLocationListener();
        new UpdateDatabase(curActivity).execute();

        //new VisitLocation(curActivity, dbLocations.get(0)).execute();
    }

    private void setVariables()
    {
        progressBarDirectionsLoading = (ProgressBar) findViewById(R.id.progressBarDirectionsLoading);
        textViewProgressText = (TextView) findViewById(R.id.textViewProgressText);
    }

    /**
     * Returns the most likely campus location
     * @param location
     * @return
     */
    public CampusLocation getCampusLocationFromGPSLocation(Location location)
    {
        LatLng pinPoint = new LatLng(location.getLatitude(), location.getLongitude());

        for (CampusLocation campusLoc : locationToVisited.keySet())
        {
            List<LatLngBounds> bounds = campusLoc.getBuildingBoundsList();
            for (int i = 0; i<bounds.size(); i++)
            {
                if (bounds.get(i).contains(pinPoint))
                {
                    return campusLoc;
                }
            }
        }
        return null;
    }

    public class VisitLocation extends AsyncTask<Void, Void, HttpResponse>
    {
        private Activity curActivity;
        private CampusLocation location;

        public VisitLocation(Activity curActivity, CampusLocation location)
        {
            this.curActivity = curActivity;
            this.location = location;
        }

        @Override
        protected HttpResponse doInBackground(Void... params) {
            try
            {
                return HttpClientHelper.visitLocation(location, curActivity);
            }
            catch (Exception e)
            {
                System.out.println(e.getMessage());
                return null;
            }
        }

        @Override
        protected void onPostExecute(HttpResponse result)
        {
            if (result == null)
            {
                Toast.makeText(curActivity, "Attempted to Update Server!", Toast.LENGTH_LONG).show();
            }

            if (result.getStatusLine().getStatusCode() == 200)
            {
                Toast.makeText(curActivity, "Updated Server!", Toast.LENGTH_LONG).show();
            }
        }
    }



    public class UpdateDatabase extends AsyncTask<Void, Void, JSONArray>
    {
        private Activity curActivity;

        public UpdateDatabase(Activity curActivity)
        {
            this.curActivity = curActivity;
        }

        @Override
        protected JSONArray doInBackground(Void... params) {
            try
            {
                return HttpClientHelper.updateDatabase(curActivity);
            }
            catch (Exception e)
            {
                System.out.println(e.getMessage());
                return null;
            }
        }

        @Override
        protected void onPostExecute(JSONArray result)
        {
//            if (result == null)
//            {
//                Toast.makeText(curActivity, "Updating Database", Toast.LENGTH_LONG).show();
//            }

//            if (result.getStatusLine().getStatusCode() == 200)
//            {
//                Toast.makeText(curActivity, "Updating Database 200", Toast.LENGTH_LONG).show();
//            }
        }
    }


    //drawer's item click listener
    private class DrawerItemClickListener implements ListView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView parent, View view, int position, long id) {
            if (position == 0)
            {
                centerMapOnMyLocation();
            }
            else if (position == 1)
            {
                displayCreateNoteDialog();
            }
            else if (position == 2)
            {
                Intent newIntent = new Intent(MapView.this, NoteListActivity.class);
                startActivity(newIntent);
            }
            else
            {
                Toast.makeText(curActivity, mDrawerItemNames[position], Toast.LENGTH_LONG).show();
            }

            mDrawerList.setItemChecked(position, true);
            mDrawerLayout.closeDrawer(mDrawerList);
            mDrawerList.setItemChecked(position, false);

        }
    }

    private void displayCreateNoteDialog()
    {
        AlertDialog.Builder alert = new AlertDialog.Builder(curActivity);

        final CampusLocation location;

        if(mMap.getMyLocation() != null)
        {
            location = getCampusLocationFromGPSLocation(mMap.getMyLocation());
        }
        else
        {
            location = null;
        }
        alert.setTitle("Create Note");
        if (location != null)
        {
            alert.setMessage("My note on " + location.getName() + ":");
        }
        else
        {
            alert.setMessage("My note on Iowa State's Campus");
        }

        // Set an EditText view to get user input
        final EditText input = new EditText(curActivity);
        alert.setView(input);

        alert.setPositiveButton("Save Note", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                String value = input.getText().toString();
                DataSource db = null;
                TourNote tourNote = null;

                try
                {
                    db = new DataSource(curActivity);
                    db.open();

                    int buildingId = 0;

                    if(mMap.getMyLocation() != null)
                    {
                        if (location != null)
                        {
                            buildingId = location.getId();
                        }
                    }

                    tourNote = db.createTourNote(buildingId, value);
                }
                catch(Exception e)
                {
                    System.out.println(e.getMessage());
                }
                finally
                {
                    if (db != null)
                    {
                        db.close();
                    }
                }


                Toast.makeText(curActivity, "Note Saved!", Toast.LENGTH_LONG).show();
            }
        });

        alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                // Canceled.
            }
        });

        alert.show();
    }

    private void setPageViewer(int locationId)
    {
        ViewPager pager = (ViewPager) findViewById(R.id.viewpager);

        List<Fragment> fragments = getFragments(locationId);

        if (pageAdapter != null) {
            pageAdapter.clearAll();
        }
        pageAdapter = new DetailPageAdapter(getSupportFragmentManager(), fragments);
        pager.setAdapter(pageAdapter);

        //Set the class used for changing the animation for sliding images on the bottom
        pager.setPageTransformer(true, new ZoomOutPageTransformer());
    }

    private void setupLocationListener()
    {
        // Acquire a reference to the system Location Manager
        LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

        // Define a listener that responds to location updates
        LocationListener locationListener = new LocationListener() {
            public void onLocationChanged(Location location) {
                try
                {
                    CampusLocation current = getCampusLocationFromGPSLocation(location);

                    //If previous or current is null, set counter to 0
                    if (previous == null || current == null)
                    {
                        previous = current;
                        counter = 0;
                        return;
                    }

                    counter++;

                    if (counter == 3)
                    {
                        Toast.makeText(curActivity, "Welcome to " + current.getName() + "!", Toast.LENGTH_LONG).show();
                        setPageViewer(current.getId());
                    }
                }
                catch (Exception e)
                {

                }
            }

            public void onStatusChanged(String provider, int status, Bundle extras) {}

            public void onProviderEnabled(String provider) {}

            public void onProviderDisabled(String provider) {}
        };

        if (locationManager != null)
        {
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1000, 0, locationListener);
        }
    }

    private void centerMapOnMyLocation()
    {
        Location location = mMap.getMyLocation();

        if (location != null) {
            LatLng myLocation = new LatLng(location.getLatitude(),location.getLongitude());
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(myLocation, 18.0f));
        }

        CampusLocation nearest = getCampusLocationFromGPSLocation(location);
        if(nearest != null)
        {
            setPageViewer(nearest.getId());
        }
    }

    private List<Fragment> getFragments(int locationId) {
        List<Fragment> fList = new ArrayList<Fragment>();

        DataSource db = new DataSource(curActivity);
        db.open();
        CampusLocation loc = db.getCampusLocationById(locationId);
        db.close();

        for (int i = 0; i<loc.getImagesList().size(); i++)
        {
            fList.add(LocationImagesFragment.newInstance(loc.getImagesList().get(i), loc.getImageDescriptionList().get(i)));
        }
        return fList;
    }

    @Override
    protected void onResume() {
        super.onResume();
        setUpMapIfNeeded();
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
                mMap.moveCamera( CameraUpdateFactory.newLatLngZoom(new LatLng(42.0255, -93.6465), 15.5f) );
                mMap.setOnMapClickListener(this);
                mMap.setOnMarkerClickListener(this);
                mMap.setMyLocationEnabled(true);
            }
        }
    }

    @Override
    public boolean onMarkerClick(Marker marker)
    {
        CampusLocation loc = markerLatLngToLocation.get(marker.getPosition());

        if(loc == null)
        {
            return false;
        }
        addToDirectionHolder(loc);
        //Toast.makeText(curActivity, loc.getName() + " was clicked.\nCoordinates:(" + loc.getMarkerLocation().latitude + "," + loc.getMarkerLocation().longitude + ")", Toast.LENGTH_LONG).show();
        //Toast.makeText(curActivity, "Location holder one: " + directionHolder[0].getName(), Toast.LENGTH_LONG).show();

        //draw directions only if two markers have been selected
        if(directionHolder[0] != null && directionHolder[1] != null)
            drawDirections();

        setPageViewer(loc.getId());


        //Toast.makeText(curActivity, marker.getTitle() + " was clicked.\nCoordinates:(" + marker.getPosition().latitude + "," + marker.getPosition().longitude + ")", Toast.LENGTH_LONG).show();
        return false;
    }
    //create a new readtask and draw the directions on the map
    private void drawDirections(){
        String url = getMapsApiDirectionsUrl();
        new ReadTask().execute(url);
    }

    private String getMapsApiDirectionsUrl(){
        String origin = "origin=" + directionHolder[0].getMarkerLocation().latitude + "," +
               directionHolder[0].getMarkerLocation().longitude;
        String dest = "destination=" + directionHolder[1].getMarkerLocation().latitude + "," +
                directionHolder[1].getMarkerLocation().longitude;
        String sensor = "sensor=false";
        String params = origin + "&" + dest + "&" + sensor;
        String output = "json";
       // String key = "key=AIzaSyB-J8aIitC_4FDb2ejV0iQCk6PYhFmbl8E";
        String url = "https://maps.googleapis.com/maps/api/directions/"
                + output + "?" + params + "&" +"mode=walking";
        return url;
    }


    private class ReadTask extends AsyncTask<String, Void, String> {

        @Override
        protected void onPreExecute()
        {
            progressBarDirectionsLoading.setVisibility(View.VISIBLE);
            textViewProgressText.setVisibility(View.VISIBLE);
        }

        @Override
        protected String doInBackground(String... url) {
            String data = "";
            try {
                HttpClientHelper http = new HttpClientHelper();
                data = http.readUrl(url[0]);
            } catch (Exception e) {
                Log.d("Background Task", e.toString());
            }
            return data;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            new ParserTask().execute(result);
        }
    }

    private class ParserTask extends AsyncTask<String, Integer, List<List<HashMap<String, String>>>> {

        @Override
        protected List<List<HashMap<String, String>>> doInBackground(String... jsonData) {

            JSONObject jObject;
            List<List<HashMap<String, String>>> routes = null;

            try {
                jObject = new JSONObject(jsonData[0]);
                PathJSONParser parser = new PathJSONParser();
                routes = parser.parse(jObject);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return routes;
        }

        @Override
        protected void onPostExecute(List<List<HashMap<String, String>>> routes) {
            ArrayList<LatLng> points = null;
            PolylineOptions polyLineOptions = null;

            // traversing through routes
            for (int i = 0; i < routes.size(); i++) {
                points = new ArrayList<LatLng>();
                polyLineOptions = new PolylineOptions();
                List<HashMap<String, String>> path = routes.get(i);

                for (int j = 0; j < path.size(); j++) {
                    HashMap<String, String> point = path.get(j);

                    double lat = Double.parseDouble(point.get("lat"));
                    double lng = Double.parseDouble(point.get("lng"));
                    LatLng position = new LatLng(lat, lng);

                    points.add(position);
                }

                polyLineOptions.addAll(points);
                polyLineOptions.width(5);
                polyLineOptions.color(Color.BLUE);
            }
            if(polyline != null)
                polyline.remove();

            polyline = mMap.addPolyline(polyLineOptions);

            progressBarDirectionsLoading.setVisibility(View.GONE);
            textViewProgressText.setVisibility(View.GONE);
        }
    }

    @Override
    public void onMapClick(LatLng latLng) {
        //Toast.makeText(curActivity, "(" + latLng.latitude + "," + latLng.longitude + ")", Toast.LENGTH_LONG).show();
    }

    /**
     * Private helper method that adds a new location to the array and takes out an old one
     * @param loc
     */
    private void addToDirectionHolder(CampusLocation loc){
        if(directionHolder[0] == null)
        {
            directionHolder[0] = loc;
        }
        else if(directionHolder[1] == null)
        {
            directionHolder[1] = loc;
        }
        else
        {
            directionHolder[0] = directionHolder[1];
            directionHolder[1] = loc;
        }
    }


    /**
     * This is where we can add markers or lines, add listeners or move the camera. In this case, we
     * just add a marker near Africa.
     * <p/>
     * This should only be called once and when we are sure that {@link #mMap} is not null.
     */
    private void setUpMap() {
        mMap.addMarker(new MarkerOptions().position(new LatLng(42.02541, -93.646072)).title("Campanile"));
        mMap.addMarker(new MarkerOptions().position(new LatLng(42.024220,-93.651840)).title("Helser Hall"));
        mMap.addMarker(new MarkerOptions().position(new LatLng(42.023917,-93.650437)).title("Friley Hall"));
        mMap.addMarker(new MarkerOptions().position(new LatLng(42.026163, -93.648340)).title("Beardshear Hall"));
        mMap.addMarker(new MarkerOptions().position(new LatLng(42.025429, -93.644472)).title("Gerdin Business Building"));
        mMap.addMarker(new MarkerOptions().position(new LatLng(42.026183, -93.644851)).title("Curtiss Hall"));
        mMap.addMarker(new MarkerOptions().position(new LatLng(42.026593,-93.644199)).title("Ross Hall"));
        mMap.addMarker(new MarkerOptions().position(new LatLng(42.026880,-93.642579)).title("Food Sciences Building"));
        mMap.addMarker(new MarkerOptions().position(new LatLng(42.027195, -93.644569)).title("Jischke Honors Building"));
        mMap.addMarker(new MarkerOptions().position(new LatLng(42.026127, -93.643395)).title("East Hall"));
        
        mMap.addMarker(new MarkerOptions().position(new LatLng(42.023616, -93.645924)).title("Memorial Union"));
        mMap.addMarker(new MarkerOptions().position(new LatLng(42.027840,-93.644100)).title("Troxel Hall"));
        mMap.addMarker(new MarkerOptions().position(new LatLng(42.028486,-93.644637)).title("Bessey Hall"));
        mMap.addMarker(new MarkerOptions().position(new LatLng(42.028374, -93.645634)).title("Palmer Building"));
        mMap.addMarker(new MarkerOptions().position(new LatLng(42.028598, -93.646525)).title("MacKay Hall"));
        mMap.addMarker(new MarkerOptions().position(new LatLng(42.0285454, -93.647469)).title("LeBaron Hall"));
        mMap.addMarker(new MarkerOptions().position(new LatLng(42.028103, -93.647554)).title("Human Nutritional Sciences Building"));
        mMap.addMarker(new MarkerOptions().position(new LatLng(42.029602,-93.643874)).title("Kildee Hall/Meats Laboratory"));
        mMap.addMarker(new MarkerOptions().position(new LatLng(42.029195,-93.646342)).title("Science Hall"));
        mMap.addMarker(new MarkerOptions().position(new LatLng(42.029390, -93.647356)).title("Physics Hall"));
        mMap.addMarker(new MarkerOptions().position(new LatLng(42.029426, -93.648654)).title("Gilman Hall"));
        mMap.addMarker(new MarkerOptions().position(new LatLng(42.029586,-93.645634)).title("Lagomarcino Hall"));
        mMap.addMarker(new MarkerOptions().position(new LatLng(42.031056,-93.649700)).title("Molecular Biology Building"));
       
        mMap.addMarker(new MarkerOptions().position(new LatLng(42.030893, -93.648628)).title("Metals Development Building"));
        mMap.addMarker(new MarkerOptions().position(new LatLng(42.030140, -93.649722)).title("Hach Hall"));
        mMap.addMarker(new MarkerOptions().position(new LatLng(42.030179,-93.648665)).title("Spedding Hall"));
        mMap.addMarker(new MarkerOptions().position(new LatLng(42.029550,-93.652694)).title("Town Engineering Building"));
        mMap.addMarker(new MarkerOptions().position(new LatLng(42.029598, -93.650950)).title("Armory"));
        mMap.addMarker(new MarkerOptions().position(new LatLng(42.028589, -93.653134)).title("College of Design"));
        mMap.addMarker(new MarkerOptions().position(new LatLng(42.027448,-93.651533)).title("Nuclear Engineering Laboratory"));

        Set<CampusLocation> locationList = locationToVisited.keySet();

        for (CampusLocation loc : locationList)
        {
            mMap.addMarker(new MarkerOptions().position(loc.getMarkerLocation()).title(loc.getName()));

            if(SHOW_BUILDING_BOUNDS)
            {
                List<LatLngBounds> bnds = loc.getBuildingBoundsList();

                for (LatLngBounds bnd : bnds)
                {
                    //mMap.addPolygon()
                    Polyline line = mMap.addPolyline(new PolylineOptions()
                            .add(bnd.northeast)
                            .add(new LatLng(bnd.southwest.latitude, bnd.northeast.longitude))
                            .add(bnd.southwest)
                            .add(new LatLng(bnd.northeast.latitude, bnd.southwest.longitude))
                            .add(bnd.northeast)
                            .width(2)
                            .color(Color.RED));
                }
            }
        }
    }

    class DetailPageAdapter extends FragmentPagerAdapter {

        private List<Fragment> fragments;

        private FragmentManager fragmentManager;

        public DetailPageAdapter (FragmentManager fm, List<Fragment> fragments) {
            super(fm);
            this.fragmentManager = fm;
            this.fragments = fragments;
        }

        public void clearAll() //Clear all page
        {
            for(int i=0; i<fragments.size(); i++)
                this.fragmentManager.beginTransaction().remove(fragments.get(i)).commit();
            fragments.clear();
        }

        @Override
        public Fragment getItem(int i) {
            return this.fragments.get(i);
        }

        @Override
        public int getCount() {
            return this.fragments.size();
        }
    }

}

