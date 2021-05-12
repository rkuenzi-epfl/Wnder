package com.github.wnder;

import android.net.Uri;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.github.wnder.picture.PicturesDatabase;
import com.github.wnder.user.GlobalUser;
import com.github.wnder.user.SignedInUser;
import com.github.wnder.user.User;
import com.github.wnder.user.UserDatabase;

import java.util.ArrayList;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class HistoryFragment extends Fragment {

    @Inject
    public PicturesDatabase picturesDb;

    @Inject
    public UserDatabase userDb;

    public HistoryFragment() {
        super(R.layout.fragment_history);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {

        ArrayList<String> pictureList = new ArrayList<>();

        userDb.getPictureList(GlobalUser.getUser(), "guessedPics")
                .thenAccept(list -> {
                    pictureList.addAll(list);
                    HistoryAdapter historyAdapter = new HistoryAdapter(pictureList, picturesDb);
                    RecyclerView historyRecycler = view.findViewById(R.id.historyRecyclerView);
                    historyRecycler.setHasFixedSize(true);
                    historyRecycler.setLayoutManager(new LinearLayoutManager(view.getContext()));
                    historyRecycler.setAdapter(historyAdapter);
                });

    }
}
