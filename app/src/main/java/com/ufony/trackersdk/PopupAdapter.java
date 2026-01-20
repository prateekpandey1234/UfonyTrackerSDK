package com.ufony.trackersdk;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.fragment.app.FragmentManager;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;
import com.google.gson.Gson;

public class PopupAdapter implements GoogleMap.InfoWindowAdapter {
    // Remove the cached popup view from here
    // private View popup=null;

    private final LayoutInflater inflater;
    // You can remove these if they are not used elsewhere in the adapter
    // private FragmentManager _fragmentManager;
    // private Context context;

    public PopupAdapter(LayoutInflater inflater, FragmentManager fragmentManager, Context context) {
        this.inflater = inflater;
        // this._fragmentManager = fragmentManager;
        // this.context = context;
    }

    @Override
    public View getInfoWindow(Marker marker) {
        // We are using getInfoContents, so we return null here.
        return null;
    }

    @SuppressLint("InflateParams")
    @Override
    public View getInfoContents(Marker marker) {
        // *** THE FIX: Inflate a new view every time the method is called. ***
        View popup = inflater.inflate(R.layout.care_taker_custom, null);

        // This ensures the data is always correct for the specific marker.
        Log.d("TAG", "Populating info window for marker: " + new Gson().toJson(marker.getTag()));

        TextView busNumber = popup.findViewById(R.id.tv_busNo);
        TextView careName = popup.findViewById(R.id.tv_care_name);
        TextView careNumber = popup.findViewById(R.id.tv_care_number);
        ImageView imgCall = popup.findViewById(R.id.call);

        // This check is important to prevent NullPointerException
        if (!(marker.getTag() instanceof GoogleMarkerContext)) {
            return null; // Or return a default view
        }

        GoogleMarkerContext markerContext = (GoogleMarkerContext) marker.getTag();

        if (markerContext.getCaretakerNumber() == null || markerContext.getCaretakerName() == null || markerContext.getCaretakerName().isEmpty() || markerContext.getCaretakerNumber().isBlank()) {
            imgCall.setVisibility(View.GONE);
            careName.setVisibility(View.GONE);
            careNumber.setVisibility(View.GONE);
        } else {
            careName.setVisibility(View.VISIBLE);
            careNumber.setVisibility(View.VISIBLE);
            imgCall.setVisibility(View.VISIBLE);
        }

        busNumber.setText(markerContext.getTitle() != null ? markerContext.getTitle() : "");
        careName.setText(markerContext.getCaretakerName() != null ? markerContext.getCaretakerName() : "");
        careNumber.setText(markerContext.getCaretakerNumber() != null ? markerContext.getCaretakerNumber() : "");

        // Return the new, freshly populated view
        return popup;
    }
}
