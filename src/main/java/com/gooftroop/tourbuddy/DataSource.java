package com.gooftroop.tourbuddy;

import android.content.ContentValues;
import android.content.Context;

import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;

import static com.gooftroop.tourbuddy.TourBuddySQLiteHelper.COLUMN_LOCATION_ID;
import static com.gooftroop.tourbuddy.TourBuddySQLiteHelper.COLUMN_SCHOOL_ID;
import static com.gooftroop.tourbuddy.TourBuddySQLiteHelper.COLUMN_LOCATION_NAME;
import static com.gooftroop.tourbuddy.TourBuddySQLiteHelper.COLUMN_LOCATION_MARKER_COORDINATES;
import static com.gooftroop.tourbuddy.TourBuddySQLiteHelper.COLUMN_LOCATION_BUILDING_BOUNDS;
import static com.gooftroop.tourbuddy.TourBuddySQLiteHelper.COLUMN_LOCATION_BACKGROUND;
import static com.gooftroop.tourbuddy.TourBuddySQLiteHelper.COLUMN_LOCATION_IMAGES_AND_DESCRIPTIONS;
import static com.gooftroop.tourbuddy.TourBuddySQLiteHelper.COLUMN_LOCATION_VISITED;
import static com.gooftroop.tourbuddy.TourBuddySQLiteHelper.COLUMN_NOTE_BUILDING_ID;
import static com.gooftroop.tourbuddy.TourBuddySQLiteHelper.COLUMN_NOTE_ID;
import static com.gooftroop.tourbuddy.TourBuddySQLiteHelper.COLUMN_NOTE_STRING;
import static com.gooftroop.tourbuddy.TourBuddySQLiteHelper.TABLE_LOCATIONS;
import static com.gooftroop.tourbuddy.TourBuddySQLiteHelper.TABLE_NOTES;


import java.util.ArrayList;
import java.util.List;

/**
 * Created by Austin on 2/19/2015.
 */
public class DataSource {

    // Database fields
    private SQLiteDatabase database;
    private TourBuddySQLiteHelper dbHelper;

    private static final String[] allLocationsColumns = {
            COLUMN_LOCATION_ID, COLUMN_SCHOOL_ID, COLUMN_LOCATION_NAME, COLUMN_LOCATION_MARKER_COORDINATES,
            COLUMN_LOCATION_BUILDING_BOUNDS, COLUMN_LOCATION_BACKGROUND,
            COLUMN_LOCATION_IMAGES_AND_DESCRIPTIONS, COLUMN_LOCATION_VISITED };

    private static final String[] allNotesColumns = {
            COLUMN_NOTE_ID, COLUMN_NOTE_BUILDING_ID, COLUMN_NOTE_STRING};

    public DataSource(Context curContext) {
        dbHelper = new TourBuddySQLiteHelper(curContext);
    }

    //Constructor
    public void open() throws SQLException {
        database = dbHelper.getWritableDatabase();
        dbHelper.onUpgrade(database, 1, 1);
    }
    //Test comment comment
    public void close() {
        dbHelper.close();
    }

    public TourNote createTourNote(int buildingId, String note) {
        long insertId = 0;
        ContentValues values = new ContentValues();
        values.put(COLUMN_NOTE_BUILDING_ID, buildingId);
        values.put(COLUMN_NOTE_STRING, note);

        insertId = database.insert(TABLE_NOTES, null, values);
        Cursor cursor = database.query(TABLE_NOTES, allNotesColumns, COLUMN_NOTE_ID + " = '" + insertId + "'", null, null, null, null);
        if (!cursor.moveToFirst())
        {
            cursor.close();
            return null;
        }
        TourNote tourNote = cursorToTourNote(cursor);
        cursor.close();
        return tourNote;
    }

    public ArrayList<TourNote> getAllNotes()  {
        ArrayList<TourNote> notes = new ArrayList<TourNote>();

        Cursor cursor = database.query(TABLE_NOTES, allNotesColumns, null, null, null, null, null);

        if (!cursor.moveToFirst())
        {
            cursor.close();
            return notes;
        }

        while (!cursor.isAfterLast()) {
            TourNote note = cursorToTourNote(cursor);
            notes.add(note);
            cursor.moveToNext();
        }

        // make sure to close the cursor
        cursor.close();
        return notes;
    }

    public TourNote getTourNoteById(int id) {
        Cursor cursor = database.query(TABLE_NOTES, allNotesColumns, COLUMN_NOTE_ID + " = '" + id +"'", null, null, null, null);
        if(!cursor.moveToFirst())
        {
            cursor.close();
            return null;
        }
        TourNote note = cursorToTourNote(cursor);
        cursor.close();
        return note;
    }

    private TourNote cursorToTourNote(Cursor cursor) {
        int id = cursor.getInt(0);
        int buildingId = cursor.getInt(1);
        String note = cursor.getString(2);

        return new TourNote(id, buildingId, note);
    }

    public CampusLocation createCampusLocation(CampusLocation loc)
    {
        return createCampusLocation(loc.getId(), loc.getSchoolId(), loc.getName(), loc.getMarkerLocation(), loc.getBuildingBoundsList(), loc.getDescription(), loc.getImagesList(), loc.getImageDescriptionList(), loc.getVisited());
    }

