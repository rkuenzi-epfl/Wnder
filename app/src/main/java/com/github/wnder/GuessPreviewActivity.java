package com.github.wnder;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

/**
 * Preview activity class
 */
public class GuessPreviewActivity extends AppCompatActivity {

    /**
     * executed on activity creation
     * @param savedInstanceState saved instance state
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Set layout
        setContentView(R.layout.activity_guess_preview);

        //Setup buttons
        findViewById(R.id.guessButton).setOnClickListener(id -> openGuessActivity());
        findViewById(R.id.skipButton).setOnClickListener(id -> openPreviewActivity());
    }

    /**
     * Executed on activity start
     */
    @Override
    protected void onStart() {
        super.onStart();
        //Get image preview
        ImageView image = findViewById(R.id.imagePreview);
        image.setImageURI(Uri.parse("android.resource://com.github.wnder/" + R.raw.ladiag));
        // TODO: Get random image from DB and display it
    }

    private void openGuessActivity() {
        // TODO: Load actual camera and picture location
        Intent intent = new Intent(this, GuessLocationActivity.class);
        intent.putExtra(GuessLocationActivity.EXTRA_CAMERA_LAT, 5.0);
        intent.putExtra(GuessLocationActivity.EXTRA_CAMERA_LNG, 5.0);
        intent.putExtra(GuessLocationActivity.EXTRA_PICTURE_LAT, 46.5197);
        intent.putExtra(GuessLocationActivity.EXTRA_PICTURE_LNG, 6.5657);
        startActivity(intent);
        finish();
    }

    private void openPreviewActivity() {
        Intent intent = new Intent(this, GuessPreviewActivity.class);
        startActivity(intent);
    }
}

