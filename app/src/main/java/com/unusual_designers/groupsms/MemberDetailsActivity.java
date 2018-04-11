package com.unusual_designers.groupsms;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import static com.unusual_designers.groupsms.GroupDatabaseHelper.GROUP_NAME;
import static com.unusual_designers.groupsms.GroupDatabaseHelper.MEMBER_ID;
import static com.unusual_designers.groupsms.GroupDatabaseHelper.MEMBER_NAME;
import static com.unusual_designers.groupsms.GroupDatabaseHelper.MEMBER_PHONE;
import static com.unusual_designers.groupsms.MemberListActivity.ACTION;
import static com.unusual_designers.groupsms.MemberListActivity.addRequest;
import static com.unusual_designers.groupsms.MemberListActivity.editRequest;

public class MemberDetailsActivity extends AppCompatActivity {

    EditText id, name, phone;
    Button done, phonebook;
    TextView or;

    int actionType;

    String currentGroupName;
    String pid, pname, pphone;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_member_details);

        actionType = getIntent().getIntExtra(ACTION, -1);
        currentGroupName = getIntent().getStringExtra(GroupDatabaseHelper.GROUP_NAME);

        id = findViewById(R.id.id);
        name = findViewById(R.id.name);
        phone = findViewById(R.id.phone);
        or = findViewById(R.id.or);

        done = findViewById(R.id.add_member_button);
        phonebook = findViewById(R.id.add_from_phonebook_button);

        if (actionType == editRequest) {
            pid = getIntent().getStringExtra(MEMBER_ID);
            pname = getIntent().getStringExtra(MEMBER_NAME);
            pphone = getIntent().getStringExtra(MEMBER_PHONE);

            id.setText(pid);
            name.setText(pname);
            phone.setText(pphone);

            phonebook.setVisibility(View.INVISIBLE);
            or.setVisibility(View.INVISIBLE);
        } else {
            phonebook.setVisibility(View.VISIBLE);
            or.setVisibility(View.VISIBLE);
        }

        done.setOnClickListener(v -> {
            String sid = id.getText().toString();
            String sname = name.getText().toString();
            String sphone = phone.getText().toString();
            if (sname.length() == 0) {
                finish();
            } else if (sname.contains("\"") || sname.contains("`") || !isPhoneNumber(sphone)) {
                StringBuffer msg = new StringBuffer();
                if (sname.contains("\"") || sname.contains("`"))
                    msg.append("Member Name should not contain \" or ` character.");
                if (!isPhoneNumber(sphone)) {
                    if (msg.length() > 0) msg.append("\n");
                    msg.append("Phone number must have 11 digits.");
                }
                Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
            } else {
                SQLiteDatabase db = DatabaseGetter.getWritableDatabase(this);
                if (actionType == addRequest)
                    addMemberFun(db, sid, sname, sphone);
                else if (actionType == editRequest) {
                    if (pphone.equals(sphone)) {
                        ContentValues cv = new ContentValues();
                        cv.put(MEMBER_ID, sid);
                        cv.put(MEMBER_NAME, sname);
                        db.update(currentGroupName, cv, MEMBER_PHONE + " = ?", new String[]{pphone});
                    } else {
                        Cursor c = db.query(currentGroupName,
                                new String[]{MEMBER_ID, MEMBER_NAME},
                                MEMBER_PHONE + " = ?",
                                new String[]{sphone}, null, null, null);
                        if (c.moveToNext()) {
                            Toast.makeText(this, String.format("A member already exists with this phone number.\n" +
                                            "Name: %s.\nID: %s.", c.getString(2), c.getString(1)),
                                    Toast.LENGTH_SHORT).show();
                        } else {
                            db.delete(currentGroupName, MEMBER_PHONE + " = ?", new String[]{pphone});
                            addMemberFun(db, sid, sname, sphone);
                        }
                        c.close();
                    }
                }
                finish();
            }
        });

        phonebook.setOnClickListener(v -> {
            startActivity(new Intent(this, ContactsLoadActivity.class)
                    .putExtra(GROUP_NAME, currentGroupName));
            finish();
        });
    }

    private void addMemberFun(SQLiteDatabase db, String id, String name, String phone) {
        ContentValues cv = new ContentValues();
        cv.put(MEMBER_ID, id);
        cv.put(MEMBER_NAME, name);
        cv.put(MEMBER_PHONE, phone);
        long rowID = db.insert(currentGroupName, MEMBER_NAME, cv);
        if (rowID == -1)
            Toast.makeText(this, "Can't add member. Try again.", Toast.LENGTH_SHORT).show();
    }

    private boolean isPhoneNumber(String s) {
        return s.startsWith("01") && s.length() == 11 && isAllDigits(s);
    }

    private boolean isAllDigits(String s) {
        for (int i = 0; i < s.length(); i++) {
            if (!Character.isDigit(s.charAt(i)))
                return false;
        }
        return true;
    }
}
