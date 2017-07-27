package com.example.android.app.myview.popularmovies;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import java.net.MalformedURLException;
import java.net.URL;

public class FilmDetailActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<String>, TrailerAdapter.TrailerClickHandler, ReviewAdapter.ReviewClickHandler {
    //Declare ui variables
    private TextView tvTitle;
    private ImageView ivPoster;
    private TextView tvReleaseDate;
    private TextView tvOverview;
    private TextView tvRating;
    private TextView tvErrorBox;
    private RecyclerView rvTrailers;
    private RecyclerView rvReviews;
    private SQLiteDatabase filmDatabase;
    private Film chosenFilm;
    private Toast displayToast;
    private static final String MOVIE_DB_API = "api_key=" + BuildConfig.MY_MOVIEDB_API_KEY;
    private static final String sqlQueryTag ="\"";
    private static final String sqlQueryStartTag = "=\"";
    private static final String TRAILER_URL_STRING = "TRAILER_URL";
    private static final int LOADER_ID = 10012;
    private static final int TRAILER_ID = 1;
    private static final int REVIEW_ID = 2;
    private static final String YOUTUBE_URI = "vnd.youtube:";
    private static final String LOADER_CHOICE_ID = "loader_choice_id";
    private TrailerAdapter trailerAdapter;
    private ReviewAdapter reviewAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //TODO SUGGESTION Consider adding Up navigation to return to your MainActivity from this Activity, better UX.
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_film_detail);
        //Get the intent that what used to start this activity
        Intent starterIntent = getIntent();
        //Declare a new JSONObject and set to null
        Film filmDetails = null;
        trailerAdapter = new TrailerAdapter(this);
        reviewAdapter = new ReviewAdapter(this);
        tvErrorBox = (TextView) findViewById(R.id.tv_detail_network_error);
        tvErrorBox.setVisibility(View.INVISIBLE);
        ActionBar test = getSupportActionBar();
        test.setDisplayHomeAsUpEnabled(true);
        //Instantiate ui elements
        tvTitle = (TextView) findViewById(R.id.tv_film_detail_title);
        tvOverview = (TextView) findViewById(R.id.tv_film_detail_overview);
        tvRating = (TextView) findViewById(R.id.tv_film_detail_rating);
        tvReleaseDate = (TextView) findViewById(R.id.tv_film_detail_release_date);
        ivPoster = (ImageView) findViewById( R.id.iv_film_detail_poster);
        FilmDatabaseHelper filmHelper = new FilmDatabaseHelper(this);
        filmDatabase = filmHelper.getWritableDatabase();
        //Try to set the filmDetails variable to a new JSONObject, created using the string that was stored in to starting intent
        // filmDetails = new JSONObject(starterIntent.getStringExtra(Intent.EXTRA_TEXT));
        if(starterIntent.getParcelableExtra(Intent.EXTRA_TEXT) instanceof  Film){
            filmDetails = starterIntent.getParcelableExtra(Intent.EXTRA_TEXT);
        }
        //If filmDetails is null, and is an instance of a JSON Object, fill the UI Elements with the releated content
        if(filmDetails != null && filmDetails instanceof  Film){
            this.chosenFilm = filmDetails;
            String title = filmDetails.getFilmName();
            //If the length of the title is greater that 45 characters, shorten to 45 and append a series of ellipses
            if(title.length() > 45) {
                title = title.substring(0,45);
                title = title + getResources().getString(R.string.title_length_check);
            }
            tvTitle.setText(title);
            String filmPoster = filmDetails.getFilmPosterPath();
            int width = getResources().getDisplayMetrics().widthPixels;
            tvRating.setText(filmDetails.getFilmRating());
            tvReleaseDate.setText(filmDetails.getFilmReleaseDate());
            String posterPath =  getResources().getString(R.string.poster_path);
            //If filmposter does not contain a ., use the default no poster image from drawables, found at http://www.pinsdaddy.com/no-image-available-icon_nquACkOxV*TJt*l2puUBRhlP12hWM2e9JtVGM0jwJfA/
            if(!filmPoster.contains(".")){
                //Picasso.with(this).load(R.drawable.no_poster_image).resize(width/2,0).into(ivPoster);
                //Otherwise use the film poster from that location
            }else Picasso.with(this).load(posterPath+ filmPoster).resize(width/2,0).into(ivPoster);
            tvOverview.setText(filmDetails.getFilmOverview());
            ivPoster.setContentDescription(getResources().getString(R.string.iv_content_description) + filmPoster);
        }else {
            //If there is an issue, display a toast to the user with advice to ensure they have a stable internet connection
            Toast errorToast = Toast.makeText(this, getResources().getString(R.string.film_json_not_found), Toast.LENGTH_LONG);
            errorToast.show();
        }

        getURL(TRAILER_ID);
        getURL(REVIEW_ID);
    }

    //Inflate the menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if(this.isFavourite()){
            getMenuInflater().inflate(R.menu.detailremove, menu);
        }else{
            getMenuInflater().inflate(R.menu.detailmenu, menu);
        }
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == R.id.mn_details_favourite){
            Log.d(FilmDetailActivity.class.getSimpleName(), chosenFilm.getFilmMovieDBId());
            this.addNewFavourite(
                    chosenFilm.getFilmName(),
                    chosenFilm.getFilmOverview(),
                    chosenFilm.getFilmRating(),
                    chosenFilm.getFilmReleaseDate(),
                    chosenFilm.getFilmPosterPath(),
                    chosenFilm.getFilmMovieDBId()
            );

        }else if(item.getItemId() == R.id.mn_details_remove){
            this.removeFavourite(chosenFilm.getFilmName());
        }
        else {
            return super.onOptionsItemSelected(item);
        }
        return true;
    }


    private void addNewFavourite(String filmName, String filmOverview, String filmRating, String filmReleaseDate, String filmPosterPath, String filmMovieDbId){
        ContentValues mapFilms = new ContentValues();
        mapFilms.put(FilmDatabaseContract.FilmDatabase.COLUMN_FILM_TITLE, filmName);
        mapFilms.put(FilmDatabaseContract.FilmDatabase.COLUMN_FILM_OVERVIEW, filmOverview);
        mapFilms.put(FilmDatabaseContract.FilmDatabase.COLUMN_FILM_RATING, filmRating);
        mapFilms.put(FilmDatabaseContract.FilmDatabase.COLUMN_FILM_RELEASE_DATE, filmReleaseDate);
        mapFilms.put(FilmDatabaseContract.FilmDatabase.COLUMN_FILM_POSTER_PATH, filmPosterPath);
        mapFilms.put(FilmDatabaseContract.FilmDatabase.COLUMN_FILM_MOVIEDB_ID, filmMovieDbId);
        displayToast = Toast.makeText(this, filmName + getResources().getString(R.string.details_added_to_favourites), Toast.LENGTH_LONG);
        displayToast.show();
        Uri filmUri = getContentResolver().insert(FilmDatabaseContract.ALL_FILMS_URI,mapFilms);
        //return filmDatabase.insert(FilmDatabaseContract.FilmDatabase.FILM_TABLE_NAME, null , mapFilms);
    }


    private boolean isFavourite(){
        Cursor filmQuery = null;
        filmQuery = getContentResolver().query(FilmDatabaseContract.ALL_FILMS_URI,
                null,
                FilmDatabaseContract.FilmDatabase.COLUMN_FILM_TITLE + "=" +this.sqlQueryTag + this.chosenFilm.getFilmName() + this.sqlQueryTag,
                null,
                null
                );
        if(filmQuery.getCount() > 0){
            return true;
        }
        return false;
    }
    private void removeFavourite(String filmName){
        getContentResolver().delete(FilmDatabaseContract.ALL_FILMS_URI, FilmDatabaseContract.FilmDatabase.COLUMN_FILM_TITLE + sqlQueryStartTag + filmName + sqlQueryTag, null);
    }

    public URL getURL(int sortCheck){
        Context con = this.getBaseContext();
        String film = null;
        switch (sortCheck){
            case TRAILER_ID: film =  con.getResources().getString(R.string.trailer_request_start)+chosenFilm.getFilmMovieDBId() + con.getResources().getString(R.string.trailer_request_end);; break;
            case REVIEW_ID: film =  con.getResources().getString(R.string.trailer_request_start)+chosenFilm.getFilmMovieDBId() + con.getResources().getString(R.string.review_request_end);; break;
        }

        String api = MOVIE_DB_API;
        //Build the URI from the string.xml value determined by the passed in int variable

        Uri filmRequest = Uri.parse(film +api ).buildUpon().build();
        //TODO SUGGESTION Consider using Uri.Builder to create URIs because it’s less error prone than Uri.parse
        URL trailerRequestURL = null;
        //Try to build the URL from the previously built URI converted to string
        try{
            trailerRequestURL = new URL(filmRequest.toString());
        }catch(MalformedURLException urlEx){
            Log.e(MainActivity.class.getSimpleName(), urlEx.getMessage());
        }

        Bundle requestBundle = new Bundle();//
        requestBundle.putString(TRAILER_URL_STRING, trailerRequestURL.toString());
        switch (sortCheck){
            case TRAILER_ID:requestBundle.putInt(LOADER_CHOICE_ID, TRAILER_ID); break;
            case REVIEW_ID:requestBundle.putInt(LOADER_CHOICE_ID, REVIEW_ID);break;
        }

        LoaderManager ldManage = getSupportLoaderManager();

        Loader<String> filmLoader = ldManage.getLoader(LOADER_ID);
        if(filmLoader == null){
            ldManage.initLoader(LOADER_ID,requestBundle,this).forceLoad();
        }
        else{
            ldManage.restartLoader(LOADER_ID,requestBundle,this).forceLoad();
        }
        return trailerRequestURL;
        //TODO SUGGESTION Consider moving this methods functionality to onPreExecute() of your AsyncTask - or call it from there.
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
            }

            @Override
            public String loadInBackground() {
                String trailerData = null;
                int sortCheck = args.getInt(LOADER_CHOICE_ID);
                switch (sortCheck){
                    case TRAILER_ID:trailerData = GeneralUtils.handleMovieDBRequests(args.getString(TRAILER_URL_STRING),null, trailerAdapter, null);break;
                    case REVIEW_ID: trailerData = GeneralUtils.handleMovieDBRequests(args.getString(TRAILER_URL_STRING),null, null, reviewAdapter);;break;
                }

                return trailerData;
            }
        };
    }

    @Override
    public void onLoadFinished(Loader<String> loader, String data) {
        if(data != null && !data.equals(GeneralUtils.EMPTY_STRING)){
             //TODO SUGGESTION Much of this heavy lifting could be done in the background thread rather than the UI thread: performance, UX.
            if(rvTrailers != null) {
                buildReviewRv();
            }else if(rvTrailers == null){
                buildTrailerRv();
            }
            buildReviewRv();
        }
        else{
            if(rvTrailers != null) {
                rvTrailers.setVisibility(View.INVISIBLE);

            }
            tvErrorBox.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onLoaderReset(Loader<String> loader) {

    }

    private void buildTrailerRv(){
        //Declare necessary JSON variables
        //Set results to a new JSONObject, passing in the String into the constructor
        // TODO SUGGESTION Move string literals to strings.xml or constants
        //Call the setfilms method on the filmToDisplay filmadapter object, passing in the films JSONArray
        //Set the rvFilms recyclerview Object equal to the RecyclerView designated by rv_films_to_display
        rvTrailers = (RecyclerView) findViewById(R.id.rv_trailers);
        tvErrorBox.setVisibility(View.INVISIBLE);
        rvTrailers.setVisibility(View.VISIBLE);
        //Get the current orientation
        LinearLayoutManager trailerList = null;
        //If its in portrait, set the Grid layout manager to use 2 items per row, it its landscape, use 4 items per row
        trailerList = new LinearLayoutManager(getBaseContext());
        //Set the rvFilms layoutManager equal to filmGrid
        rvTrailers.setLayoutManager(trailerList);
        rvTrailers.setHasFixedSize(true);
        //Set the rvFilms adapter equal to filmToDisplay
        rvTrailers.setAdapter(trailerAdapter);
    }
    private void buildReviewRv(){
        //Declare necessary JSON variables
        //Set results to a new JSONObject, passing in the String into the constructor
        // TODO SUGGESTION Move string literals to strings.xml or constants
        //Call the setfilms method on the filmToDisplay filmadapter object, passing in the films JSONArray
        //Set the rvFilms recyclerview Object equal to the RecyclerView designated by rv_films_to_display
        rvReviews = (RecyclerView) findViewById(R.id.rv_reviews);
        tvErrorBox.setVisibility(View.INVISIBLE);
        rvReviews.setVisibility(View.VISIBLE);
        //Get the current orientation
        LinearLayoutManager trailerList = null;
        //If its in portrait, set the Grid layout manager to use 2 items per row, it its landscape, use 4 items per row
        trailerList = new LinearLayoutManager(getBaseContext());
        //Set the rvFilms layoutManager equal to filmGrid
        rvReviews.setLayoutManager(trailerList);
        rvReviews.setHasFixedSize(true);
        //Set the rvFilms adapter equal to filmToDisplay
        rvReviews.setAdapter(reviewAdapter);
    }
    @Override
    public void onClickListen(int trailerArrayPosition) {
        Trailer chosenTrailer = this.trailerAdapter.getSpecificTrailer(trailerArrayPosition);
        Intent launchTrailer = new Intent(Intent.ACTION_VIEW, Uri.parse(YOUTUBE_URI + chosenTrailer.getTrailerUri()));
        startActivity(launchTrailer);
    }

    @Override
    public void onClickReviewListen(int reviewArrayPosition) {
        Review chosenReview = this.reviewAdapter.getSpecificReview(reviewArrayPosition);
        Intent viewReview = new Intent(Intent.ACTION_VIEW, Uri.parse(chosenReview.getReviewURL()));
        startActivity(viewReview);
    }
}
