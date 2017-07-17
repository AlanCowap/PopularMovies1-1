package com.example.android.app.myview.popularmovies;


import android.net.Uri;
import android.provider.BaseColumns;

import java.net.URI;

public class FilmDatabaseContract {
    public static final String CONTENT_PROVIDER_AUTHORITY = "com.example.android.app.myview.popularmovies";
    public static final Uri ALL_FILMS_URI = Uri.parse("content://" + CONTENT_PROVIDER_AUTHORITY +  "/" + FilmDatabase.FILM_TABLE_NAME);
    private static final String DELIM = "/";
    private FilmDatabaseContract(){}

    public static Uri buildResolverUri(String column, String extraParams){
        Uri resolverUri = null;
        if(column != null){
            resolverUri = Uri.parse(ALL_FILMS_URI.toString() + DELIM + column + DELIM + extraParams);
            return  resolverUri;
        }else{
            resolverUri = Uri.parse(ALL_FILMS_URI.toString() + DELIM + extraParams);
            return  resolverUri;
        }
    }


    public static class FilmDatabase implements BaseColumns{
        public static final String FILM_TABLE_NAME = "films";
        public static final String COLUMN_FILM_TITLE = "FILM_TITLE";
        public static final String COLUMN_FILM_RATING = "FILM_RATING";
        public static final String COLUMN_FILM_RELEASE_DATE = "FILM_RELEASE_DATE";
        public static final String COLUMN_FILM_OVERVIEW = "FILM_OVERVIEW";
        public static final String COLUMN_FILM_POSTER_PATH = "FILM_POSTER_PATH";
        public static final String COLUMN_FILM_MOVIEDB_ID = "FILM_MOVIEDB_ID";

    }

}
