package com.example.android.app.myview.popularmovies;

import android.content.Context;
import android.database.Cursor;
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



public class FilmAdapter extends RecyclerView.Adapter<FilmAdapter.FilmViewHolder>{
    //Declare FilmAdapter class variables
    private int numFilms;
    //private JSONArray films;
    private Film[] listOfFilms;
    private Cursor filmCursor;
    final private FilmClickListener filmClickHandler;
    //Declare constructor that takes in a FilmClickListener
    public FilmAdapter(FilmClickListener clickListener){
        this.filmClickHandler = clickListener;
    }

    //Parse the data from the cursor object into Film objects, and populate the listOfFilms array
    public void parseCursorToFilmList(Cursor filmCursor){
        listOfFilms = new Film[filmCursor.getCount()];
        for(int i = 0; i < filmCursor.getCount(); ++i){
            listOfFilms[i] = new Film(
                    filmCursor.getString(filmCursor.getColumnIndex(FilmDatabaseContract.FilmDatabase.COLUMN_FILM_TITLE)),
                    filmCursor.getString(filmCursor.getColumnIndex(FilmDatabaseContract.FilmDatabase.COLUMN_FILM_POSTER_PATH)),
                    filmCursor.getString(filmCursor.getColumnIndex(FilmDatabaseContract.FilmDatabase.COLUMN_FILM_OVERVIEW)),
                    filmCursor.getString(filmCursor.getColumnIndex(FilmDatabaseContract.FilmDatabase.COLUMN_FILM_RATING)),
                    filmCursor.getString(filmCursor.getColumnIndex(FilmDatabaseContract.FilmDatabase.COLUMN_FILM_RELEASE_DATE)),
                    filmCursor.getString(filmCursor.getColumnIndex(FilmDatabaseContract.FilmDatabase.COLUMN_FILM_MOVIEDB_ID))
            );
        }
    }


    // Set films with cursor
    public void setFilms(Cursor filmCursor){
        //Film[] testFilms = new Film[filmCursor.getCount()];
        listOfFilms = new Film[filmCursor.getCount()];
        Log.d(FilmAdapter.class.getSimpleName(),"TEST TEST 1 " + Integer.toString(listOfFilms.length));
        for(int i = 0; i < listOfFilms.length; ++i){
            Log.d(FilmAdapter.class.getSimpleName(), "TEST TEST 3 ");
            filmCursor.moveToPosition(i);
            listOfFilms[i] = new Film(
                    filmCursor.getString(filmCursor.getColumnIndex(FilmDatabaseContract.FilmDatabase.COLUMN_FILM_TITLE)),
                    filmCursor.getString(filmCursor.getColumnIndex(FilmDatabaseContract.FilmDatabase.COLUMN_FILM_POSTER_PATH)),
                    filmCursor.getString(filmCursor.getColumnIndex(FilmDatabaseContract.FilmDatabase.COLUMN_FILM_RATING)),
                    filmCursor.getString(filmCursor.getColumnIndex(FilmDatabaseContract.FilmDatabase.COLUMN_FILM_OVERVIEW)),
                    filmCursor.getString(filmCursor.getColumnIndex(FilmDatabaseContract.FilmDatabase.COLUMN_FILM_RELEASE_DATE)),
                    filmCursor.getString(filmCursor.getColumnIndex(FilmDatabaseContract.FilmDatabase.COLUMN_FILM_MOVIEDB_ID))
            );
            Log.d(FilmAdapter.class.getSimpleName(), "TEST TEST 2 " +listOfFilms[i].getFilmPosterPath());
        }
        this.numFilms = filmCursor.getCount();
    }

    //Declate a method that will set the films JSON array and the numFilms int;
    public void setFilms(JSONArray films){
        this.listOfFilms = new Film[films.length()];
        for(int i = 0; i < films.length();++i){
            try{
                JSONObject filmObject = (JSONObject) films.get(i);
                Film filmListItem = new Film(
                        filmObject.getString(Film.ORIGINAL_TITLE),
                        filmObject.getString(Film.FILM_POSTER_PATH),
                        filmObject.getString(Film.FILM_VOTE_AVERAGE),
                        filmObject.getString(Film.FILM_OVERVIEW),
                        filmObject.getString(Film.FILM_RELEASE_DATE),
                        filmObject.getString(Film.FILM_MOVIEDB_ID)

                );

                listOfFilms[i] = filmListItem;
            }catch (JSONException jsEx){
                Log.e(FilmAdapter.class.getSimpleName(), jsEx.getMessage());
            }
        }
        //this.films = films;
        this.numFilms = listOfFilms.length;
    }


    public Film getSpecificListItem(int filmPosition){
        return listOfFilms[filmPosition];
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
        holder.bindPoster(this.getSpecificListItem(position));
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
        void bindPoster(Film film){
            Context con = ivFilm.getContext();
            String filmposter = null;
            //set the filmPoster string equal to the film that is returned from the JSONObject
            filmposter = film.getFilmPosterPath();
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
