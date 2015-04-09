package com.gooftroop.tourbuddy;

/**
 * Created by Austin on 2/25/2015.
 */
public class TourNote {

    private int noteId;
    private int buildingId;
    private String note;

    public TourNote(int noteId, int buildingId, String note)
    {
        this.noteId = noteId;
        this.buildingId = buildingId;
        this.note = note;
    }

    public int getNoteId()
    {
        return noteId;
    }

    public int getBuildingId()
    {
        return buildingId;
    }

    public String getNote()
    {
        return note;
    }
}
