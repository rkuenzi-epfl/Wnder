package com.github.wnder;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ImageView;

/**
 * Defines activity for history
 */
public class HistoryActivity extends AppCompatActivity {
    //For now, a placeholder
    private ImageView image;

    /**
     * Executes when activity is created
     * @param savedInstanceState saved instance state
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Set layout
        setContentView(R.layout.activity_history);

        //set placeholder
        image = findViewById(R.id.historyImage);
        image.setImageURI(Uri.parse("android.resource://com.github.wnder/" + R.raw.ladiag));
        //When clicked open activity for specific picture history
        image.setOnClickListener((view) -> {
            Intent intent = new Intent(this, PictureHistoryActivity.class);
            startActivity(intent);
        });
    }
}