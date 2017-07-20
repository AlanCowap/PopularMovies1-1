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
import org.w3c.dom.Text;


public class TrailerAdapter extends RecyclerView.Adapter<TrailerAdapter.TrailerViewHolder>{

    private Trailer[] trailerList;
    private TextView trailerView;
    private static final String TRAILER_URI ="key";
    private static final String TRAILER_DESC = "name";
    private TrailerClickHandler trailerClickHandler;

    public TrailerAdapter(TrailerClickHandler trailerClickHandler){
        this.trailerClickHandler = trailerClickHandler;
    }
    @Override
    public TrailerViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context con = parent.getContext();
        int filmLayout = R.layout.trailer_item;
        LayoutInflater inflater = LayoutInflater.from(con);
        boolean attachToParent = false;
        View view = inflater.inflate(filmLayout,parent,attachToParent);
        return new TrailerAdapter.TrailerViewHolder(view);
    }

    @Override
    public void onBindViewHolder(TrailerViewHolder holder, int position) {
        holder.bindTrailer(trailerList[position]);
    }

    @Override
    public int getItemCount() {
        if(trailerList != null){
            return trailerList.length;
        }
        return 0;
    }
    public void setTrailerList(JSONArray trailers){
        try{
            trailerList = new Trailer[trailers.length()];
            

            for(int i = 0; i < trailers.length();++i){
                JSONObject jsOb = trailers.getJSONObject(i);
                Trailer trailerItem = new Trailer(jsOb.getString(TRAILER_URI), jsOb.getString(TRAILER_DESC));
                trailerList[i] = trailerItem;
            }
        }catch (JSONException jsEx){
            Log.e(TrailerAdapter.class.getSimpleName(), jsEx.getMessage());
        }
    }
    class TrailerViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        private View ivView;
        public TrailerViewHolder(View itemView) {
            super(itemView);
            ivView = itemView;
            trailerView = (TextView) itemView.findViewById(R.id.tv_trailer_item);
            itemView.setOnClickListener(this);
        }



        void bindTrailer(Trailer trailer){
            trailerView.setText(itemView.getResources().getString(R.string.film_detail_trailer_intro) + trailer.getTrailerDesc());
    }
        @Override
        public void onClick(View v) {
            int trailerPosition = getAdapterPosition();
            trailerClickHandler.onClickListen(trailerPosition);
        }
    }

    public interface TrailerClickHandler{
        void onClickListen(int trailerArrayPosition);
    }

    public Trailer getSpecificTrailer(int position){
        return trailerList[position];
    }
}
