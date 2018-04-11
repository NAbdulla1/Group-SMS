package com.unusual_designers.groupsms;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import static com.unusual_designers.groupsms.GroupDatabaseHelper.GROUP_NAME;
import static com.unusual_designers.groupsms.GroupDatabaseHelper.GROUP_NAMES_TABLE;
import static com.unusual_designers.groupsms.GroupDatabaseHelper.MEMBER_ID;
import static com.unusual_designers.groupsms.GroupDatabaseHelper.MEMBER_NAME;
import static com.unusual_designers.groupsms.GroupDatabaseHelper.MEMBER_PHONE;

public class GroupListActivity extends AppCompatActivity {

    TextView listName;
    ListView itemsList;
    Button addItem;
    Cursor cursor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_list);

        listName = findViewById(R.id.list_name);
        itemsList = findViewById(R.id.items_list);
        addItem = findViewById(R.id.add_item);

        addItem.setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            EditText groupNameInp = new EditText(this);
            builder.setView(groupNameInp);
            builder.setTitle("Group Name");
            builder.setPositiveButton("OK", (dialog, which) -> {
                String tmp = groupNameInp.getText().toString();
                if (tmp.length() == 0 || tmp.contains("\"") || tmp.contains("`")) {
                    Toast.makeText(GroupListActivity.this, "Group Name should not contain \" or ` character and should have length greater than 0", Toast.LENGTH_SHORT).show();
                    return;
                }

                addGroup(DatabaseGetter.getWritableDatabase(GroupListActivity.this), tmp);

                if (cursor != null)
                    cursor.requery();
            });
            builder.setNegativeButton("Cancel", null);
            builder.show();
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        cursor = DatabaseGetter.getReadableDatabase(this)
                .query(GROUP_NAMES_TABLE,
                        new String[]{"ROWID as _id", GROUP_NAME},
                        null, null, null, null,
                        GROUP_NAME);
        itemsList.setAdapter(new ListAdapter(this, cursor, true));
    }

    @Override
    protected void onStop() {
        if (cursor != null)
            cursor.close();
        super.onStop();
    }

    class ListAdapter extends CursorAdapter {

        ListAdapter(Context context, Cursor c, boolean autoRequery) {
            super(context, c, autoRequery);
        }

        @Override
        public View newView(Context context, Cursor cursor, ViewGroup parent) {
            return LayoutInflater.from(context).inflate(R.layout.group_list_row, parent, false);
        }

        @Override
        public void bindView(View view, Context context, Cursor cursor) {
            TextView itemText = view.findViewById(R.id.item_name);
            ImageButton sms = view.findViewById(R.id.sms_button);
            ImageButton delete = view.findViewById(R.id.delete_item_button);

            itemText.setText(cursor.getString(1));

            itemText.setOnClickListener(v -> {
                startActivity(new Intent(GroupListActivity.this, MemberListActivity.class)
                        .putExtra(GroupDatabaseHelper.GROUP_NAME, "`" + itemText.getText().toString() + "`"));
            });
            sms.setOnClickListener(v -> {
                Cursor c = DatabaseGetter.getReadableDatabase(GroupListActivity.this)
                        .query("`" + itemText.getText().toString() + "`",
                                new String[]{GroupDatabaseHelper.MEMBER_PHONE},
                                null, null, null, null, null);
                int cnt = 0;
                StringBuilder phoneNumbers = new StringBuilder();
                boolean semicolon = false;
                while (c.moveToNext()) {
                    cnt++;
                    if (semicolon)
                        phoneNumbers.append(';');
                    semicolon = true;
                    phoneNumbers.append(c.getString(0));
                }
                c.close();
                if (cnt > 0) {
                    startActivity(new Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:" + phoneNumbers)));
                } else {
                    Toast.makeText(GroupListActivity.this, "Group is empty.", Toast.LENGTH_SHORT).show();
                }
            });

            delete.setOnClickListener(v -> {
                AlertDialog.Builder alert = new AlertDialog.Builder(GroupListActivity.this);
                alert.setTitle("Are you sure to delete?");
                alert.setPositiveButton("Yes", (dialog, which) -> {
                    DatabaseGetter.getWritableDatabase(GroupListActivity.this).delete(GROUP_NAMES_TABLE,
                            GROUP_NAME + " = ?",
                            new String[]{itemText.getText().toString()});
                    DatabaseGetter.getWritableDatabase(GroupListActivity.this)
                            .execSQL("DROP TABLE " + "\"" + itemText.getText().toString() + "\"");
                    cursor.requery();
                });
                alert.setNegativeButton("No", null);
                alert.show();
            });
        }
    }

    private void addGroup(SQLiteDatabase db, String groupName) {
        ContentValues cv = new ContentValues();
        cv.put(GROUP_NAME, groupName);
        long rowID = db.insert(GROUP_NAMES_TABLE, GROUP_NAME, cv);
        if (rowID != -1) {
            groupName = "`" + groupName + "`";
            db.execSQL("CREATE TABLE " + groupName + " (" + MEMBER_ID + " TEXT, " + MEMBER_NAME + " TEXT NOT NULL, " + MEMBER_PHONE + " TEXT PRIMARY KEY);");
        } else
            Toast.makeText(this, "Can't add group. Try again.", Toast.LENGTH_SHORT).show();
    }
}