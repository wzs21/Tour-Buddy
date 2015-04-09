package com.gooftroop.tourbuddy;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import org.apache.http.HttpResponse;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import static com.gooftroop.tourbuddy.utils.DeviceUtils.getDeviceAndroidId;

/**
 * Created by Austin on 2/25/2015.
 */
public class HttpClientHelper {

    public static final String IMAGES_BASE_URL = "http://gala.cs.iastate.edu/~erichk/dashboard/code/img/buildings/";

    public static final String BASE_URL = "http://gala.cs.iastate.edu/~erichk/dashboard/code/requestHandler.php";

    public static HttpResponse visitLocation(CampusLocation location, Context curContext)
    {
        try
        {
            JSONObject req = new JSONObject();
            req.put("building_id", location.getId());
            req.put("type", "visit");
            req.put("device_id", getDeviceAndroidId(curContext));

            return httpPostHttpResponse(req, BASE_URL);
        }
        catch (Exception e)
        {
            System.out.println(e.getMessage());
            return null;
        }
    }

    public static int getDatabaseVersion(Context curContext)
    {
        try
        {
            JSONObject req = new JSONObject();
            req.put("type", "getVersion");
            req.put("device_id", getDeviceAndroidId(curContext));

            String response = httpPostStringResponse(req, BASE_URL);
            return Integer.parseInt(response);
        }
        catch (Exception e)
        {
            System.out.println(e.getMessage());
            return 0;
        }
    }

    public static JSONArray updateDatabase(Context curContext)
    {
        try
        {
            JSONObject req = new JSONObject();
            req.put("type", "getUpdate");
            req.put("device_id", getDeviceAndroidId(curContext));
            String raw = httpPostStringResponse(req, BASE_URL);
            return new JSONArray(raw);
        }
        catch (Exception e)
        {
            System.out.println(e.getMessage());
            return null;
        }
    }

    public static String httpPostStringResponse(JSONObject req, String url) throws Exception
    {
        DefaultHttpClient httpclient = new DefaultHttpClient();

        HttpPost httpPost = new HttpPost(url);

        if (req != null)
        {
            StringEntity se = new StringEntity(req.toString());

            //sets the post request as the resulting string
            httpPost.setEntity(se);
        }

        //Set the http post headers
        httpPost.setHeader("Accept", "application/json");
        httpPost.setHeader("Content-type", "application/json; charset=utf-8");

        //Handles what is returned from the page
        HttpResponse response = httpclient.execute(httpPost);
        ResponseHandler<String> responseHandler = new BasicResponseHandler();
        return httpclient.execute(httpPost, responseHandler);
    }

    public static HttpResponse httpPostHttpResponse(JSONObject req, String url) throws Exception
    {
        DefaultHttpClient httpclient = new DefaultHttpClient();

        HttpPost httpPost = new HttpPost(url);

        if (req != null)
        {
            StringEntity se = new StringEntity(req.toString());

            //sets the post request as the resulting string
            httpPost.setEntity(se);
        }

        //Set the http post headers
        httpPost.setHeader("Accept", "application/json");
        httpPost.setHeader("Content-type", "application/json; charset=utf-8");

        //Handles what is returned from the page
        HttpResponse response = httpclient.execute(httpPost);
        ResponseHandler<String> responseHandler = new BasicResponseHandler();
        String strResponse = httpclient.execute(httpPost, responseHandler);

        return response;
    }

    public static Bitmap getBitmapFromServer(String link)
    {
        try
        {
            URL url = new URL(IMAGES_BASE_URL + link);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoInput(true);
            connection.connect();
            InputStream input = connection.getInputStream();
            return BitmapFactory.decodeStream(input);
        }
        catch (IOException e)
        {
            e.printStackTrace();
            Log.e("getBmpFromUrl error: ", e.getMessage().toString());
            return null;
        }
    }

    public String readUrl(String mapsApiDirectionsUrl) throws IOException {
        String data = "";
        InputStream iStream = null;
        HttpURLConnection urlConnection = null;
        try {
            URL url = new URL(mapsApiDirectionsUrl);
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.connect();
            urlConnection.setConnectTimeout(20000);
            iStream = urlConnection.getInputStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(iStream));
            StringBuffer sb = new StringBuffer();
            String line = "";
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }
            data = sb.toString();
            br.close();
        } catch (Exception e) {
            Log.d("URL Read Exception", e.toString());
        } finally {
            iStream.close();
            urlConnection.disconnect();
        }
        return data;
    }
}