    public CampusLocation createCampusLocation(int id, int schoolId, String name, LatLng markerLocation, List<LatLngBounds> buildingBoundsList, String backgroundInfo, List<Integer> imagesList, List<String> imageDescriptionList, boolean visited) {
        long insertId = 0;
        ContentValues values = new ContentValues();
        values.put(COLUMN_LOCATION_ID, id);
        values.put(COLUMN_SCHOOL_ID, schoolId);
        values.put(COLUMN_LOCATION_NAME, name);
        values.put(COLUMN_LOCATION_MARKER_COORDINATES, markerLocation.latitude + "\t" + markerLocation.longitude);

        StringBuilder boundsStr = new StringBuilder();

        if ((buildingBoundsList.size() == 0))
        {
            throw new IllegalArgumentException("Must specify an even number of building bounds (two corners of each rectangle)");
        }

        boundsStr.append(buildingBoundsList.get(0).northeast.latitude + "\t" + buildingBoundsList.get(0).northeast.longitude + "\t");
        boundsStr.append(buildingBoundsList.get(0).southwest.latitude + "\t" + buildingBoundsList.get(0).southwest.longitude);

        for (int i = 1; i<buildingBoundsList.size(); i++)
        {
            boundsStr.append("\t" + buildingBoundsList.get(i).northeast.latitude + "\t" + buildingBoundsList.get(i).northeast.longitude + "\t");
            boundsStr.append(buildingBoundsList.get(i).southwest.latitude + "\t" + buildingBoundsList.get(i).southwest.longitude);

        }
        values.put(COLUMN_LOCATION_BUILDING_BOUNDS, boundsStr.toString());
        values.put(COLUMN_LOCATION_BACKGROUND, backgroundInfo);

        if (imagesList.size() == 0 || imagesList.size() != imageDescriptionList.size())
        {
            throw new IllegalArgumentException("Must contain at least one image and description. In addition images List Size should equal the images description list size");
        }

        StringBuilder imagesAndDescStr = new StringBuilder();

        imagesAndDescStr.append(imagesList.get(0)+"\t"+imageDescriptionList.get(0));

        for (int i = 1; i<imagesList.size(); i++)
        {
            imagesAndDescStr.append("\t"+imagesList.get(i)+"\t"+imageDescriptionList.get(i));
        }
        values.put(COLUMN_LOCATION_IMAGES_AND_DESCRIPTIONS, imagesAndDescStr.toString());

        int visitedInt = 0;

        if (visited)
        {
            visitedInt = 1;
        }

        values.put(COLUMN_LOCATION_VISITED, visitedInt);
        insertId = database.insert(TABLE_LOCATIONS, null, values);
        Cursor cursor = database.query(TABLE_LOCATIONS, allLocationsColumns, COLUMN_LOCATION_ID + " = '" + insertId + "'", null, null, null, null);
        if (!cursor.moveToFirst())
        {
            cursor.close();
            return null;
        }
        CampusLocation loc = cursorToCampusLocation(cursor);
        cursor.close();
        return loc;
    }

    public ArrayList<CampusLocation> getAllLocations()  {
        ArrayList<CampusLocation> campusLocations = new ArrayList<CampusLocation>();

        Cursor cursor = database.query(TABLE_LOCATIONS, allLocationsColumns, null, null, null, null, null);

        if (!cursor.moveToFirst())
        {
            cursor.close();
            return campusLocations;
        }

        while (!cursor.isAfterLast()) {
            CampusLocation GoalType = cursorToCampusLocation(cursor);
            campusLocations.add(GoalType);
            cursor.moveToNext();
        }

        // make sure to close the cursor
        cursor.close();
        return campusLocations;
    }

    public CampusLocation getCampusLocationById(int id) {
        Cursor cursor = database.query(TABLE_LOCATIONS, allLocationsColumns, COLUMN_LOCATION_ID + " = '" + id +"'", null, null, null, null);
        if(!cursor.moveToFirst())
        {
            cursor.close();
            return null;
        }
        CampusLocation loc = cursorToCampusLocation(cursor);
        cursor.close();
        return loc;
    }

    private CampusLocation cursorToCampusLocation(Cursor cursor) {
        int id = cursor.getInt(0);
        int schoolId = cursor.getInt(1);
        String name = cursor.getString(2);
        String[] markerCoordsArr = cursor.getString(3).split("\t");
        double lat = Double.parseDouble(markerCoordsArr[0]);
        double lon = Double.parseDouble(markerCoordsArr[1]);
        LatLng markerLoc = new LatLng(lat, lon);

        String[] boundsArr = cursor.getString(4).split("\t");
        ArrayList<LatLngBounds> boundsList = new ArrayList<LatLngBounds>();
        for (int i = 0; i<boundsArr.length; i=i+4)
        {
            LatLngBounds.Builder bounds = LatLngBounds.builder();
            double lat1 = Double.parseDouble(boundsArr[i]);
            double long1 = Double.parseDouble(boundsArr[i+1]);
            double lat2 = Double.parseDouble(boundsArr[i+2]);
            double long2 = Double.parseDouble(boundsArr[i+3]);

            bounds.include(new LatLng(lat1, long1));
            bounds.include(new LatLng(lat2, long2));
            boundsList.add(bounds.build());
        }
        String background = cursor.getString(5);

        ArrayList<Integer> images = new ArrayList<Integer>();
        ArrayList<String> descriptions = new ArrayList<String>();

        String[] imageDescArr = cursor.getString(6).split("\t");
        for (int i = 0; i<imageDescArr.length; i=i+2)
        {
            images.add(Integer.parseInt(imageDescArr[i]));
            descriptions.add(imageDescArr[i+1]);
        }

        boolean visited = false;

        if (cursor.getInt(7) == 1)
        {
            visited = true;
        }

        return new CampusLocation(id, schoolId, name, markerLoc, boundsList, background, images, descriptions, visited);
    }
}
