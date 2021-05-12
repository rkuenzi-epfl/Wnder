package com.github.wnder;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.github.wnder.picture.PicturesDatabase;
import com.github.wnder.user.GlobalUser;
import com.github.wnder.user.User;

import java.util.ArrayList;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class HistoryFragment extends Fragment {

    @Inject
    public PicturesDatabase picturesDb;

    public HistoryFragment() {
        super(R.layout.fragment_history);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {

        ArrayList<String> pictureList = new ArrayList<>();
        // TODO :  Use actual data!
        pictureList.add("demo1");
        pictureList.add("demo2");
        pictureList.add("demo3");
        pictureList.add("artlab");
        pictureList.add("testPicDontRm");
        pictureList.add("picture1");

        HistoryAdapter historyAdapter = new HistoryAdapter(view.getContext(), pictureList, picturesDb);
        RecyclerView historyRecycler = view.findViewById(R.id.historyRecyclerView);
        historyRecycler.setHasFixedSize(true);
        historyRecycler.setLayoutManager(new LinearLayoutManager(view.getContext()));
        historyRecycler.setAdapter(historyAdapter);

    }
}
