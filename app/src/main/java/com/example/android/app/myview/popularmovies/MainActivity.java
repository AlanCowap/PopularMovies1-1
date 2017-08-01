package com.example.android.app.myview.popularmovies;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteTransactionListener;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import android.view.View;
import android.widget.Adapter;
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

public class MainActivity extends AppCompatActivity implements FilmAdapter.FilmClickListener, LoaderManager.LoaderCallbacks<String>{
    //Declare variables to hold ui elements
    private FilmAdapter filmToDisplay;
    private RecyclerView rvFilms;
    private Toast errorMessageToast;
    private TextView tvErrorBox;
    private Toast displayToast;
    //Declare constant to hold the key for our state restore bundle
    private static final String SORT_INSTANCE = "sort_type";
    private static final String MOVIE_DB_API = "api_key=" + BuildConfig.MY_MOVIEDB_API_KEY;
    private static final int SORT_BY_MOST_POPULAR = 1;
    private static final int SORT_BY_HIGHEST_RATED = 0;
    private static final int SORT_BY_FAVOURITES = 2;
    private static final String FILM_URL_STRING = "FILM_URL";
    private static final int LOADER_ID = 10011;
    private static final String PREFERENCES_SORT_ID = "MOVIES_SORT_BY";
    private static final String FILM_CURRENT_POSITION = "FILM_LAST_KNOWN_POSITION";
    private int filmCurrentPosition;
    private SQLiteDatabase filmDatabase;
    private SharedPreferences appSharedPreferences;
    //TODO Excellent You're not storing your key in your java code, however instructions on how & where to put the key would be helpful!
    //TODO SUGGESTION Consider storing the api key only, i.e. excluding other params like "api_key="
    //Private int to store the variable used in the switch statements to determine landscape/portrait mode
    private int sortOption;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Set layout file for main activity
        setContentView(R.layout.activity_main);
        //Declare new FilmAdapter
        filmToDisplay = new FilmAdapter(this);
        tvErrorBox = (TextView) findViewById(R.id.tv_network_error);
        tvErrorBox.setVisibility(View.INVISIBLE);
        this.appSharedPreferences = getPreferences(MODE_PRIVATE);
        //Declare and instantiate a filmDatabaseHelper
        FilmDatabaseHelper filmHelper = new FilmDatabaseHelper(this);
        filmDatabase = filmHelper.getWritableDatabase();
        //Check if there is a bundle for savedinstance state, if there is, get the sortOption stored within, else just
        if(savedInstanceState != null){
            //if(savedInstanceState.getBundle(SORT_INSTANCE) != null) {
            this.sortOption = savedInstanceState.getInt(SORT_INSTANCE);
            //TODO SUGGESTION Always check if your key is in the Bundle before attempting to retrieve it
            //TODO-2.1 SUGGESTION As above.
            //TODO-2.1 Be mindful that some reviewers may not appreciate commented-out code in your submission
            this.getMovies(sortOption);
            //}
        }else{
            if(appSharedPreferences.getInt(PREFERENCES_SORT_ID, -1) == -1){
                this.getMovies(sortOption);
            }
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
        int filmLastKnownPosition = 0;
        if (rvFilms != null) {
            filmLastKnownPosition = this.getCurrentRVFilmPosition();
        }
        savedInstanceState.putInt(FILM_CURRENT_POSITION,filmLastKnownPosition);
        super.onSaveInstanceState(savedInstanceState);
    }


