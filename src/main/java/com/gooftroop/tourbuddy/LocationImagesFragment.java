package com.gooftroop.tourbuddy;

import android.app.Activity;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.gooftroop.tourbuddy.R;
import com.google.android.gms.maps.GoogleMap;

import java.util.List;

public class LocationImagesFragment extends Fragment {

    public static final String EXTRA_IMAGE_ID = "EXTRA_IMAGE_ID";

    public static final String EXTRA_IMAGE_CAPTION = "EXTRA_IMAGE_CAPTION";

    private GoogleMap mMap;

    private Activity curActivity;

    public static final LocationImagesFragment newInstance(int imageResId, String imageCaption)
    {
        LocationImagesFragment frag = new LocationImagesFragment();
        Bundle bundle = new Bundle(1);
        bundle.putInt(EXTRA_IMAGE_ID, imageResId);
        bundle.putString(EXTRA_IMAGE_CAPTION, imageCaption);
        frag.setArguments(bundle);
        return frag;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.image_view_layout, container, false);

        String imageCaption = getArguments().getString(EXTRA_IMAGE_CAPTION);
        TextView textViewCaption = (TextView) view.findViewById(R.id.textViewImageCaption);
        textViewCaption.setText(imageCaption);

        int imageResId = getArguments().getInt(EXTRA_IMAGE_ID);
        ImageView imageView = (ImageView) view.findViewById(R.id.imageViewDetailImage);
        imageView.setImageResource(imageResId);


        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        curActivity = activity;
    }
}
