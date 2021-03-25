package com.github.wnder;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class GuessPreviewActivity extends AppCompatActivity implements View.OnClickListener{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_guess_preview);
        findViewById(R.id.guessButton).setOnClickListener(this);
        findViewById(R.id.skipButton).setOnClickListener(this);

    }

    @Override
    protected void onStart() {
        super.onStart();
        // TODO: Get random image from DB and display it
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.guessButton:
                openGuessActivity();
                break;
            case R.id.skipButton:
                openPreviewActivity();
                break;
            default:
                break;
                // Other buttons can be setup in this switch
        }
    }

    private void openGuessActivity() {
        Intent intent = new Intent(this, GuessLocationActivity.class);
        startActivity(intent);
    }

    private void openPreviewActivity() {
        Intent intent = new Intent(this, GuessPreviewActivity.class);
        startActivity(intent);
    }
}

