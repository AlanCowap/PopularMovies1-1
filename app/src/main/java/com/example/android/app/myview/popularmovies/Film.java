package com.example.android.app.myview.popularmovies;

import android.os.Parcel;
import android.os.Parcelable;



public class Film implements Parcelable{

    private String filmName;
    private String filmPosterPath;
    private String filmRating;
    private String filmOverview;
    private String filmReleaseDate;
    private String filmMovieDBId;
    public static final String FILM_POSTER_PATH = "poster_path";
    public static final String FILM_VOTE_AVERAGE = "vote_average";
    public static final String FILM_RELEASE_DATE = "release_date";
    public static final String FILM_OVERVIEW = "overview";
    public static final String ORIGINAL_TITLE = "original_title";
    public static final String FILM_MOVIEDB_ID = "id";

    public Film(String filmName, String filmPosterPath, String filmRating, String filmOverview, String filmReleaseDate, String filmMovieDBId){
        super();
        this.setFilmName(filmName);
        this.setFilmPosterPath(filmPosterPath);
        this.setFilmOverview(filmOverview);
        this.setFilmRating(filmRating);
        this.setFilmReleaseDate(filmReleaseDate);
        this.setFilmMovieDBId(filmMovieDBId);
    }

    protected Film(Parcel in) {
        filmName = in.readString();
        filmPosterPath = in.readString();
        filmRating = in.readString();
        filmOverview = in.readString();
        filmReleaseDate = in.readString();
        filmMovieDBId = in.readString();
    }

    public static final Creator<Film> CREATOR = new Creator<Film>() {
        @Override
        public Film createFromParcel(Parcel in) {
            return new Film(in);
        }

        @Override
        public Film[] newArray(int size) {
            return new Film[size];
        }
    };

    public void setFilmPosterPath(String filmPosterPath) {
        this.filmPosterPath = filmPosterPath;
    }

    public void setFilmRating(String filmRating) {
        this.filmRating = filmRating;
    }

    public void setFilmOverview(String filmOverview) {
        this.filmOverview = filmOverview;
    }

    public void setFilmReleaseDate(String filmReleaseDate) {
        this.filmReleaseDate = filmReleaseDate;
    }
    public void setFilmMovieDBId(String filmMovieDBId) {
        this.filmMovieDBId = filmMovieDBId;
    }
    public void setFilmName(String filmName) {
        this.filmName = filmName;
    }

    public String getFilmPosterPath() {
        return filmPosterPath;
    }

    public String getFilmRating() {
        return filmRating;
    }

    public String getFilmOverview() {
        return filmOverview;
    }

    public String getFilmName() {
        return filmName;
    }
    public String getFilmReleaseDate() {

        return filmReleaseDate;

    }
    public String getFilmMovieDBId() {
        return filmMovieDBId;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(filmName);
        dest.writeString(filmPosterPath);
        dest.writeString(filmRating);
        dest.writeString(filmOverview);
        dest.writeString(filmReleaseDate);
        dest.writeString(filmMovieDBId);
    }
}
