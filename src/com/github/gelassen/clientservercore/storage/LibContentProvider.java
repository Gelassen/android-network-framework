package com.github.gelassen.clientservercore.storage;


import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;

public class LibContentProvider extends ContentProvider {

    private static final String DATABASE = "database.db";

    private ContentResolver cr;

    public static final String AUTHORITY = "com.github.gelassen.clientservercore";


    private static final UriMatcher uriMatcher = new UriMatcher(
            UriMatcher.NO_MATCH);


    private DatabaseHelper db;

    @Override
    public String getType(Uri uri) {
        switch (uriMatcher.match(uri)) {
        // "vnd.android.cursor.item/vnd." + AUTHORITY + ".item";
        // "vnd.android.cursor.dir/vnd." + AUTHORITY + ".dir";
        default:
            throw new IllegalArgumentException("Unsupported uri: " + uri);
        }
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        String table = null;
        int match = uriMatcher.match(uri);
//        switch (match) {
//            // set up: table = <value>
//        default:
//            throw new IllegalArgumentException("Unsupported uri: " + uri);
//        }
        long rowId = db.getWritableDatabase().insertWithOnConflict(
                table, 
                null,
                values, 
                SQLiteDatabase.CONFLICT_REPLACE);
        Uri newUri = ContentUris.withAppendedId(uri, rowId);
        cr.notifyChange(newUri, null);
        return newUri;

    }

    @Override
    public boolean onCreate() {
        cr = getContext().getContentResolver();
        db = new DatabaseHelper(getContext());
        return (db == null) ? false : true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
            String[] selectionArgs, String sortOrder) {
        SQLiteQueryBuilder builder = new SQLiteQueryBuilder();
        int match = uriMatcher.match(uri);
//        switch (match) {
//            // set up: builder.setTables(<value>) 
//        default:
//            throw new IllegalArgumentException("Unsupported uri: " + uri);
//        }
        Cursor cursor = builder.query(
                db.getReadableDatabase(), 
                projection,
                selection, 
                selectionArgs, 
                null, null, sortOrder);
        cursor.setNotificationUri(cr, uri);
        return cursor;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection,
            String[] selectionArgs) {
        String whereClause = null;
        String table = null;
        int match = uriMatcher.match(uri);
//      switch (match) {
//      // set up: builder.setTables(<value>) 
//      default:
//          throw new IllegalArgumentException("Unsupported uri: " + uri);
//      }
        int rows = db.getWritableDatabase().update(table, values, whereClause,
                selectionArgs);
        if (rows > 0) {
            cr.notifyChange(uri, null);
        }
        return rows;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        String table = null;
        String whereClause = null;
//      switch (match) {
//      // set up: builder.setTables(<value>) 
//      default:
//          throw new IllegalArgumentException("Unsupported uri: " + uri);
//      }
        int rows = db.getWritableDatabase().delete(table, whereClause,
                selectionArgs);
        if (rows > 0) {
            cr.notifyChange(uri, null);
        }
        return rows;
    }


    private class DatabaseHelper extends SQLiteOpenHelper {

        public DatabaseHelper(Context context) {
            super(context, DATABASE, null, Schema.VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        }

        private void dropTable(final String name, final SQLiteDatabase db) {
            db.execSQL("DROP TABLE IF EXISTS " + name);
        }

    }

}
