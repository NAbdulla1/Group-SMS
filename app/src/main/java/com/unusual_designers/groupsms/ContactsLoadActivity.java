package com.unusual_designers.groupsms;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CursorAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Set;
import java.util.TreeMap;

public class ContactsLoadActivity extends AppCompatActivity {

    ListView listView;
    Cursor c;
    TreeMap<String, String> map;
    boolean[] checked;

    Button addBtn;
    Button cancelBtn;

    String groupName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contacts_load);

        listView = findViewById(R.id.contactsList);
        addBtn = findViewById(R.id.addList);
        cancelBtn = findViewById(R.id.cancelList);

        groupName = getIntent().getStringExtra(GroupDatabaseHelper.GROUP_NAME);

        c = getContentResolver().query(ContactsContract.Data.CONTENT_URI,
                new String[]{ContactsContract.Data._ID, ContactsContract.Data.DISPLAY_NAME, ContactsContract.CommonDataKinds.Phone.NUMBER},
                ContactsContract.CommonDataKinds.Phone.NUMBER + " LIKE ? OR " +
                        ContactsContract.CommonDataKinds.Phone.NUMBER + " LIKE ? OR " +
                        ContactsContract.CommonDataKinds.Phone.NUMBER + " LIKE ?",
                new String[]{"01%", "%8801", "+8801%"},
                ContactsContract.Data.DISPLAY_NAME);

        if (c != null) {
            int cnt = c.getCount();
            map = new TreeMap<>();
            checked = new boolean[cnt];
            //Log.d("cursorVals", "results: " + cnt);
            listView.setAdapter(new MyCursorAdapter(this, c));
        } else {
            Toast.makeText(this, "Can't load contacts properly. Try Again.", Toast.LENGTH_SHORT).show();
            finish();
        }

        addBtn.setOnClickListener(v -> addToGroup(groupName));
        cancelBtn.setOnClickListener(v -> finish());
    }

    @Override
    protected void onDestroy() {
        if (c != null)
            c.close();
        super.onDestroy();
    }

    private class MyCursorAdapter extends CursorAdapter {

        MyCursorAdapter(Context context, Cursor cursor) {
            super(context, cursor, false);
        }

        @Override
        public View newView(Context context, Cursor cursor, ViewGroup parent) {
            //Log.d("cursorVals", "newView: cursor = " + cursor.getPosition());
            return LayoutInflater.from(context).inflate(R.layout.list_row, parent, false);
        }

        @Override
        public void bindView(View view, Context context, Cursor cursor) {
            //Log.d("cursorVals", "bindView Called when cursor at" + cursor.getPosition());
            TextView name = view.findViewById(R.id.Name);
            TextView phone = view.findViewById(R.id.Phone);
            CheckBox cb = view.findViewById(R.id.taken);

            cb.setChecked(checked[cursor.getPosition()]);
            name.setText(cursor.getString(cursor.getColumnIndex(ContactsContract.Data.DISPLAY_NAME)));
            phone.setText(cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)));

            cb.setTag(cursor.getPosition());

            cb.setOnClickListener(v -> {
                String nameStr = name.getText().toString();
                String phoneStr = makeSimple(phone.getText().toString());
                CheckBox isTakenBox = (CheckBox) v;
                if (isTakenBox.isChecked()) {
                    if (map.containsKey(phoneStr)) {
                        Toast.makeText(ContactsLoadActivity.this,
                                "Phone Number already exists in list",
                                Toast.LENGTH_SHORT).show();
                        isTakenBox.setChecked(false);
                        return;
                    }
                    checked[(Integer) v.getTag()] = true;
                    map.put(phoneStr, nameStr);
/*                    Toast.makeText(ContactsLoadActivity.this,
                            "Contact added to list.\nList size: " + map.size(),
                            Toast.LENGTH_SHORT).show();*/
                } else {
                    checked[(Integer) v.getTag()] = false;
                    map.remove(phoneStr);
/*                    Toast.makeText(ContactsLoadActivity.this,
                            "Contact removed from list.\nList size: " + map.size(),
                            Toast.LENGTH_SHORT).show();*/
                }
            });
        }
    }

    private void addToGroup(String groupName) {
        ContentValues cv = new ContentValues();
        Set<String> keys = map.keySet();
        SQLiteDatabase db = DatabaseGetter.getWritableDatabase(this);
        for (String key : keys) {
            cv.put(GroupDatabaseHelper.MEMBER_NAME, map.get(key));
            cv.put(GroupDatabaseHelper.MEMBER_PHONE, key);
            if (db.insert(groupName, GroupDatabaseHelper.MEMBER_NAME, cv) == -1) {
                Toast.makeText(this, R.string.db_insert_error_msg, Toast.LENGTH_SHORT).show();
            }
        }
        finish();
    }

    private String makeSimple(String phoneNumber) {
        if (phoneNumber.startsWith("+88"))
            return phoneNumber.substring(3);
        else if (phoneNumber.startsWith("88"))
            return phoneNumber.substring(2);
        else
            return phoneNumber;
    }
}
