package com.gooftroop.tourbuddy;

import android.text.TextUtils;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;

import java.util.List;

public class CampusLocation {

    private int id;

    private int schoolId;

    private String name;

    private LatLng markerLocation;

    private List<LatLngBounds> buildingBoundsList;

    private String description;

    private List<Integer> imagesList;

    private List<String> imageDescriptionList;

    private boolean visited;

    public CampusLocation(int id, int schoolId, String name, LatLng markerLocation, List<LatLngBounds> buildingBoundsList, String description, List<Integer> imagesList, List<String> imageDescriptionList, boolean visited)
    {
        this.id = id;

        this.schoolId = schoolId;

        if (TextUtils.isEmpty(name))
        {
            throw new IllegalArgumentException("Name should be filled in");
        }
        this.name = name;

        if (markerLocation == null)
        {
            throw new IllegalArgumentException("The LatLng object for marker location should not be null");
        }
        this.markerLocation = markerLocation;

        if (buildingBoundsList == null || buildingBoundsList.size() <= 0)
        {
            throw new IllegalArgumentException("You must specify a buildingBoundsList for this location.");
        }
        this.buildingBoundsList = buildingBoundsList;

        if (description == null)
        {
            throw new IllegalArgumentException("Background info should not be null");
        }
        this.description = description;


        if (imageDescriptionList == null)
        {
            throw new IllegalArgumentException("Images List Should not be null");
        }

        if (imagesList == null)
        {
            throw new IllegalArgumentException("Images List Should not be null");
        }

        if (imagesList.size() == 0 || (imagesList.size() != imageDescriptionList.size()))
        {
            throw new IllegalArgumentException("You must specify an images and images description list of the same size");
        }

        this.imagesList = imagesList;

        this.imageDescriptionList = imageDescriptionList;

        this.visited = visited;
    }

    public int getId()
    {
        return id;
    }

    public int getSchoolId()
    {
        return schoolId;
    }

    public String getName()
    {
        return name;
    }

    public LatLng getMarkerLocation()
    {
        return markerLocation;
    }

    public List<LatLngBounds> getBuildingBoundsList()
    {
        return buildingBoundsList;
    }

    public String getDescription()
    {
        return description;
    }

    public List<Integer> getImagesList()
    {
        return imagesList;
    }

    public List<String> getImageDescriptionList()
    {
        return imageDescriptionList;
    }

    public boolean getVisited()
    {
        return visited;
    }

    public void setVisited(boolean visited)
    {
        this.visited = visited;
    }

    /**
     * Returns true if the given LatLng point is the bounds of the building
     * @param point - The LatLng point to check if it is within the bounds of the building.
     * @return
     */
    public boolean pointIsInBounds (LatLng point)
    {
        for (int i = 0; i<buildingBoundsList.size(); i++)
        {
            if (buildingBoundsList.get(i).contains(point))
            {
                return true;
            }
        }

        return false;
    }
}
