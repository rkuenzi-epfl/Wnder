package com.github.wnder.tour;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.github.wnder.HistoryAdapter;
import com.github.wnder.R;
import com.github.wnder.picture.PicturesDatabase;
import com.github.wnder.user.GlobalUser;
import com.github.wnder.user.UserDatabase;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class TourActivity extends AppCompatActivity {

    @Inject
    public PicturesDatabase picturesDb;

    @Inject
    public UserDatabase userDb;

    @Inject
    public TourDatabase tourDb;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tour);

        userDb.getTourListForUser(GlobalUser.getUser()).thenAccept(tourList -> {
            TourAdapter tourAdapter = new TourAdapter(tourList, picturesDb, tourDb);
            RecyclerView tourRecycler = findViewById(R.id.tourRecyclerView);
            tourRecycler.setHasFixedSize(true);
            tourRecycler.setLayoutManager(new LinearLayoutManager(this));
            tourRecycler.setAdapter(tourAdapter);
        });
    }
}
