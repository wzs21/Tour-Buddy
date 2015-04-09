package com.gooftroop.tourbuddy.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.provider.Settings.Secure;
/**
 * Created by Austin on 3/31/2015.
 */
public class DeviceUtils {

    public static String getDeviceAndroidId(Context curContext)
    {
        return Secure.getString(curContext.getContentResolver(), Secure.ANDROID_ID);
    }

    public static boolean isConnectedToTheInternet(Context curContext)
    {
        ConnectivityManager cm = (ConnectivityManager) curContext.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return ((activeNetwork != null) && (activeNetwork.isConnectedOrConnecting()));
    }
}
