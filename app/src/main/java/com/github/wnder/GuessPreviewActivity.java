package com.github.wnder;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

public class GuessPreviewActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_guess_preview);
        findViewById(R.id.guessButton).setOnClickListener(id -> openGuessActivity());
        findViewById(R.id.skipButton).setOnClickListener(id -> openPreviewActivity());
    }

    @Override
    protected void onStart() {
        super.onStart();
        ImageView image = findViewById(R.id.imagePreview);
        image.setImageURI(Uri.parse("android.resource://com.github.wnder/" + R.raw.ladiag));
        // TODO: Get random image from DB and display it
    }

    private void openGuessActivity() {
        Intent intent = new Intent(this, GuessLocationActivity.class);
        intent.putExtra(GuessLocationActivity.EXTRA_CAMERA_LAT, 5.0); //TODO instead of an arbitrary 5 get an appropriate double
        intent.putExtra(GuessLocationActivity.EXTRA_CAMERA_LNG, 5.0); //TODO instead of an arbitrary 5 get an appropriate double
        intent.putExtra(GuessLocationActivity.EXTRA_PICTURE_LAT, 46.5197); //TODO instead of an arbitrary 5 get an appropriate double
        intent.putExtra(GuessLocationActivity.EXTRA_PICTURE_LNG, 6.5657); //TODO instead of an arbitrary 5 get an appropriate double
        startActivity(intent);
        finish();
    }

    private void openPreviewActivity() {
        Intent intent = new Intent(this, GuessPreviewActivity.class);
        startActivity(intent);
    }
}

