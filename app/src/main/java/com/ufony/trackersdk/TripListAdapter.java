package com.ufony.trackersdk;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

public class TripListAdapter extends RecyclerView.Adapter<TripListAdapter.TripListViewHolder> implements Filterable {

    private ArrayList<Trip> tripList = new ArrayList<>();
    private ArrayList<Trip> tripListFull = new ArrayList<>();
    private Context context;
    protected long loggedInUserId = 0;
    public TripListAdapter(ArrayList<Trip> tripList,Long userId, Context context) {
        this.tripList = tripList;
        this.context = context;
        tripListFull = new ArrayList<>(tripList);
        this.loggedInUserId = userId;
    }

    public void updateTripList(@NotNull ArrayList<Trip> tripList) {
        this.tripList = tripList;
        tripListFull = new ArrayList<>(tripList);
        notifyDataSetChanged();

    }

    @NonNull
    @Override
    public TripListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int i) {
        return new TripListViewHolder(LayoutInflater.from(context).inflate(R.layout.triplist_item, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull TripListViewHolder holder,  int position) {
        UserPreferenceManager prefs = UserPreferenceManager.Companion.forUser(loggedInUserId, context);

        holder.tvTripName.setText(tripList.get(position).getName());
        Log.d("TripList", "ActiveRoutes===2=" + tripList.get(position).getName());
        holder.tripView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                prefs.setTrips(null);
                Trip trips = tripList.get(position);
                Intent i = new Intent(context, TrackerActivitySDK.class);
                Bundle b = new Bundle();
                b.putSerializable("tripSubsCription", trips);
                i.putExtras(b);
                i.putExtra("activity", "tripList");
                Log.d("TripList", "ActiveRoutes===3=" + tripList.get(position).getName()+"=="+tripList.get(position).getRouteId());
                context.startActivity(i);
                ((Activity) context).finish();

            }
        });
    }

    @Override
    public int getItemCount() {
        return tripList.size();
    }

    @Override
    public Filter getFilter() {
        return tripFilter;
    }

    private Filter tripFilter = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            ArrayList<Trip> filteredTripList = new ArrayList<>();
            if (constraint == null || constraint.length() == 0) {
                filteredTripList.addAll(tripListFull);
            } else {
                String filterPattern = constraint.toString().toLowerCase().trim();
                for (Trip searchItem : tripListFull) {
                    if (searchItem.getName().toLowerCase().contains(filterPattern)) {
                        filteredTripList.add(searchItem);
                    }
                }
            }
            FilterResults results = new FilterResults();
            results.values = filteredTripList;
            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            tripList.clear();
            tripList.addAll((ArrayList) results.values);
            notifyDataSetChanged();
        }
    };


    public class TripListViewHolder extends RecyclerView.ViewHolder {

        TextView tvTripName;
        View tripView;

        public TripListViewHolder(View view) {
            super(view);
            tvTripName = view.findViewById(R.id.tv_tripName);
            tripView = view.findViewById(R.id.tripLayout);

        }
    }
}
