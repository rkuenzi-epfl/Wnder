package com.github.wnder.user;

import android.content.Context;
import android.net.ConnectivityManager;

import androidx.annotation.NonNull;

import com.github.wnder.Storage;
import com.github.wnder.networkService.NetworkInformation;
import com.github.wnder.picture.InternalCachePictureDatabase;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

/**
 * This class brings in useful methods to get user information
 */
public interface UserDatabase {

    /**
     * get all the scores of a user
     * @param user user from which we want the scores
     * @return a set of all the scores a user achieved
     */
    CompletableFuture<Set<Double>> getAllScores(User user);

    /**
     * Retrieve on of guessed or uploaded picture list
     * @param user the use for which we want the list
     * @param picturesListName which list we want
     * @return a Future of list of picture ids
     */
    CompletableFuture<List<String>> getPictureList(User user, String picturesListName);

    /**
     * Retrieve a picture id of a picture the user did not guess
     * @param user the use for which we want a picture id
     * @return a Future with a picture id
     */
    CompletableFuture<String> getNewPictureForUser(User user, int radius);

}
