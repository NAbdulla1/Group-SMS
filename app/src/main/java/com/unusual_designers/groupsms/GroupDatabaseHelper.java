package com.unusual_designers.groupsms;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by nayon on 12-Mar-18.
 */

public class GroupDatabaseHelper extends SQLiteOpenHelper {
    static final String DB_NAME = "GroupSMS.db";
    static final String GROUP_NAMES_TABLE = "Group_Table";//contains group names
    static final String GROUP_ID = "GroupID";
    static final String GROUP_NAME = "Group_Name";
    static final String MEMBER_ID = "Student_ID";
    static final String MEMBER_NAME = "Name";
    static final String MEMBER_PHONE = "Phone_Number";

    public GroupDatabaseHelper(Context context) {
        super(context.getApplicationContext(), DB_NAME, null, 1);
    }


    /**
     * Called when the database is created for the first time. This is where the
     * creation of tables and the initial population of the tables should happen.
     *
     * @param db The database.
     */
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + GROUP_NAMES_TABLE + " (" + GROUP_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " + GROUP_NAME + " TEXT);", null);
    }

    /**
     * Called when the database needs to be upgraded. The implementation
     * should use this method to drop tables, add tables, or do anything else it
     * needs to upgrade to the new schema version.
     * <p>
     * <p>
     * The SQLite ALTER TABLE documentation can be found
     * <a href="http://sqlite.org/lang_altertable.html">here</a>. If you add new columns
     * you can use ALTER TABLE to insert them into a live table. If you rename or remove columns
     * you can use ALTER TABLE to rename the old table, then create the new table and then
     * populate the new table with the contents of the old table.
     * </p><p>
     * This method executes within a transaction.  If an exception is thrown, all changes
     * will automatically be rolled back.
     * </p>
     *
     * @param db         The database.
     * @param oldVersion The old database version.
     * @param newVersion The new database version.
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
