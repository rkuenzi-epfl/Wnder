package com.github.wnder;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

/**
 * Activity to upload something to the db
 */
public class UploadActivity extends AppCompatActivity {
    //Button
    private Button goBackToMenu;
    //Text
    private TextView textOfSuccess;

    /**
     * Executes on activity creation
     * @param savedInstanceState saved instance state
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Set layout
        setContentView(R.layout.activity_upload);

        //Check if success or not, modify text accordingly
        boolean success = getIntent().getBooleanExtra(TakePictureActivity.HAS_SUCCEEDED, false);
        textOfSuccess = findViewById(R.id.textView4);
        if(success){
            textOfSuccess.setText(R.string.success);
        }
        else{
            textOfSuccess.setText(R.string.failure);
        }

        //Back to menu button
        goBackToMenu = findViewById(R.id.uploadToMenuButton);
        goBackToMenu.setOnClickListener((view) -> {
            Intent intent = new Intent(this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
        });
    }
}