package com.unusual_designers.groupsms;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by nayon on 12-Mar-18.
 */

public class DatabaseGetter {
    private static SQLiteOpenHelper db;
    private static void getInstance(Context context){
        if(db == null)
            db = new GroupDatabaseHelper(context);
    }
    public static SQLiteDatabase getReadableDatabase(Context context){
        getInstance(context);
        return db.getReadableDatabase();
    }
    public static SQLiteDatabase getWritableDatabase(Context context){
        getInstance(context);
        return db.getWritableDatabase();
    }
}
