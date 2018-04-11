package com.unusual_designers.groupsms;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import static com.unusual_designers.groupsms.GroupDatabaseHelper.GROUP_NAME;
import static com.unusual_designers.groupsms.GroupDatabaseHelper.MEMBER_ID;
import static com.unusual_designers.groupsms.GroupDatabaseHelper.MEMBER_NAME;
import static com.unusual_designers.groupsms.GroupDatabaseHelper.MEMBER_PHONE;

public class MemberListActivity extends AppCompatActivity {

    static final String ACTION = "action";
    static final int addRequest = 0;
    static final int editRequest = 1;

    TextView memberListName;
    ListView membersList;
    Button addMember;
    Cursor cursor;
    String currentGroupName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_member_list);

        currentGroupName = getIntent().getStringExtra(GroupDatabaseHelper.GROUP_NAME);

        memberListName = findViewById(R.id.member_list_name);
        membersList = findViewById(R.id.members_list);
        addMember = findViewById(R.id.add_member);

        addMember.setOnClickListener(v -> {
            startActivity(new Intent(this, MemberDetailsActivity.class)
                    .putExtra(ACTION, addRequest)
                    .putExtra(GROUP_NAME, currentGroupName));
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        cursor = DatabaseGetter.getReadableDatabase(this)
                .query(currentGroupName,
                        new String[]{"ROWID as _id", MEMBER_ID, MEMBER_NAME, MEMBER_PHONE},
                        null, null, null, null,
                        MEMBER_NAME);
        membersList.setAdapter(new ListAdapter(this, cursor, true));
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
            return LayoutInflater.from(context).inflate(R.layout.member_list_row, parent, false);
        }

        @Override
        public void bindView(View view, Context context, Cursor cursor) {
            TextView member_name = view.findViewById(R.id.member_name);
            TextView member_id = view.findViewById(R.id.member_id);
            ImageButton sms = view.findViewById(R.id.sms_member);
            ImageButton edit = view.findViewById(R.id.edit_member_button);
            ImageButton delete = view.findViewById(R.id.delete_member_button);

            member_id.setText(String.valueOf(cursor.getString(1)).equals("null") ? "ID: Not Set" : "ID: " + cursor.getString(1));
            member_name.setText(cursor.getString(2));
            member_name.setTag(cursor.getString(3));

            sms.setOnClickListener(v -> {
                String phNo = ((String) member_name.getTag());
                Intent smsIntent = new Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:" + phNo));
                startActivity(smsIntent);
            });
            edit.setOnClickListener(v -> {
                startActivity(new Intent(MemberListActivity.this, MemberDetailsActivity.class)
                        .putExtra(ACTION, editRequest)
                        .putExtra(MEMBER_ID, member_id.getText().toString())
                        .putExtra(MEMBER_NAME, member_name.getText().toString())
                        .putExtra(MEMBER_PHONE, (String) (member_name.getTag()))
                        .putExtra(GROUP_NAME, currentGroupName));
            });

            delete.setOnClickListener(v -> {
                AlertDialog.Builder alert = new AlertDialog.Builder(MemberListActivity.this);
                alert.setTitle("Are you sure to delete?");
                alert.setPositiveButton("Yes", (dialog, which) -> {

                    String phNo = ((String) member_name.getTag());
                    DatabaseGetter.getWritableDatabase(MemberListActivity.this)
                            .delete(currentGroupName, MEMBER_PHONE + " = ?", new String[]{phNo});
                    cursor.requery();
                });
                alert.setNegativeButton("No", null);
                alert.show();
            });
        }
    }
}
