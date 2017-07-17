package com.example.android.app.myview.popularmovies;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import android.view.View;
import android.widget.Spinner;

import android.widget.TextView;
import android.widget.Toast;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Scanner;

public class MainActivity extends AppCompatActivity implements FilmAdapter.FilmClickListener{
    //Declare variables to hold ui elements
    private FilmAdapter filmToDisplay;
    private RecyclerView rvFilms;
    private FilmHandler filmRunner;
    private Toast errorMessageToast;
    private Spinner sortByMenu;
    private TextView tvErrorBox;
    //Declare constant to hold the key for our state restore bundle
    private static final String SORT_INSTANCE = "sort_type";
    private static final String MOVIE_DB_API = BuildConfig.MY_MOVIEDB_API_KEY;
    //TODO Excellent You're not storing your key in your java code, however instructions on how & where to put the key would be helpful!
    //TODO SUGGESTION Consider storing the api key only, i.e. excluding other params like "api_key="
    //Private int to store the variable used in the switch statements to determine landscape/portrait mode
    private int sortOption;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Set layout file for main activity
        setContentView(R.layout.activity_main);
        //Initialise new FilmHandler object
        filmRunner = new FilmHandler(this.getBaseContext());
        //Declare new FilmAdapter
        filmToDisplay = new FilmAdapter(this);
        tvErrorBox = (TextView) findViewById(R.id.tv_network_error);
        tvErrorBox.setVisibility(View.INVISIBLE);
        //Check if there is a bundle for savedinstance state, if there is, get the sortOption stored within, else just
        if( savedInstanceState != null){
            this.sortOption = savedInstanceState.getInt(SORT_INSTANCE);
            //TODO SUGGESTION Always check if your key is in the Bundle before attempting to retrieve it
            this.getMovies(sortOption);
        }else{
            //If there isnt a bundle, display a toast telling the user the films are being loaded
            Toast displayToast = Toast.makeText(this, getResources().getString(R.string.loading_films), Toast.LENGTH_LONG);
            displayToast.show();
            this.getMovies(sortOption);
            //TODO SUGGESTION Consider starting the background task first and then displaying the Toast = better UX if its loaded ASAP
        }
    }
    //TODO-2 REQUIREMENT "App does not crash, force close, freeze, or otherwise function abnormally on any targeted device"
    //TODO-2 To reproduce error: (Uninstall App) > Install App > Start App > Turn off data > Change Sort order > rotate device
    //TODO-2 When data connection is lost the sort options do not function, nor do they advise user of same.
    //TODO-2 When data connection is lost the UI can remain blank for 30 seconds or more before error is displayed


    //Override the onsaveinstancestate method and store the sortOption
    @Override
    public void onSaveInstanceState(Bundle savedInstanceState){
        savedInstanceState.putInt(SORT_INSTANCE, this.sortOption);
        super.onSaveInstanceState(savedInstanceState);
    }


    //Call the filmRunner execute method, passing in the result from the called getURL method
    public void getMovies(int sortParam){
        this.filmRunner.execute(this.getURL(sortParam));
    }
    //Inflate the menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        //If the item selected is the Refresh button, check if the current list is most popular or highest rated, and call the getMovies with that option
        if(item.getItemId() == R.id.mn_menu_item ){
            filmRunner = new FilmHandler(this.getBaseContext());
            Toast displayToast = null;
            switch(sortOption){
                case 0 :  displayToast = Toast.makeText(this, getResources().getString(R.string.refreshing_list), Toast.LENGTH_LONG);displayToast.show();getMovies(0);break;
                case 1 :  displayToast = Toast.makeText(this, getResources().getString(R.string.refreshing_list), Toast.LENGTH_LONG);displayToast.show();getMovies(1);break;
            }
        }
        //If the item selected is sort by highest rated, call the getMovies method passing in that option
        if(item.getItemId() == R.id.mn_sort_by_highest_rated){
            Toast displayToast = Toast.makeText(this, getResources().getString(R.string.sorting_by_highest), Toast.LENGTH_LONG);
            displayToast.show();
            filmRunner = new FilmHandler(this.getBaseContext());
            sortOption = 0;
            //TODO SUGGESTION Considering using a named constant, rather than an arbitrary int value, more readable & less error prone.
            getMovies(sortOption);
        }
        //If the item selected is sort by most popular, call the getMovies method passing in that option
        if(item.getItemId() == R.id.mn_sort_by_most_popular){
            Toast displayToast = Toast.makeText(this, getResources().getString(R.string.sorting_by_most_popular), Toast.LENGTH_LONG);
            displayToast.show();
            filmRunner = new FilmHandler(this.getBaseContext());
            sortOption = 1;
            getMovies(sortOption);
            //TODO SUGGESTION Considering refactoring this method: e.g. a switch statement, or if-else if-else; move duplicated code to a new method as required
        }
        return super.onOptionsItemSelected(item);
    }


    public URL getURL(int sortCheck){
        Context con = this.getBaseContext();
        String film = null;
        String api = MOVIE_DB_API;
        //Build the URI from the string.xml value determined by the passed in int variable
        switch(sortCheck){
            case 1: film = con.getResources().getString(R.string.film_request); break;
            case 0: film = con.getResources().getString(R.string.film_request_rating); break;
        }

        Uri filmRequest = Uri.parse(film +api ).buildUpon().build();
        //TODO SUGGESTION Consider using Uri.Builder to create URIs because it’s less error prone than Uri.parse
        URL filmRequestURL = null;
        //Try to build the URL from the previously built URI converted to string
        try{
            filmRequestURL = new URL(filmRequest.toString());
        }catch(MalformedURLException urlEx){
            Log.e(MainActivity.class.getSimpleName(), urlEx.getMessage());
        }
        return filmRequestURL;
        //TODO SUGGESTION Consider moving this methods functionality to onPreExecute() of your AsyncTask - or call it from there.
    }


    public static String getResponseFromMovieDb(URL filmURL) throws IOException{
        //Open a new HTTP connection using the created URL
        HttpURLConnection filmCon = (HttpURLConnection) filmURL.openConnection();
        try{

            InputStream incoming = filmCon.getInputStream();
            Scanner scan = new Scanner(incoming);
            scan.useDelimiter("\\A");
            boolean hasInput = scan.hasNext();
            if(hasInput){
                return scan.next();
            }else{
                return null;
            }
        }catch(Exception ex){
            Log.e(MainActivity.class.getSimpleName(), ex.getMessage());
            return null;
        }
        finally{
            filmCon.disconnect();
        }
    }

    @Override
    public void onClickListen(int filmArrayPosition) {
        String chosenFilm = null;
        //Listen to clicks, and get the JSONObject associated with the clicked ViewHolder
        chosenFilm = this.filmToDisplay.getSpecificFilm(filmArrayPosition).toString();
        //If there is a JSONObject, launch a new Intent, with the JSONOBject converted to String using putExtra
        if(chosenFilm !=null){
            //Put the json object into the new activity and launch the new activity
            Intent detailsIntent = new Intent(MainActivity.this, FilmDetailActivity.class );
            detailsIntent.putExtra(Intent.EXTRA_TEXT, chosenFilm);
            startActivity(detailsIntent);
        }else{
            //Display a toast if there is no JSON OBject associated with that ViewHolder
            errorMessageToast = Toast.makeText(this,getResources().getString(R.string.film_json_not_found),Toast.LENGTH_LONG );
            //TODO SUGGESTION This Toast is never shown, .show()
        }

    }

    public class FilmHandler extends AsyncTask<URL, Void, String>{
        //TODO SUGGESTION Consider using AsyncTaskLoader rather than AsyncTask, for the many benefits it brings as discussed in class

        private Context cont;

        public FilmHandler(Context con){
            this.cont = con;
        }
        @Override
        protected String doInBackground(URL... urls) {

            URL filmUrls = urls[0];
            String filmResults = null;
            try{
                //Start a new Thread and get the HTTP response
                filmResults = MainActivity.getResponseFromMovieDb(filmUrls);
                // TODO AWESOME  You're doing your network requests on a background thread
            }catch(IOException ioEx){
                Log.e(MainActivity.class.getSimpleName(), ioEx.getMessage());
            }
            return filmResults;
        }

        @Override
        protected void onPostExecute(String s) {
            //When the thread has finished executing, and the response is not null or an empty string
            if(s != null && !s.equals("")){
                //Declare necessary JSON variables
                JSONObject results = null;
                JSONArray films = null;
                try{
                    //Set results to a new JSONObject, passing in the String into the constructor
                    results = new JSONObject(s);
                    //Set films equal to the JSON Array designated by the String results
                    films = results.getJSONArray("results");
                    // TODO SUGGESTION Move string literals to strings.xml or constants
                    //Call the setfilms method on the filmToDisplay filmadapter object, passing in the films JSONArray
                    filmToDisplay.setFilms(films);
                    //Set the rvFilms recyclerview Object equal to the RecyclerView designated by rv_films_to_display
                    rvFilms = (RecyclerView) findViewById(R.id.rv_films_to_display);
                    tvErrorBox.setVisibility(View.INVISIBLE);
                    rvFilms.setVisibility(View.VISIBLE);
                    //Get the current orientation
                    int orient = cont.getResources().getConfiguration().orientation;
                    GridLayoutManager filmGrid = null;
                    //If its in portrait, set the Grid layout manager to use 2 items per row, it its landscape, use 4 items per row
                    switch(orient){
                        case 1 : filmGrid = new GridLayoutManager(getBaseContext(), 2); break;
                        case 2 : filmGrid = new GridLayoutManager(getBaseContext(), 4); break;
                    }
                    //Set the rvFilms layoutManager equal to filmGrid
                    rvFilms.setLayoutManager(filmGrid);
                    rvFilms.setHasFixedSize(true);
                    //Set the rvFilms adapter equal to filmToDisplay
                    rvFilms.setAdapter(filmToDisplay);
                    //TODO SUGGESTION Much of this heavy lifting could be done in the background thread rather than the UI thread: performance, UX.
                }catch(JSONException jsonEx){
                    Log.e(MainActivity.class.getSimpleName(), jsonEx.getMessage());
                }
            }
            else{
                Log.d(MainActivity.class.getSimpleName(), "test");
                if(rvFilms != null) {
                    rvFilms.setVisibility(View.INVISIBLE);
                }
                tvErrorBox.setVisibility(View.VISIBLE);
            }
            super.onPostExecute(s);
        }
    }

}
