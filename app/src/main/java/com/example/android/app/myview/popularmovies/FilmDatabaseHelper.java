package com.example.android.app.myview.popularmovies;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;


public class FilmDatabaseHelper extends SQLiteOpenHelper{
    private static final String FILM_DATABASE_NAME = "filmDatabase.db";
    private static final int DATABASE_VERSION = 3;

    public FilmDatabaseHelper(Context con){
        super(con,FILM_DATABASE_NAME,null, DATABASE_VERSION );
    }
    @Override
    public void onCreate(SQLiteDatabase db) {
        final String SQL_FILM_TABLE_CREATE = "CREATE TABLE " +
                FilmDatabaseContract.FilmDatabase.FILM_TABLE_NAME + "(" +
                FilmDatabaseContract.FilmDatabase._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                FilmDatabaseContract.FilmDatabase.COLUMN_FILM_TITLE + " TEXT NOT NULL," +
                FilmDatabaseContract.FilmDatabase.COLUMN_FILM_RATING + " TEXT NOT NULL," +
                FilmDatabaseContract.FilmDatabase.COLUMN_FILM_RELEASE_DATE + " TEXT NOT NULL," +
                FilmDatabaseContract.FilmDatabase.COLUMN_FILM_OVERVIEW + " TEXT NOT NULL," +
                FilmDatabaseContract.FilmDatabase.COLUMN_FILM_POSTER_PATH + " TEXT NOT NULL," +
                FilmDatabaseContract.FilmDatabase.COLUMN_FILM_MOVIEDB_ID + " TEXT NOT NULL" +
                ");";

        db.execSQL(SQL_FILM_TABLE_CREATE);
    }



    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + FilmDatabaseContract.FilmDatabase.FILM_TABLE_NAME);
        onCreate(db);
    }
}
