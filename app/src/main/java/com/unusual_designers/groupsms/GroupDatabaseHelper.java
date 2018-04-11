package com.unusual_designers.groupsms;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by nayon on 12-Mar-18.
 */

public class GroupDatabaseHelper extends SQLiteOpenHelper {
    static final String DB_NAME = "group_sms.db";
    static final String GROUP_NAMES_TABLE = "Group_Table";//contains group names
    static final String GROUP_NAME = "Group_Name";
    static final String MEMBER_ID = "Student_ID";
    static final String MEMBER_NAME = "Name";
    static final String MEMBER_PHONE = "Phone_Number";

    public GroupDatabaseHelper(Context context) {
        super(context.getApplicationContext(), DB_NAME, null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + GROUP_NAMES_TABLE + "(" + GROUP_NAME + " TEXT);");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
