package com.gooftroop.tourbuddy;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;

import java.util.ArrayList;
import java.util.List;

public class TourBuddySQLiteHelper extends SQLiteOpenHelper {

    public static final String TABLE_LOCATIONS = "locations";
    public static final String COLUMN_LOCATION_ID = "_id";
    public static final String COLUMN_SCHOOL_ID = "schoolId";
    public static final String COLUMN_LOCATION_NAME = "name";
    public static final String COLUMN_LOCATION_MARKER_COORDINATES = "markerCoordinates";
    public static final String COLUMN_LOCATION_BUILDING_BOUNDS = "buildingBounds";
    public static final String COLUMN_LOCATION_BACKGROUND = "background";
    public static final String COLUMN_LOCATION_IMAGES_AND_DESCRIPTIONS = "imagesDescriptions";
    public static final String COLUMN_LOCATION_VISITED = "visited";

    public static final String TABLE_NOTES = "notes";
    public static final String COLUMN_NOTE_ID = "_id";
    public static final String COLUMN_NOTE_BUILDING_ID = "building_id";
    public static final String COLUMN_NOTE_STRING = "notesStr";


    private static final String DATABASE_NAME = "tourbuddy.db";
    private static final int DATABASE_VERSION = 1;

    private static final String[] allLocationsColumns = {
            COLUMN_LOCATION_ID, COLUMN_SCHOOL_ID, COLUMN_LOCATION_NAME, COLUMN_LOCATION_MARKER_COORDINATES,
            COLUMN_LOCATION_BUILDING_BOUNDS, COLUMN_LOCATION_BACKGROUND,
            COLUMN_LOCATION_IMAGES_AND_DESCRIPTIONS, COLUMN_LOCATION_VISITED };

    private static final String DATABASE_CREATE_LOCATIONS_TABLE = "create table "
            + TABLE_LOCATIONS + "("
            + COLUMN_LOCATION_ID + " integer primary key, "
            + COLUMN_SCHOOL_ID + " integer not null, "
            + COLUMN_LOCATION_NAME + " text not null, "
            + COLUMN_LOCATION_MARKER_COORDINATES + " text not null, "
            + COLUMN_LOCATION_BUILDING_BOUNDS + " text not null, "
            + COLUMN_LOCATION_BACKGROUND + " text not null, "
            + COLUMN_LOCATION_IMAGES_AND_DESCRIPTIONS + " text not null, "
            + COLUMN_LOCATION_VISITED + " integer not null);";

    private static final String DATABASE_CREATE_NOTES_TABLE = "create table "
            + TABLE_NOTES + "("
            + COLUMN_NOTE_ID + " integer primary key, "
            + COLUMN_NOTE_BUILDING_ID + " integer not null, "
            + COLUMN_NOTE_STRING + " text not null);";

