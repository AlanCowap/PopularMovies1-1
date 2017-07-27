package com.example.android.app.myview.popularmovies;


import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

public class GeneralUtils {

    public static final String EMPTY_STRING = "";

    public static String handleMovieDBRequests(String movieDbString,FilmAdapter filmAdapter, TrailerAdapter trailerAdapter, ReviewAdapter reviewAdapter){
        String urlString = movieDbString;
        if(urlString == null || TextUtils.isEmpty(urlString))
            return null;

        URL filmRequestURL = null;
        try{
            filmRequestURL = new URL(urlString);
        }
        catch(MalformedURLException mlURLex){
            Log.e(MainActivity.class.getSimpleName(),mlURLex.getMessage());
        }
        //Set the rvFilms adapter equal to filmToDisplay


        String urlData = null;
        try{
            //Start a new Thread and get the HTTP response
            urlData =  MainActivity.getResponseFromMovieDb(filmRequestURL);
            // TODO AWESOME  You're doing your network requests on a background thread
        }catch(IOException ioEx){
            Log.e(MainActivity.class.getSimpleName(), ioEx.getMessage());
            return null;
        }


        if(urlData != null && !urlData.equals("")){
            //Declare necessary JSON variables
            JSONObject results = null;
            JSONArray jsonData = null;
            try{
                //Set results to a new JSONObject, passing in the String into the constructor
                results = new JSONObject(urlData);
                //Set films equal to the JSON Array designated by the String results
                jsonData = results.getJSONArray("results");
                // TODO SUGGESTION Move string literals to strings.xml or constants
                //Call the setfilms method on the filmToDisplay filmadapter object, passing in the films JSONArray
                if(filmAdapter != null){
                    filmAdapter.setFilms(jsonData);
                }else if(trailerAdapter != null){
                    trailerAdapter.setTrailerList(jsonData);
                }else if(reviewAdapter != null){
                    reviewAdapter.setReviewList(jsonData);
                }

                //Set the rvFilms recyclerview Object equal to the RecyclerView designated by rv_films_to_display

                //TODO SUGGESTION Much of this heavy lifting could be done in the background thread rather than the UI thread: performance, UX.
            }catch(JSONException jsonEx){
                Log.e(MainActivity.class.getSimpleName(), jsonEx.getMessage());
            }
        }
        else{
            return null;
        }

        return urlData;
    }

}
