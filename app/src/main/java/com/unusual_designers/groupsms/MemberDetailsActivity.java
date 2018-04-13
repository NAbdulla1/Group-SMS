package com.unusual_designers.groupsms;

import android.Manifest;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
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

public class MemberDetailsActivity extends AppCompatActivity implements ActivityCompat.OnRequestPermissionsResultCallback {

    static final int REQUEST_CODE_FOR_CONTACTS = 44654;//arbitrary number
    static final String IS_FIRST_REQUEST_PREF = "isfirstrequestfile";

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
            boolean isFirst = getSharedPreferences(IS_FIRST_REQUEST_PREF, MODE_PRIVATE).getBoolean(IS_FIRST_REQUEST_PREF, true);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS)
                        != PackageManager.PERMISSION_GRANTED) {
                    getSharedPreferences(IS_FIRST_REQUEST_PREF, MODE_PRIVATE).edit().putBoolean(IS_FIRST_REQUEST_PREF, false).apply();
                    if (shouldShowRequestPermissionRationale(Manifest.permission.READ_CONTACTS)) {
                        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
                        dialog.setTitle("Information:");
                        dialog.setView(R.layout.alert_view);
                        dialog.setPositiveButton("Allow", (d, w) -> {
                            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_CONTACTS}, REQUEST_CODE_FOR_CONTACTS);
                        });
                        dialog.setNegativeButton("Not Allow", null);
                        dialog.show();
                    } else {
                        if (isFirst) {
                            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_CONTACTS}, REQUEST_CODE_FOR_CONTACTS);
                        } else {
                            Toast.makeText(this, "You have denied READ_CONTACTS permission for this application. This functionality will not work.", Toast.LENGTH_SHORT).show();
                        }
                    }
                } else {
                    startActivity(new Intent(this, ContactsLoadActivity.class)
                            .putExtra(GROUP_NAME, currentGroupName));
                    finish();
                }
            } else {
                startActivity(new Intent(this, ContactsLoadActivity.class)
                        .putExtra(GROUP_NAME, currentGroupName));
                finish();
            }
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

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_FOR_CONTACTS) {
            for (int i = 0; i < permissions.length; i++) {
                if (permissions[i].equals(Manifest.permission.READ_CONTACTS)) {
                    if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                        startActivity(new Intent(this, ContactsLoadActivity.class)
                                .putExtra(GROUP_NAME, currentGroupName));
                        finish();
                    }
                    break;
                }
            }
        }
    }
}
