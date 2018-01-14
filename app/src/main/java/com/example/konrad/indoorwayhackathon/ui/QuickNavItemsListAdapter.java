package com.example.konrad.indoorwayhackathon.ui;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.example.konrad.indoorwayhackathon.R;
import com.indoorway.android.common.sdk.IndoorwaySdk;
import com.indoorway.android.common.sdk.model.IndoorwayObjectParameters;
import com.indoorway.android.common.sdk.model.IndoorwayPosition;
import com.indoorway.android.location.sdk.IndoorwayLocationSdk;

import java.util.ArrayList;

public class QuickNavItemsListAdapter extends ArrayAdapter<IndoorwayObjectParameters> {
    private static class ViewHolder {
        private TextView itemView;
    }

    public QuickNavItemsListAdapter(Context context, int textViewResourceId, ArrayList<IndoorwayObjectParameters> items) {
        super(context, textViewResourceId, items);
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        if (convertView == null) {
            convertView = LayoutInflater.from(this.getContext())
                    .inflate(R.layout.pritip_list_item, parent, false);

            viewHolder = new ViewHolder();
            viewHolder.itemView = (TextView) convertView.findViewById(R.id.protip_list_row);

            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        final IndoorwayObjectParameters item = getItem(position);
        if (item != null) {
            // My layout has only one TextView
            // do whatever you want with your string and long
            IndoorwayPosition indoorwayPosition = IndoorwayLocationSdk.instance().position().latest();
            double distance = item.getCenterPoint().getDistanceTo(indoorwayPosition.getCoordinates());
            viewHolder.itemView.setText(String.format("%s - %f meters", item.getName(), distance));
        }
        return convertView;
    }
}
