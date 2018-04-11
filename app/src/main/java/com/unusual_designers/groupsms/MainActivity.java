package com.unusual_designers.groupsms;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

    Button viewGroup;
    Button about;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        viewGroup = findViewById(R.id.view_group);
        about = findViewById(R.id.about);

        viewGroup.setOnClickListener(v -> {
            startActivity(new Intent(this, GroupListActivity.class));
        });
        about.setOnClickListener(v -> {
            //TODO
        });
    }
}
