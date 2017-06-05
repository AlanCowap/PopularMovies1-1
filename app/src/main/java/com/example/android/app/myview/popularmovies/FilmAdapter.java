package com.example.android.app.myview.popularmovies;

import android.content.Context;
import android.support.v7.view.menu.MenuView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by User on 5/23/2017.
 */

public class FilmAdapter extends RecyclerView.Adapter<FilmAdapter.FilmViewHolder>{
    //Declare FilmAdapter class variables
    private int numFilms;
    private JSONArray films;
    final private FilmClickListener filmClickHandler;
    //Declare constructor that takes in a FilmClickListener
    public FilmAdapter(FilmClickListener clickListener){
        this.filmClickHandler = clickListener;
    }

    //Declate a method that will set the films JSON array and the numFilms int;
    public void setFilms(JSONArray films){
        this.films = films;
        this.numFilms = films.length();
    }

    //Declare a method that takes in an int, and returns the item in the films array at that position
    public JSONObject getSpecificFilm(int filmPosition){
        try {
            return (JSONObject) this.films.getJSONObject(filmPosition);
        }catch(JSONException jsEx){
            Log.e(FilmAdapter.class.getSimpleName(), jsEx.getMessage());
        }
        return null;
    }
    //Override the onCreateViewHolder Method
    @Override
    public FilmViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context con = parent.getContext();
        int filmLayout = R.layout.film_item;
        LayoutInflater inflater = LayoutInflater.from(con);
        boolean attachToParent = false;
        View view = inflater.inflate(filmLayout,parent,attachToParent);
        return new FilmViewHolder(view);
    }
    //Ovverride the onBindViewHolder method, which calls the bindPoster method on the passed in FilmViewHolder
    @Override
    public void onBindViewHolder(FilmViewHolder holder, int position) {
       try {
           holder.bindPoster((JSONObject) this.films.get(position));
       }catch(JSONException jsEx){
           Log.e(FilmViewHolder.class.getSimpleName(), jsEx.getMessage());
       }
    }
    //Get the number of items
    @Override
    public int getItemCount() {
        return this.numFilms;
    }

    //Declare a class the Extends RecyclerView view holder and implements View.OnClickListener
    class FilmViewHolder extends RecyclerView.ViewHolder implements  View.OnClickListener{
        //Declare UI variables
        ImageView poster;
        View ivFilm;
        //Declare Constructor
        public FilmViewHolder(View itemView) {
            super(itemView);
            ivFilm = itemView;
            poster = (ImageView) itemView.findViewById(R.id.iv_film_poster);
            itemView.setOnClickListener(this);
        }
        //Declare a bindPoster method,
        void bindPoster(JSONObject film){
            Context con = ivFilm.getContext();
            String filmposter = null;
            //set the filmPoster string equal to the film that is returned from the JSONObject
            try {
                filmposter = film.getString("poster_path");
            }
            catch (JSONException jsEx){
                Log.e(FilmViewHolder.class.getSimpleName(), jsEx.getMessage());
            }
            //If filmPoster isnt null, and contains a ., for file extension, use Picasso to fill the Imageview with the Image at that location. If it is null or empty
            //use the no poster image, taken from http://www.pinsdaddy.com/no-image-available-icon_nquACkOxV*TJt*l2puUBRhlP12hWM2e9JtVGM0jwJfA/
            if(filmposter != null){
                if(!filmposter.contains(".")){
                    int width = con.getResources().getDisplayMetrics().widthPixels;
                    int orient = con.getResources().getConfiguration().orientation;
                    switch (orient) {
                        case 1:
                           // Picasso.with(con).load(R.drawable.no_poster_image).resize(width / 2, 0).into(poster);
                            break;
                        case 2:
                           // Picasso.with(con).load(R.drawable.no_poster_image).resize(width / 4, 0).into(poster);
                            break;
                    }
                }
                else {
                    int width = con.getResources().getDisplayMetrics().widthPixels;
                    int orient = con.getResources().getConfiguration().orientation;
                    switch (orient) {
                        case 1:
                            Picasso.with(con).load(con.getResources().getString(R.string.poster_path) + filmposter).resize(width / 2, 0).into(poster);
                            break;
                        case 2:
                            Picasso.with(con).load(con.getResources().getString(R.string.poster_path) + filmposter).resize(width / 4, 0).into(poster);
                            break;
                    }
                }
            }

        }

        @Override
        public void onClick(View v) {
            int filmPosition = getAdapterPosition();
            filmClickHandler.onClickListen(filmPosition);
        }
    }

    public interface FilmClickListener{
        void onClickListen(int filmArrayPosition);
    }
}
