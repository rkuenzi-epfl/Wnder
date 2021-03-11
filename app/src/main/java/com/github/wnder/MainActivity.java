package com.github.wnder;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_main);
        setSupportActionBar(toolbar);

        TextView textView = toolbar.findViewById(R.id.username);
        User user = GlobalUser.getUser();
        textView.setText(user.getName());

        ImageView imageView = toolbar.findViewById(R.id.profile_picture);
        imageView.setImageURI(user.getProfilePicture());
    }
}