    private int getCurrentRVFilmPosition(){
        GridLayoutManager gridLayout = (GridLayoutManager) rvFilms.getLayoutManager();
        return gridLayout.findFirstVisibleItemPosition();
    }
    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState){
        int sortInt = savedInstanceState.getInt(SORT_INSTANCE);
        this.sortOption = sortInt;
        int currentPosition = savedInstanceState.getInt(FILM_CURRENT_POSITION);
        this.filmCurrentPosition = currentPosition;
    }

    public void onResume(){
        super.onResume();
        this.sortOption = appSharedPreferences.getInt(PREFERENCES_SORT_ID, 0);
        this.filmCurrentPosition = appSharedPreferences.getInt(FILM_CURRENT_POSITION, 0);
        getMovies(sortOption);
        //TODO-4 SUGGESTION For Phase2 onwards you will be expected to maintain state per the Udacity Core App Quality Guidelines
    }

    public void onPause(){
        super.onPause();
        SharedPreferences.Editor spEditor = this.appSharedPreferences.edit();
        spEditor.putInt(PREFERENCES_SORT_ID, this.sortOption);
        int filmLastKnownPosition = 0;
        if (rvFilms != null) {
            filmLastKnownPosition = this.getCurrentRVFilmPosition();
        }
        spEditor.putInt(FILM_CURRENT_POSITION, filmLastKnownPosition);
        spEditor.commit();

    }

    //Clear toasts
    public void clearToasts(){
        if(displayToast!=null){
            displayToast.cancel();
            return;
        }
    }
    //Generate Toasts
    public void generateToast(String toastMessage){
        this.clearToasts();
        displayToast = Toast.makeText(this, toastMessage, Toast.LENGTH_LONG);
        displayToast.show();
    }
    //Call the getMovies method, that will check the passed in int, if it is equal to SORT_BY_FAVOURITES, call the loadFromFavourites method
    //Else call getUrl, passing in sortParam
    public void getMovies(int sortParam){
        if(sortParam == SORT_BY_FAVOURITES){
            loadFromFavourites();
            return;
        }
        //this.filmRunner.execute(this.getURL(sortParam));
        this.getURL(sortParam);
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

            switch(sortOption){
                case SORT_BY_HIGHEST_RATED :  generateToast(getResources().getString(R.string.refreshing_list));getMovies(SORT_BY_HIGHEST_RATED);break;
                case SORT_BY_MOST_POPULAR : generateToast(getResources().getString(R.string.refreshing_list));getMovies(SORT_BY_MOST_POPULAR);break;
                case SORT_BY_FAVOURITES : generateToast(getResources().getString(R.string.refreshing_list));getMovies(SORT_BY_FAVOURITES);break;
            }
        }
        //If the item selected is sort by highest rated, call the getMovies method passing in that option
        if(item.getItemId() == R.id.mn_sort_by_highest_rated){
            handleMenuChoice(getResources().getString(R.string.sorting_by_highest), SORT_BY_HIGHEST_RATED);
        }
        //If the item selected is sort by most popular, call the getMovies method passing in that option
        else if(item.getItemId() == R.id.mn_sort_by_most_popular) {
            handleMenuChoice(getResources().getString(R.string.sorting_by_most_popular), SORT_BY_MOST_POPULAR);
            //TODO SUGGESTION Considering refactoring this method: e.g. a switch statement, or if-else if-else; move duplicated code to a new method as required
        }
        else if(item.getItemId() == R.id.mn_sort_by_favourites){
            handleMenuChoice(getResources().getString(R.string.sorting_by_favourites), SORT_BY_FAVOURITES);
            //TODO SUGGESTION Considering using a named constant, rather than an arbitrary int value, more readable & less error prone.
        }
        return super.onOptionsItemSelected(item);
    }


    private void handleMenuChoice(String toastMessage, int choiceInt){
        generateToast(toastMessage);
        sortOption = choiceInt;
        getMovies(sortOption);
    }

    public URL getURL(int sortCheck){
        Context con = this.getBaseContext();
        String film = null;
        String api = MOVIE_DB_API;
        //Build the URI from the string.xml value determined by the passed in int variable
        switch(sortCheck){
            case SORT_BY_MOST_POPULAR: film = con.getResources().getString(R.string.film_request); break;
            case SORT_BY_HIGHEST_RATED: film = con.getResources().getString(R.string.film_request_rating); break;
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

        Bundle requestBundle = new Bundle();//
        requestBundle.putString(FILM_URL_STRING, filmRequestURL.toString());

        LoaderManager ldManage = getSupportLoaderManager();

        Loader<String> filmLoader = ldManage.getLoader(LOADER_ID);
        if(filmLoader == null){
            ldManage.initLoader(LOADER_ID,requestBundle,this).forceLoad();
        }
        else{
            ldManage.restartLoader(LOADER_ID,requestBundle,this).forceLoad();
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
        Film chosenFilm = null;
        //Listen to clicks, and get the JSONObject associated with the clicked ViewHolder
        chosenFilm = this.filmToDisplay.getSpecificListItem(filmArrayPosition);
        //If there is a JSONObject, launch a new Intent, with the JSONOBject converted to String using putExtra
        if(chosenFilm !=null){
            //Put the json object into the new activity and launch the new activity
            Intent detailsIntent = new Intent(MainActivity.this, FilmDetailActivity.class );
            detailsIntent.putExtra(Intent.EXTRA_TEXT, chosenFilm);
            startActivity(detailsIntent);
        }else{
            //Display a toast if there is no JSON OBject associated with that ViewHolder
            errorMessageToast = Toast.makeText(this,getResources().getString(R.string.film_json_not_found),Toast.LENGTH_LONG );
            errorMessageToast.show();
            //TODO SUGGESTION This Toast is never shown, .show()
        }

    }

    @Override
    public Loader<String> onCreateLoader(int id, final Bundle args) {
        return new AsyncTaskLoader<String>(this) {

            @Override
            protected void onStartLoading(){
                super.onStartLoading();
                if(args == null){
                    return;
                }
                //TODO-2.1 SUGGESTION The if and return statements are not necessary, tidy them up.
            }

            @Override
            public String loadInBackground() {
                String filmData = null;
                switch(sortOption){
                    case SORT_BY_MOST_POPULAR: filmData = GeneralUtils.handleMovieDBRequests(args.getString(FILM_URL_STRING), filmToDisplay, null, null) ;break;
                    case SORT_BY_HIGHEST_RATED: filmData = GeneralUtils.handleMovieDBRequests(args.getString(FILM_URL_STRING), filmToDisplay, null, null);break;
                    case SORT_BY_FAVOURITES: loadFromFavourites();
                }
                return filmData;
            }
        };
    }
    @Override
    public void onLoadFinished(Loader<String> loader, String data) {
        if(data != null && !data.equals(GeneralUtils.EMPTY_STRING)){
            buildFilmRV(); //TODO SUGGESTION Much of this heavy lifting could be done in the background thread rather than the UI thread: performance, UX.
        }
        else{
            if(rvFilms != null) {
                rvFilms.setVisibility(View.INVISIBLE);
            }
            tvErrorBox.setVisibility(View.VISIBLE);
            //TODO SUGGESTION This Toast is never shown, .show()
        }
    }



    @Override
    public void onLoaderReset(Loader<String> loader) {

    }



    public void loadFromFavourites(){
        Cursor filmCursor = getContentResolver().query(FilmDatabaseContract.ALL_FILMS_URI,null,null,null, FilmDatabaseContract.FilmDatabase.COLUMN_FILM_TITLE);
        //filmToDisplay = new FilmAdapter(this);
        filmToDisplay.setFilms(filmCursor);
        buildFilmRV();
    }

    private void buildFilmRV(){
        //Declare necessary JSON variables
        //Set results to a new JSONObject, passing in the String into the constructor
        // TODO SUGGESTION Move string literals to strings.xml or constants
        //Call the setfilms method on the filmToDisplay filmadapter object, passing in the films JSONArray
        //Set the rvFilms recyclerview Object equal to the RecyclerView designated by rv_films_to_display
        rvFilms = (RecyclerView) findViewById(R.id.rv_films_to_display);
        tvErrorBox.setVisibility(View.INVISIBLE);
        rvFilms.setVisibility(View.VISIBLE);
        //Get the current orientation
        int orient = getResources().getConfiguration().orientation;
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
        if(filmCurrentPosition > 0){
            rvFilms.scrollToPosition(filmCurrentPosition);
        }
    }
}
