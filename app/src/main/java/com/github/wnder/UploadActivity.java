package com.github.wnder;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class UploadActivity extends AppCompatActivity {
    private Button goBackToMenu;
    private TextView textOfSuccess;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload);

        boolean success = getIntent().getBooleanExtra(TakePictureActivity.HAS_SUCCEEDED, false);
        textOfSuccess = findViewById(R.id.textView4);
        if(success){
            textOfSuccess.setText("Success");
        }
        else{
            textOfSuccess.setText("Failure");
        }

        goBackToMenu = findViewById(R.id.uploadToMenuButton);
        goBackToMenu.setOnClickListener((view) -> {
            Intent intent = new Intent(this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
        });
    }
}