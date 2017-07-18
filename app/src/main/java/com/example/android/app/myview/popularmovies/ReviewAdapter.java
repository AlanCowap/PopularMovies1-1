package com.example.android.app.myview.popularmovies;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by liamd on 17/07/2017.
 */

public class ReviewAdapter extends RecyclerView.Adapter<ReviewAdapter.ReviewViewHolder>{
    private Review[] reviewList;
    private TextView tvReviewContent;
    private TextView tvReviewAuthor;
    private static final String REVIEW_URL ="url";
    private static final String REVIEW_AUTHOR = "author";
    private static final String REVIEW_CONTENT = "content";
    private ReviewClickHandler reviewClickHandler;

    public ReviewAdapter(ReviewClickHandler reviewClickHandler){
        this.reviewClickHandler = reviewClickHandler;
    }


    public Review getSpecificReview(int position){
        return this.reviewList[position];
    }
    @Override
    public ReviewViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context con = parent.getContext();
        int reviewLayout = R.layout.review_item;
        LayoutInflater inflater = LayoutInflater.from(con);
        boolean attachToParent = false;
        View view = inflater.inflate(reviewLayout,parent,attachToParent);
        return new ReviewAdapter.ReviewViewHolder(view);
    }
    public void setReviewList(JSONArray reviews){
        try{
            reviewList = new Review[reviews.length()];
  

            for(int i = 0; i < reviews.length();++i){
                JSONObject jsOb = reviews.getJSONObject(i);
                Review review = new Review(jsOb.getString(REVIEW_AUTHOR), jsOb.getString(REVIEW_CONTENT), jsOb.getString(REVIEW_URL));
                reviewList[i] = review;
            }
        }catch (JSONException jsEx){
            Log.e(TrailerAdapter.class.getSimpleName(), jsEx.getMessage());
        }
    }
    @Override
    public void onBindViewHolder(ReviewViewHolder holder, int position) {
        holder.bindReview(position);
    }

    @Override
    public int getItemCount() {
        if(reviewList!= null){
            return reviewList.length;
        }
        return 0;
    }

    class ReviewViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private View ivView;
        public ReviewViewHolder(View itemView) {
            super(itemView);
            ivView = itemView;
            tvReviewAuthor = (TextView) itemView.findViewById(R.id.tv_review_author);
            tvReviewContent = (TextView) itemView.findViewById(R.id.tv_review_content);
            itemView.setOnClickListener(this);
        }


        public void bindReview(int position){
            Review review = reviewList[position];
            tvReviewContent.setText(review.getReviewContent());
            tvReviewAuthor.setText(review.getReviewAuthor());
        }

        @Override
        public void onClick(View v) {
            int reviewPosition = getAdapterPosition();
            reviewClickHandler.onClickReviewListen(reviewPosition);
        }

    }
    public interface ReviewClickHandler{
        void onClickReviewListen(int reviewArrayPosition);
    }
}
