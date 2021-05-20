package com.github.wnder;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import com.github.wnder.picture.PicturesDatabase;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class HistoryMapActivity extends AppCompatActivity {
    public static final String EXTRA_PICTURE_ID = "picture_id";

    private String pictureID = Utils.UNINITIALIZED_ID;

    @Inject
    public PicturesDatabase picturesDb;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history_map);

        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        pictureID = extras.getString(EXTRA_PICTURE_ID);

        ImageView mapSnapshotView = findViewById(R.id.mapSnapshot);
        picturesDb.getMapSnapshot(this.getApplicationContext(), pictureID).thenAccept(mapSnapshot -> mapSnapshotView.setImageBitmap(mapSnapshot));

        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE |
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN |
                View.SYSTEM_UI_FLAG_FULLSCREEN);
    }
}
