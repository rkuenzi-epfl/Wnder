package com.github.wnder;


import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.SavedStateHandle;
import androidx.lifecycle.ViewModel;

import com.github.wnder.picture.PicturesDatabase;

import java.util.Map;
import java.util.TreeMap;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class ScoreboardActivityViewModel extends ViewModel {

    private PicturesDatabase picturesDb;
    /*private UserService userService;*/

    private String uniqueId;

    private MutableLiveData<Map<String,Double>> scoreboard;

    /**
     * Scoreboard ViewModel constructor
     *
     * @param picturesDb the picture database
     * @param savedStateHandle state notably containing intent content
     */
    @Inject
    public ScoreboardActivityViewModel(PicturesDatabase picturesDb /*, UserService userService*/, SavedStateHandle savedStateHandle){
        this.picturesDb = picturesDb;

        this.scoreboard = new MutableLiveData<>(new TreeMap<String, Double>());
        this.uniqueId = savedStateHandle.get(ScoreboardActivity.EXTRA_PICTURE_ID);

        this.refreshScoreboard();
    }

    /**
     * Refresh the scoreboard
     */
    public void refreshScoreboard(){
        picturesDb.getScoreboard(uniqueId)
                .thenAccept(this.scoreboard::postValue);
                //.exceptionally();
    }

    /**
     * Get the scoreboard LiveData
     * @return scoreboard LiveData
     */
    public LiveData<Map<String,Double>> getScoreboard(){
        return scoreboard;
    }
}
