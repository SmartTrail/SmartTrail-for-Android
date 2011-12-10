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
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.geozen.smarttrail.R;
import com.geozen.smarttrail.model.Review;
import com.geozen.smarttrail.util.ImageLoader;

public class ReviewsAdapter extends BaseAdapter {

	private Activity activity;
	private ArrayList<Review> data;
	private static LayoutInflater inflater = null;
	public ImageLoader imageLoader;
	private String mBaseUrl;

	public ReviewsAdapter(Activity a, ArrayList<Review> reviews, String baseUrl) {
		activity = a;
		data = reviews;
		inflater = (LayoutInflater) activity
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		imageLoader = new ImageLoader(activity.getApplicationContext());
		mBaseUrl = baseUrl;
	}

	public int getCount() {
		return data.size();
	}

	public Review getItem(int position) {
		return data.get(position);
	}

	public long getItemId(int position) {
		return position;
	}

	public static class ViewHolder {
		public TextView mUsername;
		public TextView mNumReviews;
		public ImageView mImage;
		public TextView mReview;
		public RatingBar mRating;
		public TextView mDate;
	}

	public View getView(int position, View convertView, ViewGroup parent) {
        View vi=convertView;
        ViewHolder holder;
        if(convertView==null){
            vi = inflater.inflate(R.layout.list_item_review, null);
            holder=new ViewHolder();
            holder.mUsername=(TextView)vi.findViewById(R.id.name);
            holder.mNumReviews=(TextView)vi.findViewById(R.id.numReviews);
            holder.mImage=(ImageView)vi.findViewById(R.id.photo);
            holder.mReview=(TextView)vi.findViewById(R.id.review);
            holder.mRating=(RatingBar)vi.findViewById(R.id.rating);
            holder.mDate=(TextView)vi.findViewById(R.id.date);
            vi.setTag(holder);
        }
        else
            holder=(ViewHolder)vi.getTag();
        
        Review review = data.get(position);
       
        
        holder.mUsername.setText(review.mUsername);
        holder.mNumReviews.setText(review.mUserNumReviews + " "+activity.getResources().getString(R.string.reviews));
        holder.mReview.setText(review.mReview);
        holder.mRating.setRating(review.mRating);
        holder.mDate.setText(review.mUpdatedAtStr);

        if (review.mPhotoId.equals("null")) {
        	holder.mImage.setTag("");
        	holder.mImage.setImageResource(R.drawable.speaker_image_empty);
        } else {
        	String url= mBaseUrl + review.mPhotoId;
        	holder.mImage.setTag(url);
        	imageLoader.DisplayImage( url, activity, holder.mImage);
        }
        return vi;
    }

	public void setData(ArrayList<Review> mReviews) {
		data = mReviews;
		
	}
}