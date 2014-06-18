package com.home.clientservercore.storage;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.home.clientservercore.App;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;


public class LibOpenHelper extends SQLiteOpenHelper {

    private static final int VERSION = 1;
    private static final String DATABASE = "store.db";

    public LibOpenHelper(Context context) {
        super(context, DATABASE, null, VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        for (Class<Contract.Table> table : getTables()) {
            db.execSQL(getTableDDL(table));
        }
        for (Class<Contract.View> view : getViews()) {
            db.execSQL(getViewDDL(view));
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        for (Class<Contract.Table> table : getTables()) {
            db.execSQL("drop table if exists " + Contract.getTableName(table));
        }
        for (Class<Contract.View> view : getViews()) {
            db.execSQL("drop view if exists " + Contract.getTableName(view));
        }
        onCreate(db);
    }

    /* reflection based schema management */
    /*
     * Copyright (C) 2012 Random Android Code Snippets
     *
     * Licensed under the Apache License, Version 2.0 (the "License");
     * you may not use this file except in compliance with the License.
     * You may obtain a copy of the License at
     *
     *      http://www.apache.org/licenses/LICENSE-2.0
     *
     * Unless required by applicable law or agreed to in writing, software
     * distributed under the License is distributed on an "AS IS" BASIS,
     * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
     * See the License for the specific language governing permissions and
     * limitations under the License.
     */
    @SuppressWarnings("unchecked")
    private List<Class<Contract.Table>> getTables() {
        final ArrayList<Class<Contract.Table>> list =
                new ArrayList<Class<Contract.Table>>();
        for (final Class<?> klass : Contract.class.getDeclaredClasses()) {
            if (Contract.Table.class.isAssignableFrom(klass)
                    && !klass.equals(Contract.Table.class)) {
                list.add((Class<Contract.Table>) klass);
            }
        }
        return list;
    }

    @SuppressWarnings("unchecked")
    private List<Class<Contract.View>> getViews() {
        final ArrayList<Class<Contract.View>> list =
                new ArrayList<Class<Contract.View>>();
        for (final Class<?> klass : Contract.class.getDeclaredClasses()) {
            if (Contract.View.class.isAssignableFrom(klass)
                    && !klass.equals(Contract.View.class)) {
                list.add((Class<Contract.View>) klass);
            }
        }
        return list;
    }

    private String getTableDDL(final Class<Contract.Table> table) {
        final StringBuilder sql = new StringBuilder(128);
        sql.append("create table ").append(Contract.getTableName(table)).append(" (");
        for (final Field field : table.getFields()) {
            if (field.getName().startsWith("_")) continue;
            try {
                sql.append(field.get(null));
            } catch (Exception ignore) {
            }
            try {
                final Field type = table.getDeclaredField("_SQL_" + field.getName() + "_TYPE");
                sql.append(' ').append(type.get(null));
            } catch (Exception ignore) {
                sql.append(" TEXT");
            }
            sql.append(',');
        }
        try {
            final Field type = table.getDeclaredField("_SQL_UNIQUE");
            sql.append(' ')
                    .append("UNIQUE(")
                    .append(type.get(null))
                    .append(") ON CONFLICT REPLACE);");
        } catch (Exception ignore) {
            sql.setLength(sql.length() - 1); // chop off last comma
            sql.append(')');
        }
        Log.v(App.TAG, "DDL for " + table.getSimpleName() + ": " + sql);
        return sql.toString();
    }

    private String getViewDDL(final Class<Contract.View> view) {
        final StringBuilder sql = new StringBuilder(128);
        sql.append("create view ").append(Contract.getTableName(view)).append(" as ");
        try {
            final Field query = view.getDeclaredField("_SQL_VIEW_QUERY");
            sql.append(query.get(null));
        } catch (Exception ignore) { }
        Log.v(App.TAG, "DDL for " + view.getSimpleName() + ": " + sql);
        return sql.toString();
    }
}