    public TourBuddySQLiteHelper(Context context)
    {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    //Create all the necessary databases
    @Override
    public void onCreate(SQLiteDatabase database) {

        //Create the locations table
        database.execSQL(DATABASE_CREATE_LOCATIONS_TABLE);

        //Create the notes table
        database.execSQL(DATABASE_CREATE_NOTES_TABLE);

        createCoover(database);
        createSweeney(database);
        createAtanasoff(database);

        createHoover(database);
    }

    private void createSweeney(SQLiteDatabase database)
    {
        String name = "Sweeney Hall";
        LatLng marker = new LatLng(42.0277686, -93.65088);
        ArrayList<LatLngBounds> bounds = new ArrayList<LatLngBounds>();
        LatLngBounds.Builder bound = new LatLngBounds.Builder();
        bound.include(new LatLng(42.0273305, -93.650286))
                .include(new LatLng(42.027701, -93.65076));
        bounds.add(bound.build());
        LatLngBounds.Builder bound2 = new LatLngBounds.Builder();
        bound2.include(new LatLng(42.02766, -93.650301))
                .include(new LatLng(42.028103, -93.65186));
        bounds.add(bound2.build());
        String background = "Sweeney Hall is home to the Department of Chemical and Biological Engineering";
        ArrayList<Integer> images = new ArrayList<Integer>();
        ArrayList<String> imageDesc = new ArrayList<String>();
        images.add(R.drawable.sweeney_1);
        imageDesc.add("Sweeney Hall is named after Dr. Orland Russell Sweeney who first came to Iowa State College in 1920 from the University of Cincinnati where he had served as the head of the Chemical Engineering Department for two years.");
        images.add(R.drawable.sweeney_2);
        imageDesc.add("Sweeney Hall is home to the Department of Chemical and Biological Engineering.");
        images.add(R.drawable.sweeney_3);
        imageDesc.add("A fire on May 30th, 2014 broke out in Sweeney Hall. Fortunately no one was hurt and Sweeney has since been renovated.");
        createCampusLocation(database, 2, name, marker, bounds, background, images, imageDesc, false);
    }

    private void createCoover(SQLiteDatabase database)
    {
        String name = "Coover Hall";
        LatLng marker = new LatLng(42.028358, -93.650738);
        ArrayList<LatLngBounds> bounds = new ArrayList<LatLngBounds>();
        LatLngBounds.Builder bound = new LatLngBounds.Builder();
        bound.include(new LatLng(42.02810, -93.65027))
                .include(new LatLng(42.028847, -93.65178));
        bounds.add(bound.build());
        String background = "Coover Hall was built in 1950 and is home to the Department of Electrical and Computer Engineering.";
        ArrayList<Integer> images = new ArrayList<Integer>();
        ArrayList<String> imageDesc = new ArrayList<String>();
        images.add(R.drawable.coover_3);
        imageDesc.add("Coover Hall is home to the Department of Electrical and Computer Engineering.");
        images.add(R.drawable.coover_10);
        imageDesc.add("A classroom in Coover Hall.");
        images.add(R.drawable.coover_2);
        imageDesc.add("Coover Hall was built in 1950.");
        images.add(R.drawable.coover_4);
        imageDesc.add("Coover Hall is home to many research laboratories.");
        images.add(R.drawable.coover_5);
        imageDesc.add("A student lounge space located on the 2nd floor.");
        createCampusLocation(database, 1, name, marker, bounds, background, images, imageDesc, false);
    }

    private void createAtanasoff(SQLiteDatabase database)
    {
        String name = "Atanasoff Hall";
        LatLng marker = new LatLng(42.028170, -93.64970);
        ArrayList<LatLngBounds> bounds = new ArrayList<LatLngBounds>();
        LatLngBounds.Builder bound = new LatLngBounds.Builder();
        bound.include(new LatLng(42.02829, -93.65019))
                .include(new LatLng(42.028000, -93.6492));
        bounds.add(bound.build());
        String background = "Atanasoff Hall is primarily holds offices for Professor from the Department of Computer Science";
        ArrayList<Integer> images = new ArrayList<Integer>();
        ArrayList<String> imageDesc = new ArrayList<String>();
        images.add(R.drawable.atanasoff_1);
        imageDesc.add("Atanasoff Hall was constructed in 1969.");
        images.add(R.drawable.atanasoff_2);
        imageDesc.add("Atanasoff Hall was named after John Vincent Atanasoff.");
        images.add(R.drawable.atanasoff_3);
        imageDesc.add("The Atanasoff–Berry Computer was the first Digital Computer ever invented.");
        images.add(R.drawable.atanasoff_4);
        imageDesc.add("John Vincent Atanasoff was co-inventor of the first digital computer (the ABC), along with Clifford Berry. In honor of Mr. Atanasoff, the building bearing his name is designed to look like a microchip.");
        createCampusLocation(database, 3, name, marker, bounds, background, images, imageDesc, false);
    }

    private void createHoover(SQLiteDatabase database)
    {
        String name = "Hoover Hall";
        LatLng marker = new LatLng(42.026644,-93.651058);
        ArrayList<LatLngBounds> bounds = new ArrayList<LatLngBounds>();
        LatLngBounds.Builder bound = new LatLngBounds.Builder();
        bound.include(new LatLng(42.026814, -93.651836))
                .include(new LatLng(42.026513, -93.650390));
        bounds.add(bound.build());
        String background = "Hoover Hall has the offices for the Department of Chemical Engineering";
        ArrayList<Integer> images = new ArrayList<Integer>();
        ArrayList<String> imageDesc = new ArrayList<String>();
        images.add(R.drawable.hoover_1);
        imageDesc.add("Hoover Hall was constructed in 2004, and connects with Howe Hall through a sky bridge.");
        images.add(R.drawable.hoover_2);
        imageDesc.add("Hoover was named after Gary and Donna Hoover, ISU graduates from 1961.");
        images.add(R.drawable.hoover_3);
        imageDesc.add("Odds are, this is where you'll have your first Chemistry lectures!");
        createCampusLocation(database, 6, name, marker, bounds, background, images, imageDesc, false);
    }

    public void createCampusLocation(SQLiteDatabase database, int id, String name, LatLng markerLocation, List<LatLngBounds> buildingBoundsList, String backgroundInfo, List<Integer> imagesList, List<String> imageDescriptionList, boolean visited) {
        long insertId = 0;
        ContentValues values = new ContentValues();
        values.put(COLUMN_LOCATION_ID, id);
        values.put(COLUMN_SCHOOL_ID, 1000);
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
        database.insert(TABLE_LOCATIONS, null, values);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
