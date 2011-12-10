/**
 * GeoZen LLC
 * Copyright 2011. All rights reserved.
 */
package com.geozen.smarttrail.ui;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.geozen.smarttrail.R;
import com.geozen.smarttrail.model.Region;

public class RegionsAdapter extends BaseAdapter {

	private Activity activity;
	private ArrayList<Region> data;
	private static LayoutInflater inflater = null;
	//public ImageLoader imageLoader;
	//private String mBaseUrl;

	public RegionsAdapter(Activity a, ArrayList<Region> regions, String baseUrl) {
		activity = a;
		data = regions;
		inflater = (LayoutInflater) activity
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		//imageLoader = new ImageLoader(activity.getApplicationContext());
		//mBaseUrl = baseUrl;
	}

	public int getCount() {
		return data.size();
	}

	public Region getItem(int position) {
		return data.get(position);
	}

	public long getItemId(int position) {
		return position;
	}

	public static class ViewHolder {
		public TextView mRegionName;
		public TextView mSponsorName;
	
	}

	public View getView(int position, View convertView, ViewGroup parent) {
        View vi=convertView;
        ViewHolder holder;
        if(convertView==null){
        	
            vi = inflater.inflate(R.layout.list_item_region, null);
            holder=new ViewHolder();
            holder.mRegionName=(TextView) vi.findViewById(R.id.regionName);
            holder.mSponsorName=(TextView) vi.findViewById(R.id.sponsorName);
         
            vi.setTag(holder);
        }
        else
            holder=(ViewHolder)vi.getTag();
        
        Region region = data.get(position);
       
        
        holder.mRegionName.setText(region.mName);
        holder.mSponsorName.setText(region.mSponsorName);

     
        return vi;
    }

	public void setData(ArrayList<Region> regions) {
		data = regions;
	}
}