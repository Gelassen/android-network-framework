package com.home.clientservercore.storage;


import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;

import java.util.HashMap;

public class LibContentProvider extends ContentProvider {

    private ContentResolver cr;
    private LibOpenHelper db;

    @Override
    public boolean onCreate() {
        cr = getContext().getContentResolver();
        db = new LibOpenHelper(getContext());
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        final int matchId = matcher.match(uri);
        final String table = getTable(matchId);

        final SQLiteQueryBuilder builder = new SQLiteQueryBuilder();
        builder.setTables(table);
        String groupBy = null;
        switch (matchId) {
            case MATCH_SAMPLE:
                // do what you want
                break;
            default:
                groupBy = null;
        }

        final Cursor cursor = builder.query(db.getReadableDatabase(),projection, selection, selectionArgs, groupBy, null, sortOrder);
        cursor.setNotificationUri(cr, uri);
        return cursor;
    }

    @Override
    public String getType(Uri uri) {
        return null;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        final int matchId = matcher.match(uri);
        final String table = getTable(matchId);
        final long id = db.getWritableDatabase().insertWithOnConflict(table, null, values, SQLiteDatabase.CONFLICT_REPLACE);
        if (id > -1) {
            cr.notifyChange(uri, null);
        }
        return ContentUris.withAppendedId(uri, id);
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        final int matchId = matcher.match(uri);
        final String table = getTable(matchId);
        final int rows = db.getWritableDatabase().delete(table, selection, selectionArgs);
        notifyChange(uri, rows);
        return rows;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        final int matchId = matcher.match(uri);
        final String table = getTable(matchId);
        final int rows = db.getWritableDatabase().update(table, values, selection, selectionArgs);
        notifyChange(uri, rows);
        return rows;
    }

    private void notifyChange(final Uri uri, final int rows) {
        if (rows > 0) {
            cr.notifyChange(uri, null);
        }
    }

    private String getTable(final int matchId) {
        String table = tables.get(matchId);
        if (table == null) {
            throw new IllegalArgumentException("Tables doesn't exist for code " + matchId);
        }
        return table;
    }

    private final static UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
    private final static HashMap<Integer, String> tables = new HashMap<Integer, String>();

    private static void match(Class<? extends Contract.SqlEntity> table, int code) {
        if (tables.get(table) != null ) {
            throw new IllegalArgumentException("Duplicate code for " + table.getSimpleName());
        }

        String tableName = Contract.Join.class.isAssignableFrom(table)
                ? Contract.Join.getJoinTable((Class<Contract.Join>) table) : Contract.getTableName(table);

        String path = table.getSimpleName();
        matcher.addURI(Contract.AUTHORITY, path, code);
        tables.put(code, tableName);
    }

    private final static int MATCH_SAMPLE = 0x00000001;

    static {
        match(Contract.SampleTable.class, MATCH_SAMPLE);
    }

}
