package com.gooftroop.tourbuddy;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.widget.TextView;
import android.widget.Toast;

import static com.gooftroop.tourbuddy.utils.Constants.SHARED_PREFS_CURRENT_DATABASE_VERSION;

import com.gooftroop.tourbuddy.utils.DeviceUtils;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;


/**
 * Created by Austin on 3/31/2015.
 */
public class SplashScreen extends Activity {

    private Activity curActivity = this;

    private TextView textViewSplashScreenAction;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash_screen_layout);
        setVariables();
        if (DeviceUtils.isConnectedToTheInternet(curActivity))
        {
            new CheckForUpdate().execute();
        }
        else
        {
            displayNoConnectionAlertDialog();
        }
    }

    private void setVariables()
    {
        textViewSplashScreenAction = (TextView) findViewById(R.id.textViewSplashScreenAction);
    }

    private void displayNoConnectionAlertDialog()
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(curActivity);
        builder.setTitle("No Internet Connection");
        builder.setMessage("TourBuddy needs to connect to the internet.");

        builder.setPositiveButton("Retry", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                new CheckForUpdate().execute();
                dialog.dismiss();
            }
        });

        builder.show();
    }

    public class CheckForUpdate extends AsyncTask<Void, Void, Integer>
    {

        @Override
        protected void onPreExecute()
        {
            textViewSplashScreenAction.setText("Checking For TourBuddy Update");
            if (!DeviceUtils.isConnectedToTheInternet(curActivity))
            {
                displayNoConnectionAlertDialog();
                this.cancel(true);
            }
        }

        @Override
        protected Integer doInBackground(Void... params)
        {
            try
            {
                return HttpClientHelper.getDatabaseVersion(curActivity);
            }
            catch (Exception e)
            {
                return 0;
            }
        }

        @Override
        protected void onPostExecute(Integer result)
        {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(curActivity);
            int deviceVersion = prefs.getInt(SHARED_PREFS_CURRENT_DATABASE_VERSION, 0);

            if (result > deviceVersion)
            {
                new UpdateDatabase(result).execute();
                //We need to update the application's database
            }
            else
            {
                startMainApplication();
            }
        }
    }

    public class UpdateDatabase extends AsyncTask<Void, Void, Vector<CampusLocation>>
    {

        private int newDatabaseVersion;

        private String currentImage;
        private String currentBuilding;

        public UpdateDatabase(int newDatabaseVersion)
        {
            this.newDatabaseVersion = newDatabaseVersion;
        }

        @Override
        protected void onPreExecute()
        {
            textViewSplashScreenAction.setText("Updating TourBuddy Database");
        }

        @Override
        protected void onProgressUpdate(Void... progress) {
            textViewSplashScreenAction.setText("Getting images for " + currentBuilding + ": " + currentImage);
        }

        @Override
        protected Vector<CampusLocation> doInBackground(Void... params)
        {
            JSONArray array = HttpClientHelper.updateDatabase(curActivity);

            Vector<CampusLocation> locations = new Vector<CampusLocation>();

            for (int i = 0; i<array.length(); i++)
            {
                try
                {
                    CampusLocation loc = campusLocationFromJSONObject(array.getJSONObject(i));
                    locations.add(loc);
                }
                catch (Exception e)
                {

                }
            }

            //Next need to traverse through Array of Objects... and Download the images.

//            ArrayList<Bitmap> bitmaps = new ArrayList<>();
//            for (int i = 0; i<links.length; i++)
//            {
//                Bitmap bitmap = HttpClientHelper.getBitmapFromServer(links[i]);
//                bitmaps.add(bitmap);
//            }

            return locations;
        }

        @Override
        protected void onPostExecute(Vector<CampusLocation> result)
        {
            Toast.makeText(curActivity, "Would've updated database " + newDatabaseVersion, Toast.LENGTH_LONG ).show();
            //updateDatabase(result, newDatabaseVersion);
            startMainApplication();
        }

        private CampusLocation campusLocationFromJSONObject(JSONObject json)
        {
            try
            {
                int id = Integer.parseInt(json.getString("id"));
                int schoolId = Integer.parseInt(json.getString("school_id"));
                String name = json.getString("name");

                JSONObject markerObj = new JSONObject(json.getString("marker_locations"));
                LatLng marker = new LatLng(markerObj.getDouble("Lat"), markerObj.getDouble("Lng"));

                JSONArray boundaries = new JSONArray(json.getString("boundary"));
                if (boundaries.length() == 0 || (boundaries.length() % 2) == 1)
                {
                    return null;
                }

                ArrayList<LatLngBounds> bounds = new ArrayList<LatLngBounds>();

                //Start Here.
                for (int i = 0; i<boundaries.length(); i=i+2) {
                    LatLngBounds.Builder builder = new LatLngBounds.Builder();
                    JSONObject pointOne = boundaries.getJSONObject(i);
                    double pointOneLat = pointOne.getDouble("Lat");
                    double pointOneLng = pointOne.getDouble("Lng");
                    builder.include(new LatLng(pointOneLat, pointOneLng));

                    JSONObject pointTwo = boundaries.getJSONObject(i + 1);
                    double pointTwoLat = pointTwo.getDouble("Lat");
                    double pointTwoLng = pointTwo.getDouble("Lng");
                    builder.include(new LatLng(pointTwoLat, pointTwoLng));
                    bounds.add(builder.build());
                }


                String description = json.getString("description");
                String imagesStr = json.getString("image_location");
                String[] imageUrls = imagesStr.split(";");

                ArrayList<Bitmap> images = new ArrayList<Bitmap>(imageUrls.length);

                currentBuilding = name;
                for (int i = 0; i<imageUrls.length; i++)
                {
                    currentImage = imageUrls[i];
                    publishProgress();
                    Bitmap bitmap = HttpClientHelper.getBitmapFromServer(imageUrls[i]);
                    images.add(bitmap);
                }

                String imageDescStr = json.getString("image_descriptions");
                String[] imageDescriptions = imageDescStr.split(";");

                //http://stackoverflow.com/questions/4830711/how-to-convert-a-image-into-base64-string

//                new CampusLocation(id, schoolId, name, marker, bounds, description,
//                        //images list,
//                        imageDescList,
//                        false);
            }
            catch (Exception e)
            {
                return null;
            }



            return null;
        }
    }

    private void updateDatabase(List<CampusLocation> locations, int newDatabaseVersion)
    {
        DataSource db = new DataSource(curActivity);
        try
        {
            db.open();
            for (int i = 0; i<locations.size(); i++)
            {
                CampusLocation webVersion = locations.get(i);
                CampusLocation dbVersion = db.getCampusLocationById(webVersion.getId());

                //If no such location exists in the database, add it to the database
                if (dbVersion == null)
                {
                    db.createCampusLocation(webVersion);
                }
                else
                {
                    //Maintain whether or not this building has been visited.
                    if (dbVersion.getVisited())
                    {
                        webVersion.setVisited(dbVersion.getVisited());
                    }
                }
            }
        }
        catch (Exception e)
        {

        }
        finally
        {
            db.close();
        }

        //Set the new database version
        SharedPreferences.Editor prefs = PreferenceManager.getDefaultSharedPreferences(curActivity).edit();
        prefs.putInt(SHARED_PREFS_CURRENT_DATABASE_VERSION, newDatabaseVersion);
        prefs.apply();
    }

    private void startMainApplication()
    {
        Intent intent = new Intent(SplashScreen.this, MapView.class);
        curActivity.startActivity(intent);
        curActivity.finish();
    }
}
