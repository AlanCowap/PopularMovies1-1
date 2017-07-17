package com.example.android.app.myview.popularmovies;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteTransactionListener;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;


public class FilmDBContentProvider extends ContentProvider {

    private FilmDatabaseHelper filmDbHelper;

    private static UriMatcher staticFilmUriMatcher = buildUriMatcher();
    /*#
    * The standard for these URI matcher constants is as follows,
    * ints that start with 2, refer to select statements
    * ints that start with 3, refer to insert statements,
    * ints that start with 4, refer to update statements,
    * ints that start with 5, refer to delete statements
    * */
    private static final int REQUEST_ALL_FILMS = 200;
    private static final int REQUEST_SELECTED_FILM = 201;
    private static final int ADD_SELECTED_FILM = 301;
    private static final int REMOVE_SELECTED_FILM = 501;
    private static final String INVALID_URI_TEXT = "Invalid URI: ";
    private static final String FAILED_INSERT_TEXT = "Failed to Insert Selected film: ";
    private static final String QUERY_BY_NAME = FilmDatabaseContract.FilmDatabase.FILM_TABLE_NAME + "/*";
    public static UriMatcher buildUriMatcher(){
        UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        uriMatcher.addURI(FilmDatabaseContract.CONTENT_PROVIDER_AUTHORITY, FilmDatabaseContract.FilmDatabase.FILM_TABLE_NAME, REQUEST_ALL_FILMS);
        uriMatcher.addURI(FilmDatabaseContract.CONTENT_PROVIDER_AUTHORITY, QUERY_BY_NAME, REQUEST_SELECTED_FILM);
        return uriMatcher;
    }



    @Override
    public boolean onCreate() {
        Context con = getContext();
        filmDbHelper = new FilmDatabaseHelper(con);
        return true;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        SQLiteDatabase filmDatabase = filmDbHelper.getReadableDatabase();
        int matchCheck = staticFilmUriMatcher.match(uri);
        Cursor filmCursor = null;
        switch (matchCheck){
            case REQUEST_ALL_FILMS: filmCursor = filmDatabase.query(
                    FilmDatabaseContract.FilmDatabase.FILM_TABLE_NAME,
                    projection,
                    selection,
                    selectionArgs,
                    null,
                    null,
                    sortOrder
            );
                break;
            default: throw new UnsupportedOperationException(INVALID_URI_TEXT + uri.toString());
        }
        return filmCursor;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        return null;
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {
        SQLiteDatabase filmDatabase = filmDbHelper.getWritableDatabase();
        Uri returnFilmUri = null;
        int matchCheck = staticFilmUriMatcher.match(uri);
        switch (matchCheck){
            case REQUEST_ALL_FILMS : long checkInsert = filmDatabase.insert(FilmDatabaseContract.FilmDatabase.FILM_TABLE_NAME,null,values);
                if(checkInsert > 0) {
                    returnFilmUri = ContentUris.withAppendedId(FilmDatabaseContract.ALL_FILMS_URI,checkInsert);
                }else{
                    throw new android.database.SQLException(FAILED_INSERT_TEXT + uri);
                }
                break;
            default: throw new UnsupportedOperationException(INVALID_URI_TEXT + uri.toString());
        }
        getContext().getContentResolver().notifyChange(uri,null);
        return returnFilmUri;
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        SQLiteDatabase filmDatabase = filmDbHelper.getWritableDatabase();
        int matchCheck = staticFilmUriMatcher.match(uri);
        switch (matchCheck){
            case REQUEST_ALL_FILMS: filmDatabase.delete(
                    FilmDatabaseContract.FilmDatabase.FILM_TABLE_NAME,
                    selection,
                    selectionArgs
            );
                break;
            default: throw new UnsupportedOperationException(INVALID_URI_TEXT + uri.toString());
        }

        return 0;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection, @Nullable String[] selectionArgs) {
        return 0;
    }
}
