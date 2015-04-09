package com.gooftroop.tourbuddy;

import android.graphics.Bitmap;
import android.os.AsyncTask;

import java.util.ArrayList;

public class DownloadImagesAsyncTask extends AsyncTask<String, Void, ArrayList<Bitmap>>
{
    @Override
    protected ArrayList<Bitmap> doInBackground(String... links)
    {
        try
        {
            ArrayList<Bitmap> bitmaps = new ArrayList<Bitmap>();
            for (int i = 0; i<links.length; i++)
            {
                Bitmap bitmap = HttpClientHelper.getBitmapFromServer(links[i]);
                bitmaps.add(bitmap);
            }

            return bitmaps;
        }
        catch (Exception e)
        {
            System.out.println(e.getMessage());
            return null;
        }
    }
}