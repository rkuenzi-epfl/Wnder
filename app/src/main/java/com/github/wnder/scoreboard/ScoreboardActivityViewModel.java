package com.github.wnder.scoreboard;


import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.SavedStateHandle;
import androidx.lifecycle.ViewModel;

import com.github.wnder.picture.PicturesDatabase;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class ScoreboardActivityViewModel extends ViewModel {

    private PicturesDatabase picturesDb;
    /*private UserService userService;*/

    private String uniqueId;

    private MutableLiveData<List<Map.Entry<String,Double>>> scoreboard;

    /**
     * Scoreboard ViewModel constructor
     *
     * @param picturesDb the picture database
     * @param savedStateHandle state notably containing intent extras
     */
    @Inject
    public ScoreboardActivityViewModel(PicturesDatabase picturesDb, SavedStateHandle savedStateHandle){
        this.picturesDb = picturesDb;

        this.scoreboard = new MutableLiveData<>(new ArrayList<>());
        this.uniqueId = savedStateHandle.get(ScoreboardActivity.EXTRA_PICTURE_ID);

        this.refreshScoreboard();
    }

    /**
     * Refresh the scoreboard
     */
    public void refreshScoreboard(){
        picturesDb.getScoreboard(uniqueId)
                .thenAccept((scoreboard) -> {
                    scoreboard.removeIf(e -> e.getValue() < 0);
                    scoreboard.sort(Map.Entry.comparingByValue());
                    Collections.reverse(scoreboard);
                    this.scoreboard.postValue(scoreboard);
                });
                //.exceptionally();
    }

    /**
     * Get the scoreboard LiveData
     * @return scoreboard LiveData
     */
    public LiveData<List<Map.Entry<String,Double>>> getScoreboard(){
        return scoreboard;
    }
}
