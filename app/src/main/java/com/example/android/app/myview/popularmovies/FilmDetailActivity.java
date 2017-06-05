package com.example.android.app.myview.popularmovies;

import android.content.Context;
import android.content.Intent;
import android.media.Image;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

public class FilmDetailActivity extends AppCompatActivity {
    //Declare ui variables
    private TextView tvTitle;
    private ImageView ivPoster;
    private TextView tvReleaseDate;
    private TextView tvOverview;
    private TextView tvRating;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_film_detail);
        //Get the intent that what used to start this activity
        Intent starterIntent = getIntent();
        //Declare a new JSONObject and set to null
        JSONObject filmDetails = null;
        //Instantiate ui elements
        tvTitle = (TextView) findViewById(R.id.tv_film_detail_title);
        tvOverview = (TextView) findViewById(R.id.tv_film_detail_overview);
        tvRating = (TextView) findViewById(R.id.tv_film_detail_rating);
        tvReleaseDate = (TextView) findViewById(R.id.tv_film_detail_release_date);
        ivPoster = (ImageView) findViewById( R.id.iv_film_detail_poster);
        //Try to set the filmDetails variable to a new JSONObject, created using the string that was stored in to starting intent
        try {
            filmDetails = new JSONObject(starterIntent.getStringExtra(Intent.EXTRA_TEXT));

        }catch(JSONException jsEx){
            Log.e(FilmDetailActivity.class.getSimpleName(),jsEx.getMessage());
        }
        //If filmDetails is null, and is an instance of a JSON Object, fill the UI Elements with the releated content
        if(filmDetails != null && filmDetails instanceof  JSONObject){
            try {
                String title = filmDetails.getString("original_title");
                //If the length of the title is greater that 45 characters, shorten to 45 and append a series of ellipses
                if(title.length() > 45) {
                    title = title.substring(0,45);
                    title = title + getResources().getString(R.string.title_length_check);
                }
                tvTitle.setText(title);
                String filmPoster = filmDetails.getString("poster_path");
                int width = getResources().getDisplayMetrics().widthPixels;
                tvRating.setText(filmDetails.getString("vote_average"));
                tvReleaseDate.setText(filmDetails.getString("release_date"));
                String posterPath =  getResources().getString(R.string.poster_path);
                //If filmposter does not contain a ., use the default no poster image from drawables, found at http://www.pinsdaddy.com/no-image-available-icon_nquACkOxV*TJt*l2puUBRhlP12hWM2e9JtVGM0jwJfA/
                if(!filmPoster.contains(".")){
                    //Picasso.with(this).load(R.drawable.no_poster_image).resize(width/2,0).into(ivPoster);
                    //Otherwise use the film poster from that location
                }else Picasso.with(this).load(posterPath+ filmPoster).resize(width/2,0).into(ivPoster);
                tvOverview.setText(filmDetails.getString("overview"));
            }catch(JSONException jsEx){
                Log.e(FilmDetailActivity.class.getSimpleName(),jsEx.getMessage());
            }
        }else {
            //If there is an issue, display a toast to the user with advice to ensure they have a stable internet connection
            Toast errorToast = Toast.makeText(this, getResources().getString(R.string.film_json_not_found), Toast.LENGTH_LONG);
            errorToast.show();
        }
    }
}
